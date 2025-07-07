package com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest;

import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.resources.SignInResource;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.dtaquito_backend.dtaquito_backend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.dtaquito_backend.dtaquito_backend.users.domain.services.UserCommandService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/auth/sign-in</li>
 *         <li>POST /api/v1/auth/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    @Autowired
    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);

        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        String token = authenticatedUser.get().getRight();

        ResponseCookie jwtCookie = ResponseCookie.from("JWT_TOKEN", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(3600)
                .sameSite("Strict")
                .build();

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(
                authenticatedUser.get().getLeft(), token
        );

        // Retornar la respuesta con la cookie
        return ResponseEntity.ok()
                .header("Set-Cookie", jwtCookie.toString())
                .body(authenticatedUserResource);
    }

    @GetMapping("/is-authenticated")
    public ResponseEntity<Map<String, Boolean>> isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated = authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());

        if (isAuthenticated) {
            return ResponseEntity.ok(Map.of("authenticated", true));
        } else {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
    }

    @PostMapping("/log-out")
    public ResponseEntity<Void> logOut() {
        ResponseCookie cookie = ResponseCookie.from("JWT_TOKEN", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .build();
    }

}