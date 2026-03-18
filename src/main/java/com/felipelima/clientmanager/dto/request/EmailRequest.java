package com.felipelima.clientmanager.dto.request;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    @NotBlank(message = "Email address is required")
    @javax.validation.constraints.Email(message = "Email must be a valid format")
    private String address;
}
