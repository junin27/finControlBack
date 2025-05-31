package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.CategoryCreateDto;
// O CategoryUpdateDto individual pode ainda ser útil para o endpoint de update individual
import fincontrol.com.fincontrol.dto.CategoryUpdateDto;
import fincontrol.com.fincontrol.dto.CategoryMassUpdateDto; // Novo DTO para atualização em massa
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
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

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
    }

    public List<Category> findByUserId(UUID userId) {
        findUserById(userId);
        return categoryRepository.findAllByUserId(userId);
    }

    public Category getByIdAndUserId(UUID id, UUID userId) {
        findUserById(userId);
        return categoryRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria com ID " + id + " não encontrada ou não pertence ao usuário especificado."));
    }

    @Transactional
    public Category createCategory(CategoryCreateDto dto, UUID userId) {
        User user = findUserById(userId);

        Category category = new Category();
        category.setUserId(user.getId());
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID categoryId, UUID userId, CategoryUpdateDto dto) {
        Category categoryToUpdate = getByIdAndUserId(categoryId, userId);
        boolean needsUpdate = false;

        if (dto.getName() != null) {
            if (!StringUtils.hasText(dto.getName())) {
                throw new InvalidOperationException("O nome da categoria, se fornecido para atualização, não pode ser vazio ou apenas espaços.");
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

    // MÉTODO PARA ATUALIZAR TODAS AS CATEGORIAS DO USUÁRIO
    @Transactional
    public List<Category> massUpdateCategoriesByUser(CategoryMassUpdateDto dto, UUID userId) {
        findUserById(userId); // Valida o usuário
        List<Category> userCategories = categoryRepository.findAllByUserId(userId);

        if (userCategories.isEmpty()) {
            return userCategories; // Nenhuma categoria para atualizar
        }

        boolean nameProvidedToUpdate = dto.getName() != null;
        String newName = nameProvidedToUpdate ? dto.getName() : null;
        if (nameProvidedToUpdate && !StringUtils.hasText(newName)) {
            throw new InvalidOperationException("O nome, se fornecido para atualização em massa, não pode ser vazio ou apenas espaços.");
        }

        boolean descriptionProvidedToUpdate = dto.getDescription() != null;
        String newDescription = descriptionProvidedToUpdate ? dto.getDescription() : null;

        // Se nenhum campo foi fornecido para atualização no DTO, não faz nada.
        if (!nameProvidedToUpdate && !descriptionProvidedToUpdate) {
            return userCategories; // Retorna a lista original sem modificações
        }

        for (Category category : userCategories) {
            boolean categorySpecificUpdate = false;
            if (nameProvidedToUpdate && (newName != null && !newName.equals(category.getName()))) {
                category.setName(newName);
                categorySpecificUpdate = true;
            }
            if (descriptionProvidedToUpdate && (newDescription != null && !newDescription.equals(category.getDescription()))) {
                category.setDescription(newDescription); // Permite limpar se "" for enviado
                categorySpecificUpdate = true;
            }
            // A entidade @PreUpdate cuidará do updatedAt se houver mudança.
        }

        // Salva todas as categorias modificadas (ou todas, se preferir, para atualizar o timestamp)
        // Se só salvar as modificadas, o timestamp só atualiza nas modificadas.
        // O saveAll pode ser mais eficiente se muitas categorias forem atualizadas.
        return categoryRepository.saveAll(userCategories);
    }


    @Transactional
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        Category categoryToDelete = getByIdAndUserId(id, userId);
        try {
            categoryRepository.delete(categoryToDelete);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidOperationException("Não é possível deletar a categoria '" + categoryToDelete.getName() + "' (ID: " + id + ") pois ela está sendo utilizada em outras partes do sistema (ex: despesas).");
        }
    }

    @Transactional
    public int deleteAllCategoriesByUser(UUID userId) {
        findUserById(userId);
        List<Category> categoriesToDelete = categoryRepository.findAllByUserId(userId);
        if (categoriesToDelete.isEmpty()) {
            return 0;
        }

        // Verificação se alguma categoria está em uso antes de deletar em lote.
        // Esta é uma verificação importante para evitar DataIntegrityViolationException não tratada.
        for (Category category : categoriesToDelete) {
            // Você precisará de um método no ExpenseRepository como: boolean existsByCategoryIdAndUserId(UUID categoryId, UUID userId);
            // Ou boolean existsByCategoryId(UUID categoryId); se as despesas não forem filtradas por usuário aqui.
            if (expenseRepository.existsByCategoryId(category.getId())) { // MÉTODO HIPOTÉTICO
                throw new InvalidOperationException("Não é possível deletar todas as categorias. A categoria '" + category.getName() + "' (ID: " + category.getId() + ") está em uso por despesas.");
            }
            // Adicionar verificações para outras entidades que usam Category (ex: Bill, ExtraIncome)
        }
        // Se chegou aqui, nenhuma categoria está em uso (de acordo com as verificações)
        return categoryRepository.deleteAllByUserId(userId);
    }
}