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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import jakarta.validation.Valid; // Ainda pode ser útil para outras validações no DTO
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
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class)))
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

    @Operation(summary = "Busca uma categoria por ID do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoria ou Usuário não encontrado")
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
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Nome da categoria não fornecido ou inválido"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping
    public ResponseEntity<CategoryDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da categoria a criar. O campo 'name' é obrigatório. O campo 'description' é opcional e terá um valor padrão se não informado.",
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

        if (!StringUtils.hasText(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome da categoria é obrigatório.");
        }

        Category toSave = new Category();
        toSave.setUserId(userId);
        toSave.setName(dto.getName());

        // O campo 'description' do DTO é passado para a entidade.
        // Se for null no DTO, o @PrePersist na entidade definirá o valor padrão.
        // Se for uma string (mesmo vazia) no DTO, esse valor será usado (e o @PrePersist pode ou não sobrescrever se for só espaços).
        toSave.setDescription(dto.getDescription());


        Category saved = categoryService.save(toSave);
        CategoryDto result = toDto(saved);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(result);
    }

    @Operation(summary = "Atualiza o nome e/ou descrição de uma categoria existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex: nome vazio se fornecido para atualização)"),
            @ApiResponse(responseCode = "404", description = "Categoria ou Usuário não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
            @Parameter(description = "ID da categoria a atualizar", required = true,
                    example = "7fa85f64-1234-4562-b3fc-2c963f66afa6")
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Novos dados para a categoria. Campos não fornecidos (ou nulos) não serão alterados. Para limpar a descrição, envie uma string vazia.",
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

        // Validação: se o nome for fornecido para atualização, não pode ser apenas espaços em branco.
        if (dto.getName() != null && !StringUtils.hasText(dto.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O nome da categoria, se fornecido para atualização, não pode ser vazio ou apenas espaços.");
        }

        Category updated = categoryService.updateCategory(id, userId, dto);
        return ResponseEntity.ok(toDto(updated));
    }

    @Operation(summary = "Remove uma categoria por ID do usuário autenticado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria ou Usuário não encontrado")
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
                c.getName(),
                c.getDescription(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}