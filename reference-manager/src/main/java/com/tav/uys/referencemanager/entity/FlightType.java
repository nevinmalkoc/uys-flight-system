package com.tav.uys.referencemanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "flight_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlightType implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    @Column(nullable = false, length = 100)
    private String description;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
