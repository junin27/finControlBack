package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "BankSimpleDto", description = "Simplified bank data")
public class BankSimpleDto {
    @Schema(description = "Bank ID")
    private UUID id;
    @Schema(description = "Bank name")
    private String name; // Assuming your Bank entity has a 'name' field
}