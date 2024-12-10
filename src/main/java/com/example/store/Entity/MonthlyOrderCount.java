package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyOrderCount {
    private int month;
    private long orderCount;

    public MonthlyOrderCount(int month, long orderCount) {
        this.month = month;
        this.orderCount = orderCount;
    }
}
