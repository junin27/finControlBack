package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.CategoryDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.service.CategoryService;
import fincontrol.com.fincontrol.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "Categorias", description = "Gerenciamento de categorias do usuário")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final UserService userService;

    public CategoryController(CategoryService categoryService,
                              UserService userService) {
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @Operation(summary = "Lista todas as categorias do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(schema = @Schema(implementation = CategoryDto.class)))
    @GetMapping
    public List<CategoryDto> listAll(
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userEmail
    ) {
        User u = userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email " + userEmail));
        UUID userId = u.getId();

        return categoryService.findByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Operation(summary = "Busca uma categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getById(
            @Parameter(description = "ID da categoria", required = true,
                    example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userEmail
    ) {
        User u = userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email " + userEmail));
        UUID userId = u.getId();

        Category cat = categoryService.getByIdAndUserId(id, userId);
        return ResponseEntity.ok(toDto(cat));
    }

    @Operation(summary = "Cria uma nova categoria para o usuário autenticado")
    @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
            content = @Content(schema = @Schema(implementation = CategoryDto.class)))
    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da categoria a criar",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            )
            @Valid @RequestBody CategoryDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userEmail
    ) {
        User u = userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email " + userEmail));
        UUID userId = u.getId();

        Category toSave = new Category();
        toSave.setUserId(userId);
        toSave.setDescription(dto.getDescription());

        Category saved = categoryService.save(toSave);
        CategoryDto result = toDto(saved);

        URI location = URI.create("/api/categories/" + saved.getId());
        return ResponseEntity.created(location).body(result);
    }

    @Operation(summary = "Atualiza a descrição de uma categoria existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @Parameter(description = "ID da categoria a atualizar", required = true,
                    example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nova descrição da categoria",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))
            )
            @Valid @RequestBody CategoryDto dto,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userEmail
    ) {
        User u = userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email " + userEmail));
        UUID userId = u.getId();

        Category updated = categoryService.updateDescription(id, userId, dto.getDescription());
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(summary = "Remove uma categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria a remover", required = true,
                    example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @Parameter(hidden = true)
            @AuthenticationPrincipal String userEmail
    ) {
        User u = userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário não encontrado com email " + userEmail));
        UUID userId = u.getId();

        categoryService.deleteByIdAndUserId(id, userId);
        return ResponseEntity.noContent().build();
    }

    private CategoryDto toDto(Category c) {
        return new CategoryDto(
                c.getId(),
                c.getUserId(),
                c.getDescription(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}
