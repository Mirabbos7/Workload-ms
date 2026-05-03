package org.example.workloadms.messaging.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.example.workloadms.messaging.Listener;
import org.example.workloadms.service.WorkloadService;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerImpl implements Listener {

    private final WorkloadService workloadService;

    @Override
    @JmsListener(destination = "${activemq.queue.workload}", containerFactory = "jmsListenerContainerFactory")
    public void receiveFromMessageQueue(TrainerWorkloadRequest request,
                                        @Header(value = "txId", required = false) String txId) {
        String resolvedTxId = (txId != null && !txId.isBlank()) ? txId : UUID.randomUUID().toString();
        MDC.put("txId", resolvedTxId);

        try {
            if (!isValid(request)) {
                log.warn("Invalid workload message, trainer={}",
                        request != null ? request.getTrainerUsername() : null);
                throw new IllegalArgumentException("Invalid workload request");
            }

            log.info("Received workload message: trainer={}, action={}, duration={}",
                    request.getTrainerUsername(), request.getActionType(), request.getTrainingDuration());

            workloadService.processWorkload(request);
        } finally {
            MDC.remove("txId");
        }
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