package com.gamingmesh.jobs.container;

public class BoostData {
    private double amount;
    private long expires;

    public BoostData(double amount, long expires) {
        this.amount = amount;
        this.expires = expires;
    }

    public double getAmount() {
        return amount;
    }

    public long getExpires() {
        return expires;
    }
}