package com.felipelima.clientmanager.controller;

import com.felipelima.clientmanager.dto.response.ZipCodeResponse;
import com.felipelima.clientmanager.integration.zipcode.ZipCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for zip code lookup with automatic provider fallback.
 * 
 * The frontend calls this endpoint to auto-fill address fields
 * when the user types a zip code. The user can then edit the
 * returned values before submitting the client form.
 * 
 * If the primary provider (ViaCEP) is unavailable, the service
 * automatically falls back to OpenCEP. If all providers fail,
 * returns an error suggesting manual address input.
 */
@RestController
@RequestMapping("/cep")
@RequiredArgsConstructor
public class ZipCodeController {

    private final ZipCodeService zipCodeService;

    /**
     * GET /cep/{zipCode}
     * 
     * Looks up address data with automatic provider fallback.
     * Accepts zip code with or without mask.
     * Returns address fields with masked zip code.
     * Any authenticated user can access this endpoint.
     */
    @GetMapping("/{zipCode}")
    public ResponseEntity<ZipCodeResponse> lookup(@PathVariable String zipCode) {
        ZipCodeResponse response = zipCodeService.lookup(zipCode);
        return ResponseEntity.ok(response);
    }
}
