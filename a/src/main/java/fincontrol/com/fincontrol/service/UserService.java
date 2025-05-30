// src/main/java/fincontrol/com/fincontrol/service/UserService.java
package fincontrol.com.fincontrol.service;

import fincontrol.com.fincontrol.dto.LoginDto;
import fincontrol.com.fincontrol.dto.UserRegisterDto;
import fincontrol.com.fincontrol.dto.UserUpdateDto;
import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import io.micrometer.core.annotation.Timed;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    @Timed(value = "user.register.time", description = "Tempo para registrar um novo usuário")
    public void register(UserRegisterDto dto) {
        User u = new User();
        u.setName(dto.getName());
        u.setEmail(dto.getEmail());
        u.setPasswordHash(encoder.encode(dto.getPassword()));
        u.setSalary(dto.getSalary());
        userRepository.save(u);
    }

    @Timed(value = "user.authenticate.time", description = "Tempo para autenticar um usuário")
    public User authenticate(LoginDto dto) {
        User u = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!encoder.matches(dto.getPassword(), u.getPasswordHash())) {
            throw new RuntimeException("Credenciais inválidas");
        }
        return u;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        u.setName(dto.getName());
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            u.setPasswordHash(encoder.encode(dto.getPassword()));
        }
        u.setSalary(dto.getSalary());
        return userRepository.save(u);
    }

    @Timed(value = "user.deleteService.time", description = "Tempo para remover usuário (serviço)")
    @Transactional
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
