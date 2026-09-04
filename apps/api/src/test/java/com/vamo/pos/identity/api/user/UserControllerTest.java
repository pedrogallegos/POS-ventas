package com.vamo.pos.identity.api.user;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.vamo.pos.identity.application.CreateUserCommand;
import com.vamo.pos.identity.application.UserApplicationService;
import com.vamo.pos.identity.application.UsernameAlreadyExistsException;

/**
 * Pruebas HTTP del registro de usuarios.
 */
class UserControllerTest {

    private UserApplicationService userApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userApplicationService = mock(UserApplicationService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userApplicationService))
                .setControllerAdvice(new UserExceptionHandler())
                .build();
    }

    @Test
    void createsUserAndReturnsLocation() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(
                userApplicationService.createUser(
                        any(CreateUserCommand.class)
                )
        ).thenReturn(userId);

        mockMvc.perform(
                post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "defaultBranchId": "%s",
                                  "username": "admin",
                                  "displayName": "Administrador",
                                  "password": "Secret123!"
                                }
                                """.formatted(branchId)
                        )
        )
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        "http://localhost/api/v1/tenants/"
                                + tenantId
                                + "/users/"
                                + userId
                ))
                .andExpect(jsonPath("$.id").value(userId.toString()));

        ArgumentCaptor<CreateUserCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateUserCommand.class);

        verify(userApplicationService).createUser(
                commandCaptor.capture()
        );

        CreateUserCommand capturedCommand = commandCaptor.getValue();

        assertAll(
                () -> assertEquals(
                        tenantId,
                        capturedCommand.tenantId()
                ),
                () -> assertEquals(
                        branchId,
                        capturedCommand.defaultBranchId()
                ),
                () -> assertEquals(
                        "admin",
                        capturedCommand.username()
                ),
                () -> assertEquals(
                        "Administrador",
                        capturedCommand.displayName()
                ),
                () -> assertEquals(
                        "Secret123!",
                        capturedCommand.rawPassword()
                ),
                () -> assertNull(capturedCommand.createdBy())
        );
    }

    @Test
    void returnsConflictForDuplicateUsername() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        when(
                userApplicationService.createUser(
                        any(CreateUserCommand.class)
                )
        ).thenThrow(new UsernameAlreadyExistsException());

        mockMvc.perform(
                post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "defaultBranchId": "%s",
                                  "username": "admin",
                                  "displayName": "Administrador",
                                  "password": "Secret123!"
                                }
                                """.formatted(branchId)
                        )
        )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("USERNAME_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.title")
                        .value("Conflicto de nombre de usuario"));
    }

    @Test
    void returnsBadRequestForInvalidPassword() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        mockMvc.perform(
                post("/api/v1/tenants/{tenantId}/users", tenantId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                  "defaultBranchId": "%s",
                                  "username": "admin",
                                  "displayName": "Administrador",
                                  "password": "123"
                                }
                                """.formatted(branchId)
                        )
        )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.password")
                        .value(
                                "La contraseña debe contener entre 8 y 72 caracteres."
                        ));

        verifyNoInteractions(userApplicationService);
    }
}