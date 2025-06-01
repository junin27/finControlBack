package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankActivityCountDetailsDto", description = "Details of bank activity count (incomes or expenses)")
public class BankActivityCountDetailsDto {
    @Schema(description = "Bank ID")
    private UUID bankId;
    @Schema(description = "Bank name")
    private String bankName;
    @Schema(description = "Count of activities (e.g., number of incomes or expenses)")
    private Integer count;
}