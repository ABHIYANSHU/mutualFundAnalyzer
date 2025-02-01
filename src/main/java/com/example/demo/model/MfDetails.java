package com.example.demo.model;
import lombok.Data;
import java.util.List;

@Data
public class MfDetails {
    private Meta meta;
    private List<DataPoint> data;
    
    @Data
    public static class Meta {
        private String fund_house;
        private String scheme_type;
        private String scheme_category;
        private String scheme_code;
        private String scheme_name;
    }
    
    @Data
    public static class DataPoint {
        private String date;
        private String nav;
    }
}
