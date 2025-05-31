package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.*; // Importa todas as entidades do pacote
import fincontrol.com.fincontrol.model.enums.BillStatus; // Importa o enum
import fincontrol.com.fincontrol.repository.*; // Importa todos os repositórios

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BillService {

    private static final Logger logger = LoggerFactory.getLogger(BillService.class);

    private final BillRepository billRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository; // Repositório de Despesas
    private final BankRepository bankRepository;
    private final CategoryRepository categoryRepository;


    public BillService(BillRepository billRepository,
                       UserRepository userRepository,
                       ExpenseRepository expenseRepository, // Injetar ExpenseRepository
                       BankRepository bankRepository,
                       CategoryRepository categoryRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository; // Atribuir
        this.bankRepository = bankRepository;
        this.categoryRepository = categoryRepository;
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    // Método CORRIGIDO para buscar uma despesa específica
    private Expense findExpenseByIdAndUser(UUID expenseId, UUID userId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId) // USA findByIdAndUserId
                .orElseThrow(() -> new ResourceNotFoundException("Expense with ID " + expenseId + " not found or does not belong to the user."));
    }

    private Bank findBankByIdAndUser(UUID bankId, UUID userId) {
        return bankRepository.findByIdAndUserId(bankId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank with ID " + bankId + " not found or does not belong to the user."));
    }

    @Transactional
    public BillResponseDto createBill(BillCreateDto dto, UUID userId) {
        User user = findUserById(userId);
        Expense expense = findExpenseByIdAndUser(dto.getExpenseId(), userId); // Esta chamada agora está correta

        // Validação da data de vencimento já é feita pelo @FutureOrPresent no DTO
        // e @Valid no controller. Se chegar aqui, a data é válida.

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setExpense(expense);
        bill.setPaymentMethod(dto.getPaymentMethod());
        bill.setDueDate(dto.getDueDate());
        bill.setAutoPay(dto.getAutoPay());
        bill.setStatus(BillStatus.PENDING);

        if (dto.getBankId() != null) {
            Bank bank = findBankByIdAndUser(dto.getBankId(), userId);
            bill.setBank(bank);
        }

        Bill savedBill = billRepository.save(bill);
        return toResponseDto(savedBill);
    }

    public BillResponseDto getBillByIdAndUser(UUID billId, UUID userId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill with ID " + billId + " not found or does not belong to the user."));
        return toResponseDto(bill);
    }

    public List<BillResponseDto> getAllBillsFiltered(UUID userId, BillStatus status, UUID expenseCategoryId, UUID bankId) {
        findUserById(userId);

        Specification<Bill> spec = Specification.where(BillSpecifications.hasUserId(userId));

        if (status != null) {
            spec = spec.and(BillSpecifications.hasStatus(status));
        }
        if (expenseCategoryId != null) {
            categoryRepository.findById(expenseCategoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with ID: " + expenseCategoryId));
            spec = spec.and(BillSpecifications.hasExpenseCategoryId(expenseCategoryId));
        }
        if (bankId != null) {
            findBankByIdAndUser(bankId, userId);
            spec = spec.and(BillSpecifications.hasBankId(bankId));
        }

        return billRepository.findAll(spec).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BillResponseDto updateBill(UUID billId, BillUpdateDto dto, UUID userId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill with ID " + billId + " not found or does not belong to the user."));

        if (bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.PAID_LATE) {
            throw new InvalidOperationException("Cannot update a bill that has already been paid.");
        }

        boolean updated = false;

        if (dto.getExpenseId() != null && !dto.getExpenseId().equals(bill.getExpense().getId())) {
            Expense newExpense = findExpenseByIdAndUser(dto.getExpenseId(), userId);
            bill.setExpense(newExpense);
            updated = true;
        }

        // Lógica para atualizar ou desassociar o banco
        if (dto.getBankId() != null) { // Se um novo bankId foi fornecido
            if (bill.getBank() == null || !dto.getBankId().equals(bill.getBank().getId())) {
                Bank newBank = findBankByIdAndUser(dto.getBankId(), userId);
                bill.setBank(newBank);
                updated = true;
            }
        } else { // Se dto.getBankId() é null (porque foi enviado como null no JSON para desassociar)
            if (bill.getBank() != null) { // Só desassocie se havia um banco antes
                bill.setBank(null);
                updated = true;
            }
        }


        if (dto.getPaymentMethod() != null && dto.getPaymentMethod() != bill.getPaymentMethod()) {
            bill.setPaymentMethod(dto.getPaymentMethod());
            updated = true;
        }
        if (dto.getDueDate() != null && !dto.getDueDate().equals(bill.getDueDate())) {
            // Validação de data futura já no DTO
            bill.setDueDate(dto.getDueDate());
            updated = true;
        }
        if (dto.getAutoPay() != null && dto.getAutoPay() != bill.isAutoPay()) {
            bill.setAutoPay(dto.getAutoPay());
            updated = true;
        }

        if (updated) {
            bill = billRepository.save(bill);
        }
        return toResponseDto(bill);
    }

    @Transactional
    public void deleteBill(UUID billId, UUID userId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill with ID " + billId + " not found or does not belong to the user."));

        billRepository.delete(bill);
    }

    @Transactional
    public BillResponseDto markAsPaidManually(UUID billId, UUID userId) {
        Bill bill = billRepository.findByIdAndUserId(billId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bill with ID " + billId + " not found or does not belong to the user."));

        if (bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.PAID_LATE) {
            throw new InvalidOperationException("This bill has already been marked as paid.");
        }

        bill.setPaymentDate(LocalDate.now());
        if (LocalDate.now().isAfter(bill.getDueDate())) {
            bill.setStatus(BillStatus.PAID_LATE);
        } else {
            bill.setStatus(BillStatus.PAID);
        }
        Bill savedBill = billRepository.save(bill);
        return toResponseDto(savedBill);
    }

    @Transactional
    public void processOverdueBillsJob() {
        logger.info("Starting job to mark overdue bills...");
        List<Bill> pendingOverdueBills = billRepository.findByStatusAndDueDateBefore(
                BillStatus.PENDING,
                LocalDate.now()
        );
        for (Bill bill : pendingOverdueBills) {
            bill.setStatus(BillStatus.OVERDUE);
            billRepository.save(bill);
            logger.info("Bill ID {} marked as OVERDUE.", bill.getId());
        }
        logger.info("Overdue bills job finished. {} bills updated.", pendingOverdueBills.size());
    }

    @Transactional
    public void processAutomaticPaymentsJob() {
        logger.info("Starting job for automatic payments...");
        LocalDate today = LocalDate.now();
        List<Bill> billsToPayToday = billRepository
                .findAllByAutoPayTrueAndStatusAndDueDateAndBankIsNotNull(
                        BillStatus.PENDING,
                        today
                );

        int successfullyPaidCount = 0;
        for (Bill bill : billsToPayToday) {
            Bank bank = bill.getBank();
            Expense expense = bill.getExpense();

            if (bank.getBalance().compareTo(expense.getValue()) >= 0) {
                bank.setBalance(bank.getBalance().subtract(expense.getValue()));
                bankRepository.save(bank);

                bill.setPaymentDate(today);
                bill.setStatus(BillStatus.PAID);
                billRepository.save(bill);
                successfullyPaidCount++;
                logger.info("Bill ID {} paid automatically from bank {}. Bank balance updated.", bill.getId(), bank.getName());
            } else {
                logger.error("Bank ({}) (ID: {}) has insufficient balance to pay bill (ID: {}). Bill amount: {}, Bank balance: {}.",
                        bank.getName(), bank.getId(), bill.getId(), expense.getValue(), bank.getBalance());
            }
        }
        logger.info("Automatic payments job finished. {} bills paid successfully.", successfullyPaidCount);
    }

    private BillResponseDto toResponseDto(Bill bill) {
        User user = bill.getUser();
        Expense expense = bill.getExpense();
        Category category = expense.getCategory();
        Bank bank = bill.getBank();

        UserSimpleDto userSimpleDto = new UserSimpleDto(user.getId(), user.getName());

        CategorySimpleDto categorySimpleDto = null;
        if (category != null) {
            categorySimpleDto = new CategorySimpleDto(category.getId(), category.getName());
        }

        ExpenseSimpleDto expenseSimpleDto = new ExpenseSimpleDto(expense.getId(), expense.getName(), categorySimpleDto);

        BankSimpleDto bankSimpleDto = null;
        if (bank != null) {
            bankSimpleDto = new BankSimpleDto(bank.getId(), bank.getName());
        }

        return new BillResponseDto(
                bill.getId(),
                userSimpleDto,
                expenseSimpleDto,
                bankSimpleDto,
                bill.getPaymentMethod(),
                bill.getDueDate(),
                bill.isAutoPay(),
                bill.getStatus(),
                bill.getPaymentDate(),
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}
