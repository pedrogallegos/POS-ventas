package com.vamo.pos.identity.application;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vamo.pos.identity.domain.AppUser;
import com.vamo.pos.identity.repository.AppUserRepository;

/**
 * Coordina los casos de uso relacionados con usuarios.
 */
@Service
public class UserApplicationService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Spring inyecta ambas dependencias mediante el constructor.
     * No necesitamos @Autowired porque solamente existe un constructor.
     */
    public UserApplicationService(
        AppUserRepository appUserRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = Objects.requireNonNull(
            appUserRepository,
            "AppUserRepository es obligatorio."
        );
        this.passwordEncoder = Objects.requireNonNull(
            passwordEncoder,
            "PasswordEncoder es obligatorio."
        );
    }
    /**
     * Registra un usuario y devuelve su identificador.
     * La transacción garantiza que la operación completa tenga éxito
     * o que ningún cambio sea confirmado.
     */
    @Transactional
    public UUID createUser(CreateUserCommand command) {
        CreateUserCommand validatedCommand = Objects.requireNonNull(command, "CreateUserCommand es obligatorio.");

        String normalizedUsername = validatedCommand.username().toLowerCase(Locale.ROOT);
        if (appUserRepository.existsByTenantIdAndNormalizedUsername(validatedCommand.tenantId(), normalizedUsername)) {
            throw new UsernameAlreadyExistsException();
        }

        String passwordHash = passwordEncoder.encode(validatedCommand.rawPassword());

        AppUser user = new AppUser(
            validatedCommand.tenantId(),
            validatedCommand.defaultBranchId(),
            validatedCommand.username(),
            validatedCommand.displayName(),
            passwordHash,
            validatedCommand.createdBy()
        );

        AppUser savedUser = appUserRepository.save(user);

        return savedUser.getId();
    }
}
