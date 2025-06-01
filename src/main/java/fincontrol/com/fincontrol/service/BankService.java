package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.InsufficientBalanceException;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BankService {
    private final BankRepository bankRepo;
    private final ExtraIncomeRepository incomeRepo;
    private final ExpenseRepository expenseRepo;
    private final UserRepository userRepo;

    // Mensagens de erro
    private static final String ERROR_ADD_MONEY_AMOUNT_INVALID        = "O valor para entrada de dinheiro no banco precisa ser maior que 0, pois não há como somar um valor menor que 1.";
    private static final String ERROR_ADD_MONEY_ALL_AMOUNT_INVALID    = "O valor para entrada de dinheiro nos bancos precisa ser maior que 0.";
    private static final String ERROR_REMOVE_MONEY_AMOUNT_INVALID     = "O valor para saída de dinheiro no banco precisa ser maior que 0, pois não há como remover um valor menor que 1.";
    private static final String ERROR_REMOVE_MONEY_ALL_AMOUNT_INVALID = "O valor para saída de dinheiro nos bancos precisa ser maior que 0.";
    private static final String ERROR_INSUFFICIENT_FUNDS_GENERIC      = "Saldo insuficiente para realizar a operação.";
    private static final String ERROR_TRANSFER_AMOUNT_INVALID         = "O valor da transferência precisa ser maior que zero.";
    private static final String ERROR_TRANSFER_SAME_BANK              = "O banco de origem e destino não podem ser os mesmos.";
    private static final String ERROR_TRANSFER_INSUFFICIENT_FUNDS     = "Saldo insuficiente no banco de origem para realizar a transferência.";

    public BankService(BankRepository b,
                       ExtraIncomeRepository i,
                       ExpenseRepository e,
                       UserRepository u) {
        this.bankRepo   = b;
        this.incomeRepo = i;
        this.expenseRepo= e;
        this.userRepo   = u;
    }

    /**
     * Obtem o ID do usuário que ficou no SecurityContext (por causa do seu JWTAuthenticationFilter),
     * converte para UUID e busca o User correspondente. Se não achar, lança ResourceNotFoundException.
     */
    private User getCurrentUser() {
        String principalName = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            UUID userId = UUID.fromString(principalName); // Converte o principal (que é o ID do usuário em String) para UUID
            return userRepo.findById(userId) // Busca o usuário pelo ID
                    .orElseThrow(() -> {
                        log.warn("Usuário autenticado não encontrado no banco de dados com ID: {}", userId);
                        return new ResourceNotFoundException("Usuário autenticado não encontrado com ID: " + userId);
                    });
        } catch (IllegalArgumentException e) {
            log.warn("Principal no SecurityContext não é um UUID válido: {}. Detalhes do erro: {}", principalName, e.getMessage());
            // Lançar ResourceNotFoundException pode ser mais apropriado ou uma exceção específica de autenticação.
            // Isso indica um problema com o token ou com a configuração do filtro de segurança.
            throw new ResourceNotFoundException("Principal de autenticação inválido ou malformado: " + principalName);
        }
    }

    @Transactional
    public BankDto create(BankCreateDto dto) {
        User currentUser = getCurrentUser();
        Bank b = new Bank();
        b.setName(dto.getName());
        b.setDescription(dto.getDescription());
        b.setUser(currentUser);

        if (dto.getInitialBalance() != null) {
            b.setBalance(dto.getInitialBalance());
        } else {
            b.setBalance(BigDecimal.ZERO);
        }

        bankRepo.save(b);
        log.info("Banco '{}' criado para o usuário {}", b.getName(), currentUser.getId());
        return toDto(b);
    }

    @Transactional(readOnly = true)
    public List<BankDto> listAll() {
        User currentUser = getCurrentUser();
        log.debug("Listando todos os bancos para o usuário {}", currentUser.getId());
        return bankRepo.findAllByUserId(currentUser.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BankDto update(UUID id, BankUpdateDto dto) {
        User currentUser = getCurrentUser();
        Bank b = bankRepo.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + id + " para o usuário atual.")
                );

        boolean needsUpdate = false;
        if (dto.getName() != null && !dto.getName().equals(b.getName())) {
            b.setName(dto.getName());
            needsUpdate = true;
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(b.getDescription())) {
            b.setDescription(dto.getDescription());
            needsUpdate = true;
        }
        if (dto.getBalance() != null && b.getBalance().compareTo(dto.getBalance()) != 0) {
            b.setBalance(dto.getBalance());
            needsUpdate = true;
        }

        if (needsUpdate) {
            bankRepo.save(b);
            log.info("Banco '{}' (ID: {}) atualizado para o usuário {}",
                    b.getName(), b.getId(), currentUser.getId());
        }
        return toDto(b);
    }

    @Transactional
    public void deleteAll(UUID id) {
        User currentUser = getCurrentUser();
        Bank b = bankRepo.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + id + " para o usuário atual ao tentar deletar.")
                );
        bankRepo.deleteById(b.getId());
        log.info("Banco '{}' (ID: {}) deletado para o usuário {}",
                b.getName(), b.getId(), currentUser.getId());
    }

    @Transactional
    public void clearIncomes(UUID bankId) {
        User currentUser = getCurrentUser();
        Bank bank = bankRepo.findByIdAndUserId(bankId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + bankId +
                                " ou não pertence ao usuário " + currentUser.getId())
                );

        // Soma todas as rendas extras daquele banco para este usuário
        BigDecimal totalIncomes = incomeRepo.sumIncomeByBank(bankId, currentUser);
        if (totalIncomes == null) {
            totalIncomes = BigDecimal.ZERO;
        }

        // Deleta todas as rendas extras daquele banco para o usuário
        incomeRepo.deleteAllByUserAndBankId(currentUser, bankId);

        bank.setBalance(bank.getBalance().subtract(totalIncomes));
        bankRepo.save(bank);
        log.info("Rendas do banco '{}' (ID: {}) limpas e saldo atualizado para o usuário {}",
                bank.getName(), bankId, currentUser.getId());
    }

    @Transactional
    public void clearExpenses(UUID bankId) {
        User currentUser = getCurrentUser();
        Bank bank = bankRepo.findByIdAndUserId(bankId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + bankId +
                                " ou não pertence ao usuário " + currentUser.getId())
                );

        BigDecimal totalExpensesCleared = expenseRepo.sumExpenseByBank(bankId, currentUser);
        if (totalExpensesCleared == null) {
            totalExpensesCleared = BigDecimal.ZERO;
        }

        expenseRepo.deleteAllByUserAndBankId(currentUser, bankId);

        bank.setBalance(bank.getBalance().add(totalExpensesCleared));
        bankRepo.save(bank);
        log.info("Despesas do banco '{}' (ID: {}) limpas e saldo atualizado para o usuário {}",
                bank.getName(), bankId, currentUser.getId());
    }

    @Transactional(readOnly = true)
    public BankDto getBankById(UUID bankId) {
        User currentUser = getCurrentUser();
        Bank b = bankRepo.findByIdAndUserId(bankId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + bankId +
                                " ou não pertence ao usuário " + currentUser.getId())
                );
        return toDto(b);
    }

    @Transactional
    public BankDto addMoneyToBank(UUID bankId, AmountDto dto) {
        User currentUser = getCurrentUser();
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(ERROR_ADD_MONEY_AMOUNT_INVALID);
        }

        Bank b = bankRepo.findByIdAndUserId(bankId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + bankId +
                                " ou não pertence ao usuário " + currentUser.getId())
                );

        b.setBalance(b.getBalance().add(dto.getAmount()));
        bankRepo.save(b);
        log.info("Adicionado {} ao saldo do banco '{}' (ID: {}) para o usuário {}",
                dto.getAmount(), b.getName(), bankId, currentUser.getId());
        return toDto(b);
    }

    @Transactional
    public List<BankDto> addMoneyToAllBanks(AmountDto dto) {
        User currentUser = getCurrentUser();
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(ERROR_ADD_MONEY_ALL_AMOUNT_INVALID);
        }

        List<Bank> userBanks = bankRepo.findAllByUserId(currentUser.getId());
        if (userBanks.isEmpty()) {
            log.info("Nenhum banco encontrado para adicionar dinheiro para o usuário {}.", currentUser.getId());
            return Collections.emptyList();
        }

        for (Bank b : userBanks) {
            b.setBalance(b.getBalance().add(dto.getAmount()));
        }
        List<Bank> savedBanks = bankRepo.saveAll(userBanks);
        log.info("Adicionado {} ao saldo de {} bancos para o usuário {}",
                dto.getAmount(), savedBanks.size(), currentUser.getId());
        return savedBanks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BankDto removeMoneyFromBank(UUID bankId, AmountDto dto) {
        User currentUser = getCurrentUser();
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(ERROR_REMOVE_MONEY_AMOUNT_INVALID);
        }

        Bank b = bankRepo.findByIdAndUserId(bankId, currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Banco não encontrado com ID: " + bankId +
                                " ou não pertence ao usuário " + currentUser.getId())
                );

        if (b.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    ERROR_INSUFFICIENT_FUNDS_GENERIC +
                            " Saldo atual no banco '" + b.getName() + "': " + b.getBalance() +
                            ". Tentativa de remover: " + dto.getAmount()
            );
        }

        b.setBalance(b.getBalance().subtract(dto.getAmount()));
        bankRepo.save(b);
        log.info("Removido {} do saldo do banco '{}' (ID: {}) para o usuário {}",
                dto.getAmount(), b.getName(), bankId, currentUser.getId());
        return toDto(b);
    }

    @Transactional
    public List<BankDto> removeMoneyFromAllBanks(AmountDto dto) {
        User currentUser = getCurrentUser();
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(ERROR_REMOVE_MONEY_ALL_AMOUNT_INVALID);
        }

        List<Bank> userBanks = bankRepo.findAllByUserId(currentUser.getId());
        if (userBanks.isEmpty()) {
            log.info("Nenhum banco encontrado para remover dinheiro para o usuário {}.", currentUser.getId());
            return Collections.emptyList();
        }

        for (Bank b : userBanks) {
            if (b.getBalance().compareTo(dto.getAmount()) < 0) {
                throw new InsufficientBalanceException(
                        "Falha ao remover dinheiro de todos os bancos: O banco '" +
                                b.getName() + "' (ID: " + b.getId() + ") possui saldo insuficiente (" +
                                b.getBalance() + ") para a remoção de " + dto.getAmount() + "."
                );
            }
            b.setBalance(b.getBalance().subtract(dto.getAmount()));
        }

        List<Bank> savedBanks = bankRepo.saveAll(userBanks);
        log.info("Removido {} do saldo de {} bancos para o usuário {}",
                dto.getAmount(), savedBanks.size(), currentUser.getId());
        return savedBanks.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BankTransferResponseDto transferBetweenBanks(BankTransferDto transferDto) {
        User currentUser = getCurrentUser();
        UUID currentUserId = currentUser.getId();

        if (transferDto.getAmount() == null || transferDto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException(ERROR_TRANSFER_AMOUNT_INVALID);
        }

        if (transferDto.getSourceBankId().equals(transferDto.getDestinationBankId())) {
            throw new InvalidOperationException(ERROR_TRANSFER_SAME_BANK);
        }

        Bank sourceBank = bankRepo.findByIdAndUserId(transferDto.getSourceBankId(), currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Banco de origem não encontrado com ID: " + transferDto.getSourceBankId() +
                                        " ou não pertence ao usuário."
                        )
                );

        Bank destinationBank = bankRepo.findByIdAndUserId(transferDto.getDestinationBankId(), currentUserId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Banco de destino não encontrado com ID: " + transferDto.getDestinationBankId() +
                                        " ou não pertence ao usuário."
                        )
                );

        BigDecimal sourceBalanceBefore = sourceBank.getBalance();
        BigDecimal destinationBalanceBefore = destinationBank.getBalance();

        if (sourceBalanceBefore.compareTo(transferDto.getAmount()) < 0) {
            throw new InsufficientBalanceException(
                    ERROR_TRANSFER_INSUFFICIENT_FUNDS +
                            " Saldo atual no banco de origem '" + sourceBank.getName() + "': " + sourceBalanceBefore +
                            ". Tentativa de transferir: " + transferDto.getAmount()
            );
        }

        sourceBank.setBalance(sourceBalanceBefore.subtract(transferDto.getAmount()));
        destinationBank.setBalance(destinationBalanceBefore.add(transferDto.getAmount()));

        Bank updatedSourceBank = bankRepo.save(sourceBank);
        Bank updatedDestinationBank = bankRepo.save(destinationBank);

        log.info("Transferência de {} do banco '{}' ({}) para o banco '{}' ({}) realizada com sucesso para o usuário {}",
                transferDto.getAmount(),
                sourceBank.getName(), sourceBank.getId(),
                destinationBank.getName(), destinationBank.getId(),
                currentUserId
        );

        BankTransferLegDto sourceLegDto = new BankTransferLegDto(
                updatedSourceBank.getId(),
                updatedSourceBank.getName(),
                sourceBalanceBefore,
                updatedSourceBank.getBalance()
        );

        BankTransferLegDto destinationLegDto = new BankTransferLegDto(
                updatedDestinationBank.getId(),
                updatedDestinationBank.getName(),
                destinationBalanceBefore,
                updatedDestinationBank.getBalance()
        );

        return new BankTransferResponseDto(
                "Transferência realizada com sucesso!",
                transferDto.getAmount(),
                sourceLegDto,
                destinationLegDto,
                LocalDateTime.now()
        );
    }

    @Transactional
    public List<BankDto> updateAllBanks(List<BankBulkUpdateItemDto> dtoList) {
        User currentUser = getCurrentUser();
        List<Bank> updatedBanksList = new ArrayList<>();

        for (BankBulkUpdateItemDto dto : dtoList) {
            Bank b = bankRepo.findByIdAndUserId(dto.getId(), currentUser.getId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Banco com ID " + dto.getId() + " não encontrado ou não pertence ao usuário."
                            )
                    );

            boolean needsUpdate = false;
            if (dto.getName() != null && !dto.getName().equals(b.getName())) {
                b.setName(dto.getName());
                needsUpdate = true;
            }
            if (dto.getDescription() != null && !dto.getDescription().equals(b.getDescription())) {
                b.setDescription(dto.getDescription());
                needsUpdate = true;
            }
            if (dto.getBalance() != null && b.getBalance().compareTo(dto.getBalance()) != 0) {
                b.setBalance(dto.getBalance());
                needsUpdate = true;
            }

            if (needsUpdate) {
                updatedBanksList.add(bankRepo.save(b));
            } else {
                updatedBanksList.add(b); // Adiciona o banco mesmo sem alteração para manter a lista completa
            }
        }

        log.info("Atualização em massa de {} bancos processada para o usuário {}",
                dtoList.size(), currentUser.getId()
        );
        return updatedBanksList.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllUserBanks() {
        User currentUser = getCurrentUser();
        List<Bank> userBanks = bankRepo.findAllByUserId(currentUser.getId());
        if (!userBanks.isEmpty()) {
            bankRepo.deleteAll(userBanks); // Se deleteAllByUserId não estiver disponível ou preferir assim
            log.info("Todos os {} bancos do usuário {} foram deletados.",
                    userBanks.size(), currentUser.getId()
            );
        } else {
            log.info("Nenhum banco encontrado para deletar para o usuário {}.", currentUser.getId());
        }
    }

    @Transactional(readOnly = true)
    public BankMetricsDto getBankMetrics() {
        User currentUser = getCurrentUser();
        List<Bank> userBanks = bankRepo.findAllByUserId(currentUser.getId());

        if (userBanks.isEmpty()) {
            log.debug("Nenhum banco encontrado para calcular métricas para o usuário {}", currentUser.getId());
            return new BankMetricsDto(
                    BigDecimal.ZERO,    // totalBalanceAllBanks
                    null,               // bankWithHighestBalance
                    null,               // bankWithLowestBalance
                    BigDecimal.ZERO,    // averageBalancePerBank
                    null,               // highestExpenseDto
                    null,               // highestIncomeDto
                    0L,                 // totalExtraIncomesCount
                    0L,                 // totalExpensesCount
                    null,               // mostIncomesDto
                    null,               // mostExpensesDto
                    BigDecimal.ZERO,    // avgIncomeValue
                    BigDecimal.ZERO,    // avgExpenseValue
                    BigDecimal.ZERO,    // avgIncomeCount
                    BigDecimal.ZERO     // avgExpenseCount
            );
        }

        BigDecimal totalBalanceAllBanks = userBanks.stream()
                .map(Bank::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Bank bankWithHighestBalance = userBanks.stream()
                .max(Comparator.comparing(Bank::getBalance))
                .orElse(null);

        Bank bankWithLowestBalance = userBanks.stream()
                .min(Comparator.comparing(Bank::getBalance))
                .orElse(null);

        BigDecimal averageBalancePerBank = totalBalanceAllBanks.divide(
                BigDecimal.valueOf(userBanks.size()), 2, RoundingMode.HALF_UP
        );

        List<ExtraIncome> allIncomes = new ArrayList<>();
        List<Expense>    allExpenses = new ArrayList<>();

        for (Bank bank : userBanks) {
            if (bank.getIncomes() != null) { // Adicionar verificação de nulo para evitar NPE
                allIncomes.addAll(bank.getIncomes());
            }
            if (bank.getExpenses() != null) { // Adicionar verificação de nulo para evitar NPE
                allExpenses.addAll(bank.getExpenses());
            }
        }

        ExtraIncome highestIncomeEntity = allIncomes.stream()
                .filter(Objects::nonNull) // Garantir que a renda não seja nula
                .filter(income -> income.getAmount() != null) // Garantir que o valor da renda não seja nulo
                .max(Comparator.comparing(ExtraIncome::getAmount))
                .orElse(null);

        Expense highestExpenseEntity = allExpenses.stream()
                .filter(Objects::nonNull) // Garantir que a despesa não seja nula
                .filter(e -> e.getValue() != null) // Garantir que o valor da despesa não seja nulo
                .max(Comparator.comparing(Expense::getValue))
                .orElse(null);

        BankTransactionDetailsDto highestIncomeDto  = null;
        if (highestIncomeEntity != null && highestIncomeEntity.getBank() != null) {
            highestIncomeDto = new BankTransactionDetailsDto(
                    highestIncomeEntity.getBank().getId(),
                    highestIncomeEntity.getBank().getName(),
                    highestIncomeEntity.getAmount(),
                    highestIncomeEntity.getName()
            );
        }

        BankTransactionDetailsDto highestExpenseDto = null;
        if (highestExpenseEntity != null && highestExpenseEntity.getBank() != null) {
            highestExpenseDto = new BankTransactionDetailsDto(
                    highestExpenseEntity.getBank().getId(),
                    highestExpenseEntity.getBank().getName(),
                    highestExpenseEntity.getValue(),
                    highestExpenseEntity.getDescription()
            );
        }

        long totalExtraIncomesCount = allIncomes.size();
        long totalExpensesCount     = allExpenses.size();

        BankActivityCountDetailsDto mostIncomesDto = null;
        if (!userBanks.isEmpty()) { // Já verificado acima, mas redundância não prejudica aqui
            Bank bankWithMostIncomes = userBanks.stream()
                    .filter(Objects::nonNull) // Garantir que o banco não seja nulo
                    .max(Comparator.comparing(b -> (b.getIncomes() != null ? b.getIncomes().size() : 0)))
                    .orElse(null);
            if (bankWithMostIncomes != null) {
                mostIncomesDto = new BankActivityCountDetailsDto(
                        bankWithMostIncomes.getId(),
                        bankWithMostIncomes.getName(),
                        bankWithMostIncomes.getIncomes() != null ? bankWithMostIncomes.getIncomes().size() : 0
                );
            }
        }

        BankActivityCountDetailsDto mostExpensesDto = null;
        if (!userBanks.isEmpty()) { // Já verificado acima
            Bank bankWithMostExpenses = userBanks.stream()
                    .filter(Objects::nonNull) // Garantir que o banco não seja nulo
                    .max(Comparator.comparing(b -> (b.getExpenses() != null ? b.getExpenses().size() : 0)))
                    .orElse(null);
            if (bankWithMostExpenses != null) {
                mostExpensesDto = new BankActivityCountDetailsDto(
                        bankWithMostExpenses.getId(),
                        bankWithMostExpenses.getName(),
                        bankWithMostExpenses.getExpenses() != null ? bankWithMostExpenses.getExpenses().size() : 0
                );
            }
        }

        BigDecimal totalIncomeValue = allIncomes.stream()
                .filter(income -> income != null && income.getAmount() != null)
                .map(ExtraIncome::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenseValue = allExpenses.stream()
                .filter(expense -> expense != null && expense.getValue() != null)
                .map(Expense::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // As médias devem ser calculadas apenas se houver bancos, para evitar DivisionByZero
        BigDecimal avgIncomeValue  = BigDecimal.ZERO;
        BigDecimal avgExpenseValue = BigDecimal.ZERO;
        BigDecimal avgIncomeCount  = BigDecimal.ZERO;
        BigDecimal avgExpenseCount = BigDecimal.ZERO;

        if (!userBanks.isEmpty()) { // Re-check to ensure no division by zero
            avgIncomeValue = totalIncomeValue.divide(BigDecimal.valueOf(userBanks.size()), 2, RoundingMode.HALF_UP);
            avgExpenseValue = totalExpenseValue.divide(BigDecimal.valueOf(userBanks.size()), 2, RoundingMode.HALF_UP);
            avgIncomeCount = BigDecimal.valueOf(totalExtraIncomesCount)
                    .divide(BigDecimal.valueOf(userBanks.size()), 2, RoundingMode.HALF_UP);
            avgExpenseCount = BigDecimal.valueOf(totalExpensesCount)
                    .divide(BigDecimal.valueOf(userBanks.size()), 2, RoundingMode.HALF_UP);
        }

        log.debug("Métricas calculadas para o usuário {}", currentUser.getId());
        return new BankMetricsDto(
                totalBalanceAllBanks,
                bankWithHighestBalance != null
                        ? new BankBalanceDetailsDto(
                        bankWithHighestBalance.getId(),
                        bankWithHighestBalance.getName(),
                        bankWithHighestBalance.getBalance()
                )
                        : null,
                bankWithLowestBalance != null
                        ? new BankBalanceDetailsDto(
                        bankWithLowestBalance.getId(),
                        bankWithLowestBalance.getName(),
                        bankWithLowestBalance.getBalance()
                )
                        : null,
                averageBalancePerBank, // Esta já estava correta, pois userBanks.size() é > 0 neste ponto
                highestExpenseDto,
                highestIncomeDto,
                totalExtraIncomesCount,
                totalExpensesCount,
                mostIncomesDto,
                mostExpensesDto,
                avgIncomeValue,
                avgExpenseValue,
                avgIncomeCount,
                avgExpenseCount
        );
    }

    /**
     * Converte uma entidade Bank para BankDto, incluindo soma de rendas e despesas.
     */
    private BankDto toDto(Bank b) {
        User currentUser = getCurrentUser(); // getCurrentUser() já foi corrigido

        BigDecimal totalIn = incomeRepo.sumIncomeByBank(b.getId(), currentUser);
        if (totalIn == null) {
            totalIn = BigDecimal.ZERO;
        }

        BigDecimal totalOut = expenseRepo.sumExpenseByBank(b.getId(), currentUser);
        if (totalOut == null) {
            totalOut = BigDecimal.ZERO;
        }

        return new BankDto(
                b.getId(),
                b.getName(),
                b.getDescription(),
                totalIn,
                totalOut,
                b.getBalance(),
                b.getCreatedAt(),
                b.getUpdatedAt()
        );
    }
}