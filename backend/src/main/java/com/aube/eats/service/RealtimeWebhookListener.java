package com.aube.eats.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@Component
public class RealtimeWebhookListener {
    private static final Logger logger = LoggerFactory.getLogger(RealtimeWebhookListener.class);
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String webhookUrl;
    private final String webhookSecret;

    public RealtimeWebhookListener(RestClient.Builder builder, ObjectMapper objectMapper,
        @Value("${app.realtime.webhook-url}") String webhookUrl,
        @Value("${app.realtime.webhook-secret:}") String webhookSecret) {
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
        this.webhookUrl = webhookUrl;
        this.webhookSecret = webhookSecret;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void send(OrderStatusChangedEvent event) {
        if (webhookSecret.isBlank()) return;
        try {
            byte[] body = objectMapper.writeValueAsBytes(Map.of("orderId", event.orderId().toString(), "status", event.status().name()));
            String signature = HexFormat.of().formatHex(hmac(body));
            restClient.post().uri(webhookUrl).contentType(MediaType.APPLICATION_JSON).header("x-aube-signature", signature).body(body).retrieve().toBodilessEntity();
        } catch (JsonProcessingException | RestClientException exception) {
            logger.warn("Could not publish order status for {}", event.orderId(), exception);
        } catch (Exception exception) {
            logger.warn("Could not sign order status for {}", event.orderId(), exception);
        }
    }

    private byte[] hmac(byte[] body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return mac.doFinal(body);
    }
}