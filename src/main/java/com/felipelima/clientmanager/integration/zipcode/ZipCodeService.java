package com.felipelima.clientmanager.integration.zipcode;

import java.util.List;

import org.springframework.stereotype.Service;

import com.felipelima.clientmanager.dto.response.ZipCodeResponse;
import com.felipelima.clientmanager.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrates zip code lookup with automatic fallback.
 *
 * Uses the Chain of Responsibility pattern: tries each provider in order
 * (defined by @Order annotation). If the primary provider fails (service
 * unavailable), it automatically tries the next one. If all providers fail,
 * it throws a clear error message.
 *
 * The providers are injected as a List<ZipCodeProvider>, which Spring
 * automatically orders by @Order value. This means adding a new provider
 * is just creating a new class with @Order(3) — no changes here.
 *
 * Flow:
 * 1. Try ViaCEP (primary, @Order 1)
 * - Success → return address
 * - Invalid ZIP → return "invalid zip code" error
 * - Service down → try next provider
 * 2. Try OpenCEP (fallback, @Order 2)
 * - Success → return address
 * - Invalid ZIP → return "invalid zip code" error
 * - Service down → all providers exhausted
 * 3. All failed → return "all services unavailable" error
 *
 */
@Service
@Slf4j
public class ZipCodeService {

    private final List<ZipCodeProvider> providers;

    public ZipCodeService(List<ZipCodeProvider> providers) {
        this.providers = providers;
        log.info("Zip code providers registered: {}",
                providers.stream()
                        .map(ZipCodeProvider::getProviderName)
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("none"));
    }

    /**
     * Looks up a zip code, trying each provider in order.
     *
     * Returns null from a provider means "invalid zip code" (not a service
     * failure).
     * RuntimeException from a provider means "service unavailable" (try next).
     */
    public ZipCodeResponse lookup(String zipCode) {
        RuntimeException lastException = null;

        for (ZipCodeProvider provider : providers) {
            try {
                ZipCodeResponse response = provider.lookup(zipCode);

                if (response == null) {
                    throw new BusinessException("Invalid zip code: " + zipCode);
                }

                log.info("Zip code {} resolved by {}", zipCode, provider.getProviderName());
                return response;

            } catch (BusinessException ex) {
                // Invalid zip code — don't try other providers, the ZIP itself is wrong
                throw ex;

            } catch (RuntimeException ex) {
                // Service unavailable — try next provider
                log.warn("Provider {} failed, trying next. Error: {}",
                        provider.getProviderName(), ex.getMessage());
                lastException = ex;
            }
        }

        // All providers failed
        log.error("All zip code providers are unavailable");
        throw new BusinessException(
                "All zip code services are currently unavailable. "
                        + "You can fill the address fields manually.");
    }
}
