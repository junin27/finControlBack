package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserRegisterDto;
import fincontrol.com.fincontrol.dto.UserUpdateDto;
import fincontrol.com.fincontrol.exception.InvalidOperationException;
import fincontrol.com.fincontrol.exception.ResourceNotFoundException;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import org.springframework.security.authentication.BadCredentialsException; // Usar para erros de autenticação
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils; // Para StringUtils.hasText

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern; // Para validações de regex se necessário aqui

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    // Regex para validar nome completo (ao menos duas palavras, somente letras, espaços, apóstrofos)
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-ZÀ-ú']+(\\s[a-zA-ZÀ-ú']+)+$");
    // Regex para validar senha (mínimo 6 chars, 1 letra, 1 número)
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,}$");


    public UserService(UserRepository userRepository,
                       PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Timed(value = "user.register.time", description = "Tempo para registrar um novo usuário")
    @Transactional
    public User register(UserRegisterDto dto) {
        // Validações de DTO (NotBlank, Email, Size, PositiveOrZero, Pattern) são feitas pelo @Valid no Controller.
        // Aqui focamos nas validações que cruzam campos ou regras de negócio mais complexas.

        // 1. Verificar se as senhas coincidem
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidOperationException("As duas senhas não conferem, elas precisam ter os mesmos caracteres exatamente iguais.");
        }

        // As validações de formato de nome, e-mail, força de senha e salário positivo
        // já estão no DTO com anotações e serão verificadas pelo @Valid no AuthController.
        // A validação de e-mail único também está no AuthController.

        User u = new User();
        u.setName(dto.getName().trim()); // Remover espaços extras do nome
        u.setEmail(dto.getEmail().toLowerCase().trim()); // Armazenar e-mail em minúsculas e sem espaços extras
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        u.setSalary(dto.getSalary());
        return userRepository.save(u);
    }

    @Timed(value = "user.authenticate.time", description = "Tempo para autenticar um usuário")
    public User authenticate(LoginDto dto) {
        // Validações de DTO (NotBlank, Email) são feitas pelo @Valid no AuthController.
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
            // Se houver um campo confirmPassword no UserUpdateDto, validar aqui também.
            // if (dto.getConfirmPassword() == null || !dto.getPassword().equals(dto.getConfirmPassword())) {
            //     throw new InvalidOperationException("As senhas para atualização não conferem.");
            // }
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
}
