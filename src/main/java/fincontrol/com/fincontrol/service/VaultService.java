package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.*;
import fincontrol.com.fincontrol.exception.InsufficientBalanceException;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.model.Vault;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.repository.VaultRepository;
// Removido import desnecessário de ExpenseRepository e ExtraIncomeRepository se não usados diretamente aqui
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private final VaultRepository vaultRepository;
    private final UserRepository userRepository;
    private final BankRepository bankRepository;

    public VaultService(VaultRepository vaultRepository,
                        UserRepository userRepository,
                        BankRepository bankRepository) {
        this.vaultRepository = vaultRepository;
        this.userRepository = userRepository;
        this.bankRepository = bankRepository;
    }

    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));
    }

    // Método CORRIGIDO para usar findByIdAndUserId do BankRepository
    private Bank getBankByIdAndUser(UUID bankId, User user) {
        return bankRepository.findByIdAndUserId(bankId, user.getId()) // Corrigido aqui
                .orElseThrow(() -> new ResourceNotFoundException("Você não possui o banco com ID " + bankId + " cadastrado ou ele não pertence a você."));
    }

    @Transactional
    public VaultDto createVault(VaultCreateDto dto, UUID userId) {
        User user = getUserById(userId);

        if (dto.getInitialAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidOperationException("O valor inicial do cofre não pode ser negativo.");
        }

        Vault vault = new Vault();
        vault.setName(dto.getName());
        vault.setDescription(dto.getDescription()); // @PrePersist cuidará do default se nulo/vazio
        vault.setAmount(dto.getInitialAmount());
        vault.setCurrency(dto.getCurrency());
        vault.setUser(user);

        if (dto.getBankId() != null) {
            Bank bank = getBankByIdAndUser(dto.getBankId(), user); // Esta chamada agora está correta
            if (bank.getBalance().compareTo(dto.getInitialAmount()) < 0) {
                throw new InsufficientBalanceException("O seu saldo no banco " + bank.getName() + " (ID: " + bank.getId() + ") é de " + bank.getBalance() + ", que é menor que o valor solicitado de " + dto.getInitialAmount() + ".");
            }
            bank.setBalance(bank.getBalance().subtract(dto.getInitialAmount()));
            bankRepository.save(bank);
            vault.setBank(bank);
        }

        Vault savedVault = vaultRepository.save(vault);
        return toDto(savedVault);
    }

    @Transactional
    public VaultDto updateVault(UUID vaultId, VaultUpdateDto dto, UUID userId) {
        User user = getUserById(userId);
        Vault vault = vaultRepository.findByIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cofre com ID " + vaultId + " não encontrado ou não pertence ao usuário."));

        boolean updated = false;
        if (dto.getName() != null && StringUtils.hasText(dto.getName()) && !dto.getName().equals(vault.getName())) {
            vault.setName(dto.getName());
            updated = true;
        }
        if (dto.getDescription() != null && !dto.getDescription().equals(vault.getDescription())) {
            vault.setDescription(dto.getDescription());
            updated = true;
        }
        if (dto.getCurrency() != null && StringUtils.hasText(dto.getCurrency()) && !dto.getCurrency().equals(vault.getCurrency())) {
            vault.setCurrency(dto.getCurrency());
            updated = true;
        }

        if (updated) {
            vault = vaultRepository.save(vault);
        }
        return toDto(vault);
    }

    @Transactional
    public void deleteVault(UUID vaultId, UUID userId) {
        User user = getUserById(userId);
        Vault vault = vaultRepository.findByIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cofre com ID " + vaultId + " não encontrado ou não pertence ao usuário."));

        if (vault.getBank() != null) {
            Bank bank = getBankByIdAndUser(vault.getBank().getId(), user); // Re-busca o banco para garantir que é do usuário e está atualizado
            bank.setBalance(bank.getBalance().add(vault.getAmount()));
            bankRepository.save(bank);
            vaultRepository.delete(vault);
        } else {
            if (vault.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                throw new InvalidOperationException("Não é possível excluir o cofre (ID: " + vaultId + "), pois ele não está vinculado a nenhum banco e o valor de " + vault.getAmount() + " " + vault.getCurrency() + " será perdido. Para poder excluí-lo, remova o valor total do cofre antes.");
            }
            vaultRepository.delete(vault);
        }
    }

    public List<VaultDto> getAllVaultsByUser(UUID userId) {
        User user = getUserById(userId);
        return vaultRepository.findAllByUserId(userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public VaultDto getVaultByIdAndUser(UUID vaultId, UUID userId) {
        User user = getUserById(userId);
        return vaultRepository.findByIdAndUserId(vaultId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Cofre com ID " + vaultId + " não encontrado ou não pertence ao usuário."));
    }

    public List<VaultDto> getVaultsByBankAndUser(UUID bankId, UUID userId) {
        User user = getUserById(userId);
        Bank bank = getBankByIdAndUser(bankId, user); // Valida se o banco pertence ao usuário
        return vaultRepository.findAllByBankIdAndUserId(bank.getId(), userId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }


    private VaultDto toDto(Vault vault) {
        String bankName = null;
        if (vault.getBank() != null) {
            // Para evitar N+1 select, se o nome do banco não for essencial aqui e já estiver carregado, use-o.
            // Se não, uma nova busca seria necessária se vault.getBank() for apenas uma referência LAZY não inicializada com nome.
            // Assumindo que o objeto Bank dentro de Vault pode ter seu nome acessado:
            if (StringUtils.hasText(vault.getBank().getName())) {
                bankName = vault.getBank().getName();
            } else {
                bankName = "Banco sem nome"; // Caso o banco exista mas não tenha nome
            }
        }
        // Não definimos "Banco não Informado pelo Usuário" aqui, pois o bankId no DTO será null.
        // A lógica de exibição de "Banco não Informado" seria no frontend ou num campo String específico no DTO, se necessário.

        return new VaultDto(
                vault.getId(),
                vault.getName(),
                vault.getDescription(),
                vault.getAmount(),
                vault.getCurrency(),
                vault.getBank() != null ? vault.getBank().getId() : null,
                bankName, // Nome do banco ou null
                vault.getUser().getId(),
                vault.getCreatedAt(),
                vault.getUpdatedAt()
        );
    }


    // Método auxiliar para buscar o cofre ou lançar exceção
    private Vault getVaultByIdAndUserOrThrow(UUID vaultId, UUID userId) {
        return vaultRepository.findByIdAndUserId(vaultId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cofre com ID " + vaultId + " não encontrado ou não pertence ao usuário."));
    }

    @Transactional
    public VaultTransactionResponseDto withdrawFromVault(UUID vaultId, VaultTransactionRequestDto dto, UUID userId) {
        User user = getUserById(userId); // Valida o usuário
        Vault vault = getVaultByIdAndUserOrThrow(vaultId, userId);

        BigDecimal amountToWithdraw = dto.getAmount();
        if (amountToWithdraw.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("O valor do saque deve ser positivo.");
        }

        BigDecimal balanceBefore = vault.getAmount();

        if (balanceBefore.compareTo(amountToWithdraw) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente no cofre (ID: " + vaultId + "). Saldo atual: " + balanceBefore + ", Saque solicitado: " + amountToWithdraw);
        }

        vault.setAmount(balanceBefore.subtract(amountToWithdraw));
        Vault updatedVault = vaultRepository.save(vault);
        BigDecimal balanceAfter = updatedVault.getAmount();

        // Se o cofre estiver vinculado a um banco, o valor sacado do cofre retorna ao banco.
        if (vault.getBank() != null) {
            Bank bank = getBankByIdAndUser(vault.getBank().getId(), user); // Re-busca para garantir consistência
            bank.setBalance(bank.getBalance().add(amountToWithdraw));
            bankRepository.save(bank);
        }

        return new VaultTransactionResponseDto(
                vault.getId(),
                vault.getName(),
                "WITHDRAWAL",
                amountToWithdraw,
                balanceBefore,
                balanceAfter,
                vault.getCurrency(),
                vault.getBank() != null ? vault.getBank().getId() : null,
                vault.getBank() != null ? vault.getBank().getName() : null,
                LocalDateTime.now()
        );
    }

    @Transactional
    public VaultTransactionResponseDto depositToVault(UUID vaultId, VaultTransactionRequestDto dto, UUID userId) {
        User user = getUserById(userId); // Valida o usuário
        Vault vault = getVaultByIdAndUserOrThrow(vaultId, userId);

        BigDecimal amountToDeposit = dto.getAmount();
        if (amountToDeposit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("O valor do depósito deve ser positivo.");
        }

        BigDecimal balanceBefore = vault.getAmount();

        // Se o cofre estiver vinculado a um banco, o valor depositado no cofre sai do banco.
        if (vault.getBank() != null) {
            Bank bank = getBankByIdAndUser(vault.getBank().getId(), user); // Re-busca para garantir consistência
            if (bank.getBalance().compareTo(amountToDeposit) < 0) {
                throw new InsufficientBalanceException("Saldo insuficiente no banco (ID: " + bank.getId() + ") para cobrir o depósito no cofre. Saldo do banco: " + bank.getBalance() + ", Depósito solicitado: " + amountToDeposit);
            }
            bank.setBalance(bank.getBalance().subtract(amountToDeposit));
            bankRepository.save(bank);
        }
        // Se não estiver vinculado a um banco, o dinheiro "surge" no cofre (dinheiro em espécie, por exemplo)

        vault.setAmount(balanceBefore.add(amountToDeposit));
        Vault updatedVault = vaultRepository.save(vault);
        BigDecimal balanceAfter = updatedVault.getAmount();

        return new VaultTransactionResponseDto(
                vault.getId(),
                vault.getName(),
                "DEPOSIT",
                amountToDeposit,
                balanceBefore,
                balanceAfter,
                vault.getCurrency(),
                vault.getBank() != null ? vault.getBank().getId() : null,
                vault.getBank() != null ? vault.getBank().getName() : null,
                LocalDateTime.now()
        );
    }

}