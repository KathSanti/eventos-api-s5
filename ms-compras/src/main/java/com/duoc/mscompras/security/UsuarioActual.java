package com.duoc.mscompras.security;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

// Quién está haciendo la petición y qué roles desde token
public record UsuarioActual(String id, List<String> roles) {

    public static UsuarioActual desde(Jwt jwt) {
    
        String oid = jwt.getClaimAsString("oid");
        String id = (oid != null) ? oid : jwt.getSubject();

        List<String> roles = jwt.getClaimAsStringList("roles");
        return new UsuarioActual(id, roles != null ? List.copyOf(roles) : List.of());
    }

    public boolean tieneRol(String rol) {
        return roles.contains(rol);
    }

    public boolean esAdmin() {
        return tieneRol("ADMIN");
    }
}