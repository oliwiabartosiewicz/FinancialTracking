package com.example.trackie;

public class Receipt {
    private String date;
    private String total;
    private String imageUrl;
    //model class for receipt
    public Receipt() {
    }
//blueprint for a receipt
    public Receipt(String date, String total, String imageUrl) {
        this.date = date;
        this.total = total;
        this.imageUrl = imageUrl;
    }

    public String getDate() { return date; }
    public String getTotal() { return total; }
    public String getImageUrl() { return imageUrl; }
}

