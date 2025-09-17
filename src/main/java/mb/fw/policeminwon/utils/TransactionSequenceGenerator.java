package mb.fw.policeminwon.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionSequenceGenerator {
    private static final AtomicInteger seq = new AtomicInteger(0);

    public static String getNextSequence() {
        int current = seq.getAndUpdate(n -> (n >= 999) ? 0 : n + 1);
        return String.format("%03d", current);
    }
}