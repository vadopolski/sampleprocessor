package com.example.stage.model;

public class Card {
    private String number;
    private String issuedBy;

    public Card() {
    }

    public Card(String number, String issuedBy) {
        this.number = number;
        this.issuedBy = issuedBy;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String name) {
        this.number = name;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }
}