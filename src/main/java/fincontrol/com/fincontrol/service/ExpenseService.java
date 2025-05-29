package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.ExpenseCreateDto;
import fincontrol.com.fincontrol.dto.ExpenseDto;
import fincontrol.com.fincontrol.dto.ExpenseUpdateDto;
import fincontrol.com.fincontrol.model.Bank;
import fincontrol.com.fincontrol.model.Category;
import fincontrol.com.fincontrol.model.Expense;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.BankRepository;
import fincontrol.com.fincontrol.repository.CategoryRepository;
import fincontrol.com.fincontrol.repository.ExpenseRepository;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Import para StringUtils

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final CategoryRepository categoryRepo;
    private final BankRepository bankRepo;
    private final UserRepository userRepo;

    public ExpenseService(ExpenseRepository e,
                          CategoryRepository c,
                          BankRepository b,
                          UserRepository u) {
        this.expenseRepo  = e;
        this.categoryRepo = c;
        this.bankRepo     = b;
        this.userRepo     = u;
    }

    @Transactional
    public ExpenseDto create(ExpenseCreateDto dto, UUID userId) {
        Category cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId()));

        Expense ex = new Expense();
        ex.setName(dto.getName());
        ex.setDescription(dto.getDescription()); // Será tratado pelo @PrePersist se for null/vazio
        ex.setValue(dto.getValue());
        ex.setCategory(cat);
        ex.setExpenseDate(dto.getExpenseDate()); // Define a data da despesa

        if (dto.getBankId() != null) {
            Bank bank = bankRepo.findById(dto.getBankId())
                    .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId()));
            ex.setBank(bank);
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para o ID: " + userId));
        ex.setUser(user);

        Expense salvo = expenseRepo.save(ex);
        return toDto(salvo);
    }

    public List<ExpenseDto> listAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return expenseRepo.findAllByUserId(currentUser.getId()).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseDto update(UUID id, ExpenseUpdateDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Expense e = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada para o ID: " + id));

        if (!e.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta despesa não pertence ao usuário autenticado.");
        }

        boolean needsUpdate = false;

        if (dto.getName() != null && StringUtils.hasText(dto.getName())) {
            if(!dto.getName().equals(e.getName())){
                e.setName(dto.getName());
                needsUpdate = true;
            }
        }
        // Para description, se o DTO trouxer null, não alteramos. Se trouxer uma string (mesmo vazia), atualizamos.
        // O valor padrão "Campo não informado..." só se aplica na criação via @PrePersist.
        if (dto.getDescription() != null) {
            if(!dto.getDescription().equals(e.getDescription())) {
                e.setDescription(dto.getDescription());
                needsUpdate = true;
            }
        }
        if (dto.getValue() != null) {
            if(e.getValue().compareTo(dto.getValue()) != 0) {
                e.setValue(dto.getValue());
                needsUpdate = true;
            }
        }
        // Atualiza expenseDate. Se dto.getExpenseDate() for null, a data será removida (setada para null).
        if (dto.getExpenseDate() != e.getExpenseDate() && // Otimização para evitar setar se for o mesmo
                (dto.getExpenseDate() != null || e.getExpenseDate() != null)) { // Evita se ambos forem null
            e.setExpenseDate(dto.getExpenseDate());
            needsUpdate = true;
        }

        if (dto.getCategoryId()  != null) {
            if (e.getCategory() == null || !dto.getCategoryId().equals(e.getCategory().getId())) {
                Category cat = categoryRepo.findById(dto.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Categoria não encontrada para o ID: " + dto.getCategoryId()));
                e.setCategory(cat);
                needsUpdate = true;
            }
        }

        // Lógica para atualizar ou desassociar o banco
        if (dto.getBankId() != null) { // Se um bankId foi fornecido no DTO
            if (e.getBank() == null || !dto.getBankId().equals(e.getBank().getId())) { // Se o banco atual é diferente ou nulo
                Bank b = bankRepo.findById(dto.getBankId())
                        .orElseThrow(() -> new RuntimeException("Banco não encontrado para o ID: " + dto.getBankId()));
                e.setBank(b);
                needsUpdate = true;
            }
        } else { // Se bankId não foi fornecido no DTO, não significa necessariamente que queremos remover.
            // Para remover, o cliente deveria enviar explicitamente "bankId": null no JSON.
            // No entanto, como UUID bankId no DTO será null se não vier no JSON, precisamos de uma lógica mais explícita
            // se quisermos distinguir "não enviado" de "enviado como null para limpar".
            // Por simplicidade aqui: se dto.getBankId() é null, não alteramos o banco.
            // Se você quiser permitir limpar o banco, o DTO precisaria de um wrapper ou o cliente enviar um valor especial.
            // Ou, se o campo "bankId" estiver presente no JSON com valor null, dto.getBankId() será null.
            // Para permitir desassociar:
            // if (e.getBank() != null && dto.getBankId() == null) { // Apenas se bankId foi intencionalmente setado para null no DTO (precisaria de um DTO mais esperto)
            //     // Para o DTO atual, se dto.getBankId() é null, ele não entra no if anterior.
            //     // Se quisermos que a omissão do bankId no DTO de update *não* desassocie, esta lógica está ok.
            //     // Se quisermos que um `bankId: null` no JSON *desassocie*, precisamos de:
            //     // Verifique se a intenção é realmente limpar. Uma forma é se o DTO pode distinguir "não presente" de "presente e nulo".
            //     // Assumindo que se dto.getBankId() é null, não mudamos a menos que já fosse diferente.
            //     // A lógica atual NÃO LIMPARÁ o banco se bankId for omitido no DTO de update.
            //     // Para permitir limpar, se o campo está no JSON como null:
            //     // (Esta parte requer que você decida como o DTO deve se comportar com campos nulos para "limpeza")
            //     // Se dto.isBankIdPresentAndNull() por exemplo (não padrão)
            // }
        }


        if(needsUpdate) {
            return toDto(expenseRepo.save(e));
        }
        return toDto(e); // Retorna o DTO mesmo se não houve save, para refletir o estado atual
    }

    @Transactional
    public void delete(UUID id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Expense expenseToDelete = expenseRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Despesa não encontrada para o ID: " + id));

        if (!expenseToDelete.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Acesso negado: Esta despesa não pertence ao usuário autenticado.");
        }
        expenseRepo.deleteById(id);
    }

    private ExpenseDto toDto(Expense e) {
        UUID bankId = null;
        String bankDisplayName;

        if (e.getBank() != null) {
            bankId = e.getBank().getId();
            if (StringUtils.hasText(e.getBank().getName())) {
                bankDisplayName = e.getBank().getName();
            } else {
                bankDisplayName = "Banco sem Nome Definido";
            }
        } else {
            bankDisplayName = "Banco não Informado pelo Usuário";
        }

        UUID categoryId = e.getCategory() != null ? e.getCategory().getId() : null;
        String categoryName = e.getCategory() != null ? e.getCategory().getName() : "Categoria não Informada";


        return new ExpenseDto(
                e.getId(),
                e.getName(),
                e.getDescription(), // Já terá o valor padrão da entidade se aplicável na criação
                e.getValue(),
                e.getExpenseDate(), // Adicionado expenseDate
                categoryId,
                categoryName,
                bankId,
                bankDisplayName,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}