package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.*;

import fincontrol.com.fincontrol.dto.error.ErrorResponseDto;
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
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    private User getAuthenticatedUser(@AuthenticationPrincipal String userEmail) {
        return userService.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário autenticado não encontrado com email " + userEmail));
    }

    // --- CRUD Individual (Existente) ---
    @Operation(summary = "Lista todas as categorias do usuário autenticado")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CategoryDetailResponseDto.class))))
    @GetMapping
    public List<CategoryDetailResponseDto> listAll(
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        return categoryService.findByUserId(authenticatedUser.getId())
                .stream()
                .map(category -> toDetailResponseDto(category, authenticatedUser))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Busca uma categoria por ID do usuário autenticado")
    // ... ApiResponses existentes ...
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailResponseDto> getById(
            @Parameter(description = "ID da categoria", required = true) @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        Category cat = categoryService.getByIdAndUserId(id, authenticatedUser.getId());
        return ResponseEntity.ok(toDetailResponseDto(cat, authenticatedUser));
    }

    @Operation(summary = "Cria uma nova categoria para o usuário autenticado")
    // ... ApiResponses existentes ...
    @PostMapping
    public ResponseEntity<CategoryDetailResponseDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da categoria a criar.", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryCreateDto.class))
            )
            @Valid @RequestBody CategoryCreateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        Category savedCategory = categoryService.createCategory(dto, authenticatedUser.getId());
        CategoryDetailResponseDto responseDto = toDetailResponseDto(savedCategory, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCategory.getId())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "Atualiza uma categoria existente do usuário autenticado")
    // ... ApiResponses existentes ...
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDetailResponseDto> update(
            @Parameter(description = "ID da categoria a atualizar") @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para a categoria.", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryUpdateDto.class))
            )
            @Valid @RequestBody CategoryUpdateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        Category updatedCategory = categoryService.updateCategory(id, authenticatedUser.getId(), dto);
        return ResponseEntity.ok(toDetailResponseDto(updatedCategory, authenticatedUser));
    }

    @Operation(summary = "Remove uma categoria por ID do usuário autenticado")
    // ... ApiResponses existentes ...
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria a remover") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        categoryService.deleteByIdAndUserId(id, authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    // --- NOVOS Endpoints para Operações em Lote ---

    @Operation(summary = "Atualiza TODAS as categorias do usuário autenticado com os mesmos dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todas as categorias do usuário foram atualizadas com sucesso (ou nenhuma alteração foi necessária).",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDetailResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: nome vazio se fornecido)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @PutMapping("/user-all") // Endpoint para atualização em massa de TODAS as categorias do usuário
    public ResponseEntity<List<CategoryDetailResponseDto>> massUpdateUserCategories(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados a serem aplicados a TODAS as categorias do usuário. Envie apenas os campos (name, description) que deseja atualizar em todas.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryMassUpdateDto.class))
            )
            @Valid @RequestBody CategoryMassUpdateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        List<Category> updatedCategories = categoryService.massUpdateCategoriesByUser(dto, authenticatedUser.getId());
        List<CategoryDetailResponseDto> responseDtos = updatedCategories.stream()
                .map(category -> toDetailResponseDto(category, authenticatedUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "Deleta TODAS as categorias do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as categorias do usuário foram deletadas com sucesso (ou o usuário não tinha categorias)."),
            @ApiResponse(responseCode = "400", description = "Não foi possível deletar todas as categorias (ex: algumas estão em uso por despesas).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @DeleteMapping("/user-all") // Endpoint para deletar todas as categorias do usuário
    public ResponseEntity<Void> deleteAllUserCategories(
            @Parameter(hidden = true) @AuthenticationPrincipal String userEmail
    ) {
        User authenticatedUser = getAuthenticatedUser(userEmail);
        categoryService.deleteAllCategoriesByUser(authenticatedUser.getId());
        return ResponseEntity.noContent().build();
    }

    // Mapeia Category e User para o DTO de resposta detalhado e aninhado
    private CategoryDetailResponseDto toDetailResponseDto(Category category, User user) {
        UserSimpleDto userSimpleDto = new UserSimpleDto(user.getId(), user.getName());

        CategoryDataDto categoryDataDto = new CategoryDataDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );

        return new CategoryDetailResponseDto(userSimpleDto, categoryDataDto);
    }
}