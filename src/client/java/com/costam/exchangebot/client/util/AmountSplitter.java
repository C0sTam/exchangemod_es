package com.costam.exchangebot.client.util;

import java.util.ArrayList;
import java.util.List;

public class AmountSplitter {

    public static List<Long> splitAmount(long amount) {
        List<Long> parts = new ArrayList<>();
        while (amount > 0) {
            long chunk = Math.min(100_000, amount);
            parts.add(chunk);
            amount -= chunk;
        }
        return parts;
    }

}
