package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findByUserId(UUID userId) {
        return categoryRepository.findAllByUserId(userId);
    }

    public Optional<Category> findByIdAndUserId(UUID id, UUID userId) {
        return categoryRepository.findByIdAndUserId(id, userId);
    }

    public Category save(Category category) {
        return categoryRepository.save(category);
    }

    public Optional<Category> updateDescription(UUID id, UUID userId, String newDesc) {
        return categoryRepository.findByIdAndUserId(id, userId)
                .map(cat -> {
                    cat.setDescription(newDesc);
                    return categoryRepository.save(cat);
                });
    }

    public boolean deleteByIdAndUserId(UUID id, UUID userId) {
        long removed = categoryRepository.deleteByIdAndUserId(id, userId);
        return removed > 0;
    }
}
