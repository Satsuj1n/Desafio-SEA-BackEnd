package com.felipelima.clientmanager.integration.viacep;

import com.felipelima.clientmanager.exception.BusinessException;
import com.felipelima.clientmanager.service.MaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for the ViaCEP API (https://viacep.com.br/).
 * 
 * Isolated in the integration package so that:
 * 1. If ViaCEP changes their API, only this class is affected
 * 2. If we switch to another provider, only this class changes
 * 3. It can be easily mocked in tests
 * 
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ViaCepClient {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/%s/json/";

    private final RestTemplate restTemplate;

    /**
     * Looks up address data by zip code.
     * 
     * Accepts zip code with or without mask (70040-010 or 70040010).
     * Returns the ViaCEP response with address fields.
     * Throws BusinessException if zip code is invalid or service is unavailable.
     */
    @SuppressWarnings("null")
    public ViaCepResponse findByZipCode(String zipCode) {
        String cleanZipCode = MaskUtils.removeNonDigits(zipCode);

        log.info("Looking up zip code: {}", cleanZipCode);

        try {
            String url = String.format(VIACEP_URL, cleanZipCode);
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);

            if (response == null || Boolean.TRUE.equals(response.getErro())) {
                throw new BusinessException("Invalid zip code: " + zipCode);
            }

            return response;

        } catch (RestClientException ex) {
            log.error("Error calling ViaCEP API: {}", ex.getMessage());
            throw new BusinessException("Unable to reach zip code service. Please try again later.");
        }
    }
}
