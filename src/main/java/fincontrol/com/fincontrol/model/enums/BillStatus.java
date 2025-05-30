package fincontrol.com.fincontrol.model.enums;

// Enum for Bill Status
public enum BillStatus {
    PENDING("Pendente"),
    PAID("Pago"),
    OVERDUE("Atrasado"),
    PAID_LATE("Pago com Atraso");

    private final String displayName;

    BillStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static BillStatus fromString(String text) {
        for (BillStatus s : BillStatus.values()) {
            if (s.displayName.equalsIgnoreCase(text) || s.name().equalsIgnoreCase(text)) {
                return s;
            }
        }
        // Default to PENDING or throw an exception
        // throw new IllegalArgumentException("No status found for string: " + text);
        return PENDING;
    }
}
