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
 * Fallback zip code provider using OpenCEP API (https://opencep.com/).
 * 
 * @Order(2) marks this as the second provider to try,
 * only used when the primary provider (ViaCEP) fails.
 * 
 * OpenCEP is a free, open-source alternative to ViaCEP
 * with a compatible response format.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class OpenCepProvider implements ZipCodeProvider {

    private static final String OPENCEP_URL = "https://opencep.com/v1/%s";

    private final RestTemplate restTemplate;

    @Override
    public ZipCodeResponse lookup(String zipCode) {
        String cleanZip = MaskUtils.removeNonDigits(zipCode);

        log.info("[OpenCEP] Looking up zip code: {}", cleanZip);

        try {
            String url = String.format(OPENCEP_URL, cleanZip);
            OpenCepResponse response = restTemplate.getForObject(url, OpenCepResponse.class);

            if (response == null || response.getCep() == null) {
                log.warn("[OpenCEP] Invalid zip code: {}", cleanZip);
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
            log.error("[OpenCEP] Service unavailable: {}", ex.getMessage());
            throw new RuntimeException("OpenCEP unavailable", ex);
        }
    }

    @Override
    public String getProviderName() {
        return "OpenCEP";
    }
}
