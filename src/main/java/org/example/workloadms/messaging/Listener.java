package org.example.workloadms.messaging;

import org.example.workloadms.dto.request.TrainerWorkloadRequest;

public interface Listener {

    void receiveFromMessageQueue(TrainerWorkloadRequest request);
}
