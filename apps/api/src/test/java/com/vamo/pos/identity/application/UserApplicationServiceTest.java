package com.vamo.pos.identity.application;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vamo.pos.identity.domain.AppUser;
import com.vamo.pos.identity.repository.AppUserRepository;

/**
 * Pruebas unitarias para registrar usuarios.
 */
class UserApplicationServiceTest {
    private AppUserRepository appUserRepository;
    private PasswordEncoder passwordEncoder;
    private UserApplicationService service;

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        service = new UserApplicationService(appUserRepository, passwordEncoder);
    }

    /**
     * Verifica el flujo exitoso y captura la entidad enviada al repositorio.
     */
    @Test
    void createUserWithEncodedPassword() {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID createdUserId = UUID.randomUUID();

        CreateUserCommand command = new CreateUserCommand(
                tenantId,
                branchId,
                " Admin ",
                " Administrador ",
                "Secret123!",
                null);

        when(appUserRepository.existsByTenantIdAndNormalizedUsername(tenantId, "admin")).thenReturn(false);
        when(passwordEncoder.encode("Secret123!")).thenReturn("{bcrypt}encoded-password-hash");

        AppUser persistedUser = mock(AppUser.class);

        when(persistedUser.getId()).thenReturn(createdUserId);
        when(appUserRepository.save(any(AppUser.class))).thenReturn(persistedUser);

        UUID result = service.createUser(command);

        ArgumentCaptor<AppUser> userCaptor = ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(userCaptor.capture());
        verify(passwordEncoder).encode("Secret123!");

        AppUser capturedUser = userCaptor.getValue();

        assertAll(
                () -> assertEquals(createdUserId, result),
                () -> assertEquals(tenantId, capturedUser.getTenantId()),
                () -> assertEquals(branchId, capturedUser.getDefaultBranchId()),
                () -> assertEquals("Admin", capturedUser.getUsername()),
                () -> assertEquals("admin", capturedUser.getNormalizedUsername()),
                () -> assertEquals("Administrador", capturedUser.getDisplayName()),
                () -> assertEquals("{bcrypt}encoded-password-hash", capturedUser.getPasswordHash()));
    }

    /**
     * Un username duplicado debe detenerse antes de generar el hash
     * o intentar guardar otra entidad.
     */
    @Test
    void rejectsDuplicateUsername() {
        UUID tenantId = UUID.randomUUID();

        CreateUserCommand command = new CreateUserCommand(
                tenantId,
                UUID.randomUUID(),
                "ADMIN",
                "Administrador",
                "Secret123!",
                null);

        when(appUserRepository.existsByTenantIdAndNormalizedUsername(tenantId, "admin")).thenReturn(true);

        assertThrows(UsernameAlreadyExistsException.class, () -> service.createUser(command));

        verify(passwordEncoder, never()).encode(anyString());
        verify(appUserRepository, never()).save(any(AppUser.class));
    }
}
