package com.duoc.msresumenes.client;

import com.duoc.msresumenes.dto.CompraDto;

/**
 * Comunicación con el microservicio ms-compras.
 * Es una interfaz para poder probar ResumenService sin levantar ms-compras 
 */
public interface ComprasClient {

    /**
     * Devuelve los datos de la compra si el usuario del token puede verla.
     * Lanza ApiException: 404 si no existe o está desactivada, 403 si es de otra persona,
     * 503 si ms-compras no responde.
     */
    CompraDto obtenerCompra(Long numero);
}
