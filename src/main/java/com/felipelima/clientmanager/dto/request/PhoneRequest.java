package com.felipelima.clientmanager.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.felipelima.clientmanager.validation.ValidPhoneType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRequest {

    @NotNull(message = "Phone type is required")
    @ValidPhoneType
    private String type;

    @NotBlank(message = "Phone number is required")
    private String number;
}
