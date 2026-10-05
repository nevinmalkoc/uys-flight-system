package com.tav.uys.flight_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {
    private String username;
    private String password;
    private String role; // Kayıt olurken kullanılabilir: ADMIN, OPERATOR vb.
}