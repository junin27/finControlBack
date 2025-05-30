package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.CategoryDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Para StringUtils.hasText

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public List<Category> findByUserId(UUID userId) {
        return repo.findAllByUserId(userId);
    }

    public Category getByIdAndUserId(UUID id, UUID userId) {
        return repo.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoria não encontrada com ID " + id + " para o usuário especificado."));
    }

    @Transactional
    public Category save(Category category) {
        // A lógica de @PrePersist na entidade Category cuidará dos timestamps e do description padrão.
        return repo.save(category);
    }

    @Transactional
    public Category updateCategory(UUID id, UUID userId, CategoryDto categoryDto) {
        Category categoryToUpdate = getByIdAndUserId(id, userId);
        boolean needsUpdate = false;

        // Atualiza o nome se fornecido no DTO e diferente do existente (e não apenas espaços em branco)
        if (categoryDto.getName() != null && StringUtils.hasText(categoryDto.getName())) {
            if (!categoryDto.getName().equals(categoryToUpdate.getName())) {
                categoryToUpdate.setName(categoryDto.getName());
                needsUpdate = true;
            }
        }

        // Atualiza a descrição se o campo 'description' estiver presente no DTO (mesmo que seja string vazia)
        // Se o campo não estiver no JSON (resultando em DTO.description == null), não atualiza.
        // Para permitir limpar a descrição, o cliente envia {"description": ""}
        // Para não alterar a descrição, o cliente omite o campo "description" do JSON.
        if (categoryDto.getDescription() != null) { // Checa se a propriedade foi enviada
            if (!categoryDto.getDescription().equals(categoryToUpdate.getDescription())) {
                categoryToUpdate.setDescription(categoryDto.getDescription());
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            // O @PreUpdate na entidade Category cuidará do updatedAt
            return repo.save(categoryToUpdate);
        }
        return categoryToUpdate;
    }

    @Transactional
    public void deleteByIdAndUserId(UUID id, UUID userId) {
        long removedCount = repo.deleteByIdAndUserId(id, userId);
        if (removedCount == 0) {
            // Isso significa que ou a categoria não existe ou não pertence ao usuário,
            // ou já foi deletada.
            throw new ResourceNotFoundException(
                    "Categoria com ID " + id + " não encontrada para o usuário especificado ou já removida.");
        }
    }
}