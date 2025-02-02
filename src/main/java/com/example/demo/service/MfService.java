package com.example.demo.service;

import com.example.demo.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.stream.IntStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class MfService {
    private final RestTemplate restTemplate;
    private static final String LIST_API = "https://api.mfapi.in/mf";
    private static final String DETAILS_API = "https://api.mfapi.in/mf/";

    // Double the thread pool size
    private final ExecutorService executor = Executors.newFixedThreadPool(
        4 * Runtime.getRuntime().availableProcessors()
    );

    public MfService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MfDetails> getAllSchemeDetailsParallel() {
        MutualFund[] funds = restTemplate.getForObject(LIST_API, MutualFund[].class);
        if (funds == null) return List.of();

        List<CompletableFuture<MfDetails>> futures = List.of(funds).stream()
            .limit(100)
            .map(fund -> fetchSchemeDetailsAsync(fund.getSchemeCode()))
            .collect(Collectors.toList());

        return futures.stream()
            .map(CompletableFuture::join)
            .filter(details -> details != null && details.getMeta() != null)
            .collect(Collectors.toList());
    }

    private CompletableFuture<MfDetails> fetchSchemeDetailsAsync(String schemeCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MfDetails details = restTemplate.getForObject(
                    DETAILS_API + schemeCode,
                    MfDetails.class
                );
                if (details != null) {
                    processDetails(details); // CPU-intensive work
                }
                return details;
            } catch (Exception e) {
                System.err.println("Error fetching scheme " + schemeCode + ": " + e.getMessage());
                return null;
            }
        }, executor);
    }

    private void processDetails(MfDetails details) {
        // Example: Calculate average NAV
        double averageNav = details.getData().stream()
            .mapToDouble(data -> Double.parseDouble(data.getNav()))
            .average()
            .orElse(0.0);
        
        int prime = (int) IntStream.rangeClosed(2, details.getMeta().getSchemeCode())
                .parallel() // Uses multiple CPU cores
                .filter(MfService::isPrime)
                .count();
        
        details.getMeta().setAverageNav(averageNav);
    }

    public static boolean isPrime(int num) {
        if (num < 2) return false;
        for (int i = 2; i * i <= num; i++) {
            if (num % i == 0) return false;
        }
        return true;
    }
}
