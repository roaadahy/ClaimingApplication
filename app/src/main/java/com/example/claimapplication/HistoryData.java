package com.example.claimapplication;

public class HistoryData {

    private String address;
    private String claimDateTime;
    private String status;
    private String authentic;

    public HistoryData(String address, String claimDateTime, String status, String authentic) {
        this.address = address;
        this.claimDateTime = claimDateTime;
        this.status = status;
        this.authentic = authentic;
    }

    public String getAddress() {
        return address;
    }

    public String getClaimDateTime() {
        return claimDateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getAuthentic() {
        return authentic;
    }
}
