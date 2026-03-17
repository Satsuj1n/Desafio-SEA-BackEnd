package com.felipelima.clientmanager.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
import javax.validation.constraints.NotBlank;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {
 
    @NotBlank(message = "Email address is required")
    @javax.validation.constraints.Email(message = "Email must be a valid format")
    private String address;
}
