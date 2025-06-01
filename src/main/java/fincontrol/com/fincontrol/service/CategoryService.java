package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.CategoryCreateDto;
import fincontrol.com.fincontrol.dto.CategoryMassUpdateDto;
import fincontrol.com.fincontrol.dto.CategoryUpdateDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public CategoryService(CategoryRepository categoryRepository,
                           UserRepository userRepository,
                           ExpenseRepository expenseRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
    }

    /**
     * Lê o Authentication do SecurityContext, supõe que auth.getName() == UUID do usuário.
     * Faz findById(...) no UserRepository. Se falhar, lança ResourceNotFoundException.
     */
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new ResourceNotFoundException("Usuário não autenticado");
        }

        UUID userId;
        try {
            userId = UUID.fromString(auth.getName());
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Formato de identificador de usuário inválido: " + auth.getName());
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuário autenticado não encontrado com ID: " + userId));
    }

    /**
     * Lista todas as categorias vinculadas ao usuário logado.
     */
    public List<Category> listAllCategories() {
        User user = getAuthenticatedUser();
        return categoryRepository.findAllByUserId(user.getId());
    }

    /**
     * Busca uma categoria específica (por ID) do usuário logado.
     */
    public Category getCategoryById(UUID categoryId) {
        User user = getAuthenticatedUser();
        return categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria com ID " + categoryId + " não encontrada ou não pertence ao usuário."
                ));
    }

    /**
     * Cria uma nova categoria para o usuário logado.
     */
    @Transactional
    public Category createCategory(CategoryCreateDto dto) {
        User user = getAuthenticatedUser();

        Category category = new Category();
        category.setUserId(user.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return categoryRepository.save(category);
    }

    /**
     * Atualiza uma categoria existente do usuário logado.
     */
    @Transactional
    public Category updateCategory(UUID categoryId, CategoryUpdateDto dto) {
        User user = getAuthenticatedUser();
        Category categoryToUpdate = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria com ID " + categoryId + " não encontrada ou não pertence ao usuário."
                ));

        boolean needsUpdate = false;

        if (dto.getName() != null) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new InvalidOperationException(
                        "O nome da categoria, se fornecido para atualização, não pode ser vazio ou apenas espaços."
                );
            }
            if (!dto.getName().equals(categoryToUpdate.getName())) {
                categoryToUpdate.setName(dto.getName());
                needsUpdate = true;
            }
        }

        if (dto.getDescription() != null) {
            if (!dto.getDescription().equals(categoryToUpdate.getDescription())) {
                categoryToUpdate.setDescription(dto.getDescription());
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            return categoryRepository.save(categoryToUpdate);
        }
        return categoryToUpdate;
    }

    /**
     * Atualiza em massa todas as categorias do usuário logado.
     * Se nenhum campo for fornecido no DTO, retorna a lista sem alterações.
     */
    @Transactional
    public List<Category> massUpdateCategories(CategoryMassUpdateDto dto) {
        User user = getAuthenticatedUser();
        List<Category> userCategories = categoryRepository.findAllByUserId(user.getId());

        if (userCategories.isEmpty()) {
            return userCategories;
        }

        boolean nameProvided = dto.getName() != null;
        String newName = dto.getName();
        if (nameProvided && !StringUtils.hasText(newName)) {
            throw new InvalidOperationException(
                    "O nome, se fornecido para atualização em massa, não pode ser vazio ou apenas espaços."
            );
        }

        boolean descriptionProvided = dto.getDescription() != null;
        String newDescription = dto.getDescription();

        if (!nameProvided && !descriptionProvided) {
            return userCategories;
        }

        for (Category category : userCategories) {
            boolean updated = false;
            if (nameProvided && newName != null && !newName.equals(category.getName())) {
                category.setName(newName);
                updated = true;
            }
            if (descriptionProvided && newDescription != null && !newDescription.equals(category.getDescription())) {
                category.setDescription(newDescription);
                updated = true;
            }
            // O @PreUpdate na entidade pode atualizar updatedAt, se configurado.
        }

        return categoryRepository.saveAll(userCategories);
    }

    /**
     * Deleta uma categoria específica do usuário logado.
     * Se a categoria estiver em uso por despesas, lança InvalidOperationException.
     */
    @Transactional
    public void deleteCategory(UUID categoryId) {
        User user = getAuthenticatedUser();
        Category categoryToDelete = categoryRepository.findByIdAndUserId(categoryId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria com ID " + categoryId + " não encontrada ou não pertence ao usuário."
                ));

        try {
            categoryRepository.delete(categoryToDelete);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException(
                    "Não é possível deletar a categoria '" +
                            categoryToDelete.getName() +
                            "' (ID: " + categoryId +
                            ") pois ela está sendo utilizada em outras partes do sistema (ex: despesas)."
            );
        }
    }

    /**
     * Deleta todas as categorias do usuário logado.
     * Retorna a quantidade de categorias efetivamente deletadas.
     * Se alguma estiver em uso por despesas, lança InvalidOperationException.
     */
    @Transactional
    public int deleteAllCategories() {
        User user = getAuthenticatedUser();
        List<Category> categoriesToDelete = categoryRepository.findAllByUserId(user.getId());

        if (categoriesToDelete.isEmpty()) {
            return 0;
        }

        for (Category category : categoriesToDelete) {
            if (expenseRepository.existsByCategoryIdAndUserId(category.getId(), user.getId())) {
                throw new InvalidOperationException(
                        "Não é possível deletar todas as categorias. " +
                                "A categoria '" + category.getName() + "' (ID: " + category.getId() +
                                ") está em uso por despesas."
                );
            }
        }

        return categoryRepository.deleteAllByUserId(user.getId());
    }
}
