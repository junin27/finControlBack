package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserRegisterDto;
import fincontrol.com.fincontrol.dto.UserUpdateDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication; // Adicionado
import org.springframework.security.core.context.SecurityContextHolder; // Adicionado
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
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.");
        }
        User u = new User();
        u.setName(dto.getName().trim());
        u.setEmail(dto.getEmail().toLowerCase().trim());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        u.setSalary(dto.getSalary());
        return userRepository.save(u);
    }

    @Timed(value = "user.authenticate.time", description = "Tempo para autenticar um usuário")
    public User authenticate(LoginDto dto) {
        User u = userRepository.findByEmail(dto.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("O email '" + dto.getEmail() + "' não está cadastrado no sistema."));

        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())) {
            throw new BadCredentialsException("A sua senha está incorreta.");
        }
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

        if (dto.getName() != null) {
            String trimmedName = dto.getName().trim();
            if (!StringUtils.hasText(trimmedName)) {
                throw new InvalidOperationException("O nome não pode ser vazio ou conter apenas espaços.");
            }
            if (!NAME_PATTERN.matcher(trimmedName).matches()) {
                throw new InvalidOperationException("O nome deve ser completo (ao menos duas palavras) e conter apenas letras, espaços ou apóstrofos.");
            }
            u.setName(trimmedName);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            if (dto.getPassword().length() < 6) {
                throw new InvalidOperationException("A nova senha deve ter no mínimo 6 caracteres.");
            }
            if (!PASSWORD_PATTERN.matcher(dto.getPassword()).matches()) {
                throw new InvalidOperationException("A nova senha está fraca, ela precisa possuir ao menos letras e números.");
            }
            u.setPasswordHash(encoder.encode(dto.getPassword()));
        }

        if (dto.getSalary() != null) {
            if (dto.getSalary().compareTo(BigDecimal.ZERO) < 0) {
                throw new InvalidOperationException("O salário mensal precisa ser maior ou igual a 0.");
            }
            u.setSalary(dto.getSalary());
        }
        return userRepository.save(u);
    }

    @Timed(value = "user.deleteService.time", description = "Tempo para remover usuário (serviço)")
    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + id + " para exclusão.");
        }
        userRepository.deleteById(id);
    }

    // --- MÉTODO ADICIONADO ---
    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return The UUID of the authenticated user.
     * @throws ResourceNotFoundException if the user is not authenticated or not found in the database.
     * @throws IllegalStateException if the authenticated principal is not of an expected type.
     */
    public UUID getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            // Em um cenário ideal, o Spring Security já bloquearia o acesso não autenticado aos endpoints protegidos.
            // Esta exceção é uma salvaguarda adicional.
            throw new ResourceNotFoundException("User not authenticated. Cannot retrieve authenticated user ID.");
        }

        String username; // Geralmente o e-mail, conforme sua configuração de segurança
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            // Se o principal for diretamente uma String (pode acontecer em algumas configurações de teste ou customizadas)
            username = (String) principal;
        } else {
            // Se o tipo do principal não for esperado
            throw new IllegalStateException("Authenticated principal is not of an expected type: " + principal.getClass().getName() +
                    ". Could not extract username for user lookup.");
        }

        // Utiliza o método findByEmail que já existe e normaliza o e-mail (toLowerCase, trim)
        User user = this.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found in database with email: " + username));

        // Assumindo que sua entidade User tem um método getId() que retorna UUID
        return user.getId();
    }
}