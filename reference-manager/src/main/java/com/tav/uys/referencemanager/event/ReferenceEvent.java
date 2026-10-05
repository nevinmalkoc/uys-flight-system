package com.tav.uys.referencemanager.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReferenceEvent {
    private String eventType; // CREATED, UPDATED, DELETED
    private String entityType; // AIRLINE, STATION, AIRCRAFT, ROUTE, FLIGHT_TYPE
    private Long entityId;
    private Object payload;
    private LocalDateTime timestamp;
}
