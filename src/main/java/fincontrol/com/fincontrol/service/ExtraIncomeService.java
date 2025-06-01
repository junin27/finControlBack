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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    // Adicionar CategoryRepository se Category for uma entidade e precisar ser validada
    // private final CategoryRepository categoryRepository;

    @Autowired
    public ExtraIncomeService(ExtraIncomeRepository extraIncomeRepository,
                              UserRepository userRepository,
                              BankRepository bankRepository /*, CategoryRepository categoryRepository */) {
        this.extraIncomeRepository = extraIncomeRepository;
        this.userRepository = userRepository;
        this.bankRepository = bankRepository;
        // this.categoryRepository = categoryRepository;
    }

    /**
     * Obtém o usuário autenticado a partir do SecurityContext.
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Nenhuma autenticação válida encontrada no SecurityContext.");
            throw new ResourceNotFoundException("Usuário não autenticado. Não é possível determinar o principal.");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof String)) {
            logger.error("O principal da autenticação não é uma String como esperado. Principal é do tipo: {}", principal.getClass().getName());
            throw new IllegalStateException("O principal da autenticação não é do tipo esperado (String contendo UserID).");
        }
        String userIdString = (String) principal;
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
            throw new ResourceNotFoundException("Identificador de usuário inválido na autenticação: " + userIdString);
        }
    }

    /**
     * Cria uma nova renda extra para o usuário autenticado.
     */
    @Transactional
    public ExtraIncomeDto createExtraIncome(ExtraIncomeCreateDto dto) {
        User user = getAuthenticatedUser();

        // Mantido como no original: busca global do banco.
        // Considerar validar se o banco pertence ao usuário se for uma regra de negócio.
        // Ex: bankRepository.findByIdAndUser(dto.getBankId(), user)
        Bank bank = bankRepository.findById(dto.getBankId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + dto.getBankId()
                ));

        // Se CategoryId se refere a uma entidade Category, valide-a também.
        // Ex: Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow(...);
        // E se a categoria também deve pertencer ao usuário: categoryRepository.findByIdAndUser(dto.getCategoryId(), user)

        ExtraIncome ei = new ExtraIncome();
        ei.setName(dto.getName().trim());
        ei.setDescription(dto.getDescription()); // Mantido como no original
        ei.setAmount(dto.getAmount());
        ei.setDate(dto.getDate());
        ei.setCategoryId(dto.getCategoryId());
        ei.setBank(bank);
        ei.setUser(user);
        ei.setCreatedAt(Instant.now());
        ei.setUpdatedAt(Instant.now());

        ExtraIncome saved = extraIncomeRepository.save(ei);
        logger.info("Renda extra ID {} criada para o usuário ID {}", saved.getId(), user.getId());
        return ExtraIncomeDto.fromEntity(saved);
    }

    /**
     * Lista todas as rendas extras do usuário autenticado. (NOVO MÉTODO ADICIONADO)
     */
    public List<ExtraIncomeDto> findAllByCurrentUser() {
        User user = getAuthenticatedUser();
        List<ExtraIncome> list = extraIncomeRepository.findAllByUser(user); // Assumindo que findAllByUser(User user) existe
        if (list.isEmpty()) {
            logger.info("Nenhuma renda extra encontrada para o usuário ID {}", user.getId());
            return Collections.emptyList();
        }
        return list.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas as rendas extras de um usuário por banco.
     */
    public List<ExtraIncomeDto> listByBank(UUID bankId) {
        User user = getAuthenticatedUser();
        // Validação de propriedade do banco mantida, pois parece ser uma boa prática.
        Bank bank = bankRepository.findByIdAndUserId(bankId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Banco não encontrado com ID: " + bankId + " para o usuário atual."
                ));
        // Chamada ao repositório restaurada para usar bankId diretamente, como no original.
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bank.getId());
        return list.stream()
                .map(ExtraIncomeDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Lista todas as rendas extras de um usuário por categoria.
     */
    public List<ExtraIncomeDto> listByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();
        // Se Category for uma entidade, valide-a aqui.
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId); // Mantido como no original
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

        boolean changed = false; // Adicionado para evitar save desnecessário

        if (dto.getName() != null) {
            String nameTrim = dto.getName().trim();
            if (nameTrim.isEmpty()) {
                throw new IllegalArgumentException("O campo name é obrigatório."); // Mantido como no original
            }
            if(!existing.getName().equals(nameTrim)){
                existing.setName(nameTrim);
                changed = true;
            }
        }
        if (dto.getDescription() != null) {
            // Lógica de descrição restaurada para o original
            String newDescription = dto.getDescription().trim().isEmpty()
                    ? "Campo não Informado pelo Usuário"
                    : dto.getDescription();
            if (existing.getDescription() == null || !existing.getDescription().equals(newDescription)) {
                existing.setDescription(newDescription);
                changed = true;
            }
        }
        if (dto.getAmount() != null && existing.getAmount().compareTo(dto.getAmount()) != 0) {
            existing.setAmount(dto.getAmount());
            changed = true;
        }
        if (dto.getDate() != null && !existing.getDate().equals(dto.getDate())) {
            existing.setDate(dto.getDate());
            changed = true;
        }
        if (dto.getCategoryId() != null && (existing.getCategoryId() == null || !existing.getCategoryId().equals(dto.getCategoryId()))) {
            // Validar se a categoria existe e pertence ao usuário, se aplicável
            existing.setCategoryId(dto.getCategoryId());
            changed = true;
        }
        if (dto.getBankId() != null && (existing.getBank() == null || !existing.getBank().getId().equals(dto.getBankId()))) {
            Bank bank = bankRepository.findByIdAndUserId(dto.getBankId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Banco não encontrado com ID: " + dto.getBankId() + " para o usuário atual."
                    ));
            existing.setBank(bank);
            changed = true;
        }

        if (changed) {
            existing.setUpdatedAt(Instant.now());
            ExtraIncome saved = extraIncomeRepository.save(existing);
            logger.info("Renda extra ID {} atualizada para o usuário ID {}", saved.getId(), user.getId());
            return ExtraIncomeDto.fromEntity(saved);
        }
        // Se nada mudou, retorna o DTO da entidade existente sem salvar novamente.
        logger.info("Nenhuma alteração na renda extra ID {} do usuário ID {}", id, user.getId());
        return ExtraIncomeDto.fromEntity(existing);
    }

    /**
     * Atualiza em lote todas as rendas extras de um usuário.
     * Lógica restaurada para a versão original, que é mais eficiente.
     */
    @Transactional
    public List<ExtraIncomeDto> batchUpdate(List<ExtraIncomeBatchUpdateDto> dtos) {
        User user = getAuthenticatedUser();

        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, ExtraIncomeBatchUpdateDto> mapaDto = new HashMap<>();
        for (ExtraIncomeBatchUpdateDto bDto : dtos) {
            if (bDto.getId() == null) {
                throw new IllegalArgumentException("Id da renda extra é obrigatório em cada objeto de atualização.");
            }
            if (bDto.getName() != null && bDto.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("O campo name não pode ser vazio para a renda ID: " + bDto.getId());
            }
            mapaDto.put(bDto.getId(), bDto);
        }

        // Busca todas as entidades de uma vez (mais eficiente)
        List<ExtraIncome> incomesToUpdate = extraIncomeRepository.findAllByIdInAndUser(new ArrayList<>(mapaDto.keySet()), user);

        if (incomesToUpdate.size() != mapaDto.size()) {
            Set<UUID> foundIds = incomesToUpdate.stream().map(ExtraIncome::getId).collect(Collectors.toSet());
            List<UUID> missingOrUnownedIds = mapaDto.keySet().stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ResourceNotFoundException(
                    "Algumas rendas extras não foram encontradas ou não pertencem ao usuário: " + missingOrUnownedIds
            );
        }

        List<ExtraIncome> actuallyUpdatedIncomes = new ArrayList<>();
        for (ExtraIncome income : incomesToUpdate) {
            ExtraIncomeBatchUpdateDto bDto = mapaDto.get(income.getId());
            boolean changed = false;

            if (bDto.getName() != null && !income.getName().equals(bDto.getName().trim())) {
                income.setName(bDto.getName().trim());
                changed = true;
            }
            if (bDto.getDescription() != null) {
                String newDescription = bDto.getDescription().trim().isEmpty()
                        ? "Campo não Informado pelo Usuário"
                        : bDto.getDescription();
                if (income.getDescription() == null || !income.getDescription().equals(newDescription)) {
                    income.setDescription(newDescription);
                    changed = true;
                }
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
                actuallyUpdatedIncomes.add(income); // Adiciona apenas se realmente mudou
            }
        }

        if (!actuallyUpdatedIncomes.isEmpty()) {
            extraIncomeRepository.saveAll(actuallyUpdatedIncomes);
            logger.info("{} rendas extras atualizadas em lote para o usuário ID {}", actuallyUpdatedIncomes.size(), user.getId());
        }

        // Retorna o estado atual de todas as rendas processadas (alteradas ou não)
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
            logger.info("Usuário ID {} não possui nenhuma entrada de dinheiro cadastrada para deletar.", user.getId());
            return;
        }
        extraIncomeRepository.deleteAllByUser(user); // Assumindo que este método existe
        logger.info("Todas as rendas extras do usuário ID {} foram deletadas.", user.getId());
    }

    /**
     * Deleta todas as rendas extras de um usuário por banco.
     * Restaurado para usar o método direto do repositório, como no original.
     */
    @Transactional
    public void deleteAllByBank(UUID bankId) {
        User user = getAuthenticatedUser();
        Bank bank = bankRepository.findByIdAndUserId(bankId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Banco com ID " + bankId + " não encontrado ou não pertence ao usuário."));

        // Verifica se há rendas para este banco antes de tentar deletar, para log ou evitar chamada desnecessária
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndBankId(user, bank.getId());
        if (list.isEmpty()) {
            logger.info("Nenhuma renda extra encontrada para o banco ID {} do usuário ID {}. Nenhuma deleção necessária.", bankId, user.getId());
            return;
        }
        extraIncomeRepository.deleteAllByUserAndBankId(user, bank.getId()); // Assumindo que este método existe
        logger.info("Todas as rendas extras do banco ID {} para o usuário ID {} foram deletadas.", bankId, user.getId());
    }

    /**
     * Deleta todas as rendas extras de um usuário por categoria.
     * Restaurado para usar o método direto do repositório, como no original.
     */
    @Transactional
    public void deleteAllByCategory(UUID categoryId) {
        User user = getAuthenticatedUser();
        // Validar se a categoria existe, se necessário
        List<ExtraIncome> list = extraIncomeRepository.findAllByUserAndCategoryId(user, categoryId);
        if (list.isEmpty()) {
            logger.info("Nenhuma renda extra encontrada para a categoria ID {} do usuário ID {}. Nenhuma deleção necessária.", categoryId, user.getId());
            return;
        }
        extraIncomeRepository.deleteAllByUserAndCategoryId(user, categoryId); // Assumindo que este método existe
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
        List<ExtraIncome> incomesToSave = new ArrayList<>(); // Para salvar em lote
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.add(value);
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            incomesToSave.add(income);
            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .addedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        extraIncomeRepository.saveAll(incomesToSave);
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
        income.setAmount(newAmt);
        income.setUpdatedAt(Instant.now());
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
        List<ExtraIncome> incomesToSave = new ArrayList<>(); // Para salvar em lote
        for (ExtraIncome income : list) {
            BigDecimal previous = income.getAmount();
            BigDecimal newAmt = previous.subtract(value);
            income.setAmount(newAmt);
            income.setUpdatedAt(Instant.now());
            incomesToSave.add(income);
            result.add(
                    ExtraIncomeAmountOperationDto.builder()
                            .id(income.getId())
                            .previousAmount(previous)
                            .subtractedValue(value)
                            .newAmount(newAmt)
                            .build()
            );
        }
        extraIncomeRepository.saveAll(incomesToSave);
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

        fromIncome.setAmount(fromPrevious.subtract(value));
        fromIncome.setUpdatedAt(Instant.now());
        toIncome.setAmount(toPrevious.add(value));
        toIncome.setUpdatedAt(Instant.now());

        extraIncomeRepository.save(fromIncome); // Pode ser saveAll(Arrays.asList(fromIncome, toIncome))
        extraIncomeRepository.save(toIncome);

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
}
