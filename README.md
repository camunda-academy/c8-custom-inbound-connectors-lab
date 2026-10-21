> A Connector template for new C8 inbound connector
>
To adapt this project for another connector, update the connector-specific resources:
>
* [README](./README.md): update the title and connector description.
* [Element templates](./element-templates/): update the file-watch templates or replace them with templates for your connector.
* [POM](./pom.xml): update artifact metadata.
* Connector implementation: update `MyConnectorExecutable`, `MyConnectorProperties`, and the subscription classes under `src/main/java/io/camunda/connector/inbound/`; set the `@InboundConnector` type to match the element templates.
* [Service Provider Interface (SPI)](./src/main/resources/META-INF/services/io.camunda.connector.api.inbound.InboundConnectorExecutable): update the registered executable class if it is renamed.
>
>
> About [creating Connectors](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk/#creating-a-custom-connector)
>
> Check out the [Connectors SDK](https://github.com/camunda/connectors)

# Important concepts

When building an inbound connector, there are several important concepts to understand:

- **Activation:** When a process definition with this connector is deployed. This is a synchronous operation, so long-running tasks should be started asynchronously.
- **[Activation condition](https://docs.camunda.io/docs/components/connectors/use-connectors/#activation-condition):** ⚠️ Do not mistaken with the Activation. An activation condition is a BPMN expression that must evaluate to true for the connector to correlate.
- **Deactivation:** When the process definition is deleted or a new version is deployed. Use this to clean up resources.
- **[Correlation](https://docs.camunda.io/docs/components/connectors/use-connectors/#correlation):** Matches incoming events to waiting process instances using correlation keys.
- **[Deduplication](https://docs.camunda.io/docs/components/connectors/use-connectors/inbound/#connector-deduplication):** Sometimes you might want to have multiple BPMN elements listening to the same event source. For example, you might want to link multiple connector events to the same message queue consumer and activate only one of them based on the message content.
- **[Message ID](https://docs.camunda.io/docs/components/connectors/use-connectors/#message-id-expression):** Used for deduplication - events with the same message ID are processed only once.

For more details, see the [Inbound Connector SDK documentation](https://docs.camunda.io/docs/components/connectors/custom-built-connectors/connector-sdk).

We strongly recommend reading through the [Messages documentation](https://docs.camunda.io/docs/components/concepts/messages/) as Inbound connectors rely heavily on the concepts explained there.

# Connector Template

Camunda File Watch Inbound Connector

Watches a configured directory for file creation, modification, or deletion events. The selected event is correlated
with a waiting intermediate event or starts a process, depending on the BPMN element used. The event payload contains
the event type, monitored directory, and file name.

## Build

Using JDK 21, you can package the Connector by running the following command:

```bash
mvn clean package
```

This builds the connector JAR. Element templates are maintained in `element-templates/`.

The Connector SDK dependencies are declared with `provided` scope and are supplied by the Connector Runtime.

### Shading dependencies

You can use the `maven-shade-plugin` defined in the [Maven configuration](./pom.xml) to relocate common dependencies
that are used in other Connectors and
the [Connector Runtime](https://github.com/camunda/connectors).
This helps to avoid classpath conflicts when the Connector is executed.

For example, without shading, you might encounter errors like:
```
java.lang.NoSuchMethodError: com.fasterxml.jackson.databind.ObjectMapper.setSerializationInclusion(Lcom/fasterxml/jackson/annotation/JsonInclude$Include;)Lcom/fasterxml/jackson/databind/ObjectMapper;
```
This occurs when your connector and the runtime use different versions of the same library (e.g., Jackson).

Use the `relocations` configuration in the Maven Shade plugin to define the dependencies that should be shaded.
The [Maven Shade documentation](https://maven.apache.org/plugins/maven-shade-plugin/examples/class-relocation.html)
provides more details on relocations.

## API

### Input

| Name | Description | Example |
|------|-------------|---------|
| Event to monitor | File event kind: `ENTRY_CREATE`, `ENTRY_MODIFY`, or `ENTRY_DELETE` | `ENTRY_CREATE` |
| Directory | Path to the directory to watch | `/var/inbox` |
| Polling interval | Time in seconds between checks for events | `30` |

### Output

```json
{
  "event": {
    "monitoredEvent": "ENTRY_CREATE",
    "directory": "/var/inbox",
    "fileName": "report.csv"
  }
}
```

## Testing
### Unit Tests

Run the tests with Maven:
```bash
mvn clean verify
```

### Local environment

#### Prerequisites
You will need the following tools installed on your machine:
1. Camunda Modeler, which is available in two variants:
    - [Desktop Modeler](https://camunda.com/download/modeler/) for a local installation.
    - [Web Modeler](https://modeler.camunda.io/) for an online experience.

2. [Docker](https://www.docker.com/products/docker-desktop), which is required to run the Camunda platform.

#### Setting Up the Camunda platform

The Connectors Runtime requires a running Camunda platform to interact with. To set up a local Camunda environment, follow these steps:

1. Clone the [Camunda distributions repository](https://github.com/camunda/camunda-distributions) and navigate to the Camunda 8.10 Docker Compose directory:

```shell
git clone https://github.com/camunda/camunda-distributions.git
cd camunda-distributions/docker-compose/versions/camunda-8.10
```

The project uses Camunda 8.10 and Connectors 8.10 dependencies in `pom.xml`; use a compatible platform version.

Either comment out the connectors service, or use the `--scale` flag to exclude it:

```shell
docker compose -f docker-compose-core.yaml up --scale connectors=0
```

#### Configure Modeler and Use Your Connector

Configure Modeler to load the files in the `element-templates` directory, following the
[Element Templates documentation](https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/).
Use `File Watch Start Connector.json` for a start event or `File Watch Intermediate Connector.json` for an intermediate
catch event.

#### Using Your Connector
Then, to use your connector in a local Camunda environment, follow these steps:

1. Run `io.camunda.connector.inbound.LocalConnectorRuntime` from the IDE with the test classpath; the class is under `src/test/java`.
2. Open the Camunda Desktop Modeler and create a new BPMN diagram.
3. Design a process that incorporates your newly created connector.
4. Deploy the process to your local Camunda platform.
5. Verify the process in Camunda Operate. With the documented local Compose configuration, the URL is [http://localhost:8088/operate](http://localhost:8088/operate); the default credentials are `demo` / `demo`.

### SaaS environment

#### Creating an API Client

The local Connectors Runtime requires connection details to interact with your Camunda SaaS cluster. Use a cluster compatible with the Camunda and Connectors versions configured in `pom.xml`. To set this up, follow these steps:

1. Navigate to Camunda [SaaS](https://console.camunda.io).
2. Create a cluster on a version compatible with the Camunda and Connectors dependencies in `pom.xml`.
3. Select your cluster, then go to the `API` section and click `Create new Client`.
4. Ensure the `zeebe` checkbox is selected, then click `Create`.
5. Copy the configuration details displayed under the `Spring Boot` tab.
6. Add the copied configuration to `src/test/resources/application.properties` (or the active runtime configuration).

#### Using Your Connector

1. Start `io.camunda.connector.inbound.LocalConnectorRuntime` from the IDE with the test classpath; the class is under `src/test/java`.
2. Access the Web Modeler and create a new project.
3. Click on `Create new`, then select `Upload files`. Upload the connector template from the repository you have.
4. After uploading, **publish the connector template** by clicking the Publish button.
5. In the same folder, create a new BPMN diagram.
6. Design and start a process that incorporates your new connector.

## Element Templates

The file-watch templates are `element-templates/File Watch Start Connector.json` and
`element-templates/File Watch Intermediate Connector.json`.

The Maven build does not generate element templates; update the checked-in file-watch templates directly.

The repository also contains generic starter templates; they are not configured for the file-watch connector.
