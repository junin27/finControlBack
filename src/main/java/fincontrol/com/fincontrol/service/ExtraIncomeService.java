// ExtraIncomeService.java
package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException; // Certifique-se de que esta exceção está definida
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.ExtraIncomeRepository;
import fincontrol.com.fincontrol.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; // <-- IMPORTAÇÃO CORRETA
import org.springframework.security.core.context.SecurityContextHolder; // <-- IMPORTAÇÃO CORRETA
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExtraIncomeService {

    private static final Logger logger = LoggerFactory.getLogger(ExtraIncomeService.class);

    private final ExtraIncomeRepository extraIncomeRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    @Autowired
    public ExtraIncomeService(ExtraIncomeRepository extraIncomeRepository,
                              UserRepository userRepository,
                              BankRepository bankRepository) {
        this.extraIncomeRepository = extraIncomeRepository;
        this.userRepository = userRepository;
        this.bankRepository = bankRepository;
    }

    /**
     * Cria uma nova renda extra para o usuário autenticado.
     */
    @Transactional
    public ExtraIncomeDto createExtraIncome(ExtraIncomeCreateDto dto) {
        User user = getAuthenticatedUser(); // Agora usa o método implementado

        Bank bank = bankRepository.findById(dto.getBankId()) // Você pode querer verificar se o banco pertence ao usuário também: bankRepository.findByIdAndUserId(dto.getBankId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + dto.getBankId()
                ));

        ExtraIncome ei = new ExtraIncome();
        ei.setName(dto.getName().trim());
        ei.setDescription(dto.getDescription());
        ei.setAmount(dto.getAmount());
        ei.setDate(dto.getDate());
        ei.setCategoryId(dto.getCategoryId()); // Supondo que CategoryId seja apenas um UUID e não uma entidade vinculada aqui
        ei.setBank(bank);
        ei.setUser(user);
        ei.setCreatedAt(Instant.now());
        ei.setUpdatedAt(Instant.now());

        // Se o banco deve ter seu saldo atualizado ao adicionar renda:
        // bank.setBalance(bank.getBalance().add(ei.getAmount()));
        // bankRepository.save(bank); // Salvar o banco atualizado

        ExtraIncome saved = extraIncomeRepository.save(ei);
        logger.info("Renda extra ID {} criada para o usuário ID {}", saved.getId(), user.getId());
        return ExtraIncomeDto.fromEntity(saved); // Supondo que ExtraIncomeDto tenha um método estático fromEntity
    }


    /**
     * Lista todas as rendas extras de um usuário por banco.
     */
    public List<ExtraIncomeDto> listByBank(UUID bankId) {
        User user = getAuthenticatedUser();

        // Verifica se o banco existe. Seria bom também verificar se o banco pertence ao usuário.
        Bank bank = bankRepository.findByIdAndUserId(bankId, user.getId()) // Verifica se o banco pertence ao usuário
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + bankId + " para o usuário atual."
                ));

        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bank.getId()); // Usa o ID do banco validado
        // A exceção abaixo pode ser redundante se findAllByUserAndBankId retornar lista vazia e isso for aceitável.
        // if (list.isEmpty()) {
        //     throw new ResourceNotFoundException(
        //             String.format("Nenhuma renda extra encontrada para o banco de id (%s) do usuário", bankId)
        //     );
        // }
        return list.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }


    /**
     * Lista todas as rendas extras de um usuário por categoria.
     */
    public List<ExtraIncomeDto> listByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();

        // Se você tiver uma entidade Category e um CategoryRepository:
        // Category category = categoryRepository.findById(categoryId)
        //        .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + categoryId));
        // E se Category também for associada ao User, valide isso também.

        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId);
        // A exceção abaixo pode ser redundante.
        // if (list.isEmpty()) {
        //     throw new ResourceNotFoundException(
        //             String.format("Nenhuma renda extra encontrada para a categoria de id (%s) do usuário", categoryId)
        //     );
        // }
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
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada para o usuário", id)
                ));
        return ExtraIncomeDto.fromEntity(ei);
    }


    /**
     * Atualiza uma renda extra específica (fields parciais).
     */
    @Transactional
    public ExtraIncomeDto updateExtraIncome(UUID id, ExtraIncomeUpdateDto dto) {
        User user = getAuthenticatedUser();
        ExtraIncome existing = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada para o usuário", id)
                ));

        // BigDecimal oldAmount = existing.getAmount(); // Para ajustar saldo do banco se o valor mudar

        if (dto.getName() != null) {
            String nameTrim = dto.getName().trim();
            if (nameTrim.isEmpty()) {
                throw new IllegalArgumentException("O campo name é obrigatório.");
            }
            existing.setName(nameTrim);
        }
        if (dto.getDescription() != null) {
            existing.setDescription(dto.getDescription().trim().isEmpty()
                    ? "Campo não Informado pelo Usuário" // Considere se isso é desejável ou se deve manter a descrição existente.
                    : dto.getDescription());
        }
        if (dto.getAmount() != null) {
            existing.setAmount(dto.getAmount());
        }
        if (dto.getDate() != null) {
            existing.setDate(dto.getDate());
        }
        if (dto.getCategoryId() != null) {
            // Validar se a categoria existe e pertence ao usuário, se aplicável
            existing.setCategoryId(dto.getCategoryId());
        }
        if (dto.getBankId() != null) {
            Bank bank = bankRepository.findByIdAndUserId(dto.getBankId(), user.getId()) // Garante que o novo banco pertence ao usuário
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Banco não encontrado com ID: " + dto.getBankId() + " para o usuário atual."
                    ));
            // Se a renda está mudando de banco ou o valor mudou, os saldos dos bancos envolvidos podem precisar de ajuste.
            // Esta lógica pode ficar complexa e pode ser melhor tratada por operações específicas de "mover renda" ou "ajustar valor".
            existing.setBank(bank);
        }

        existing.setUpdatedAt(Instant.now());
        ExtraIncome saved = extraIncomeRepository.save(existing);

        // Lógica para ajustar saldo do banco se o valor da renda mudou e o banco é o mesmo
        // if (dto.getAmount() != null && existing.getBank() != null) {
        //     Bank currentBank = existing.getBank();
        //     BigDecimal difference = saved.getAmount().subtract(oldAmount);
        //     currentBank.setBalance(currentBank.getBalance().add(difference));
        //     bankRepository.save(currentBank);
        // }
        logger.info("Renda extra ID {} atualizada para o usuário ID {}", saved.getId(), user.getId());
        return ExtraIncomeDto.fromEntity(saved);
    }

    /**
     * Atualiza em lote todas as rendas extras de um usuário.
     */
    @Transactional
    public List<ExtraIncomeDto> batchUpdate(List<ExtraIncomeBatchUpdateDto> dtos) {
        User user = getAuthenticatedUser();

        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        // Validações básicas e mapeamento
        Map<UUID, ExtraIncomeBatchUpdateDto> mapaDto = new HashMap<>();
        for (ExtraIncomeBatchUpdateDto bDto : dtos) {
            if (bDto.getId() == null) {
                throw new IllegalArgumentException("Id da renda extra é obrigatório em cada objeto de atualização.");
            }
            // Adicione outras validações de DTO aqui se necessário (campos vazios, etc.)
            if (bDto.getName() != null && bDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("O campo name não pode ser vazio para a renda ID: " + bDto.getId());
            }
            mapaDto.put(bDto.getId(), bDto);
        }

        List<ExtraIncome> incomesToUpdate = extraIncomeRepository.findAllByIdInAndUser(new ArrayList<>(mapaDto.keySet()), user);

        if (incomesToUpdate.size() != mapaDto.size()) {
            // Alguns IDs fornecidos não foram encontrados ou não pertencem ao usuário
            Set<UUID> foundIds = incomesToUpdate.stream().map(ExtraIncome::getId).collect(Collectors.toSet());
            List<UUID> missingOrUnownedIds = mapaDto.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException(
                    "Algumas rendas extras não foram encontradas ou não pertencem ao usuário: " + missingOrUnownedIds
            );
        }

        List<ExtraIncome> updatedIncomes = new ArrayList<>();
        for (ExtraIncome income : incomesToUpdate) {
            ExtraIncomeBatchUpdateDto bDto = mapaDto.get(income.getId());
            boolean changed = false;

            if (bDto.getName() != null && !income.getName().equals(bDto.getName().trim())) {
                income.setName(bDto.getName().trim());
                changed = true;
            }
            if (bDto.getDescription() != null && (income.getDescription() == null || !income.getDescription().equals(bDto.getDescription()))) {
                income.setDescription(bDto.getDescription().trim().isEmpty()
                        ? "Campo não Informado pelo Usuário"
                        : bDto.getDescription());
                changed = true;
            }
            if (bDto.getAmount() != null && income.getAmount().compareTo(bDto.getAmount()) != 0) {
                income.setAmount(bDto.getAmount());
                changed = true;
            }
            if (bDto.getDate() != null && !income.getDate().equals(bDto.getDate())) {
                income.setDate(bDto.getDate());
                changed = true;
            }
            if (bDto.getCategoryId() != null && (income.getCategoryId() == null || !income.getCategoryId().equals(bDto.getCategoryId()))) {
                // Validar se a categoria existe
                income.setCategoryId(bDto.getCategoryId());
                changed = true;
            }
            if (bDto.getBankId() != null && (income.getBank() == null || !income.getBank().getId().equals(bDto.getBankId()))) {
                Bank bank = bankRepository.findByIdAndUserId(bDto.getBankId(), user.getId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Banco não encontrado com ID: " + bDto.getBankId() + " para o usuário."
                        ));
                income.setBank(bank);
                changed = true;
            }

            if (changed) {
                income.setUpdatedAt(Instant.now());
                updatedIncomes.add(income);
            }
        }

        if (!updatedIncomes.isEmpty()) {
            extraIncomeRepository.saveAll(updatedIncomes);
            logger.info("{} rendas extras atualizadas em lote para o usuário ID {}", updatedIncomes.size(), user.getId());
        }

        // Retorna todas as rendas que foram consideradas (mesmo as não alteradas)
        // ou apenas as alteradas, dependendo do requisito. Aqui, retornamos o estado atual das processadas.
        return incomesToUpdate.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Deleta uma renda extra específica.
     */
    @Transactional
    public void deleteById(UUID id) {
        User user = getAuthenticatedUser();
        ExtraIncome existing = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada para o usuário", id)
                ));

        // Se o saldo do banco deve ser ajustado ao deletar a renda:
        // Bank bank = existing.getBank();
        // if (bank != null) {
        //     bank.setBalance(bank.getBalance().subtract(existing.getAmount()));
        //     bankRepository.save(bank);
        // }
        extraIncomeRepository.delete(existing);
        logger.info("Renda extra ID {} deletada para o usuário ID {}", id, user.getId());
    }

    /**
     * Deleta todas as rendas extras do usuário.
     */
    @Transactional
    public void deleteAll() {
        User user = getAuthenticatedUser();
        List<ExtraIncome> userIncomes = extraIncomeRepository.findAllByUser(user);
        if (userIncomes.isEmpty()) {
            // Lançar exceção ou apenas logar e retornar, dependendo do comportamento desejado.
            logger.info("Usuário ID {} não possui nenhuma entrada de dinheiro cadastrada para deletar.", user.getId());
            // throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada");
            return;
        }
        // Lógica para ajustar saldos de todos os bancos afetados, se necessário
        // Map<Bank, BigDecimal> bankAdjustments = new HashMap<>();
        // for (ExtraIncome income : userIncomes) {
        //     if (income.getBank() != null) {
        //         bankAdjustments.merge(income.getBank(), income.getAmount(), BigDecimal::add);
        //     }
        // }
        // for (Map.Entry<Bank, BigDecimal> entry : bankAdjustments.entrySet()) {
        //     Bank bank = entry.getKey();
        //     bank.setBalance(bank.getBalance().subtract(entry.getValue()));
        //     bankRepository.save(bank);
        // }
        extraIncomeRepository.deleteAllByUser(user);
        logger.info("Todas as rendas extras do usuário ID {} foram deletadas.", user.getId());
    }

    /**
     * Deleta todas as rendas extras de um usuário por banco.
     */
    @Transactional
    public void deleteAllByBank(UUID bankId) {
        User user = getAuthenticatedUser();
        Bank bank = bankRepository.findByIdAndUserId(bankId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Banco com ID " + bankId + " não encontrado ou não pertence ao usuário."));

        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bank.getId());
        if (list.isEmpty()) {
            logger.info("Nenhuma renda extra encontrada para o banco ID {} do usuário ID {}.", bankId, user.getId());
            // throw new ResourceNotFoundException(
            //         String.format("Nenhuma renda extra encontrada para o banco de id (%s) do usuário", bankId)
            // );
            return;
        }
        // BigDecimal totalAmountFromBank = list.stream().map(ExtraIncome::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        extraIncomeRepository.deleteAllByUserAndBankId(user, bank.getId()); // Este método já deve existir no seu repo
        // bank.setBalance(bank.getBalance().subtract(totalAmountFromBank));
        // bankRepository.save(bank);
        logger.info("Todas as rendas extras do banco ID {} para o usuário ID {} foram deletadas.", bankId, user.getId());
    }

    /**
     * Deleta todas as rendas extras de um usuário por categoria.
     */
    @Transactional
    public void deleteAllByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();
        // Validar se a categoria existe, se necessário
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId);
        if (list.isEmpty()) {
            logger.info("Nenhuma renda extra encontrada para a categoria ID {} do usuário ID {}.", categoryId, user.getId());
            // throw new ResourceNotFoundException(
            //         String.format("Nenhuma renda extra encontrada para a categoria de id (%s) do usuário", categoryId)
            // );
            return;
        }
        // Lógica para ajustar saldos de bancos afetados, se necessário
        extraIncomeRepository.deleteAllByUserAndCategoryId(user, categoryId); // Este método já deve existir no seu repo
        logger.info("Todas as rendas extras da categoria ID {} para o usuário ID {} foram deletadas.", categoryId, user.getId());
    }

    /**
     * Soma valor a uma renda extra específica.
     */
    @Transactional
    public ExtraIncomeAmountOperationDto addToOne(UUID id, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a ser adicionado deve ser maior que zero.");
        }
        User user = getAuthenticatedUser();
        ExtraIncome income = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada para o usuário", id)
                ));
        BigDecimal previous = income.getAmount();
        BigDecimal newAmt = previous.add(value);
        income.setAmount(newAmt);
        income.setUpdatedAt(Instant.now());
        // Se o banco associado deve ter seu saldo atualizado
        // if (income.getBank() != null) {
        //    Bank bank = income.getBank(); // Já está carregado
        //    bank.setBalance(bank.getBalance().add(value));
        //    bankRepository.save(bank);
        // }
        extraIncomeRepository.save(income);
        logger.info("Valor {} adicionado à renda extra ID {} (usuário ID {}). Saldo anterior: {}, novo saldo: {}", value, id, user.getId(), previous, newAmt);

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
    @Transactional
    public List<ExtraIncomeAmountOperationDto> addToAll(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a ser adicionado deve ser maior que zero.");
        }
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUser(user);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada.");
        }
        List<ExtraIncomeAmountOperationDto> result = new ArrayList<>();
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.add(value);
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            // Se o banco associado deve ter seu saldo atualizado
            // if (income.getBank() != null) {
            //    Bank bank = income.getBank();
            //    bank.setBalance(bank.getBalance().add(value));
            //    // Não precisa salvar o banco individualmente aqui se saveAll(incomes) já o fizer via cascade ou se o banco for gerenciado de outra forma.
            //    // Caso contrário, precisaria coletar os bancos e salvá-los.
            // }
            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .addedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        extraIncomeRepository.saveAll(list); // Salva todas as rendas modificadas
        // Aqui você precisaria de uma lógica para atualizar os saldos dos bancos afetados se não for por cascade.
        logger.info("Valor {} adicionado a todas as {} rendas extras do usuário ID {}.", value, list.size(), user.getId());
        return result;
    }

    /**
     * Subtrai valor de uma renda extra específica.
     */
    @Transactional
    public ExtraIncomeAmountOperationDto subtractFromOne(UUID id, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a ser subtraído deve ser maior que zero.");
        }
        User user = getAuthenticatedUser();
        ExtraIncome income = extraIncomeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A entrada de dinheiro de id (%s) não está cadastrada para o usuário", id)
                ));
        BigDecimal previous = income.getAmount();
        BigDecimal newAmt = previous.subtract(value);
        // Adicionar validação se newAmt pode ser negativo, se for uma regra de negócio
        // if (newAmt.compareTo(BigDecimal.ZERO) < 0) {
        //     throw new IllegalArgumentException("O valor da renda não pode ser negativo após a subtração.");
        // }
        income.setAmount(newAmt);
        income.setUpdatedAt(Instant.now());
        // if (income.getBank() != null) {
        //    Bank bank = income.getBank();
        //    bank.setBalance(bank.getBalance().subtract(value));
        //    bankRepository.save(bank);
        // }
        extraIncomeRepository.save(income);
        logger.info("Valor {} subtraído da renda extra ID {} (usuário ID {}). Saldo anterior: {}, novo saldo: {}", value, id, user.getId(), previous, newAmt);

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
    @Transactional
    public List<ExtraIncomeAmountOperationDto> subtractFromAll(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a ser subtraído deve ser maior que zero.");
        }
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUser(user);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("O usuário não possui nenhuma entrada de dinheiro cadastrada.");
        }
        List<ExtraIncomeAmountOperationDto> result = new ArrayList<>();
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.subtract(value);
            // if (newAmt.compareTo(BigDecimal.ZERO) < 0) {
            //     logger.warn("Subtração resultaria em valor negativo para renda ID {}, pulando ou ajustando para zero.", income.getId());
            //     // Decida como lidar: pular, setar para zero, ou permitir negativo.
            //     // newAmt = BigDecimal.ZERO; // Exemplo: ajustar para zero
            // }
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .subtractedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        extraIncomeRepository.saveAll(list);
        // Atualizar saldos dos bancos afetados, se necessário.
        logger.info("Valor {} subtraído de todas as {} rendas extras do usuário ID {}.", value, list.size(), user.getId());
        return result;
    }

    /**
     * Transfere valor entre duas rendas extras.
     */
    @Transactional
    public ExtraIncomeTransferDto transfer(UUID fromId, UUID toId, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero.");
        }
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("A renda de origem e destino não podem ser a mesma.");
        }

        User user = getAuthenticatedUser();
        ExtraIncome fromIncome = extraIncomeRepository.findByIdAndUser(fromId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A renda extra de origem (ID: %s) não está cadastrada para o usuário.", fromId)
                ));
        ExtraIncome toIncome = extraIncomeRepository.findByIdAndUser(toId, user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("A renda extra de destino (ID: %s) não está cadastrada para o usuário.", toId)
                ));

        BigDecimal fromPrevious = fromIncome.getAmount();
        BigDecimal toPrevious = toIncome.getAmount();

        // if (fromPrevious.compareTo(value) < 0) {
        //     throw new IllegalArgumentException("Saldo insuficiente na renda de origem para transferência.");
        // }

        fromIncome.setAmount(fromPrevious.subtract(value));
        fromIncome.setUpdatedAt(Instant.now());
        toIncome.setAmount(toPrevious.add(value));
        toIncome.setUpdatedAt(Instant.now());

        extraIncomeRepository.save(fromIncome);
        extraIncomeRepository.save(toIncome);

        // Note: A transferência entre rendas extras não afeta o saldo dos bancos diretamente,
        // a menos que as rendas estejam em bancos diferentes E você queira modelar isso como uma transferência bancária.
        // A lógica atual apenas move o "valor" entre os registros de ExtraIncome.

        String timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        logger.info("Valor {} transferido da renda ID {} para a renda ID {} (usuário ID {}).", value, fromId, toId, user.getId());

        return ExtraIncomeTransferDto.builder()
                .fromId(fromId)
                .toId(toId)
                .fromPreviousAmount(fromPrevious)
                .toPreviousAmount(toPrevious)
                .transferredValue(value)
                .fromNewAmount(fromIncome.getAmount())
                .toNewAmount(toIncome.getAmount())
                .timestamp(timestamp)
                .build();
    }


    /**
     * Obtém o usuário autenticado a partir do SecurityContext.
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Nenhuma autenticação válida encontrada no SecurityContext.");
            // Lançar uma exceção mais específica de segurança/autenticação seria melhor
            throw new ResourceNotFoundException("Usuário não autenticado. Não é possível determinar o principal.");
        }

        Object principal = authentication.getPrincipal();

        // Verifica se o principal é a String do ID do usuário, como definido pelo JWTAuthenticationFilter
        if (!(principal instanceof String)) {
            logger.error("O principal da autenticação não é uma String como esperado. Principal é do tipo: {}", principal.getClass().getName());
            throw new IllegalStateException("O principal da autenticação não é do tipo esperado (String contendo UserID).");
        }

        String userIdString = (String) principal;
        logger.debug("Tentando obter usuário autenticado com ID (String): {}", userIdString);

        // Se, por algum motivo, o 'anonymousUser' chegou até aqui (o filtro JWT deveria ter prevenido)
        if ("anonymousUser".equals(userIdString)) {
            logger.warn("Tentativa de operação por 'anonymousUser'. Isso não deveria acontecer após o filtro JWT.");
            throw new ResourceNotFoundException("Operação não permitida para usuário anônimo.");
        }

        try {
            UUID userId = UUID.fromString(userIdString);
            return userRepository.findById(userId)
                    .orElseThrow(() -> {
                        logger.warn("Usuário autenticado com ID {} (convertido de String '{}') não encontrado no banco de dados.", userId, userIdString);
                        return new ResourceNotFoundException("Usuário autenticado (ID: " + userIdString + ") não encontrado no banco de dados.");
                    });
        } catch (IllegalArgumentException e) {
            logger.error("Principal da autenticação ('{}') não é um UUID válido.", userIdString, e);
            // Isso também indica um problema, pois o filtro JWT deveria ter fornecido um UUID válido como String.
            throw new ResourceNotFoundException("Identificador de usuário inválido na autenticação: " + userIdString);
        }
    }
}