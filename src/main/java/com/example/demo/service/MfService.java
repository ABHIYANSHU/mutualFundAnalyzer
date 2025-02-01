package com.example.demo.service;
import com.example.demo.model.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MfService {
    private final RestTemplate restTemplate;
    private static final String LIST_API = "https://api.mfapi.in/mf";
    private static final String DETAILS_API = "https://api.mfapi.in/mf/";

    public MfService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<MfDetails> getAllSchemeDetails() {
        MutualFund[] funds = restTemplate.getForObject(LIST_API, MutualFund[].class);
        List<MfDetails> allDetails = new ArrayList<>();
        
        if (funds != null) {
            for (MutualFund fund : funds) {
                try {
                    MfDetails details = restTemplate.getForObject(
                        DETAILS_API + fund.getSchemeCode(), 
                        MfDetails.class
                    );
                    if (details != null) {
                        allDetails.add(details);
                    }
                } catch (Exception e) {
                    System.err.println("Error fetching scheme " + fund.getSchemeCode());
                }
            }
        }
        return allDetails;
    }
}
