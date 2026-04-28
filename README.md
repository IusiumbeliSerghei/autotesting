# GitHub REST API – Automated Test Framework

**Студент:** IusiumbeliSerghei | **API:** https://api.github.com  
**Репозиторий:** https://github.com/IusiumbeliSerghei/autotesting

---

## Требования

- Java JDK 11+
- Apache Maven 3.8+

---

## Быстрый старт

### Конфигурация уже настроена

Файл `src/test/resources/config.properties` содержит:

```properties
base.url=https://api.github.com
github.token=ghp_5tLw************************AYEW
github.username=IusiumbeliSerghei
```

> Полный токен уже вписан в локальный файл `src/test/resources/config.properties`.  
> Файл исключён из Git (`.gitignore`) — запускайте `mvn clean test` без изменений.

---

### Запустить тесты

```bash
mvn clean test
```

---

### Открыть отчёт

```
target\surefire-reports\index.html   # HTML-отчёт TestNG
target\logs\test.log                 # Полный лог выполнения
```

---

## Запуск отдельных классов

```bash
mvn test -Dtest=AuthTest
mvn test -Dtest=RepositoryLifecycleTest
mvn test -Dtest=HeadersTest
mvn test -Dtest=NegativeTest
mvn test -Dtest=ParameterizedRepoTest
```

---

## Postman

Импортировать `postman/GitHub_API_Collection.json` в Postman.  
В переменных коллекции установить `token` и `username`, затем **Run collection**.
