package com.duoc.msresumenes.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * EXTRA OPCIONAL (desactivado si GATEWAY_SECRET está vacío).
 *
 * Problema: la EC2 tiene IP pública, así que alguien podría llamar a
 * http://IP:8082/api/... directo y saltarse el API Gateway (y el token de Azure).
 *
 * Solución: el API Gateway agrega a cada petición el header X-Gateway-Secret con un
 * valor que solo él conoce (parameter mapping). Si el header no viene o no coincide,
 * se responde 403. /salud queda libre porque lo usa el pipeline desde la misma EC2.
 */
@Component
public class OrigenGatewayFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Gateway-Secret";

    private final byte[] secreto;

    public OrigenGatewayFilter(@Value("${app.gateway.secret:}") String secreto) {
        this.secreto = secreto == null ? new byte[0] : secreto.trim().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return secreto.length == 0 || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String recibido = request.getHeader(HEADER);
        // MessageDigest.isEqual compara en tiempo constante (no revela cuántos caracteres coinciden).
        if (recibido != null && MessageDigest.isEqual(secreto, recibido.trim().getBytes(StandardCharsets.UTF_8))) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":403,\"error\":\"Forbidden\",\"mensaje\":\"Acceso directo no permitido: usa la URL del API Gateway\"}");
    }
}
