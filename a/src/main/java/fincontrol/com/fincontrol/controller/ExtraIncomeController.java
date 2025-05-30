// src/main/java/fincontrol/com/fincontrol/controller/ExtraIncomeController.java
package fincontrol.com.fincontrol.controller;

import fincontrol.com.fincontrol.dto.ExtraIncomeDto;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.ExtraIncome;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import fincontrol.com.fincontrol.service.ExtraIncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Tag(name = "Renda Extra", description = "Gerenciamento de rendas extras do usuário")
@RestController
@RequestMapping("/api/extra-income")
public class ExtraIncomeController {

    private final ExtraIncomeService service;
    private final UserRepository userRepo;

    public ExtraIncomeController(ExtraIncomeService service,
                                 UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    @Operation(summary = "Cria nova renda extra para o usuário autenticado")
    @PostMapping
    public ResponseEntity<ExtraIncome> create(@Valid @RequestBody ExtraIncomeDto dto,
                                              Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + principal.getName()));
        ExtraIncome saved = service.add(user.getId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Operation(summary = "Lista todas as rendas extras do usuário autenticado")
    @GetMapping
    public List<ExtraIncome> list(Principal principal) {
        User user = userRepo.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email " + principal.getName()));
        return service.listByUser(user.getId());
    }
}
