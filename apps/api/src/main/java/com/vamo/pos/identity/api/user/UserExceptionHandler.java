package com.vamo.pos.identity.api.user;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vamo.pos.identity.application.UsernameAlreadyExistsException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Convierte errores del módulo de usuarios en respuestas HTTP claras.
 */
@RestControllerAdvice(assignableTypes = UserController.class)
public class UserExceptionHandler {

    /**
     * Un username duplicado representa un conflicto con el estado actual.
     */
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUsernameAlreadyExists(
        UsernameAlreadyExistsException exception,
        HttpServletRequest request
    ) {
        ProblemDetail problem = createProblem(
            HttpStatus.CONFLICT,
            "Conflicto de nombre de usuario",
            exception.getMessage(),
            "USERNAME_ALREADY_EXISTS",
            request
        );

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(problem);
    }

    /**
     * Convierte los errores de @Valid en una respuesta con cada campo inválido.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationErrors(
        MethodArgumentNotValidException exception,
        HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach(error -> fieldErrors.putIfAbsent(
                error.getField(),
                error.getDefaultMessage()
            ));

        ProblemDetail problem = createProblem(
            HttpStatus.BAD_REQUEST,
            "Solicitud inválida",
            "Uno o más campos contienen errores.",
            "VALIDATION_ERROR",
            request
        );
        problem.setProperty("fieldErrors", fieldErrors);

        return ResponseEntity
            .badRequest()
            .body(problem);
    }

    /**
     * Construye una respuesta compatible con Problem Details.
     */
    private static ProblemDetail createProblem(
        HttpStatus status,
        String title,
        String detail,
        String code,
        HttpServletRequest request
    ) {
       ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            status,
            detail
       );

       problem.setTitle(title);
       problem.setInstance(URI.create(request.getRequestURI()));
       problem.setProperty("code", code);

       return problem;
    }
}
