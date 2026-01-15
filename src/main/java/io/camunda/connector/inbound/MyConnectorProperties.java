package io.camunda.connector.inbound;

public record MyConnectorProperties(
    String eventToMonitor,
    String directory,
    String pollingInterval
) {}
