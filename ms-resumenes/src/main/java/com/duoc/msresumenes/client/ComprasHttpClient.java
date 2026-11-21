package com.duoc.msresumenes.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.duoc.msresumenes.dto.CompraDto;
import com.duoc.msresumenes.exception.ApiException;

/**
 * Llama a ms-compras por HTTP (red interna de Docker: http://ms-compras:8081).
 * Reenvía el token del usuario ("token relay"): ms-compras revisa con él
 * que la compra exista, esté activa y sea de esta persona que consulta.
 */
@Component
public class ComprasHttpClient implements ComprasClient {

    private final RestClient restClient;

    public ComprasHttpClient(@Value("${app.compras.url}") String urlBase) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(factory)
                .build();
    }

    @Override
    public CompraDto obtenerCompra(Long numero) {
        try {
            CompraDto compra = restClient.get()
                    .uri("/api/compras/{numero}", numero)
                    .headers(h -> h.setBearerAuth(tokenDelUsuario()))  
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CompraDto.class);
            if (compra == null) {
                throw ApiException.servicioNoDisponible("ms-compras respondió sin datos para la compra N° " + numero, null);
            }
            return compra;
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.noEncontrado("No existe la compra N° " + numero);          
        } catch (HttpClientErrorException.Forbidden e) {
            throw ApiException.prohibido("La compra N° " + numero + " no te pertenece");     
        } catch (RestClientException e) {
            throw ApiException.servicioNoDisponible(
                    "No se pudo consultar la compra N° " + numero + " en ms-compras", e);      
        }
    }

    // Saca el token que Spring Security ya validó en esta petición
    private static String tokenDelUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            return jwtAuth.getToken().getTokenValue();
        }
        throw ApiException.servicioNoDisponible("No hay token de usuario para reenviar a ms-compras", null);
    }
}