package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.CategoryCreateDto;
import fincontrol.com.fincontrol.dto.CategoryDataDto;
import fincontrol.com.fincontrol.dto.CategoryDetailResponseDto;
import fincontrol.com.fincontrol.dto.CategoryMassUpdateDto;
import fincontrol.com.fincontrol.dto.CategoryUpdateDto;
import fincontrol.com.fincontrol.dto.UserSimpleDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.service.CategoryService;
import fincontrol.com.fincontrol.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.Parameter;

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

    /**
     * Converte o principal (auth.getName()) em UUID e faz findById no UserService.
     * Se não encontrar, lança ResourceNotFoundException.
     */
    private User getAuthenticatedUser(@AuthenticationPrincipal String userIdString) {
        if (userIdString == null) {
            throw new ResourceNotFoundException("Usuário não autenticado");
        }
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Formato de identificador de usuário inválido: " + userIdString);
        }
        return userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário autenticado não encontrado com ID " + userId));
    }

    // ------------------------ CRUD Individual ------------------------

    @Operation(summary = "Lista todas as categorias do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDetailResponseDto.class))))
    })
    @GetMapping
    public List<CategoryDetailResponseDto> listAll(
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        User authenticatedUser = getAuthenticatedUser(userIdString);
        return categoryService.listAllCategories()
                .stream()
                .map(cat -> toDetailResponseDto(cat, authenticatedUser))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Busca uma categoria por ID do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDetailResponseDto> getById(
            @Parameter(description = "ID da categoria", required = true)
            @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        User authenticatedUser = getAuthenticatedUser(userIdString);
        Category cat = categoryService.getCategoryById(id);
        return ResponseEntity.ok(toDetailResponseDto(cat, authenticatedUser));
    }

    @Operation(summary = "Cria uma nova categoria para o usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @PostMapping
    public ResponseEntity<CategoryDetailResponseDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da categoria a criar.", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryCreateDto.class))
            )
            @Valid @RequestBody CategoryCreateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        User authenticatedUser = getAuthenticatedUser(userIdString);
        // O service cria usando o usuário logado, não precisa passar ID daqui
        Category savedCategory = categoryService.createCategory(dto);
        CategoryDetailResponseDto responseDto = toDetailResponseDto(savedCategory, authenticatedUser);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCategory.getId())
                .toUri();
        return ResponseEntity.created(location).body(responseDto);
    }

    @Operation(summary = "Atualiza uma categoria existente do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDetailResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDetailResponseDto> update(
            @Parameter(description = "ID da categoria a atualizar") @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para a categoria.", required = true,
                    content = @Content(schema = @Schema(implementation = CategoryUpdateDto.class))
            )
            @Valid @RequestBody CategoryUpdateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        User authenticatedUser = getAuthenticatedUser(userIdString);
        Category updatedCategory = categoryService.updateCategory(id, dto);
        return ResponseEntity.ok(toDetailResponseDto(updatedCategory, authenticatedUser));
    }

    @Operation(summary = "Remove uma categoria por ID do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria ou usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema())),
            @ApiResponse(responseCode = "400", description = "Não é possível remover esta categoria (em uso)",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID da categoria a remover") @PathVariable UUID id,
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        getAuthenticatedUser(userIdString); // somente para validar que o usuário existe
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ---------------- Novos endpoints para operações em lote ----------------

    @Operation(summary = "Atualiza TODAS as categorias do usuário autenticado com os mesmos dados fornecidos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Todas as categorias do usuário foram atualizadas com sucesso",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = CategoryDetailResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: nome vazio)",
                    content = @Content(mediaType = "application/json", schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @PutMapping("/user-all")
    public ResponseEntity<List<CategoryDetailResponseDto>> massUpdateUserCategories(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados a serem aplicados a TODAS as categorias do usuário.",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CategoryMassUpdateDto.class))
            )
            @Valid @RequestBody CategoryMassUpdateDto dto,
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        User authenticatedUser = getAuthenticatedUser(userIdString);
        List<Category> updatedCategories = categoryService.massUpdateCategories(dto);
        List<CategoryDetailResponseDto> responseDtos = updatedCategories.stream()
                .map(category -> toDetailResponseDto(category, authenticatedUser))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    @Operation(summary = "Deleta TODAS as categorias do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Todas as categorias do usuário foram deletadas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não foi possível deletar todas as categorias (alguma em uso)",
                    content = @Content(mediaType = "application/json", schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json", schema = @Schema()))
    })
    @DeleteMapping("/user-all")
    public ResponseEntity<Void> deleteAllUserCategories(
            @Parameter(hidden = true) @AuthenticationPrincipal String userIdString
    ) {
        getAuthenticatedUser(userIdString);
        categoryService.deleteAllCategories();
        return ResponseEntity.noContent().build();
    }

    /**
     * Faz a conversão de entidade Category + User → CategoryDetailResponseDto
     */
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
