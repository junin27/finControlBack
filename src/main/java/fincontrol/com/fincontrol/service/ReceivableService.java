package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExtraIncomeSimpleDto;
import fincontrol.com.fincontrol.dto.ReceivableCreateDto;
import fincontrol.com.fincontrol.dto.ReceivableResponseDto;
import fincontrol.com.fincontrol.dto.ReceivableUpdateDto;
import fincontrol.com.fincontrol.dto.UserSimpleDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.Receivable;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.model.enums.ReceivableStatusEnum;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.ReceivableRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.lang.Long;

@Service
@RequiredArgsConstructor
public class ReceivableService {

    private static final Logger logger = LoggerFactory.getLogger(ReceivableService.class);

    private final ReceivableRepository receivableRepository;
    private final UserRepository userRepository;
    private final ExtraIncomeRepository extraIncomeRepository;
    private final BankRepository bankRepository;


    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private ExtraIncome findExtraIncomeByIdAndUser(Long extraIncomeId, User user) {
        return extraIncomeRepository.findByIdAndUserId(extraIncomeId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("ExtraIncome with ID " + extraIncomeId + " not found or does not belong to the user."));
    }

    @Transactional
    public ReceivableResponseDto createReceivable(ReceivableCreateDto dto, UUID userId) {
        User user = findUserById(userId);

        if (dto.getDueDate().isBefore(LocalDate.now())) {
            throw new InvalidOperationException("Due date cannot be in the past.");
        }

        ExtraIncome extraIncome = findExtraIncomeByIdAndUser(dto.getExtraIncomeId(), user);

        Receivable receivable = new Receivable();
        receivable.setUser(user);
        receivable.setExtraIncome(extraIncome);
        receivable.setReceiptMethod(dto.getReceiptMethod());
        receivable.setDueDate(dto.getDueDate());
        receivable.setAutomaticBankReceipt(dto.getAutomaticBankReceipt());
        receivable.setStatus(ReceivableStatusEnum.PENDING);

        Receivable savedReceivable = receivableRepository.save(receivable);
        return toResponseDto(savedReceivable);
    }

    public Page<ReceivableResponseDto> getAllReceivablesFiltered(UUID userId, ReceivableStatusEnum status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return receivableRepository.findByUserAndFilters(userId, status, startDate, endDate, pageable)
                .map(this::toResponseDto);
    }

    public ReceivableResponseDto getReceivableByIdAndUser(UUID receivableId, UUID userId) {
        Receivable receivable = receivableRepository.findByIdAndUserId(receivableId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable with ID " + receivableId + " not found or does not belong to the user."));
        return toResponseDto(receivable);
    }

    @Transactional
    public ReceivableResponseDto updateReceivable(UUID receivableId, ReceivableUpdateDto dto, UUID userId) {
        Receivable receivable = receivableRepository.findByIdAndUserId(receivableId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable with ID " + receivableId + " not found or does not belong to the user."));

        if (receivable.getStatus() == ReceivableStatusEnum.RECEIVED || receivable.getStatus() == ReceivableStatusEnum.RECEIVED_LATE) {
            throw new InvalidOperationException("Cannot update a receivable that has already been marked as received. Status: " + receivable.getStatus());
        }

        boolean updated = false;

        if (dto.getReceiptMethod() != null && dto.getReceiptMethod() != receivable.getReceiptMethod()) {
            receivable.setReceiptMethod(dto.getReceiptMethod());
            updated = true;
        }

        if (dto.getDueDate() != null && !dto.getDueDate().equals(receivable.getDueDate())) {
            if (dto.getDueDate().isBefore(LocalDate.now())) {
                throw new InvalidOperationException("New due date cannot be in the past.");
            }
            receivable.setDueDate(dto.getDueDate());
            if (receivable.getStatus() == ReceivableStatusEnum.OVERDUE && dto.getDueDate().isAfter(LocalDate.now().minusDays(1))) {
                receivable.setStatus(ReceivableStatusEnum.PENDING);
            }
            updated = true;
        }

        if (dto.getAutomaticBankReceipt() != null && !dto.getAutomaticBankReceipt().equals(receivable.getAutomaticBankReceipt())) {
            receivable.setAutomaticBankReceipt(dto.getAutomaticBankReceipt());
            updated = true;
        }

        if (updated) {
            Receivable savedReceivable = receivableRepository.save(receivable);
            return toResponseDto(savedReceivable);
        }
        return toResponseDto(receivable);
    }


    @Transactional
    public ReceivableResponseDto markAsReceivedManually(UUID receivableId, UUID userId) {
        Receivable receivable = receivableRepository.findByIdAndUserId(receivableId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable with ID " + receivableId + " not found or does not belong to the user."));

        // Verifica se a conta já foi recebida para evitar processamento duplicado
        if (receivable.getStatus() == ReceivableStatusEnum.RECEIVED || receivable.getStatus() == ReceivableStatusEnum.RECEIVED_LATE) {
            throw new InvalidOperationException("Receivable with ID " + receivableId + " has already been marked as received.");
        }

        // Define o novo status
        if (receivable.getStatus() == ReceivableStatusEnum.PENDING) {
            receivable.setStatus(ReceivableStatusEnum.RECEIVED);
        } else if (receivable.getStatus() == ReceivableStatusEnum.OVERDUE) {
            receivable.setStatus(ReceivableStatusEnum.RECEIVED_LATE);
        } else {
            // Não deveria chegar aqui se a checagem acima for feita, mas é uma salvaguarda
            throw new InvalidOperationException("Cannot mark receivable with status " + receivable.getStatus() + " as received through this action.");
        }

        // *** NOVA LÓGICA: Adicionar ao saldo do banco IMEDIATAMENTE se aplicável ***
        if (Boolean.TRUE.equals(receivable.getAutomaticBankReceipt())) { // Checa se é true e não null
            ExtraIncome extraIncome = receivable.getExtraIncome(); // Assumindo que está EAGER ou a transação cobre
            if (extraIncome.getBank() != null) {
                Bank bank = bankRepository.findById(extraIncome.getBank().getId())
                        .orElseThrow(() -> {
                            logger.error("Bank with ID {} associated with ExtraIncome ID {} not found for manual receipt processing of Receivable ID {}.",
                                    extraIncome.getBank().getId(), extraIncome.getId(), receivable.getId());
                            // Mesmo que o banco não seja encontrado, a conta ainda será marcada como recebida,
                            // mas o saldo bancário não será atualizado. Pode ser um erro de configuração.
                            // Ou você pode optar por lançar uma exceção e reverter a transação se a atualização do banco for crítica.
                            // Por ora, apenas logamos e continuamos a marcar a conta como recebida.
                            return new ResourceNotFoundException("Bank for automatic receipt processing not found, but receivable status will be updated. Receivable ID: " + receivable.getId());
                        });

                // Adiciona o valor da renda extra ao saldo do banco
                try {
                    bank.setBalance(bank.getBalance().add(extraIncome.getAmount()));
                    bankRepository.save(bank);
                    logger.info("Receivable ID {} (ExtraIncome ID {}) marked as received manually. Amount {} added to Bank ID {}.",
                            receivable.getId(), extraIncome.getId(), extraIncome.getAmount(), bank.getId());
                } catch (Exception e) {
                    logger.error("Failed to update bank balance for receivable ID {} during manual marking. Error: {}", receivable.getId(), e.getMessage());
                    // Considere se deve lançar uma exceção aqui para reverter a mudança de status da conta a receber
                    // ou se a conta deve ser marcada como recebida mesmo com falha na atualização do banco.
                    // Por ora, a conta será marcada como recebida.
                }
            } else {
                logger.warn("Receivable ID {} (ExtraIncome ID {}) marked as received manually and was set for automatic bank receipt, but the associated ExtraIncome has no linked bank. Bank balance not updated.",
                        receivable.getId(), extraIncome.getId());
            }
        }

        Receivable savedReceivable = receivableRepository.save(receivable);
        return toResponseDto(savedReceivable);
    }

    @Transactional
    public void deleteReceivable(UUID receivableId, UUID userId) {
        Receivable receivable = receivableRepository.findByIdAndUserId(receivableId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Receivable with ID " + receivableId + " not found or does not belong to the user."));

        if (receivable.getStatus() == ReceivableStatusEnum.RECEIVED || receivable.getStatus() == ReceivableStatusEnum.RECEIVED_LATE) {
            logger.warn("Deleting receivable ID {} with status {}. Note: If this was an automatic bank receipt, the bank transaction is not automatically reversed by this deletion.",
                    receivable.getId(), receivable.getStatus());
        }

        receivableRepository.delete(receivable);
        logger.info("Receivable ID {} deleted successfully.", receivable.getId());
    }


    @Transactional
    public void processOverdueReceivablesJob() {
        logger.info("Starting job to mark overdue receivables...");
        List<Receivable> pendingOverdue = receivableRepository.findAllByStatusAndDueDateBefore(
                ReceivableStatusEnum.PENDING,
                LocalDate.now()
        );
        for (Receivable receivable : pendingOverdue) {
            receivable.setStatus(ReceivableStatusEnum.OVERDUE);
            receivableRepository.save(receivable);
            logger.info("Receivable ID {} (ExtraIncome ID {}) marked as OVERDUE.", receivable.getId(), receivable.getExtraIncome().getId());
        }
        if (!pendingOverdue.isEmpty()) {
            logger.info("Overdue receivables job finished. {} receivables updated.", pendingOverdue.size());
        } else {
            logger.info("Overdue receivables job finished. No receivables to update.");
        }
    }

    @Transactional
    public void processAutomaticBankReceiptsJob() {
        logger.info("Starting job for automatic bank receipts...");
        LocalDate today = LocalDate.now();
        List<Receivable> receivablesForAutoReceipt = receivableRepository
                .findAllByStatusAndAutomaticBankReceiptIsTrueAndDueDateIsLessThanEqual(
                        ReceivableStatusEnum.PENDING, // Processa APENAS PENDENTES para evitar duplicidade com a ação manual
                        today
                );

        int successfullyReceivedCount = 0;
        for (Receivable receivable : receivablesForAutoReceipt) {
            // A verificação se já é RECEIVED não é estritamente necessária aqui
            // porque o query já filtra por PENDING. Mas como uma dupla checagem, não faria mal.
            // if (receivable.getStatus() == ReceivableStatusEnum.RECEIVED || receivable.getStatus() == ReceivableStatusEnum.RECEIVED_LATE) {
            //     logger.info("Skipping receivable ID {} as it's already processed.", receivable.getId());
            //     continue;
            // }

            ExtraIncome extraIncome = receivable.getExtraIncome();

            if (extraIncome.getBank() != null) {
                Bank bank = bankRepository.findById(extraIncome.getBank().getId())
                        .orElseThrow(() -> {
                            logger.error("Bank with ID {} associated with ExtraIncome ID {} not found for automatic receipt of Receivable ID {}.",
                                    extraIncome.getBank().getId(), extraIncome.getId(), receivable.getId());
                            return new ResourceNotFoundException("Bank for automatic receipt not found. Receivable ID: " + receivable.getId());
                        });

                bank.setBalance(bank.getBalance().add(extraIncome.getAmount()));
                bankRepository.save(bank);

                receivable.setStatus(ReceivableStatusEnum.RECEIVED);
                receivableRepository.save(receivable);
                successfullyReceivedCount++;
                logger.info("Receivable ID {} (ExtraIncome ID {}) automatically received. Amount {} added to Bank ID {}.",
                        receivable.getId(), extraIncome.getId(), extraIncome.getAmount(), bank.getId());
            } else {
                logger.warn("Receivable ID {} (ExtraIncome ID {}) is set for automatic bank receipt, but the associated ExtraIncome has no linked bank.",
                        receivable.getId(), extraIncome.getId());
            }
        }
        if (successfullyReceivedCount > 0) {
            logger.info("Automatic bank receipts job finished. {} receivables processed successfully.", successfullyReceivedCount);
        } else {
            logger.info("Automatic bank receipts job finished. No receivables eligible for automatic processing were found or processed.");
        }
    }

    private ReceivableResponseDto toResponseDto(Receivable receivable) {
        ReceivableResponseDto dto = new ReceivableResponseDto();
        dto.setId(receivable.getId());

        ExtraIncome extraIncome = receivable.getExtraIncome();
        ExtraIncomeSimpleDto extraIncomeDto = new ExtraIncomeSimpleDto();
        extraIncomeDto.setId(extraIncome.getId());
        extraIncomeDto.setDescription(extraIncome.getDescription());
        extraIncomeDto.setValue(extraIncome.getAmount());
        if (extraIncome.getBank() != null) {
            extraIncomeDto.setBankId(extraIncome.getBank().getId());
            extraIncomeDto.setBankName(extraIncome.getBank().getName());
        }
        dto.setExtraIncome(extraIncomeDto);

        dto.setReceiptMethod(receivable.getReceiptMethod());
        dto.setDueDate(receivable.getDueDate());
        dto.setAutomaticBankReceipt(receivable.getAutomaticBankReceipt());
        dto.setStatus(receivable.getStatus());

        User user = receivable.getUser();
        UserSimpleDto userDto = new UserSimpleDto(user.getId(), user.getName());
        dto.setUser(userDto);

        dto.setCreatedAt(receivable.getCreatedAt());
        dto.setUpdatedAt(receivable.getUpdatedAt());
        return dto;
    }
}