package org.example.workloadms.messaging;

import lombok.extern.slf4j.Slf4j;
import org.example.workloadms.dto.request.TrainerWorkloadRequest;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DlqListener {

    @JmsListener(destination = "ActiveMQ.DLQ", containerFactory = "jmsListenerContainerFactory")
    public void onDeadLetter(TrainerWorkloadRequest message) {
        log.error("Message landed in DLQ: trainer={}, action={}",
                message != null ? message.getTrainerUsername() : "null",
                message != null ? message.getActionType() : "null");
    }
}