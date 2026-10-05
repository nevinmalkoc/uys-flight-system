package com.tav.uys.referencemanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 4)
    private String origin;
    @Column(nullable = false, length = 4)
    private String destination;
    private Integer distanceKm;
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
