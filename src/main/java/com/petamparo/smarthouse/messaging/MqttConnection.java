package com.petamparo.smarthouse.messaging;

import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnProperty(name = "smarthouse.mqtt.enabled", havingValue = "true")
public class MqttConnection implements MqttCallbackExtended {

    private static final Logger log = LoggerFactory.getLogger(MqttConnection.class);
    private static final int QOS = 1;
    private static final long RETRY_SECONDS = 10;

    private final String brokerUrl;
    private final String clientId;
    private final String username;
    private final String password;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<String, IMqttMessageListener> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();

    private MqttAsyncClient client;

    public MqttConnection(@Value("${smarthouse.mqtt.broker-url}") String brokerUrl,
                          @Value("${smarthouse.mqtt.client-id:}") String clientId,
                          @Value("${smarthouse.mqtt.username:}") String username,
                          @Value("${smarthouse.mqtt.password:}") String password,
                          ApplicationEventPublisher eventPublisher) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId.isBlank() ? "smarthouse-api-" + UUID.randomUUID().toString().substring(0, 8) : clientId;
        this.username = username;
        this.password = password;
        this.eventPublisher = eventPublisher;
    }

    public void subscribe(String topic, IMqttMessageListener listener) {
        subscriptions.put(topic, listener);
        if (isConnected()) {
            subscribeNow(topic, listener);
        }
    }

    public boolean publish(String topic, String payload, boolean retained) {
        if (!isConnected()) {
            log.warn("MQTT desconectado; mensagem para {} não foi publicada", topic);
            return false;
        }
        try {
            client.publish(topic, payload.getBytes(StandardCharsets.UTF_8), QOS, retained);
            log.info("Publicado em {}: {}", topic, payload);
            return true;
        } catch (MqttException e) {
            log.warn("Falha ao publicar em {}: {}", topic, e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() throws MqttException {
        client = new MqttAsyncClient(brokerUrl, clientId, new MemoryPersistence());
        client.setCallback(this);
        connect();
    }

    private void connect() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        if (!username.isBlank()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }

        try {
            client.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken token) {
                }

                @Override
                public void onFailure(IMqttToken token, Throwable e) {
                    scheduleRetry(e);
                }
            });
        } catch (MqttException e) {
            scheduleRetry(e);
        }
    }

    private void scheduleRetry(Throwable cause) {
        Throwable reason = cause.getCause() != null ? cause.getCause() : cause;
        log.warn("Não foi possível conectar ao broker MQTT {}: {}. Nova tentativa em {}s",
                brokerUrl, reason, RETRY_SECONDS);
        retryExecutor.schedule(this::connect, RETRY_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverUri) {
        log.info("MQTT {} em {}", reconnect ? "reconectado" : "conectado", serverUri);
        subscriptions.forEach(this::subscribeNow);
        eventPublisher.publishEvent(new MqttConnectedEvent(reconnect));
    }

    private void subscribeNow(String topic, IMqttMessageListener listener) {
        try {
            client.subscribe(topic, QOS, listener);
            log.info("Assinando {}", topic);
        } catch (MqttException e) {
            log.error("Falha ao assinar o tópico {}", topic, e);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("Conexão MQTT perdida: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @PreDestroy
    public void stop() {
        retryExecutor.shutdownNow();
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect().waitForCompletion(2000);
            }
            client.close();
        } catch (MqttException e) {
            log.warn("Erro ao encerrar o cliente MQTT: {}", e.getMessage());
        }
    }
}
