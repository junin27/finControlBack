package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema; // Adicionar import
import lombok.Data;
import lombok.NoArgsConstructor; // Adicionado para consistência, opcional

@Data
@NoArgsConstructor // Adicionado para consistência, opcional
@Schema(name = "BankUpdateDto", description = "Dados para atualizar um banco existente")
public class BankUpdateDto {

    @Schema(description = "Novo nome do banco (opcional)", example = "Banco Investimentos")
    private String name;

    @Schema(description = "Nova descrição opcional do banco", example = "Conta para aplicações financeiras")
    private String description;

}