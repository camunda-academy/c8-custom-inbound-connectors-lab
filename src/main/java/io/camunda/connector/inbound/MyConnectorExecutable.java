package io.camunda.connector.inbound;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.camunda.connector.api.annotation.InboundConnector;
import io.camunda.connector.api.inbound.CorrelationRequest;
import io.camunda.connector.api.inbound.InboundConnectorContext;
import io.camunda.connector.api.inbound.InboundConnectorExecutable;
import io.camunda.connector.inbound.subscription.WatchServiceSubscription;
import io.camunda.connector.inbound.subscription.WatchServiceSubscriptionEvent;

@InboundConnector(
        name = "Watch Service Inbound Connector", 
        type = "io.camunda:watchserviceinbound:1"
    )  
public class MyConnectorExecutable implements InboundConnectorExecutable<InboundConnectorContext> {

    private WatchServiceSubscription subscription;
    private InboundConnectorContext context;
    private ExecutorService executorService;
    public CompletableFuture<?> future;

    @Override
    public void activate(InboundConnectorContext context) {
        MyConnectorProperties props = context.bindProperties(MyConnectorProperties.class);
        this.context = context;
        this.executorService = Executors.newSingleThreadExecutor();

        this.future =
        CompletableFuture.runAsync(
            () -> {
            new WatchServiceSubscription(
                props.eventToMonitor(), 
                props.directory(), 
                props.pollingInterval(), 
                this::onEvent);
            },
            this.executorService);
    }

    private void onEvent(WatchServiceSubscriptionEvent rawEvent) {
        context.correlate(CorrelationRequest.builder().variables(new MyConnectorEvent(rawEvent)).build());
    }

    @Override
    public void deactivate() {
        subscription.stop();
    }
}
