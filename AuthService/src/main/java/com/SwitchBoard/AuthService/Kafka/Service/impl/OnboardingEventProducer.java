package com.SwitchBoard.AuthService.Kafka.Service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import switchboard.schemas.OTPNotificationEvent;
import switchboard.schemas.OnboardingEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingEventProducer {
    private final KafkaTemplate<String, OnboardingEvent> kafkaTemplate;

    @Value("${app.topic.onboarding-notification}")
    private String topic;

    public void publishOnboardingNotification(String emailID, String fullName) {
        log.info("OnboardingEventProducer : publishOnboardingNotification : Publishing Onboarding notification for email - {}", emailID);

        try {
            OnboardingEvent event= new OnboardingEvent(emailID, fullName);
            log.debug("OnboardingEventProducer : publishOnboardingNotification : Created OnboardingEvent: {}", event);
            kafkaTemplate.send(topic, event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("OnboardingEventProducer : publishOnboardingNotification : Successfully published OnboardingEvent to {} with offset {}",
                                    topic, result.getRecordMetadata().offset());
                        } else {
                            log.error("OnboardingEventProducer : publishOnboardingNotification : Failed to publish OnboardingEvent: {}", ex.getMessage(), ex);
                        }
                    });
        } catch (Exception e) {
            log.error("OnboardingEventProducer : publishOnboardingNotification : Error creating or publishing OnboardingEvent: {}", e.getMessage(), e);
            throw e;
        }
    }
}