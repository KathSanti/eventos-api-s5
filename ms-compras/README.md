# ms-compras

Microservicio de **eventos y compras de entradas**. Usa la base de datos (**AWS RDS MySQL**).

[← Volver al README principal](../README.md)

## Endpoints

| Método | Ruta | Quién puede | Qué hace |
|---|---|---|---|
| GET | `/api/eventos` | Público | Lista los eventos |
| POST | `/api/eventos` | ADMIN, VENDEDOR | Crea un evento |
| POST | `/api/compras` | CLIENTE, ADMIN | Compra entradas y devuelve el `numeroResumen` |
| GET | `/api/compras/{numero}` | CLIENTE, ADMIN | Detalle de la compra (el cliente solo ve las suyas) |
| DELETE | `/api/compras/{numero}` | ADMIN | Desactiva la compra (no la borra) |
| GET | `/salud` | Público | Health check que usa el pipeline |

## Seguridad

Agregué **Spring Security** como Resource Server en `security/SecurityConfig.java`. La autorización funciona en **2 capas**:

1. **Token (`SecurityConfig`):** valida el JWT de Entra ID (firma, emisor, audiencia y expiración). 
    - Sin token o con un token inválido responde **401**
    - CSRF va desactivado y sin sesiones, porque es una API *stateless* que trabaja con tokens.

2. **Rol (`@PreAuthorize` en controllers):** revisa si el rol del usuario puede usar ese endpoint. Si no tiene el rol, responde **403**.

Además, `CompraService` revisa que la compra sea **del usuario**. El dueño se saca del claim `oid` del token (clase `UsuarioActual`), no del email que viene en el body.

## Validaciones

- **Body (`@Valid`):** nombre y email obligatorios, email válido y cantidad entre 1 y 10. En eventos, la fecha debe ser futura y el precio al menos 1.
- El evento tiene que existir (**404**) y estar disponible, sin que su fecha ya haya pasado (**409**).
- No se pueden crear dos eventos con el mismo nombre (**409**).
- El total se calcula con el precio guardado en la BD, no con lo que manda el cliente.
- Un cliente no puede ver la compra de otra persona (**403**).
- **Desactivar es un borrado lógico:** la compra queda en la BD con `activa = false`. El cliente ya no la ve (**404**), pero el admin sí.

## Errores

Todos los errores responden con el mismo JSON (`timestamp`, `status`, `error`, `mensaje`) por los programado en `GlobalExceptionHandler`. Los errores de negocio usan una sola clase, `ApiException` (404, 409 y 403).


## Tests

```bash
./mvnw clean verify
```

`CompraServiceTest` prueba las reglas de negocio: 
- dueño de la compra, compra ajena (403)
- desactivación (404 para el cliente, visible para el admin)
- evento no disponible (409) 
- recursos que no existen (404).
