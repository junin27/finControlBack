package fincontrol.com.fincontrol.model.enums;

// Enum for Payment Methods
public enum PaymentMethod {
    CASH("Dinheiro"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    PIX("Pix"),
    BANK_SLIP("Boleto"), // Boleto
    CHECK("Cheque"), // Cheque
    LOAN("Empréstimo"), // Empréstimo
    TRANSFER("Transferência"), // Transferência
    CRYPTOCURRENCY("Criptomoeda"), // Criptomoeda
    OTHER("Outro"); // Outro

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Optional: method to convert from String to Enum, useful in DTOs
    public static PaymentMethod fromString(String text) {
        for (PaymentMethod b : PaymentMethod.values()) {
            if (b.displayName.equalsIgnoreCase(text) || b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("No payment method found for string: " + text);
    }
}
