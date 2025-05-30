package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "CategorySimpleDto", description = "Simplified category data")
public class CategorySimpleDto {
    @Schema(description = "Category ID")
    private UUID id;
    @Schema(description = "Category name")
    private String name; // Assuming your Category entity has a 'name' field
}