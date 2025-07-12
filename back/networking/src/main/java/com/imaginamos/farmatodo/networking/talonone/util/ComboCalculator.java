package com.imaginamos.farmatodo.networking.talonone.util;

import java.util.Objects;

public class ComboCalculator {

    public static int maxCombos(int[] stock, int[] unitsRequired) {

        if (Objects.isNull(stock) || Objects.isNull(unitsRequired)) {
            return 0;
        }

        if (stock.length != unitsRequired.length) {
            return 0;
        }

        int maxCombos = Integer.MAX_VALUE;

        for (int i = 0; i < stock.length; i++) {
            if (unitsRequired[i] > 0) {
                int possibleCombos = stock[i] / unitsRequired[i];
                maxCombos = Math.min(maxCombos, possibleCombos);
            }
        }

        return maxCombos;
    }
}
