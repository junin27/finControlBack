// ExtraIncomeService.java
package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtraIncomeService {

    @Autowired
    private ExtraIncomeRepository extraIncomeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BankRepository bankRepository; // ← Injeção do BankRepository

    /**
     * Cria uma nova renda extra para o usuário autenticado.
     */
    public ExtraIncomeDto createExtraIncome(ExtraIncomeCreateDto dto) {
        User user = getAuthenticatedUser();

        Bank bank = bankRepository.findById(dto.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + dto.getBankId()
                ));

        ExtraIncome ei = new ExtraIncome();
        ei.setName(dto.getName().trim());
        ei.setDescription(dto.getDescription());
        ei.setAmount(dto.getAmount());
        ei.setDate(dto.getDate());
        ei.setCategoryId(dto.getCategoryId());
        ei.setBank(bank);
        ei.setUser(user);
        ei.setCreatedAt(Instant.now());
        ei.setUpdatedAt(Instant.now());

        ExtraIncome saved = extraIncomeRepository.save(ei);
        return ExtraIncomeDto.fromEntity(saved);
    }


    /**
     * Lista todas as rendas extras de um usuário por banco.
     */
    public List<ExtraIncomeDto> listByBank(UUID bankId) {
        User user = getAuthenticatedUser();

        // Verifica se o banco existe e pertence ao usuário
        bankRepository.findById(bankId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + bankId
                ));

        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bankId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("O banco de id (%s) não está cadastrada ao usuário", bankId)
            );
        }
        return list.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     * Lista todas as rendas extras de um usuário por categoria.
     */
    public List<ExtraIncomeDto> listByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();

        // Se você tiver repositório de Category, cheque existência aqui:
        // categoryRepository.findById(categoryId)
        //        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + categoryId));

        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("A categoria de id (%s) não está cadastrada ao usuário", categoryId)
            );
        }
        return list.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca uma renda extra por id e usuário.
     */
    public ExtraIncomeDto getById(UUID id) {
        User user = getAuthenticatedUser();
        ExtraIncome ei = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                ));
        return ExtraIncomeDto.fromEntity(ei);
    }


    /**
     * Atualiza uma renda extra específica (fields parciais).
     */
    public ExtraIncomeDto updateExtraIncome(UUID id, ExtraIncomeUpdateDto dto) {
        User user = getAuthenticatedUser();
        ExtraIncome existing = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                ));

        if (dto.getName() != null) {
            String nameTrim = dto.getName().trim();
            if (nameTrim.isEmpty()) {
                throw new IllegalArgumentException("O campo name é obrigatório, pois não é possível fazer uma entrada de dinheiro sem nome");
            }
            existing.setName(nameTrim);
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim().isEmpty()
                    ? "Campo não Informado pelo Usuário"
                    : dto.getDescription());
        }
        if (dto.getAmount() != null) {
            existing.setAmount(dto.getAmount());
        }
        if (dto.getDate() != null) {
            existing.setDate(dto.getDate());
        }
        if (dto.getCategoryId() != null) {
            existing.setCategoryId(dto.getCategoryId());
        }
        if (dto.getBankId() != null) {
            Bank bank = bankRepository.findById(dto.getBankId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Banco não encontrado com ID: " + dto.getBankId()
                    ));
            existing.setBank(bank);
        }

        existing.setUpdatedAt(Instant.now());
        ExtraIncome saved = extraIncomeRepository.save(existing);
        return ExtraIncomeDto.fromEntity(saved);
    }

    /**
     * Atualiza em lote todas as rendas extras de um usuário.
     */
    @Transactional
    public List<ExtraIncomeDto> batchUpdate(List<ExtraIncomeBatchUpdateDto> dtos) {
        User user = getAuthenticatedUser();
        long totalUser = extraIncomeRepository.countByUser(user);
        if (totalUser == 0) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada");
        }

        Map<UUID, ExtraIncomeBatchUpdateDto> mapaDto = new HashMap<>();
        for (ExtraIncomeBatchUpdateDto bDto : dtos) {
            if (bDto.getId() == null) {
                throw new IllegalArgumentException("Id da renda extra é obrigatório em cada objeto de atualização");
            }
            mapaDto.put(bDto.getId(), bDto);
        }

        List<ExtraIncome> incomes = extraIncomeRepository.findAllById(
                new ArrayList<>(mapaDto.keySet())
        );

        for (UUID id : mapaDto.keySet()) {
            boolean pertence = incomes.stream()
                    .anyMatch(e -> e.getId().equals(id) && e.getUser().equals(user));
            if (!pertence) {
                throw new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                );
            }
        }

        for (ExtraIncome income : incomes) {
            ExtraIncomeBatchUpdateDto bDto = mapaDto.get(income.getId());
            if (bDto.getName() != null && bDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("O campo name é obrigatório, pois não é possível fazer uma entrada de dinheiro sem nome");
            }
            if (bDto.getCategoryId() != null && bDto.getCategoryId().equals(null)) {
                throw new IllegalArgumentException("O campo category é obrigatório, pois não é possível fazer uma entrada de dinheiro sem categoria");
            }
            if (bDto.getAmount() != null && bDto.getAmount().equals(null)) {
                throw new IllegalArgumentException("O campo amount é obrigatório, pois não é possível fazer uma entrada de dinheiro sem valor");
            }
            if (bDto.getBankId() != null && bDto.getBankId().equals(null)) {
                throw new IllegalArgumentException("O campo bank é obrigatório, pois não é possível fazer uma entrada de dinheiro sem relação a um banco");
            }
            if (bDto.getDate() != null && bDto.getDate().equals(null)) {
                throw new IllegalArgumentException("O campo date é obrigatório, pois não é possível fazer uma entrada de dinheiro sem uma data");
            }
        }

        for (ExtraIncome income : incomes) {
            ExtraIncomeBatchUpdateDto bDto = mapaDto.get(income.getId());
            if (bDto.getName() != null) {
                income.setName(bDto.getName().trim());
            }
            if (bDto.getDescription() != null) {
                income.setDescription(bDto.getDescription().trim().isEmpty()
                        ? "Campo não Informado pelo Usuário"
                        : bDto.getDescription());
            }
            if (bDto.getAmount() != null) {
                income.setAmount(bDto.getAmount());
            }
            if (bDto.getDate() != null) {
                income.setDate(bDto.getDate());
            }
            if (bDto.getCategoryId() != null) {
                income.setCategoryId(bDto.getCategoryId());
            }
            if (bDto.getBankId() != null) {
                Bank bank = bankRepository.findById(bDto.getBankId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Banco não encontrado com ID: " + bDto.getBankId()
                        ));
                income.setBank(bank);
            }
            income.setUpdatedAt(Instant.now());
        }

        List<ExtraIncome> savedList = extraIncomeRepository.saveAll(incomes);
        return savedList.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deleta uma renda extra específica.
     */
    public void deleteById(UUID id) {
        User user = getAuthenticatedUser();
        ExtraIncome existing = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                ));
        extraIncomeRepository.delete(existing);
    }

    /**
     * Deleta todas as rendas extras do usuário.
     */
    public void deleteAll() {
        User user = getAuthenticatedUser();
        long totalUser = extraIncomeRepository.countByUser(user);
        if (totalUser == 0) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada");
        }
        extraIncomeRepository.deleteAllByUser(user);
    }

    /**
     * Deleta todas as rendas extras de um usuário por banco.
     */
    public void deleteAllByBank(UUID bankId) {
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bankId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("O banco de id (%s) não está cadastrada ao usuário", bankId)
            );
        }
        extraIncomeRepository.deleteAllByUserAndBankId(user, bankId);
    }

    /**
     * Deleta todas as rendas extras de um usuário por categoria.
     */
    public void deleteAllByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format("A categoria de id (%s) não está cadastrada ao usuário", categoryId)
            );
        }
        extraIncomeRepository.deleteAllByUserAndCategoryId(user, categoryId);
    }

    /**
     * Soma valor a uma renda extra específica.
     */
    public ExtraIncomeAmountOperationDto addToOne(UUID id, BigDecimal value) {
        User user = getAuthenticatedUser();
        ExtraIncome income = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                ));
        BigDecimal previous = income.getAmount();
        BigDecimal newAmt = previous.add(value);
        income.setAmount(newAmt);
        income.setUpdatedAt(Instant.now());
        extraIncomeRepository.save(income);

        return ExtraIncomeAmountOperationDto.builder()
                .id(id)
                .previousAmount(previous)
                .addedValue(value)
                .newAmount(newAmt)
                .build();
    }

    /**
     * Soma valor a todas as rendas extras do usuário.
     */
    public List<ExtraIncomeAmountOperationDto> addToAll(BigDecimal value) {
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUser(user);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada");
        }
        List<ExtraIncomeAmountOperationDto> result = new ArrayList<>();
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.add(value);
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            extraIncomeRepository.save(income);

            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .addedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        return result;
    }

    /**
     * Subtrai valor de uma renda extra específica.
     */
    public ExtraIncomeAmountOperationDto subtractFromOne(UUID id, BigDecimal value) {
        User user = getAuthenticatedUser();
        ExtraIncome income = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", id)
                ));
        BigDecimal previous = income.getAmount();
        BigDecimal newAmt = previous.subtract(value);
        income.setAmount(newAmt);
        income.setUpdatedAt(Instant.now());
        extraIncomeRepository.save(income);

        return ExtraIncomeAmountOperationDto.builder()
                .id(id)
                .previousAmount(previous)
                .subtractedValue(value)
                .newAmount(newAmt)
                .build();
    }


    /**
     * Subtrai valor de todas as rendas extras do usuário.
     */
    public List<ExtraIncomeAmountOperationDto> subtractFromAll(BigDecimal value) {
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUser(user);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada");
        }
        List<ExtraIncomeAmountOperationDto> result = new ArrayList<>();
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.subtract(value);
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            extraIncomeRepository.save(income);

            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .subtractedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        return result;
    }

    /**
     * Transfere valor entre duas rendas extras.
     */
    @Transactional
    public ExtraIncomeTransferDto transfer(UUID fromId, UUID toId, BigDecimal value) {
        User user = getAuthenticatedUser();
        ExtraIncome from = extraIncomeRepository.findByIdAndUser(fromId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", fromId)
                ));
        ExtraIncome to = extraIncomeRepository.findByIdAndUser(toId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada ao usuário", toId)
                ));

        BigDecimal fromPrevious = from.getAmount();
        BigDecimal toPrevious = to.getAmount();

        from.setAmount(fromPrevious.subtract(value));
        from.setUpdatedAt(Instant.now());
        to.setAmount(toPrevious.add(value));
        to.setUpdatedAt(Instant.now());

        extraIncomeRepository.save(from);
        extraIncomeRepository.save(to);

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return ExtraIncomeTransferDto.builder()
                .fromId(fromId)
                .toId(toId)
                .fromPreviousAmount(fromPrevious)
                .toPreviousAmount(toPrevious)
                .transferredValue(value)
                .fromNewAmount(fromPrevious.subtract(value))
                .toNewAmount(toPrevious.add(value))
                .timestamp(timestamp)
                .build();
    }


    /**
     * Obtém o usuário autenticado a partir do SecurityContext. Ajuste conforme sua implementação de JWT.
     */
    public User getAuthenticatedUser() {
        // Exemplo com Spring Security:
        // Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // String email = auth.getName();
        // return userRepository.findByEmail(email)
        //         .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        throw new UnsupportedOperationException("Método de obtenção de usuário autenticado não implementado");
    }
}
