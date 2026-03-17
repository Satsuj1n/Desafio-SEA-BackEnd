package com.felipelima.clientmanager.controller;

import com.felipelima.clientmanager.dto.response.ZipCodeResponse;
import com.felipelima.clientmanager.integration.viacep.ViaCepClient;
import com.felipelima.clientmanager.integration.viacep.ViaCepResponse;
import com.felipelima.clientmanager.service.MaskUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for zip code lookup via ViaCEP integration.
 * 
 * The frontend calls this endpoint to auto-fill address fields
 * when the user types a zip code. The user can then edit the
 * returned values before submitting the client form.
 */
@RestController
@RequestMapping("/cep")
@RequiredArgsConstructor
public class ZipCodeController {

    private final ViaCepClient viaCepClient;

    /**
     * GET /cep/{zipCode}
     * 
     * Looks up address data from ViaCEP.
     * Accepts zip code with or without mask.
     * Returns address fields with masked zip code.
     * Any authenticated user can access this endpoint.
     */
    @GetMapping("/{zipCode}")
    public ResponseEntity<ZipCodeResponse> lookup(@PathVariable String zipCode) {
        ViaCepResponse viaCep = viaCepClient.findByZipCode(zipCode);

        ZipCodeResponse response = new ZipCodeResponse(
                MaskUtils.maskZipCode(MaskUtils.removeNonDigits(viaCep.getCep())),
                viaCep.getLogradouro(),
                viaCep.getComplemento(),
                viaCep.getBairro(),
                viaCep.getLocalidade(),
                viaCep.getUf()
        );

        return ResponseEntity.ok(response);
    }
}
