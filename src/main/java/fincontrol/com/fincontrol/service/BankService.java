package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.BankCreateDto;
import fincontrol.com.fincontrol.dto.BankDto;
import fincontrol.com.fincontrol.dto.BankUpdateDto;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BankService {
    private final BankRepository bankRepo;
    private final ExtraIncomeRepository incomeRepo;
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    public BankService(BankRepository b,
                       ExtraIncomeRepository i,
                       ExpenseRepository e,
                       UserRepository u) {         // ← novo
        this.bankRepo   = b;
        this.incomeRepo = i;
        this.expenseRepo= e;
        this.userRepo   = u;
    }
    public BankDto create(BankCreateDto dto) {
        // 1) quem está logado?
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2) monte o Bank e associe
        Bank b = new Bank();
        b.setName(dto.getName());
        b.setDescription(dto.getDescription());
        b.setUser(u);                     // ← aqui
        bankRepo.save(b);

        return toDto(b);
    }

    public List<BankDto> listAll() {
        return bankRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public BankDto update(UUID id, BankUpdateDto dto) {
        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));
        if (dto.getName() != null)       b.setName(dto.getName());
        if (dto.getDescription() != null)b.setDescription(dto.getDescription());
        bankRepo.save(b);
        return toDto(b);
    }

    public void deleteAll(UUID id) {
        bankRepo.deleteById(id);
    }

    @Transactional
    public void clearIncomes(UUID id) {
        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));
        incomeRepo.deleteByBankId(id);
        b.setName(null);
        b.setDescription(null);
        bankRepo.save(b);
    }

    @Transactional
    public void clearExpenses(UUID id) {
        Bank b = bankRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Banco não encontrado"));
        expenseRepo.deleteByBankId(id);
        b.setName(null);
        b.setDescription(null);
        bankRepo.save(b);
    }

    private BankDto toDto(Bank b) {
        BigDecimal in  = incomeRepo.sumIncomeByBank(b.getId());
        BigDecimal out = expenseRepo.sumExpenseByBank(b.getId());
        return new BankDto(b.getId(), b.getName(), b.getDescription(), in, out);
    }
}
