package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExtraIncomeDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.*;
import fincontrol.com.fincontrol.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class ExtraIncomeService {

    private final ExtraIncomeRepository extraRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ExtraIncomeService(ExtraIncomeRepository extraRepo,
                              UserRepository userRepo,
                              CategoryRepository categoryRepo) {
        this.extraRepo = extraRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    public ExtraIncome add(UUID userId, ExtraIncomeDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID\n " + userId));
        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + dto.getCategoryId()));

        ExtraIncome ei = new ExtraIncome();
        ei.setUser(user);
        ei.setCategory(category);
        ei.setAmount(dto.getAmount());
        ei.setDescription(dto.getDescription());
        ei.setDate(dto.getDate());

        return extraRepo.save(ei);
    }

    public List<ExtraIncome> listByUser(UUID userId) {
        return extraRepo.findByUserId(userId);
    }
}