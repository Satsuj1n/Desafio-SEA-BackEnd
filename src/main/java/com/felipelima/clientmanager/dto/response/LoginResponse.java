package com.felipelima.clientmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String type;
    private String role;

    public LoginResponse(String token, String role) {
        this.token = token;
        this.type = "Bearer";
        this.role = role;
    }
}
