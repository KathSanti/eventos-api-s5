package com.duoc.msresumenes.client;

import com.duoc.msresumenes.dto.CompraDto;

/**
 * Comunicación con el microservicio ms-compras.
 * Es una interfaz para poder probar ResumenService sin levantar ms-compras (ver los tests).
 */
public interface ComprasClient {

    /**
     * Devuelve los datos de la compra.
     * @throws com.duoc.msresumenes.exception.RecursoNoEncontradoException si la compra no existe
     * @throws com.duoc.msresumenes.exception.ServicioComprasException si ms-compras no responde
     */
    CompraDto obtenerCompra(Long numero);
}
