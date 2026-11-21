# Eventos Microservicios · AWS API Gateway + Azure AD B2C

Desarrollo Cloud Native (CDY2204) · Semana 4 · Actividad "Configurando IDaaS y API Manager".

Sistema de venta de entradas dividido en **2 microservicios Spring Boot**, desplegados con **GitHub Actions** en una
**EC2**, con datos en **RDS** y archivos en **S3**. Todos los endpoints se publican en **AWS API Gateway (HTTP API)**
y se autentican con **Azure AD B2C** (IDaaS). El backend ya **no** usa Spring Security ni JWT propio.

## Arquitectura

```text
   Postman ──(1) login──────────────────────────► Azure AD B2C (IDaaS)
      │      ◄──────────── access token (JWT) ──┘
      │
      │ (2) Authorization: Bearer <token>
      ▼
   AWS API Gateway (HTTP API) ── autorizador JWT: valida firma, iss, aud, exp y scope
      │                                 │
      │ /api/eventos, /api/compras/**   │ /api/resumenes/**
      ▼                                 ▼
 ┌──────────────── EC2 (Docker Compose) ──────────────────────┐
 │  ms-compras :8081  ◄── HTTP interno ──  ms-resumenes :8082 │
 └──────┬──────────────────────────────────────────┬──────────┘
        ▼                                          ▼
   AWS RDS MySQL (compras_db)            AWS S3 bucket  {numero}/resumen-compra-{numero}.txt
```

## Cómo se separó el proyecto en microservicios

El proyecto de la semana 2 era **un solo backend** (monolito) con login, roles, eventos, compras, clientes y S3.
Se separó por **responsabilidad** y cada microservicio es **dueño de su propio almacenamiento**:

| Microservicio | Responsabilidad | Almacenamiento | Puerto |
|---|---|---|---|
| `ms-compras` | Eventos y compras de entradas | **RDS MySQL** (`compras_db`) | 8081 |
| `ms-resumenes` | Los 5 endpoints del resumen de compra (archivo y S3) | **S3** | 8082 |

- `ms-resumenes` **no** tiene base de datos: cuando necesita los datos de una compra se los pide a `ms-compras`
  por HTTP (`GET /api/compras/{numero}`), por la red interna de Docker (`http://ms-compras:8081`).
- Se **eliminó** todo lo que ahora resuelve Azure AD B2C: `SecurityConfig`, `JwtAuthFilter`, `JwtService`,
  `AuthController` (registro/login), `Cliente` con contraseña BCrypt, roles, bloqueo por intentos y admin por IP.
- Como ya no hay usuarios en la BD, la compra guarda `nombreCliente` y `emailCliente`, que llegan en el body.

## Endpoints

Todos se inscriben en el API Gateway con el autorizador de Azure AD B2C.

| Método | Ruta en el Gateway | Microservicio | Qué hace |
|---|---|---|---|
| GET | `/api/eventos` | ms-compras | Lista los eventos (se crean 3 de ejemplo al arrancar) |
| POST | `/api/compras` | ms-compras | Compra entradas → devuelve `numeroResumen` |
| GET | `/api/compras/{numero}` | ms-compras | Detalle de la compra |
| GET | `/api/resumenes/{numero}` | ms-resumenes | **Genera el resumen como archivo físico** (.txt descargable) |
| POST | `/api/resumenes/{numero}/s3` | ms-resumenes | **Sube el resumen a S3** en la carpeta `{numero}/` |
| PUT | `/api/resumenes/{numero}/s3` | ms-resumenes | **Modifica** el archivo (form-data, campo `file`, .txt) |
| GET | `/api/resumenes/{numero}/s3` | ms-resumenes | **Descarga** el archivo desde S3 |
| DELETE | `/api/resumenes/{numero}/s3` | ms-resumenes | **Borra** el archivo de S3 |

`GET /salud` existe en ambos (lo usa el pipeline). No se publica en el Gateway.

Ejemplo de body para comprar:

```json
{ "nombreCliente": "Ana Pérez", "emailCliente": "ana@correo.cl", "nombreEvento": "Festival de Jazz", "cantidad": 2 }
```

## Estructura

```text
eventos-microservicios/
├── .github/workflows/deploy.yml   # tests → docker (2 imágenes) → deploy EC2
├── deploy/compose.ec2.yml         # lo que corre en la EC2
├── docker-compose.yml             # prueba local con MySQL
├── ms-compras/                    # Spring Boot + JPA (RDS)
├── ms-resumenes/                  # Spring Boot + S3 + RestClient
└── postman/                       # colección + archivo para el PUT
```

## Ejecutar en local

```bash
cp .env.example .env              # completa S3_BUCKET y las 3 credenciales de AWS Academy

# Opción A: cada microservicio por separado (ms-compras usa H2 en memoria)
cd ms-compras   && ./mvnw spring-boot:run      # http://localhost:8081
cd ms-resumenes && ./mvnw spring-boot:run      # http://localhost:8082  (en otra terminal)

# Opción B: todo con Docker (MySQL local en vez de RDS)
docker compose up --build
```

En local no hay Gateway ni token: las peticiones van directo a `localhost:8081` / `localhost:8082`.

## CI/CD (GitHub Actions)

`.github/workflows/deploy.yml` mantiene el esquema del workflow del profe:

1. **tests**: `mvn verify` de cada microservicio (matrix), en PRs y push a `main`.
2. **docker**: publica `DOCKERHUB_USERNAME/eventos-ms-compras` y `eventos-ms-resumenes` (tags `latest` y `sha`).
3. **deploy-ec2**: copia `deploy/compose.ec2.yml` por SCP, hace `pull` + `up` por SSH y prueba `/salud` en 8081 y 8082.

*Settings → Secrets and variables → Actions*:

| Secret | Valor |
|---|---|
| `DOCKERHUB_USERNAME` · `DOCKERHUB_TOKEN` | Usuario y token de Docker Hub (Read & Write) |
| `EC2_HOST` · `EC2_USER` · `EC2_SSH_KEY` | IP elástica · `ubuntu` · contenido completo de `labsuser.pem` |
| `DB_HOST` · `DB_USER` · `DB_PASSWORD` | Endpoint de RDS · usuario maestro · contraseña maestra |
| `AWS_ACCESS_KEY_ID` · `AWS_SECRET_ACCESS_KEY` · `AWS_SESSION_TOKEN` | Learner Lab → AWS Details → AWS CLI (cambian en cada sesión) |
| `GATEWAY_SECRET` | *Opcional* (ver "Extra") |

| Variable | Valor |
|---|---|
| `S3_BUCKET` | Nombre de tu bucket |
| `AWS_REGION` | `us-east-1` (opcional) |
| `DB_NAME` | `compras_db` (opcional) |

> En cada sesión nueva del Learner Lab: actualiza los 3 secrets `AWS_*` y haz **Actions → Run workflow**.

## Configuración en AWS y Azure (resumen)

El paso a paso completo, con espacios para las capturas, está en **`docs/Guia-paso-a-paso-Semana4.docx`**.

1. **S3**: bucket privado con Versioning.
2. **EC2**: Security Group con 22, 8081 y 8082 abiertos; Docker instalado; IP elástica.
3. **RDS**: MySQL conectado a la EC2 (sin acceso público).
4. **GitHub**: secrets → push a `main` → verificar `http://IP:8081/salud` y `http://IP:8082/salud`.
5. **Azure AD B2C**: user flow con issuer en formato `tfp`, app de la API con scope expuesto y redirect URI de Postman.
6. **API Gateway (HTTP API)**: 5 integraciones HTTP hacia la EC2, 8 rutas, autorizador JWT
   (`issuer` = `iss` del token, `audience` = client ID de la API) asignado a todas las rutas.
   
