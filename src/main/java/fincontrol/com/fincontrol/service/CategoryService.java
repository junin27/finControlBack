package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import org.springframework.stereotype.Service;

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
                        "Categoria não encontrada com ID " + id));
    }

    public Category save(Category category) {
        return repo.save(category);
    }

    public Category updateDescription(UUID id, UUID userId, String newDescription) {
        Category cat = getByIdAndUserId(id, userId);
        cat.setDescription(newDescription);
        return repo.save(cat);
    }

    public void deleteByIdAndUserId(UUID id, UUID userId) {
        long removed = repo.deleteByIdAndUserId(id, userId);
        if (removed == 0) {
            throw new ResourceNotFoundException("Categoria não encontrada com ID " + id);
        }
    }
}
