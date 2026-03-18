package com.felipelima.clientmanager.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponse {

    private Long id;
    private String name;
    private String cpf;
    private AddressResponse address;
    private List<PhoneResponse> phones;
    private List<EmailResponse> emails;
}
