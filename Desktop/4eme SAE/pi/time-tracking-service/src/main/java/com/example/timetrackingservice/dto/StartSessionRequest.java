package com.example.timetrackingservice.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class StartSessionRequest {
    private UUID contractId;
    private UUID freelancerId;
}
