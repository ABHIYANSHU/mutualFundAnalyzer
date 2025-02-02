package com.example.demo.service;

import com.example.demo.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class MfService {
    private final RestTemplate restTemplate;
    private static final String LIST_API = "https://api.mfapi.in/mf";
    private static final String DETAILS_API = "https://api.mfapi.in/mf/";

    // Create a thread pool matching the number of CPU cores
    private final ExecutorService executor = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors()
    );

    public MfService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MfDetails> getAllSchemeDetailsParallel() {
        // Fetch all schemes
        MutualFund[] funds = restTemplate.getForObject(LIST_API, MutualFund[].class);
        if (funds == null) return List.of();

        // Create a list of CompletableFuture for parallel processing
        List<CompletableFuture<MfDetails>> futures = List.of(funds).stream()
            .map(fund -> fetchSchemeDetailsAsync(fund.getSchemeCode()))
            .collect(Collectors.toList());

        // Wait for all futures to complete and collect results
        return futures.stream()
            .map(CompletableFuture::join)
            .filter(details -> details != null && details.getMeta() != null)
            .collect(Collectors.toList());
    }

    private CompletableFuture<MfDetails> fetchSchemeDetailsAsync(String schemeCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return restTemplate.getForObject(
                    DETAILS_API + schemeCode,
                    MfDetails.class
                );
            } catch (Exception e) {
                System.err.println("Error fetching scheme " + schemeCode + ": " + e.getMessage());
                return null;
            }
        }, executor);
    }
}
