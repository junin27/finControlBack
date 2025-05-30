package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

// --- UserSimpleDto.java ---
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "UserSimpleDto", description = "Simplified user data")
public class UserSimpleDto {
    @Schema(description = "User ID")
    private UUID id;
    @Schema(description = "User name")
    private String name; // Assuming your User entity has a 'name' field
}