package fincontrol.com.fincontrol.service; // Ou fincontrol.com.fincontrol.security

import fincontrol.com.fincontrol.model.User;
import fincontrol.com.fincontrol.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@Service // Registra este serviço como um bean gerenciado pelo Spring
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        // 'usernameOrId' aqui será o userIdString (UUID como String)
        // vindo do JWTAuthenticationFilter quando o Spring Security precisa do UserDetails.
        try {
            UUID userId = UUID.fromString(usernameOrId);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com ID: " + userId +
                            ". O ID foi fornecido como 'username' para UserDetailsService."));

            // Define as autoridades (papéis/permissões). Ajuste conforme sua necessidade.
            Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
            // Se você tiver papéis na sua entidade User, mapeie-os aqui.

            // O primeiro argumento para o construtor de org.springframework.security.core.userdetails.User
            // é o 'username' que será armazenado no UserDetails.
            // Usar o email do usuário aqui é uma prática comum e segura.
            // O password é o hash da senha armazenado no banco.
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(), // Username para o UserDetails (pode ser o email)
                    user.getPasswordHash(), // O hash da senha do seu banco
                    authorities
            );

        } catch (IllegalArgumentException e) {
            // Se o usernameOrId não for um UUID válido.
            throw new UsernameNotFoundException("Identificador ('" + usernameOrId + "') fornecido como username para UserDetailsService não é um UUID válido.", e);
        }
    }
}