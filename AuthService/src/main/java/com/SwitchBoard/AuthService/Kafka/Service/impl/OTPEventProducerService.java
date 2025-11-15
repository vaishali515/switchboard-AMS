package com.SwitchBoard.AuthService.Kafka.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import switchboard.schemas.OTPNotificationEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPEventProducerService {

    private final KafkaTemplate<String, OTPNotificationEvent> kafkaTemplate;

    @Value("${app.topic.otp-notification}")
    private String topic;

    public void publishOTPNotification(String emailID, String otp) {
        log.info("OTPEventProducerService : publishOTPNotification : Publishing OTP for email - {}", emailID);

        try {
            OTPNotificationEvent event = new OTPNotificationEvent(emailID, otp);
            log.debug("OTPEventProducerService : publishOTPNotification : Created OTPNotificationEvent: {}", event);

            kafkaTemplate.send(topic, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("OTPEventProducerService : publishOTPNotification : Successfully published OTPNotificationEvent to {} with offset {}",
                                    topic, result.getRecordMetadata().offset());
                        } else {
                            log.error("OTPEventProducerService : publishOTPNotification : Failed to publish OTPNotificationEvent: {}", ex.getMessage(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("OTPEventProducerService : publishOTPNotification : Error creating or publishing OTPNotificationEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}