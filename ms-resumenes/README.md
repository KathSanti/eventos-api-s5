# ms-resumenes

Microservicio de **resúmenes de compra**: genera el archivo del resumen y lo maneja en **AWS S3**. No tiene base de datos; los datos de la compra se los pide a **ms-compras** mediante token y http interno.

[← Volver al README principal](../README.md)

## Endpoints

| Método | Ruta | Quién puede | Qué hace |
|---|---|---|---|
| GET | `/api/resumenes/{numero}` | CLIENTE, ADMIN | Genera el resumen como archivo `.txt` descargable |
| POST | `/api/resumenes/{numero}/s3` | CLIENTE, ADMIN | Sube el resumen a S3 en la carpeta `{numero}/` |
| PUT | `/api/resumenes/{numero}/s3` | CLIENTE, ADMIN | Modifica el archivo (form-data, campo `file`) |
| GET | `/api/resumenes/{numero}/s3` | CLIENTE, ADMIN | Descarga el archivo desde S3 |
| DELETE | `/api/resumenes/{numero}/s3` | ADMIN | Borra el archivo de S3 |
| GET | `/salud` | Público | Health check que usa el pipeline |

## Seguridad

Usa el mismo **`SecurityConfig`** que ms-compras, con la autorización en **2 capas**:

1. **Token (`SecurityConfig`):** valida el JWT de Entra ID. Todo pide token, menos `/salud`. Sin token responde **401**.
2. **Rol (`@PreAuthorize` en el controller):** se puso en la clase para CLIENTE y ADMIN, y el `DELETE` lo sobrescribe para que solo lo use ADMIN. Sin rol responde **403**.

Además, le pide la compra a ms-compras por **HTTP interno**, dentro de la red de Docker (`http://ms-compras:8081`, sin pasar por el API Gateway), y **le reenvía el token del usuario** (*token relay*). Así ms-compras puede revisar que la compra sea de esa persona y que siga activa.

## Validaciones

- El número de resumen tiene que ser mayor que 0 (**400**).
- **Antes de cualquier operación consulta a ms-compras:** si la compra no existe o está desactivada responde **404**, y si es de otra persona responde **403**. Aunque el archivo esté en S3, no se entrega.
- **Antes de modificar, descargar o borrar, verifica que el archivo exista en S3.** Si no está responde **404**, y hay que subirlo primero con `POST .../s3`.
- El archivo para modificar debe ser `.txt`, no estar vacío y pesar como máximo 1 MB (**400** / **413**).
- La ruta en S3 la arma el servidor (`{numero}/resumen-compra-{numero}.txt`). El cliente nunca elige el bucket ni la carpeta.

## Errores

Los errores de negocio usan `ApiException` (400, 403, 404 y 503) y responden con el mismo JSON gracias a `GlobalExceptionHandler`. Si S3 falla (por ejemplo, credenciales de AWS Academy vencidas), responde **502**.

## Tests

```bash
./mvnw clean verify
```

`ResumenServiceTest` son pruebas unitarias **sin AWS y sin ms-compras**: usan un ms-compras falso y un "bucket" en memoria. Incluyen los casos de compra ajena (403) y compra desactivada (404). 
