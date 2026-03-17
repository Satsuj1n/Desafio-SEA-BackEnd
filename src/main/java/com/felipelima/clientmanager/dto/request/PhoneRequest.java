package com.felipelima.clientmanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRequest {

    @NotNull(message = "Phone type is required")
    private String type;

    @NotBlank(message = "Phone number is required")
    private String number;
}