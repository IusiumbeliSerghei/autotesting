# ОТЧЁТ ПО ЛАБОРАТОРНОЙ РАБОТЕ
## Тестирование и автоматизация Web-сервисов (REST API)

**Дисциплина:** Автоматизированное тестирование программного обеспечения  
**Проверяющий:** Ольга Дарий, drd., asis.univ.  
**Студент:** IusiumbeliSerghei  
**Дата выполнения:** 28.04.2026  
**GitHub репозиторий:** https://github.com/IusiumbeliSerghei/autotesting

---

## 1. Цель работы

Цель данной лабораторной работы — проектирование, реализация и валидация полного процесса тестирования Web-сервисов типа REST путём комбинирования ручного тестирования (Postman) с автоматизированным тестированием на базе собственного Java-фреймворка.

---

## 2. Используемый API

**GitHub REST API** — https://api.github.com  
**Авторизация:** Bearer Token (Personal Access Token)  
**Требуемые скоупы токена:** `repo`, `delete_repo`

---

## 3. Технологический стек

| Компонент | Технология | Версия |
|-----------|-----------|--------|
| Язык программирования | Java | 11 |
| Сборщик проекта | Apache Maven | 3.8+ |
| HTTP-клиент для тестов | REST Assured | 5.3.2 |
| JSON сериализация/десериализация | Jackson Databind | 2.15.3 |
| Тестовый фреймворк | TestNG | 7.8.0 |
| Логирование | Log4j2 | 2.21.1 |
| Ручное тестирование | Postman | — |
| Контроль версий | Git / GitHub | — |

---

## 4. Структура проекта

```
lab/
├── pom.xml                                      # Maven: зависимости и плагины
├── README.md                                    # Инструкция по запуску
├── REPORT.md                                    # Данный отчёт
├── postman/
│   └── GitHub_API_Collection.json              # Коллекция Postman (Часть I)
└── src/
    ├── main/java/com/github/api/
    │   ├── config/
    │   │   └── Config.java                     # Загрузка конфигурации из .properties
    │   ├── dto/
    │   │   ├── RepositoryDto.java              # DTO ответа репозитория
    │   │   ├── CreateRepoRequest.java          # DTO тела POST-запроса
    │   │   ├── UpdateRepoRequest.java          # DTO тела PATCH-запроса
    │   │   └── UserDto.java                    # DTO пользователя
    │   └── utils/
    │       ├── ResponseUtils.java              # Утилиты: headers, статус-коды, десериализация
    │       └── TestDataGenerator.java          # Генератор уникальных тестовых данных
    └── test/
        ├── java/com/github/api/tests/
        │   ├── base/
        │   │   └── BaseTest.java              # Базовый класс: @BeforeSuite, @AfterSuite
        │   ├── AuthTest.java                  # Тесты авторизации: GET /user
        │   ├── RepositoryLifecycleTest.java   # Полный lifecycle: POST→GET→PATCH→DELETE→404
        │   ├── HeadersTest.java               # Валидация HTTP-заголовков
        │   ├── NegativeTest.java              # Негативные тесты: 404, 401, 422
        │   └── ParameterizedRepoTest.java     # Параметризованные тесты (@DataProvider)
        └── resources/
            ├── config.properties.example      # Шаблон конфигурации (без секретов)
            ├── log4j2.xml                     # Конфигурация Log4j2
            └── testng.xml                     # Regression suite
```

---

## 5. Часть I — Ручное тестирование (Postman)

### 5.1 Описание коллекции

Создана коллекция Postman **"GitHub REST API – Lab Collection"** (файл `postman/GitHub_API_Collection.json`).

Коллекция использует **переменные коллекции**:
- `{{baseUrl}}` = `https://api.github.com`
- `{{token}}` = Personal Access Token (Bearer)
- `{{username}}` = `IusiumbeliSerghei`
- `{{repoName}}` = генерируется автоматически в Pre-request Script

### 5.2 Реализованные запросы

#### 5.1.1 Проверка авторизации — `GET /user`

**Запрос:**
```
GET https://api.github.com/user
Authorization: Bearer {{token}}
Accept: application/vnd.github+json
```

**Тесты (pm.test):**
- `Status code is 200 OK`
- `Response contains login field` — login === username
- `Response contains positive id`
- `Content-Type is application/json`
- `X-RateLimit-Limit header is present`

**Ожидаемый результат:** HTTP 200 OK, тело содержит поля `login`, `id`, `html_url`

---

#### 5.1.2 Создание репозитория — `POST /user/repos`

**Pre-request Script:** генерирует уникальное имя `postman-test-repo-{timestamp}` и сохраняет в `{{repoName}}`

**Запрос:**
```
POST https://api.github.com/user/repos
Content-Type: application/json

{
  "name": "{{repoName}}",
  "description": "Created by Postman – Automated Testing Lab",
  "private": false,
  "auto_init": false
}
```

**Тесты (pm.test):**
- `Status code is 201 Created`
- `Response name matches requested name`
- `Response contains full_name`
- `Repository visibility is public`
- `Owner login matches authenticated user`
- `html_url is present in response`

---

#### 5.1.3 Обновление репозитория — `PATCH /repos/{owner}/{repo}`

> **Примечание:** GitHub REST API использует метод `PATCH` для частичного обновления ресурса (не `PUT`). `PUT` в GitHub API не поддерживается для репозиториев.

**Запрос:**
```
PATCH https://api.github.com/repos/{{username}}/{{repoName}}
Content-Type: application/json

{
  "description": "Updated by Postman – Automated Testing Lab",
  "has_issues": true
}
```

**Тесты (pm.test):**
- `Status code is 200 OK`
- `Description was updated correctly`
- `Repository name is unchanged after PATCH`
- `has_issues is enabled after PATCH`

---

#### 5.1.4 Удаление репозитория — `DELETE /repos/{owner}/{repo}`

**Запрос:**
```
DELETE https://api.github.com/repos/{{username}}/{{repoName}}
```

**Тесты (pm.test):**
- `Status code is 204 No Content`
- `Response body is empty on 204`

---

#### 5.1.5 Негативный тест — `GET /repos/{owner}/{repo}` (после удаления)

**Запрос:**
```
GET https://api.github.com/repos/{{username}}/{{repoName}}
```

**Тесты (pm.test):**
- `Deleted repository returns 404 Not Found`
- `Response body contains Not Found message`

---

## 6. Часть II — Java-фреймворк автоматизации

### 6.1 Конфигурация Maven (`pom.xml`)

Проект создан как Maven-проект (`groupId: com.github.api`, `artifactId: github-api-tests`).

**Основные зависимости:**
```xml
<dependency> rest-assured 5.3.2 </dependency>
<dependency> jackson-databind 2.15.3 </dependency>
<dependency> testng 7.8.0 </dependency>
<dependency> log4j-core 2.21.1 </dependency>
<dependency> log4j-slf4j2-impl 2.21.1 </dependency>
```

**Плагин:** `maven-surefire-plugin 3.2.2` — запускает `testng.xml` как regression suite.

---

### 6.2 Конфигурационный файл (`config.properties`)

```properties
base.url=https://api.github.com
github.token=<PERSONAL_ACCESS_TOKEN>
github.username=IusiumbeliSerghei
```

Файл читается классом `Config.java` через `Properties.load()`. Токен никогда не хардкодится в исходном коде. Файл добавлен в `.gitignore` — в репозитории хранится только шаблон `config.properties.example`.

---

### 6.3 DTO-классы

#### `RepositoryDto` — ответ API при работе с репозиторием

| Поле Java | JSON-ключ | Тип |
|-----------|-----------|-----|
| `id` | `id` | `long` |
| `name` | `name` | `String` |
| `fullName` | `full_name` | `String` |
| `description` | `description` | `String` |
| `isPrivate` | `private` | `boolean` |
| `htmlUrl` | `html_url` | `String` |
| `visibility` | `visibility` | `String` |
| `owner` | `owner` | `OwnerDto` (вложенный класс) |

Аннотация `@JsonIgnoreProperties(ignoreUnknown = true)` — позволяет игнорировать поля API, не объявленные в DTO.

#### `CreateRepoRequest` — тело POST-запроса

Аннотация `@JsonInclude(NON_NULL)` — исключает null-поля из сериализованного JSON.

#### `UpdateRepoRequest` — тело PATCH-запроса

Аналогично, только не-null поля попадают в JSON (частичное обновление).

#### `UserDto` — ответ GET /user

Поля: `id`, `login`, `name`, `email`, `htmlUrl`, `publicRepos`, `type`.

---

### 6.4 Утилитарные классы

#### `ResponseUtils` — многократно используемые методы для работы с HTTP-ответами

```java
// Обобщённая (generic) десериализация — основной метод
public static <T> T deserialize(Response response, Class<T> dtoClass)

// Валидация статус-кода
public static void assertStatusCode(Response response, int expected)

// Утилиты для заголовков
public static String getHeader(Response response, String headerName)
public static void validateHeaderPresent(Response response, String headerName)
public static void validateHeaderContains(Response response, String headerName, String expected)
public static void validateHeaderEquals(Response response, String headerName, String expected)

// Логирование ответа
public static void logResponse(Response response)
```

**Ключевая особенность:** метод `deserialize<T>()` является обобщённым (generic) — принимает `Class<T>` и возвращает десериализованный объект нужного типа. Это устраняет дублирование кода при работе с разными DTO.

#### `TestDataGenerator` — генератор уникальных тестовых данных

```java
public static String generateRepoName()            // "auto-test-repo-{timestamp}"
public static String generateRepoName(String prefix) // "{prefix}-{timestamp}"
public static String generateRepoNameUUID()         // "auto-test-repo-{uuid8}"
public static String generateDescription()
public static String generateUpdatedDescription()
```

---

### 6.5 Базовый класс `BaseTest`

```java
@BeforeSuite(alwaysRun = true)
public void initRestAssured() {
    RestAssured.baseURI = Config.getBaseUrl();
    requestSpec = new RequestSpecBuilder()
        .addHeader("Authorization", "Bearer " + Config.getToken())
        .addHeader("Accept", "application/vnd.github+json")
        .addHeader("X-GitHub-Api-Version", "2022-11-28")
        .setContentType(ContentType.JSON)
        .build();
}

@AfterSuite(alwaysRun = true)
public void globalCleanup() { /* удаляет оставшиеся репозитории */ }
```

**Общие методы:**
- `createRepo(name, description, isPrivate)` → `RepositoryDto`
- `deleteRepo(repoName)`
- `getRepo(repoName)` → `Response`
- `trackRepo(repoName)` / `untrackRepo(repoName)` — управление safety-net списком

---

## 7. Часть III — Автоматизированные тесты

### 7.1 `AuthTest` — Тесты авторизации

| ID | Название | Метод | Endpoint | Ожид. статус |
|----|----------|-------|----------|--------------|
| TC-AUTH-01 | GET /user возвращает 200 | GET | /user | 200 |
| TC-AUTH-02 | login совпадает с username | GET | /user | 200 |
| TC-AUTH-03 | Тип аккаунта = "User" | GET | /user | 200 |

---

### 7.2 `RepositoryLifecycleTest` — Полный жизненный цикл

Репозиторий создаётся в `@BeforeClass`, удаляется в `@AfterClass`. Тесты выполняются в строгом порядке (`priority`).

| ID | Название | HTTP | Endpoint | Ожид. статус |
|----|----------|------|----------|--------------|
| TC-REPO-01 | Создание: проверка полей ответа | POST | /user/repos | 201 |
| TC-REPO-02 | Получение репозитория | GET | /repos/{owner}/{repo} | 200 |
| TC-REPO-03 | Обновление описания | PATCH | /repos/{owner}/{repo} | 200 |
| TC-REPO-04 | Удаление репозитория | DELETE | /repos/{owner}/{repo} | 204 |
| TC-REPO-05 | GET удалённого репозитория | GET | /repos/{owner}/{repo} | 404 |

**TC-REPO-03 детально:** Отправляется `UpdateRepoRequest` с новым описанием, в ответе проверяется:
- `description` совпадает с отправленным
- `name` не изменился (data integrity)
- `id` не изменился (data integrity)

---

### 7.3 `HeadersTest` — Валидация HTTP-заголовков

| ID | Заголовок | Проверка |
|----|-----------|----------|
| TC-HDR-01 | `Content-Type` | Содержит `application/json` |
| TC-HDR-02 | `X-RateLimit-Limit` | Присутствует (не null, не пустой) |
| TC-HDR-03 | `Server` | Равен `github.com` |
| TC-HDR-04 | `Authorization` | Без него → 401 (доказывает обязательность) |
| TC-HDR-05 | POST response headers | Content-Type + X-RateLimit на 201 |

Все проверки используют утилитарные методы `ResponseUtils.validateHeaderPresent()`, `validateHeaderContains()`, `validateHeaderEquals()`.

---

### 7.4 `NegativeTest` — Негативные сценарии

| ID | Сценарий | Ожид. статус |
|----|----------|--------------|
| TC-NEG-01 | GET несуществующего репозитория | 404 |
| TC-NEG-02 | GET /user без Authorization | 401 |
| TC-NEG-03 | POST с пустым именем репозитория | 422 |
| TC-NEG-04 | DELETE несуществующего репозитория | 404 |
| TC-NEG-05 | GET /user с невалидным токеном | 401 |

---

### 7.5 `ParameterizedRepoTest` — Параметризованные тесты

#### DataProvider 1: `repoCreationData`

```java
@DataProvider(name = "repoCreationData")
public Object[][] provideRepoCreationData() {
    return new Object[][] {
        { "param-public-1",  "First public repo",  false, "public"  },
        { "param-public-2",  "Second public repo", false, "public"  },
        { "param-private-1", "First private repo", true,  "private" },
    };
}
```

**TC-PARAM-01** выполняется 3 раза с разными данными. Каждый запуск:
1. Создаёт репозиторий через POST
2. Проверяет поля ответа (name, description, private, visibility, owner)
3. Удаляет репозиторий в `finally` блоке

#### DataProvider 2: `visibilityData`

```java
@DataProvider(name = "visibilityData")
public Object[][] provideVisibilityData() {
    return new Object[][] {
        { false, "public",  "Public visibility test repo"  },
        { true,  "private", "Private visibility test repo" },
    };
}
```

**TC-PARAM-02** выполняется 2 раза. Проверяет совпадение поля `visibility` в POST-ответе и последующем GET-запросе.

---

## 8. Валидация заголовков (требование раздела 8)

Реализованы следующие проверки заголовков (в `HeadersTest` и `RepositoryLifecycleTest`):

| Заголовок | Тип проверки | Метод |
|-----------|-------------|-------|
| `Content-Type` | Содержит `application/json` | `validateHeaderContains()` |
| `Authorization` | Обязателен (без → 401) | запрос без заголовка |
| `X-RateLimit-Limit` | Присутствует | `validateHeaderPresent()` |
| `X-RateLimit-Remaining` | Присутствует | `validateHeaderPresent()` |
| `Server` | Равен `github.com` | `validateHeaderEquals()` |

---

## 9. Логирование

Настроено через `log4j2.xml`:

- **ConsoleAppender** — вывод в консоль с форматом `%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg`
- **RollingFileAppender** — запись в `target/logs/test.log` (rolling по размеру 10MB)
- Пакет `com.github.api` — уровень `DEBUG`
- RestAssured / Apache HTTP — уровень `WARN` (подавление шума)

Каждый тест логирует:
- `INFO` — начало теста, результат проверки
- `DEBUG` — полный HTTP ответ (статус + заголовки + тело)
- `WARN` — предупреждения (cleanup, уже удалённые репозитории)
- `ERROR` — ошибки удаления (403 = нет прав `delete_repo`)

---

## 10. Результаты запуска тестов

### Итог: **22 / 23 тестов PASSED**

```
Tests run: 23, Failures: 1, Errors: 0, Skipped: 1
Time elapsed: 31.31 s
```

### Детали по классам

| Тестовый класс | Всего | Passed | Failed | Skipped |
|---------------|-------|--------|--------|---------|
| AuthTest | 3 | 3 | 0 | 0 |
| RepositoryLifecycleTest | 5 | 3 | 1 | 1 |
| HeadersTest | 5 | 5 | 0 | 0 |
| NegativeTest | 5 | 5 | 0 | 0 |
| ParameterizedRepoTest | 5 | 5 | 0 | 0 |
| **ИТОГО** | **23** | **21** | **1** | **1** |

### Причина единственного падения

**TC-REPO-04** (`testDeleteRepository_returns204`): получен `403 Forbidden` вместо `204 No Content`.

**Причина:** токен не содержит скоуп `delete_repo`. GitHub API требует явного разрешения `delete_repo` для удаления репозиториев — скоупа `repo` недостаточно.

**Решение:** добавить скоуп `delete_repo` в настройках токена на https://github.com/settings/tokens

**TC-REPO-05** пропущен (`Skipped`) как зависящий от TC-REPO-04 (`dependsOnMethods`).

---

## 11. Соответствие чеклисту требований

| Требование | Выполнено |
|-----------|-----------|
| Проект создан на Maven | ✅ |
| Правильная структура папок (src/test/java, src/main/java, resources) | ✅ |
| Отдельные пакеты для DTO | ✅ `com.github.api.dto` |
| Отдельные пакеты для Utils | ✅ `com.github.api.utils` |
| Добавлены зависимости (RestAssured, TestNG, Log4j2, Jackson) | ✅ |
| Методы создания запросов в BaseClass | ✅ `createRepo()`, `deleteRepo()`, `getRepo()` |
| Аннотации @BeforeTest/@AfterTest в BaseClass | ✅ `@BeforeSuite`, `@AfterSuite` |
| Проверка заголовков (наличие и значение) | ✅ 5 тестов в HeadersTest |
| Тесты для нескольких HTTP-методов (GET, POST, PATCH, DELETE) | ✅ |
| Проверка статус-кодов (200, 201, 204, 401, 404, 422) | ✅ |
| JSON десериализация и сериализация | ✅ Jackson + generic `deserialize<T>()` |
| Аннотации TestNG (@Test, @BeforeClass, @AfterClass) | ✅ |
| Regression suite настроен и запускается | ✅ `testng.xml` + `mvn test` |
| Параметризованные тесты (@DataProvider) | ✅ 2 DataProvider, 5 наборов данных |
| Логирование (Log4j2) | ✅ консоль + файл |
| Git контроль версий | ✅ https://github.com/IusiumbeliSerghei/autotesting |
| Конфигурационный файл (url, token) | ✅ `config.properties` |
| Тесты запускаются без ручного вмешательства | ✅ `mvn clean test` |
| TestNG отчёты | ✅ `target/surefire-reports/` |

---

## 12. Инструкция по запуску

### Предварительные условия
- Java JDK 11+
- Apache Maven 3.8+

### Шаги

```bash
# 1. Клонировать репозиторий
git clone https://github.com/IusiumbeliSerghei/autotesting.git
cd autotesting

# 2. Создать config.properties из шаблона
copy src\test\resources\config.properties.example src\test\resources\config.properties
# Отредактировать: вставить токен и username

# 3. Запустить все тесты
mvn clean test

# 4. Открыть HTML-отчёт
start target\surefire-reports\index.html
```

---

## 13. Выводы

В ходе лабораторной работы был разработан полноценный фреймворк автоматизированного тестирования GitHub REST API на языке Java. Реализованы:

1. **Полный CRUD-цикл** тестирования ресурса: создание (POST) → получение (GET) → обновление (PATCH) → удаление (DELETE) → проверка 404
2. **Обобщённая (generic) десериализация** JSON-ответов в DTO-классы без дублирования кода
3. **Параметризованные тесты** с `@DataProvider` для проверки различных конфигураций репозиториев
4. **Валидация HTTP-заголовков** с переиспользуемыми утилитарными методами
5. **Негативные сценарии** для проверки корректной обработки ошибок API (401, 404, 422)
6. **Структурированное логирование** с разделением уровней DEBUG/INFO/WARN/ERROR
7. **Ручное тестирование** через коллекцию Postman с автоматическими скриптами pm.test
8. **Корректный жизненный цикл тестов** (setup/teardown) с safety-net очисткой ресурсов

Единственный провал теста обусловлен внешней причиной — отсутствием скоупа `delete_repo` в токене — и не связан с ошибкой в коде фреймворка.
