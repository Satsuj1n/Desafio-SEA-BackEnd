package com.felipelima.clientmanager.integration.zipcode;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Maps the JSON response from ViaCEP API.
 *
 * Example response from https://viacep.com.br/ws/70040010/json/:
 * {
 * "cep": "70040-010",
 * "logradouro": "SBS Quadra 2",
 * "complemento": "",
 * "bairro": "Asa Sul",
 * "localidade": "Brasília",
 * "uf": "DF",
 * "erro": false
 * }
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) tells Jackson to skip
 *                                     any fields in the JSON that don't have a
 *                                     matching field here.
 *                                     This protects us if ViaCEP adds new
 *                                     fields in the future.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViaCepResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade;
    private String uf;
    private Boolean erro;
}
