package com.felipelima.clientmanager.integration.zipcode;

import com.felipelima.clientmanager.dto.response.ZipCodeResponse;
import com.felipelima.clientmanager.service.MaskUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Primary zip code provider using ViaCEP API (https://viacep.com.br/).
 * 
 * @Order(1) marks this as the first provider to try.
 * If this fails, ZipCodeService will try the next provider in order.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class ViaCepProvider implements ZipCodeProvider {

    private static final String VIACEP_URL = "https://viacep.com.br/ws/%s/json/";

    private final RestTemplate restTemplate;

    @Override
    public ZipCodeResponse lookup(String zipCode) {
        String cleanZip = MaskUtils.removeNonDigits(zipCode);

        log.info("[ViaCEP] Looking up zip code: {}", cleanZip);

        try {
            String url = String.format(VIACEP_URL, cleanZip);
            ViaCepResponse response = restTemplate.getForObject(url, ViaCepResponse.class);

            if (response == null || Boolean.TRUE.equals(response.getErro())) {
                log.warn("[ViaCEP] Invalid zip code: {}", cleanZip);
                return null;
            }

            return new ZipCodeResponse(
                    MaskUtils.maskZipCode(cleanZip),
                    response.getLogradouro(),
                    response.getComplemento(),
                    response.getBairro(),
                    response.getLocalidade(),
                    response.getUf()
            );

        } catch (RestClientException ex) {
            log.error("[ViaCEP] Service unavailable: {}", ex.getMessage());
            throw new RuntimeException("ViaCEP unavailable", ex);
        }
    }

    @Override
    public String getProviderName() {
        return "ViaCEP";
    }
}
