package com.example.demo.controller;
import com.example.demo.model.MfDetails;
import com.example.demo.service.MfService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class MfController {
    private final MfService mfService;

    public MfController(MfService mfService) {
        this.mfService = mfService;
    }

    @GetMapping("/mf")
    public String getMfDetails(Model model) {
        List<MfDetails> allDetails = mfService.getAllSchemeDetails();
        model.addAttribute("allDetails", allDetails);
        return "mf-details";
    }
}
