package com.felipelima.clientmanager.integration.zipcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps the JSON response from OpenCEP API (https://opencep.com/).
 *
 * OpenCEP uses the same field names as ViaCEP, making it a good fallback.
 * Kept as a separate class because external API contracts can diverge over
 * time.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenCepResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
}
