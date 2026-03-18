package com.felipelima.clientmanager.dto.request;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ú0-9 ]+$", message = "Name must contain only letters, numbers, and spaces")
    private String name;

    @NotBlank(message = "CPF is required")
    private String cpf;

    @Valid
    private AddressRequest address;

    @NotEmpty(message = "At least one phone is required")
    @Valid
    private List<PhoneRequest> phones;

    @NotEmpty(message = "At least one email is required")
    @Valid
    private List<EmailRequest> emails;
}
