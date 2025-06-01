package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExtraIncomeDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.*;
import fincontrol.com.fincontrol.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects; // Para Objects.nonNull
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ExtraIncomeService {

    private final ExtraIncomeRepository extraRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final BankRepository bankRepo;

    public ExtraIncomeService(ExtraIncomeRepository extraRepo,
                              UserRepository userRepo,
                              CategoryRepository categoryRepo,
                              BankRepository bankRepo) {
        this.extraRepo = extraRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.bankRepo = bankRepo;
    }

    // Método consistente para obter o usuário autenticado internamente, se necessário.
    private User getCurrentUser() {
        String principalName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UUID userId = UUID.fromString(principalName);
            return userRepo.findById(userId)
                    .orElseThrow(() -> {
                        log.warn("Usuário autenticado não encontrado no banco de dados com ID: {}", userId);
                        return new ResourceNotFoundException("Usuário autenticado não encontrado com ID: " + userId);
                    });
        } catch (IllegalArgumentException e) {
            log.error("Identificador do usuário ('{}') no token de autenticação não é um UUID válido.", principalName, e);
            throw new ResourceNotFoundException("Identificador do usuário no token de autenticação não é um UUID válido: " + principalName, e);
        }
    }

    @Transactional
    public ExtraIncomeDto createIncome(UUID userId, ExtraIncomeDto dto) {
        // O userId aqui é passado pelo controller. Valida se o usuário existe.
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + dto.getCategoryId()));

        ExtraIncome income = new ExtraIncome();
        income.setUser(user);
        income.setCategory(category);
        income.setAmount(dto.getAmount());
        income.setDescription(dto.getDescription());
        income.setDate(dto.getDate());

        if (dto.getBankId() != null) {
            Bank bank = bankRepo.findByIdAndUserId(dto.getBankId(), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Banco não encontrado com ID: " + dto.getBankId() + " ou não pertence ao usuário."));

            income.setBank(bank);
            bank.setBalance(bank.getBalance().add(dto.getAmount()));
            bankRepo.save(bank);
        }

        ExtraIncome savedIncome = extraRepo.save(income);
        log.info("Renda extra criada com ID: {} para o usuário {}", savedIncome.getId(), userId);
        return convertToDto(savedIncome);
    }

    private ExtraIncomeDto convertToDto(ExtraIncome extraIncome) {
        ExtraIncomeDto dto = new ExtraIncomeDto();
        dto.setId(extraIncome.getId());
        dto.setDescription(extraIncome.getDescription());
        dto.setAmount(extraIncome.getAmount());
        dto.setDate(extraIncome.getDate());
        // Assumindo que ExtraIncome NÃO tem createdAt/updatedAt, conforme modelo fornecido.
        // Se adicionar, inclua aqui.
        // dto.setCreatedAt(extraIncome.getCreatedAt());
        // dto.setUpdatedAt(extraIncome.getUpdatedAt());

        if (extraIncome.getCategory() != null) {
            dto.setCategoryId(extraIncome.getCategory().getId());
            dto.setCategoryName(extraIncome.getCategory().getName());
        }

        if (extraIncome.getBank() != null) {
            dto.setBankId(extraIncome.getBank().getId());
            dto.setBankName(extraIncome.getBank().getName());
        }

        if (extraIncome.getUser() != null) {
            dto.setUserId(extraIncome.getUser().getId());
            dto.setUserName(extraIncome.getUser().getName()); // Ajuste se o nome do campo/método for outro
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ExtraIncomeDto> getIncomesByUser(UUID userId) {
        if (!userRepo.existsById(userId)) {
            log.warn("Tentativa de listar rendas extras para usuário inexistente: {}", userId);
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId + " para listar rendas extras.");
        }
        List<ExtraIncome> incomes = extraRepo.findByUserIdWithBank(userId);
        log.info("Encontradas {} rendas extras para o usuário {}", incomes.size(), userId);
        return incomes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExtraIncomeDto> getIncomesByBankAndUser(UUID bankId, UUID userId) {
        if (!bankRepo.findByIdAndUserId(bankId, userId).isPresent()) {
            log.warn("Tentativa de listar rendas extras para banco {} do usuário {} falhou: banco não encontrado ou não pertence ao usuário.", bankId, userId);
            throw new ResourceNotFoundException("Banco com ID " + bankId + " não encontrado ou não pertence ao usuário " + userId + ".");
        }
        List<ExtraIncome> incomes = extraRepo.findByBankIdAndUserIdWithBank(bankId, userId);
        log.info("Encontradas {} rendas extras para o banco {} do usuário {}", incomes.size(), bankId, userId);
        return incomes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}