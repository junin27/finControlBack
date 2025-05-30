// src/main/java/fincontrol/com/fincontrol/controller/UserController.java
package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.UserDto;
import fincontrol.com.fincontrol.dto.UserUpdateDto;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.service.UserService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Timed(value = "user.list.time", description = "Tempo para listar todos os usuários")
    @Operation(summary = "Lista todos os usuários")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    @GetMapping
    public List<UserDto> listAll() {
        return userService.findAll()
                .stream()
                .map(u -> new UserDto(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getSalary(),
                        u.getCreatedAt(),
                        u.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Timed(value = "user.get.time", description = "Tempo para buscar um usuário por ID")
    @Operation(summary = "Busca um usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(
            @Parameter(description = "ID do usuário", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        return userService.findById(id)
                .map(u -> new UserDto(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getSalary(),
                        u.getCreatedAt(),
                        u.getUpdatedAt()
                ))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Timed(value = "user.update.time", description = "Tempo para atualizar um usuário")
    @Operation(summary = "Atualiza dados de um usuário existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(
            @Parameter(description = "ID do usuário a ser atualizado", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Campos a serem atualizados",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserUpdateDto.class))
            )
            @RequestBody UserUpdateDto dto
    ) {
        User updated = userService.update(id, dto);
        UserDto out = new UserDto(
                updated.getId(),
                updated.getName(),
                updated.getEmail(),
                updated.getSalary(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );
        return ResponseEntity.ok(out);
    }

    @Timed(value = "user.delete.time", description = "Tempo para remover um usuário")
    @Operation(summary = "Remove um usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID do usuário a ser removido", required = true,
                    example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id
    ) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
