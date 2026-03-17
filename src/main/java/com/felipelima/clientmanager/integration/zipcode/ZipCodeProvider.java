package com.felipelima.clientmanager.integration.zipcode;

import com.felipelima.clientmanager.dto.response.ZipCodeResponse;

/**
 * Strategy interface for zip code lookup providers.
 * 
 * Any zip code service (ViaCEP, OpenCEP, etc.) implements this interface.
 * This allows the ZipCodeService to swap providers transparently,
 * following the Dependency Inversion Principle: the system depends on
 * the abstraction, not on a specific external API.
 * 
 * In Django terms, this is like defining an abstract base class
 * that multiple service implementations can inherit from.
 */
public interface ZipCodeProvider {

    /**
     * Looks up address data by zip code.
     * Returns ZipCodeResponse if found, null if the zip code is invalid.
     * Throws RuntimeException if the service is unavailable.
     */
    ZipCodeResponse lookup(String zipCode);

    /**
     * Returns the provider name for logging purposes.
     */
    String getProviderName();
}
