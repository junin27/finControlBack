package fincontrol.com.fincontrol.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;
// Não precisamos de categoryId ou bankId aqui para uma atualização em massa simples.
// Se a ideia fosse mover todas as despesas para uma nova categoria/banco, eles seriam adicionados.

@Data
@Schema(name = "ExpenseMassUpdateDto", description = "Dados para atualizar campos específicos em TODAS as despesas de um usuário. Envie apenas os campos que deseja aplicar a todas as despesas.")
public class ExpenseMassUpdateDto {

    @Size(max = 255, message = "A descrição não pode exceder 255 caracteres.")
    @Schema(description = "Nova descrição a ser aplicada a todas as despesas do usuário (opcional). Envie \"\" (string vazia) para limpar a descrição de todas.", example = "Revisão Geral de Despesas")
    private String description;

    @Schema(description = "Nova data a ser aplicada a todas as despesas do usuário (opcional, formato yyyy-MM-dd).", example = "2025-06-15")
    private LocalDate expenseDate;

    @Schema(description="Novo ID de categoria para TODAS as despesas (opcional)")
    private UUID categoryId;

    @Schema(description="Novo ID de banco para TODAS as despesas (opcional, envie um UUID válido ou null para desassociar)")
    private UUID bankId;

}
