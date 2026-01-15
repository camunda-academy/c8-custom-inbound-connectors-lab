package io.camunda.connector.inbound.subscription;

/**
 * Data model of an event consumed by inbound Connector (e.g. originating from an external system)
 *
 * @param monitoredEvent
 * @param directory
 * @param fileName
 */

public record WatchServiceSubscriptionEvent(
    String monitoredEvent,
    String directory, 
    String fileName
){}
