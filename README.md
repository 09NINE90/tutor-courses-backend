# Teacher Portal — Courses Service

Микросервис управления курсами для платформы Teacher Portal.
Обеспечивает создание курсов, управление участниками, приглашения по ссылкам и токенам,
дашборд курсов с пагинацией и интеграцию с user-service для получения профилей.

---

- [Быстрый старт](#быстрый-старт)
- [Технологии](#технологии)
- [Структура проекта](#структура-проекта)
- [REST API](#rest-api)
- [Модель данных](#модель-данных)
- [Статусы участников](#статусы-участников)
- [Архитектура и поток приглашений](#архитектура-и-поток-приглашений)
- [Безопасность](#безопасность)
- [Миграции БД](#миграции-бд)
- [Конфигурация](#конфигурация)
- [Интеграция с user-service](#интеграция-с-user-service)
- [Docker](#docker)
- [Swagger](#swagger)

---

## Быстрый старт

**Требования:** Java 25, PostgreSQL, MinIO, запущенный `tutor-auth-backend` (user-service)

1. Скопируйте `template.env` → `.env` и заполните переменные
2. Запустите PostgreSQL и MinIO
3. `./gradlew bootRun` — сервис поднимется на `http://localhost:8082`

Миграции Flyway применятся автоматически при первом запуске.

---

## Технологии

| Категория        | Технологии                                                |
|------------------|-----------------------------------------------------------|
| **Язык**         | Java 25                                                   |
| **Фреймворк**    | Spring Boot 4.0.6                                         |
| **Сборка**       | Gradle 9.5.0                                              |
| **БД**           | PostgreSQL + Flyway                                       |
| **Безопасность** | Spring Security, JWT (jjwt 0.12.6 + кастомная библиотека) |
| **Хранилище**    | MinIO (S3-совместимое, для изображений курсов)            |
| **ORM**          | Spring Data JPA + Hibernate                               |
| **Маппинг**      | ModelMapper 3.2.6 (подключён, используется ручной маппер) |
| **Документация** | SpringDoc OpenAPI (Swagger UI)                            |
| **Boilerplate**  | Lombok                                                    |

---

## Структура проекта

```
tutor-courses-backend/
├── src/main/java/ru/razumoff/
│   ├── TutorCoursesApplication.java           # Точка входа (@SpringBootApplication)
│   ├── Constants.java                         # Константы ApiDocs, MinIO
│   │
│   ├── config/                                # Spring-конфигурация
│   │   ├── MinioConfig.java                   # Бин MinioClient
│   │   ├── OpenApiConfig.java                 # BearerAuth схема для Swagger
│   │   └── security/
│   │       ├── SecurityConfig.java            # SecurityFilterChain, CORS, @EnableMethodSecurity
│   │       ├── JwtAuthFilter.java             # JWT-фильтр (OncePerRequestFilter)
│   │       └── JwtService.java                # Генерация/валидация/парсинг JWT
│   │
│   ├── courses/
│   │   ├── api/                               # REST-контроллеры
│   │   │   ├── CoursesApi.java                # /api/courses — дашборд, invite-links, join
│   │   │   └── CourseApi.java                 # /api/course — create, get, members, invite, confirm
│   │   │
│   │   ├── dao/
│   │   │   ├── dto/                           # DTO запросов и ответов
│   │   │   │   ├── CourseRsDto.java           # Краткая карточка курса
│   │   │   │   ├── CoursePageRsDto.java       # Курс + статус участника
│   │   │   │   ├── CourseMemberRsDto.java     # Участник курса (профиль + статус)
│   │   │   │   ├── CreateCourseRqDto.java     # Запрос создания курса
│   │   │   │   ├── DashboardResponse.java     # Пагинированный ответ дашборда
│   │   │   │   ├── InviteLinkRsDto.java       # Пригласительная ссылка
│   │   │   │   ├── CreateInviteLinkRqDto.java # Запрос создания ссылки
│   │   │   │   ├── JoinCourseRqDto.java       # Запрос присоединения (token)
│   │   │   │   └── internal/
│   │   │   │       └── InviteUserDto.java     # Внутренний DTO приглашения
│   │   │   ├── entity/                        # JPA-сущности
│   │   │   │   ├── CourseEntity.java          # Таблица "courses"
│   │   │   │   ├── CourseEnrollmentEntity.java # Таблица "course_enrollments"
│   │   │   │   └── CourseInviteLinkEntity.java # Таблица "course_invite_links"
│   │   │   ├── enumz/                         # Перечисления
│   │   │   │   ├── CourseStatus.java          # ACTIVE, ARCHIVED
│   │   │   │   └── EnrollmentStatus.java      # INVITED, ACTIVE, SUSPENDED, BLOCKED, DROPPED, NO_ACCESS
│   │   │   └── repository/                    # Spring Data JPA репозитории
│   │   │       ├── CourseRepository.java
│   │   │       ├── CourseEnrollmentRepository.java
│   │   │       └── CourseInviteLinkRepository.java
│   │   │
│   │   └── service/
│   │       ├── ICourseService.java            # Интерфейс
│   │       └── CourseService.java             # Реализация всей бизнес-логики
│   │
│   ├── exeptions/
│   │   └── GlobalExceptionHandler.java        # @RestControllerAdvice
│   │
│   ├── integration/ (опечатка в пакете: "integretion")
│   │   ├── config/
│   │   │   ├── RestTemplateConfig.java        # Бин RestTemplate с интерсепторами
│   │   │   └── JwtRequestInterceptor.java     # Проброс JWT в исходящие запросы
│   │   └── users/
│   │       ├── IUserIntegrationService.java   # Интерфейс
│   │       └── UserIntegrationService.java    # Вызов user-service за профилями
│   │
│   ├── mapper/
│   │   └── CourseMapper.java                  # Enrollment + Profile → CourseMemberRsDto
│   │
│   └── minio/
│       ├── IMinioFileService.java             # Интерфейс
│       └── MinioFileService.java              # Загрузка/удаление/presigned URL
│
├── src/main/resources/
│   ├── application.yml                        # Основная конфигурация
│   ├── banner.txt                             # ASCII-баннер при старте
│   └── db/migration/                          # Flyway-миграции (V1–V7)
│
├── build.gradle                               # Зависимости и сборка
├── Dockerfile                                 # Образ для деплоя
├── .env                                       # Переменные окружения (локальные)
└── template.env                               # Шаблон .env
```

---

## REST API

Все эндпоинты требуют аутентификации (JWT в `Authorization: Bearer <token>`).

### Курсы пользователя (`/api/courses`)

| Метод  | Путь                                    | Описание                                | Query params                                                          |
|--------|-----------------------------------------|-----------------------------------------|-----------------------------------------------------------------------|
| `GET`  | `/api/courses/dashboard`                | Дашборд курсов (пагинация + сортировка) | `page_number` (0), `page_size` (12), `sort` (lastViewDesc, createdAt) |
| `POST` | `/api/courses/{course_id}/invite-links` | Создать пригласительную ссылку          | — (body: `CreateInviteLinkRqDto`)                                     |
| `GET`  | `/api/courses/{course_id}/invite-links` | Получить последнюю валидную ссылку      | —                                                                     |
| `POST` | `/api/courses/{courseId}/join`          | Присоединиться к курсу по ссылке        | — (body: `JoinCourseRqDto`)                                           |

### Управление курсом (`/api/course`)

| Метод  | Путь                                     | Описание                               | Permissions          | Body                            |
|--------|------------------------------------------|----------------------------------------|----------------------|---------------------------------|
| `POST` | `/api/course/create`                     | Создать курс (multipart)               | `COURSE_CREATE`      | title, description, image (opt) |
| `GET`  | `/api/course/{course_id}`                | Получить данные курса + свой статус    | —                    | —                               |
| `PUT`  | `/api/course/{course_id}`                | Обновить дату просмотра (`lastViewAt`) | —                    | —                               |
| `GET`  | `/api/course/{course_id}/members`        | Список участников с профилями          | —                    | —                               |
| `POST` | `/api/course/{course_id}/invite`         | Пригласить пользователя по userId      | `COURSE_INVITE_SEND` | id (userId)                     |
| `POST` | `/api/course/{course_id}/invite/confirm` | Принять приглашение (INVITED → ACTIVE) | —                    | —                               |

---

## Модель данных

### Сущности

| Сущность                 | Таблица               | Описание                                                                  |
|--------------------------|-----------------------|---------------------------------------------------------------------------|
| `CourseEntity`           | `courses`             | Курс: title, description, image_s3_key, владелец, статус, даты просмотра  |
| `CourseEnrollmentEntity` | `course_enrollments`  | Участие пользователя в курсе: userId, статус, кто пригласил, даты         |
| `CourseInviteLinkEntity` | `course_invite_links` | Пригласительная ссылка: UUID-токен, лимит использований, срок, активность |

**Связи:**

- `Course ↔ CourseEnrollment` (one-to-many)
- `Course ↔ CourseInviteLink` (one-to-many)
- `CourseEnrollment → Course` (many-to-one, lazy)

**Ключевые особенности:**

- `CourseEntity.id` генерируется в `@PrePersist` через `UUID.randomUUID()` (не через `@GeneratedValue`)
- `CourseEnrollment` имеет уникальный constraint на `(course_id, user_id)` — один пользователь = одна запись на курс
- `CourseInviteLinkEntity` поддерживает счётчик использований (`usesCount`) и лимит (`maxUses`)

---

## Статусы участников

| Статус      | Метка         | `hasFullAccess()` | `canView()` | Описание                                     |
|-------------|---------------|-------------------|-------------|----------------------------------------------|
| `INVITED`   | Приглашён     | false             | false       | Пользователь приглашён, но ещё не подтвердил |
| `ACTIVE`    | Активный      | true              | true        | Полноценный участник курса                   |
| `SUSPENDED` | Приостановлен | false             | true        | Доступ ограничен, но просмотр разрешён       |
| `BLOCKED`   | Заблокирован  | false             | false       | Доступ полностью заблокирован                |
| `DROPPED`   | Удалён        | false             | false       | Покинул курс                                 |
| `NO_ACCESS` | Нет доступа   | false             | false       | Специальный статус без доступа               |

---

## Архитектура и поток приглашений

### Прямое приглашение (по userId)

```
Владелец курса → POST /api/course/{id}/invite (userId)
    → CourseService.inviteUserToCourse()
    → Создание CourseEnrollment со статусом INVITED
    → Отправка уведомления (через внешний сервис, если подключён)

Приглашённый → POST /api/course/{id}/invite/confirm
    → CourseService.confirmInvite()
    → Статус INVITED → ACTIVE (если не BLOCKED/DROPPED/SUSPENDED)
```

### Приглашение по ссылке

```
Владелец → POST /api/courses/{id}/invite-links (maxUses, expiresDays)
    → Создание CourseInviteLinkEntity с UUID-токеном

Получатель ссылки → POST /api/courses/{id}/join (token)
    → CourseService.joinWithInvite()
    → Валидация: токен существует, активен, не просрочен, лимит не исчерпан
    → Инкремент usesCount
    → Upsert enrollment: если уже есть запись — переводит в ACTIVE, если нет — создаёт ACTIVE
```

### Дашборд

- **Для владельца курса (tutor):** курсы, где `ownerId == principal.id`
- **Для участника (student):** курсы через `course_enrollments` с любым статусом
- Пагинация + сортировка: по `lastViewAt DESC` (по умолчанию) или `createdAt DESC`

---

## Безопасность

### JWT-аутентификация

- Все эндпоинты требуют JWT в `Authorization: Bearer <token>`
- `JwtAuthFilter` извлекает userId, username, role, permissions из токена
- Авторизация на уровне методов через `@PreAuthorize`:
    - `COURSE_CREATE` — создание курса
    - `COURSE_INVITE_SEND` — отправка приглашений

### CORS

Разрешены запросы только с `${FRONT_ORIGIN}` (по умолчанию `http://localhost:3000`).

---

## Миграции БД

| Версия | Файл                           | Описание                                                          |
|--------|--------------------------------|-------------------------------------------------------------------|
| V1     | `V1__create_courses_table.sql` | Таблица `courses` с CHECK constraint на status                    |
| V2     | `V2__course_enrollments.sql`   | Таблица `course_enrollments`, UNIQUE(course_id, user_id), индексы |
| V3     | `V3__invited_by_column.sql`    | Колонка `invited_by UUID` — кто пригласил                         |
| V4     | `V4__add_new_status.sql`       | Добавление статуса `NO_ACCESS` в CHECK constraint                 |
| V5     | `V5__rename_image_column.sql`  | `image_url` → `image_s3_key`, VARCHAR(50)                         |
| V6     | `V6__course_invite_links.sql`  | Таблица `course_invite_links` с токенами и лимитами               |
| V7     | `V7__add_last_view_column.sql` | `last_view_at` в courses и course_enrollments                     |

Flyway применяется автоматически при старте. `ddl-auto=none`.

---

## Конфигурация

Основные переменные окружения (`.env`):

| Переменная                          | Описание                        | Значение по умолчанию                          |
|-------------------------------------|---------------------------------|------------------------------------------------|
| `URL_DB`                            | JDBC URL PostgreSQL             | —                                              |
| `USERNAME_DB` / `PASSWORD_DB`       | Учётка БД                       | —                                              |
| `DB_SCHEMA`                         | Schema PostgreSQL               | `public`                                       |
| `JWT_SECRET_KEY`                    | Секрет JWT (Base64)             | `ajscqSVPNj4GNzF+Ln2H6yaE2etWGExa618+TDP96ZE=` |
| `JWT_ACCESS_EXPIRATION_MS`          | TTL access токена               | `300000` (5 мин)                               |
| `JWT_REFRESH_EXPIRATION_MS`         | TTL refresh токена              | `604800000` (7 дней)                           |
| `MINIO_URL`                         | URL MinIO                       | `http://localhost:9000`                        |
| `USERNAME_MINIO` / `PASSWORD_MINIO` | Учётка MinIO                    | `minio` / `minio123`                           |
| `BUCKET_NAME`                       | Бакет для изображений курсов    | `avatar-images`                                |
| `FRONT_ORIGIN`                      | URL фронтенда для CORS          | `http://localhost:3000`                        |
| `USER_SERVICE_HOST`                 | URL user-service (auth-backend) | `http://localhost:8081`                        |

---

## Интеграция с user-service

Сервис обращается к **tutor-auth-backend** для получения профилей пользователей:

| Параметр           | Значение                                                                               |
|--------------------|----------------------------------------------------------------------------------------|
| **Хост**           | `${USER_SERVICE_HOST}` (по умолчанию `http://localhost:8081`)                          |
| **Endpoint**       | `POST /api/user/profiles`                                                              |
| **Тело запроса**   | `List<UUID>` — список идентификаторов пользователей                                    |
| **Ответ**          | `List<ProfileRsDto>` — id, email, firstName, lastName, avatarUrl                       |
| **Аутентификация** | JWT пробрасывается через `JwtRequestInterceptor` (автоматически из текущего контекста) |

**Где используется:**

- `GET /api/course/{id}/members` — получение списка участников с профилями
- `CourseMapper` — объединение `CourseEnrollmentEntity` + `ProfileRsDto` → `CourseMemberRsDto`

**Важно:** При недоступности user-service выбрасывается `PlatformException` с кодом `INTEGRATION_AUTH_SERVICE_ERROR`.

---

## Docker

```bash
./gradlew build
docker build -t tutor-courses .
docker run -p 8082:8082 --env-file .env tutor-courses
```

Образ: `eclipse-temurin:25-jdk`, порт `8082`.

---

## Swagger

- **Swagger UI:** http://localhost:8082/swagger-ui/index.html
- **OpenAPI YAML:** http://localhost:8082/teacher-portal/courses-service/api-docs.yaml

---

## Внутренние зависимости

Проект использует три внутренние библиотеки из приватного Maven-репозитория (`minio.razum0ff.ru`):

| Артефакт     | Версия | Предоставляет                                |
|--------------|--------|----------------------------------------------|
| `api-errors` | 1.0.5  | `ErrorCode`, `ApiError`, `PlatformException` |
| `base-utils` | 1.0.0  | Базовые утилиты                              |
| `jwt`        | 1.0.2  | `JwtUserPrincipal` (principal из JWT)        |

**Integration DTO:**

- `ProfileRsDto` — профиль пользователя из user-service (id, email, firstName, lastName, avatarUrl)
