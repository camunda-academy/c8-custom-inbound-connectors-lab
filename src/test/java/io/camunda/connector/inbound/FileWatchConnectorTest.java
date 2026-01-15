package io.camunda.connector.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import io.camunda.connector.runtime.test.inbound.InboundConnectorContextBuilder;

import org.junit.jupiter.api.BeforeEach;

public class FileWatchConnectorTest {

    private String eventToMonitor;
    private String directory;
    private String pollingInterval;

    @BeforeEach
    void setUp() {
        eventToMonitor = "ENTRY_CREATE";
        //Replace with your directory test path
        directory = "C:\\Users\\Camunda\\Downloads";
        pollingInterval = "30";
    }

    @Test
    void shouldCreatePropertiesWithValidValues() {
        // given & when
        var properties = new MyConnectorProperties(eventToMonitor, directory, pollingInterval);

        // then
        assertThat(properties.eventToMonitor()).isEqualTo("ENTRY_CREATE");
        assertThat(properties.directory()).isEqualTo("C:\\Users\\Camunda\\Downloads");
        assertThat(properties.pollingInterval()).isEqualTo("30");
    }

    @Test
    void shouldHandleNullEventToMonitor() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties(null, directory, pollingInterval))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleNullDirectory() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties(eventToMonitor, null, pollingInterval))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleNullPollingInterval() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties(eventToMonitor, directory, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleEmptyEventToMonitor() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties("", directory, pollingInterval))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleEmptyDirectory() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties(eventToMonitor, "", pollingInterval))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleInvalidPollingInterval() {
        // given & when & then
        assertThatThrownBy(() -> new MyConnectorProperties(eventToMonitor, directory, "invalid"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldHandleDifferentEventTypes() {
        // given
        String[] events = {"ENTRY_CREATE", "ENTRY_MODIFY", "ENTRY_DELETE"};

        // when & then
        for (String event : events) {
            var properties = new MyConnectorProperties(event, directory, pollingInterval);
            assertThat(properties.eventToMonitor()).isEqualTo(event);
        }
    }

    @Test
    void shouldFailWhenValidate_NoPollingInterval() {
        // given   
        var input = new MyConnectorProperties(eventToMonitor, directory, pollingInterval);
        var context = InboundConnectorContextBuilder.create().properties(input).build();

        // when
        var connectorInput = context.bindProperties(MyConnectorProperties.class);

        // then
        assertThat(connectorInput)
            .isInstanceOf(MyConnectorProperties.class)
            .extracting("pollingInterval")
            .isEqualTo("30");
    }
}