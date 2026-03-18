package com.felipelima.clientmanager.controller;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.felipelima.clientmanager.dto.request.LoginRequest;
import com.felipelima.clientmanager.dto.response.LoginResponse;
import com.felipelima.clientmanager.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /auth/login
     *
     * Receives username + password, returns a JWT token.
     * This endpoint is public (configured in SecurityConfig).
     *
     * Equivalent in Django REST Framework:
     * class LoginView(APIView):
     * permission_classes = [AllowAny]
     * def post(self, request):
     * serializer = LoginSerializer(data=request.data)
     * serializer.is_valid(raise_exception=True)
     * return Response(auth_service.login(serializer.validated_data))
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }
}
