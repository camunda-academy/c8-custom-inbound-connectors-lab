package io.camunda.connector.inbound;

public record MyConnectorProperties(
    String eventToMonitor,
    String directory,
    String pollingInterval
) {
    public MyConnectorProperties {
        if (eventToMonitor == null || eventToMonitor.isEmpty()) {
            throw new IllegalArgumentException("eventToMonitor must not be null or empty");
        }
        if (directory == null || directory.isEmpty()) {
            throw new IllegalArgumentException("directory must not be null or empty");
        }
        Long.parseLong(pollingInterval);
    }
}
