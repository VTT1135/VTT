package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyRevenue {
    private int month;
    private double totalRevenue;

    public MonthlyRevenue(int month, double totalRevenue) {
        this.month = month;
        this.totalRevenue = totalRevenue;
    }
}
