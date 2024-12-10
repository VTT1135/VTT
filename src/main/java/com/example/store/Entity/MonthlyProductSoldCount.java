package com.example.store.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlyProductSoldCount {
    private int month;
    private long productSoldCount;

    public MonthlyProductSoldCount(int month, long productSoldCount) {
        this.month = month;
        this.productSoldCount = productSoldCount;
    }
}
