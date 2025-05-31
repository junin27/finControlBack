package fincontrol.com.fincontrol.model.enums;

public enum ReceiptMethodEnum {
    CASH, // Dinheiro
    CREDIT_CARD, // Cartão de Crédito
    DEBIT_CARD,  // Cartão de Débito
    PIX,
    BANK_SLIP,   // Boleto
    CHECK,       // Cheque
    LOAN,        // Empréstimo (se aplicável como meio de receber de terceiros)
    TRANSFER,    // Transferência
    CRYPTOCURRENCY,
    OTHER
}