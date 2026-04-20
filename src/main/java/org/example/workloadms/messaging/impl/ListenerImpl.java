package org.example.workloadms.messaging.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.messaging.Listener;
import org.example.workloadms.service.WorkloadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerImpl implements Listener {

    private final JmsTemplate jmsTemplate;
    private final WorkloadService workloadService;

    @Value("${activemq.queue.workload}")
    private String queue;

    @Value("${activemq.queue.workload-dlq}")
    private String dlq;

    @Override
    @JmsListener(destination = "${activemq.queue.workload}")
    public void receiveFromMessageQueue(TrainerWorkloadRequest request) {
        if (!isValid(request)) {
            log.warn("Invalid workload message, forwarding to DLQ. trainer={}",
                    request != null ? request.getTrainerUsername() : null);
            jmsTemplate.convertAndSend(dlq, request);
            return;
        }

        log.info("Received workload message: trainer={}, action={}, duration={}",
                request.getTrainerUsername(), request.getActionType(), request.getTrainingDuration());

        workloadService.processWorkload(request);
    }

    private boolean isValid(TrainerWorkloadRequest request) {
        return request != null
                && hasText(request.getTrainerUsername())
                && request.getActionType() != null
                && request.getTrainingDate() != null
                && request.getTrainingDuration() > 0;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
