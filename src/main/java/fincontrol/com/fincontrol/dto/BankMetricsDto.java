package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankMetricsDto", description = "Consolidated financial metrics for a user's banks")
public class BankMetricsDto {
    @Schema(description = "Total balance summed across all user's banks")
    private BigDecimal totalBalanceAllBanks;

    @Schema(description = "Bank with the highest balance")
    private BankBalanceDetailsDto bankWithHighestBalance;

    @Schema(description = "Bank with the lowest balance")
    private BankBalanceDetailsDto bankWithLowestBalance;

    @Schema(description = "Average balance per bank for the user")
    private BigDecimal averageBalancePerBank;

    @Schema(description = "Details of the highest expense linked to any of the user's banks")
    private BankTransactionDetailsDto highestExpenseLinkedToBank;

    @Schema(description = "Details of the highest extra income linked to any of the user's banks")
    private BankTransactionDetailsDto highestExtraIncomeLinkedToBank;

    @Schema(description = "Total count of extra income records across all user's banks")
    private Long totalExtraIncomesCount;

    @Schema(description = "Total count of expense records across all user's banks")
    private Long totalExpensesCount;

    @Schema(description = "Bank with the most extra income records")
    private BankActivityCountDetailsDto bankWithMostExtraIncomes;

    @Schema(description = "Bank with the most expense records")
    private BankActivityCountDetailsDto bankWithMostExpenses;

    @Schema(description = "Average value of extra incomes per bank (total income value / number of banks)")
    private BigDecimal averageExtraIncomeValuePerBank;

    @Schema(description = "Average value of expenses per bank (total expense value / number of banks)")
    private BigDecimal averageExpenseValuePerBank;

    @Schema(description = "Average count of extra incomes per bank (total income count / number of banks)")
    private BigDecimal averageExtraIncomeCountPerBank;

    @Schema(description = "Average count of expenses per bank (total expense count / number of banks)")
    private BigDecimal averageExpenseCountPerBank;
}