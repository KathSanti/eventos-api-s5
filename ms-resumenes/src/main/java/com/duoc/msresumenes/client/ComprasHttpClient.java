package com.duoc.msresumenes.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.duoc.msresumenes.config.OrigenGatewayFilter;
import com.duoc.msresumenes.dto.CompraDto;
import com.duoc.msresumenes.exception.RecursoNoEncontradoException;
import com.duoc.msresumenes.exception.ServicioComprasException;

/**
 * Llama a ms-compras por HTTP con RestClient.
 *
 * En la EC2 ambos contenedores están en la misma red de Docker, así que la URL es
 * http://ms-compras:8081 (el nombre del servicio en compose.ec2.yml). Esta llamada es
 * INTERNA: no pasa por el API Gateway, por eso no necesita el token de Azure.
 */
@Component
public class ComprasHttpClient implements ComprasClient {

    private final RestClient restClient;

    public ComprasHttpClient(@Value("${app.compras.url}") String urlBase,
                             @Value("${app.gateway.secret:}") String secretoGateway) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(10));

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(urlBase)
                .requestFactory(factory);
        // Si el EXTRA del secreto está activo, ms-compras también lo exige: se lo enviamos.
        if (StringUtils.hasText(secretoGateway)) {
            builder.defaultHeader(OrigenGatewayFilter.HEADER, secretoGateway.trim());
        }
        this.restClient = builder.build();
    }

    @Override
    public CompraDto obtenerCompra(Long numero) {
        try {
            CompraDto compra = restClient.get()
                    .uri("/api/compras/{numero}", numero)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(CompraDto.class);
            if (compra == null) {
                throw new ServicioComprasException("ms-compras respondió sin datos para la compra N° " + numero, null);
            }
            return compra;
        } catch (HttpClientErrorException.NotFound e) {
            throw new RecursoNoEncontradoException("No existe la compra N° " + numero);
        } catch (RestClientException e) {
            throw new ServicioComprasException("No se pudo consultar la compra N° " + numero + " en ms-compras", e);
        }
    }
}
