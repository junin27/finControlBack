package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserRegisterDto;
import fincontrol.com.fincontrol.dto.UserUpdateDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ú']+(\\s[a-zA-ZÀ-ú']+)+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$");


    public UserService(UserRepository userRepository,
                       PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Timed(value = "user.register.time", description = "Tempo para registrar um novo usuário")
    @Transactional
    public User register(UserRegisterDto dto) {
        if (userRepository.findByEmail(dto.getEmail().toLowerCase().trim()).isPresent()) {
            throw new InvalidOperationException("O e-mail '" + dto.getEmail() + "' já está cadastrado.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.");
        }
        User u = new User();
        u.setName(dto.getName().trim());
        u.setEmail(dto.getEmail().toLowerCase().trim());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        u.setSalary(dto.getSalary());
        User savedUser = userRepository.save(u);
        log.info("Usuário registrado com ID: {} e email: {}", savedUser.getId(), savedUser.getEmail());
        return savedUser;
    }

    @Timed(value = "user.authenticate.time", description = "Tempo para autenticar um usuário")
    public User authenticate(LoginDto dto) {
        User u = userRepository.findByEmail(dto.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("O email '" + dto.getEmail() + "' não está cadastrado no sistema."));

        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())) {
            throw new BadCredentialsException("A sua senha está incorreta.");
        }
        log.info("Usuário autenticado com email: {}", u.getEmail());
        return u;
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        return userRepository.findByEmail(email.toLowerCase().trim());
    }

    @Timed(value = "user.findAll.time", description = "Tempo para listar usuários (serviço)")
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Timed(value = "user.findById.time", description = "Tempo para buscar usuário por ID (serviço)")
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Timed(value = "user.updateService.time", description = "Tempo para atualização de usuário (serviço)")
    @Transactional
    public User update(UUID id, UserUpdateDto dto) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + id));

        boolean needsUpdate = false;
        if (dto.getName() != null) {
            String trimmedName = dto.getName().trim();
            if (!StringUtils.hasText(trimmedName)) {
                throw new InvalidOperationException("O nome não pode ser vazio ou conter apenas espaços.");
            }
            if (!NAME_PATTERN.matcher(trimmedName).matches()) {
                throw new InvalidOperationException("O nome deve ser completo (ao menos duas palavras) e conter apenas letras, espaços ou apóstrofos.");
            }
            if (!trimmedName.equals(u.getName())) {
                u.setName(trimmedName);
                needsUpdate = true;
            }
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 6) {
                throw new InvalidOperationException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            if (!PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
                throw new InvalidOperationException("A nova senha está fraca, ela precisa possuir ao menos letras e números.");
            }
            u.setPasswordHash(encoder.encode(dto.getPassword()));
            needsUpdate = true;
        }

        if (dto.getSalary() != null) {
            if (dto.getSalary().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("O salário mensal precisa ser maior ou igual a 0.");
            }
            if (u.getSalary().compareTo(dto.getSalary()) != 0) {
                u.setSalary(dto.getSalary());
                needsUpdate = true;
            }
        }
        if (needsUpdate) {
            User updatedUser = userRepository.save(u);
            log.info("Usuário (ID: {}) atualizado.", updatedUser.getId());
            return updatedUser;
        }
        return u;
    }

    @Timed(value = "user.deleteService.time", description = "Tempo para remover usuário (serviço)")
    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id + " para exclusão.");
        }
        // Adicionar aqui a lógica para desassociar/deletar entidades relacionadas ao usuário ANTES de deletar o usuário,
        // para evitar DataIntegrityViolationException (ex: bancos, categorias, rendas, despesas).
        // Exemplo: bankRepository.deleteAllByUserId(id); categoryRepository.deleteAllByUserId(id); etc.
        // Ou configurar CascadeType.REMOVE nas relações na entidade User.
        userRepository.deleteById(id);
        log.info("Usuário (ID: {}) deletado.", id);
    }

    // --- MÉTODO CORRIGIDO ---
    /**
     * Retrieves the ID of the currently authenticated user from the SecurityContext.
     * The principal name is expected to be the user's UUID as a String,
     * as set by JWTAuthenticationFilter.
     *
     * @return The UUID of the authenticated user.
     * @throws ResourceNotFoundException if the user is not authenticated,
     * the principal identifier is not a valid UUID,
     * or the user is not found in the database.
     * @throws IllegalStateException if the authenticated principal is not of an expected type.
     */
    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            log.warn("Tentativa de obter ID de usuário não autenticado.");
            throw new ResourceNotFoundException("Usuário não autenticado. Não é possível recuperar o ID do usuário autenticado.");
        }

        String principalIdentifier;
        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            principalIdentifier = (String) principal;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Este caso é um fallback. O JWTAuthenticationFilter define o principal como String (UUID).
            // Se UserDetails.getUsername() retornar o email, esta lógica falharia em converter para UUID.
            // Idealmente, o principal já é a String do UUID.
            principalIdentifier = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            log.debug("Principal é instância de UserDetails, username (esperado ser UUID String) obtido: {}", principalIdentifier);
        } else {
            log.error("Principal autenticado não é do tipo String nem UserDetails: {}", principal.getClass().getName());
            throw new IllegalStateException("Principal autenticado não é de um tipo esperado: " + principal.getClass().getName() +
                    ". Não foi possível extrair o identificador do usuário.");
        }

        try {
            UUID userId = UUID.fromString(principalIdentifier);
            // Valida se o usuário com este ID realmente existe no banco de dados.
            if (!userRepository.existsById(userId)) {
                log.warn("ID de usuário autenticado ({}) presente no token não foi encontrado no banco de dados.", userId);
                throw new ResourceNotFoundException("Usuário autenticado (ID do token: " + userId + ") não encontrado no banco de dados.");
            }
            log.debug("ID do usuário autenticado recuperado com sucesso: {}", userId);
            return userId;
        } catch (IllegalArgumentException e) {
            log.error("Identificador do usuário ('{}') obtido do token de autenticação não é um UUID válido.", principalIdentifier, e);
            throw new ResourceNotFoundException("Identificador do usuário ('" + principalIdentifier + "') no token de autenticação não é um UUID válido.", e);
        }
    }
}