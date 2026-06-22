package com.yipe.finance.entity;

public enum TransactionType {
    DEBIT("Débito/Pix"),
    CREDIT("Crédito"),
    VR("VR"),
    INVESTMENT("Investimento"),
    RESERVE("Reserva"),
    INCOME("Entrada"),
    ADJUSTMENT_INCOME("Ajuste Entrada"),
    ADJUSTMENT_EXPENSE("Ajuste Saída");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TransactionType fromDisplayName(String name) {
        for (TransactionType t : values()) {
            if (t.displayName.equals(name)) return t;
        }
        throw new IllegalArgumentException("Unknown type: " + name);
    }
}
