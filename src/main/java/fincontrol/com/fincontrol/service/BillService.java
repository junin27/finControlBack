package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.*;
import fincontrol.com.fincontrol.model.enums.BillStatus;
import fincontrol.com.fincontrol.repository.*;

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
    private final ExpenseRepository expenseRepository;
    private final BankRepository bankRepository;
    // CategoryRepository might not be directly needed if Expense entity has Category loaded or its name
    // but it's good for validating categoryId in filters.
    private final CategoryRepository categoryRepository;


    public BillService(BillRepository billRepository,
                       UserRepository userRepository,
                       ExpenseRepository expenseRepository,
                       BankRepository bankRepository,
                       CategoryRepository categoryRepository) {
        this.billRepository = billRepository;
        this.userRepository = userRepository;
        this.expenseRepository = expenseRepository;
        this.bankRepository = bankRepository;
        this.categoryRepository = categoryRepository;
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private Expense findExpenseByIdAndUser(UUID expenseId, UUID userId) {
        return expenseRepository.findByIdAndUserId(expenseId, userId) // Usa o método correto com dois argumentos
                .orElseThrow(() -> new ResourceNotFoundException("Despesa com ID " + expenseId + " não encontrada ou não pertence ao usuário."));
    }

    private Bank findBankByIdAndUser(UUID bankId, UUID userId) {
        return bankRepository.findByIdAndUserId(bankId, userId) // Assumes BankRepository has findByIdAndUserId
                .orElseThrow(() -> new ResourceNotFoundException("Bank with ID " + bankId + " not found or does not belong to the user."));
    }

    @Transactional
    public BillResponseDto createBill(BillCreateDto dto, UUID userId) {
        User user = findUserById(userId);
        Expense expense = findExpenseByIdAndUser(dto.getExpenseId(), userId);

        // Validation for dueDate is handled by @FutureOrPresent in DTO + @Valid in Controller
        // if (dto.getDueDate().isBefore(LocalDate.now())) {
        //     throw new InvalidOperationException("Due date cannot be in the past.");
        // }

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setExpense(expense);
        bill.setPaymentMethod(dto.getPaymentMethod());
        bill.setDueDate(dto.getDueDate());
        bill.setAutoPay(dto.getAutoPay());
        bill.setStatus(BillStatus.PENDING); // Default status

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
        findUserById(userId); // Validate user exists

        Specification<Bill> spec = Specification.where(BillSpecifications.hasUserId(userId));

        if (status != null) {
            spec = spec.and(BillSpecifications.hasStatus(status));
        }
        if (expenseCategoryId != null) {
            categoryRepository.findById(expenseCategoryId) // Validate category exists
                    .orElseThrow(() -> new ResourceNotFoundException("Expense category not found with ID: " + expenseCategoryId));
            spec = spec.and(BillSpecifications.hasExpenseCategoryId(expenseCategoryId));
        }
        if (bankId != null) {
            findBankByIdAndUser(bankId, userId); // Validate bank exists and belongs to user
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

        if (dto.getBankId() != null) { // If a new bankId is provided
            if (bill.getBank() == null || !dto.getBankId().equals(bill.getBank().getId())) {
                Bank newBank = findBankByIdAndUser(dto.getBankId(), userId);
                bill.setBank(newBank);
                updated = true;
            }
        } else { // If bankId is not in DTO, client might intend to disassociate.
            // To explicitly disassociate, client should send "bankId": null in JSON.
            // If dto.getBankId() is null because it was sent as null in JSON:
            if (bill.getBank() != null && dto.getBankId() == null) { // Check if bankId field was present in JSON and set to null
                // This part requires knowing if the field was explicitly set to null in the DTO.
                // A common pattern is to have a boolean flag in DTO like "clearBankId".
                // For simplicity, if bankId is null in DTO, we'll disassociate if there was a bank.
                bill.setBank(null);
                updated = true;
            }
        }

        if (dto.getPaymentMethod() != null && dto.getPaymentMethod() != bill.getPaymentMethod()) {
            bill.setPaymentMethod(dto.getPaymentMethod());
            updated = true;
        }
        if (dto.getDueDate() != null && !dto.getDueDate().equals(bill.getDueDate())) {
            // Validation for dueDate is handled by @FutureOrPresent in DTO + @Valid in Controller
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

        // Add any business logic before deletion if needed (e.g., cannot delete if PAGO)
        // if (bill.getStatus() == BillStatus.PAID || bill.getStatus() == BillStatus.PAID_LATE) {
        //     throw new InvalidOperationException("Cannot delete a bill that has already been paid.");
        // }
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

    // --- Scheduled Job Methods ---
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
                        BillStatus.PENDING, // Only try to pay PENDING bills automatically on due date
                        today
                );

        int successfullyPaidCount = 0;
        for (Bill bill : billsToPayToday) {
            Bank bank = bill.getBank(); // Bank is already associated and not null due to query
            Expense expense = bill.getExpense(); // Expense is associated

            if (bank.getBalance().compareTo(expense.getValue()) >= 0) {
                bank.setBalance(bank.getBalance().subtract(expense.getValue()));
                bankRepository.save(bank);

                bill.setPaymentDate(today);
                bill.setStatus(BillStatus.PAID); // Paid on due date
                billRepository.save(bill);
                successfullyPaidCount++;
                logger.info("Bill ID {} paid automatically from bank {}. Bank balance updated.", bill.getId(), bank.getName());
            } else {
                logger.error("Bank ({}) (ID: {}) has insufficient balance to pay bill (ID: {}). Bill amount: {}, Bank balance: {}.",
                        bank.getName(), bank.getId(), bill.getId(), expense.getValue(), bank.getBalance());
                // Optionally, mark the bill as OVERDUE or PENDING_INSUFFICIENT_FUNDS, or notify user
                // For now, it remains PENDING and will be marked OVERDUE by the other job if not paid.
            }
        }
        logger.info("Automatic payments job finished. {} bills paid successfully.", successfullyPaidCount);
    }

    // --- DTO Conversion ---
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
