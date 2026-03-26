# Наукотека (Naukoteka) - Android

Социальная образовательная платформа с чатом, аудио/видео звонками и профилями пользователей.

## Архитектура

**Multi-module Gradle проект:**
- `app/` - UI, ViewModels, Services, DI
- `domain/` - бизнес-логика (чистый Kotlin, без Android-зависимостей)
- `data/` - репозитории, API, кэши
- `core/` - общие утилиты

**Паттерны:**
- MVP (Moxy) - старые экраны (профиль, сфера, навигация)
- MVVM + Jetpack Compose - новые экраны (чат, звонки)
- Repository pattern в `data/` модуле

**DI:** Hilt (основной) + Toothpick (легаси, кастомные скоупы). Идет миграция на Hilt.

## Ключевые модули

### Чат (`mvvm/chat/`, `ui/chat/`)
- Socket.IO для real-time сообщений (`SocketService` / `SocketServiceImpl` - синглтон)
- `ChatListViewModel` - список диалогов
- `ChatDialogViewModel` - экран диалога с сообщениями
- Папки, опросы, пересылка, групповые чаты

### Звонки (`mvvm/call/`, `ui/call/`, `flashphoner/`)
- Flashphoner WCS (WebRTC) для медиа-потоков
- `CallViewModel` - управление звонком (Flashphoner RoomApi)
- `IncomingCallViewModel` - обработка входящих звонков через сокет
- `IncomingCallSocketService` - foreground service для фонового приема звонков/сообщений
- cType: 2 = аудио, 3 = видео, 6 = завершение, 2001-2008 и 6001-6004 = управление
- WCS URL: `wss://stage.naukotheka.ru:8443`
- Полная документация: `docs/calls.md`

### Push-уведомления (`services/`)
- `NaukotekaPushService` - Firebase Cloud Messaging
- `IncomingCallSocketService` - foreground service с Socket.IO для фоновых событий

## Сокет-архитектура

**Важно:** `SocketServiceImpl` поддерживает **множественные слушатели** через тегированную систему:
```kotlin
socketService.setOnEvent("message", "MyTag") { data -> ... }
socketService.removeEvent("message", "MyTag")
```
Каждый потребитель ОБЯЗАН использовать уникальный тег. Без тега слушатель регистрируется с `__default__` тегом.

Активные слушатели `"message"`:
- `IncomingCallSocketService` - фоновые нотификации (звонки + сообщения)
- `IncomingCallViewModel` - навигация на экран звонка
- `ChatDialogViewModel` - обновление сообщений в открытом диалоге

## Навигация

- AndroidX Navigation с XML nav graphs: `nav_graph_chat`, `nav_graph_profile`, `nav_graph_sphere`
- Cicerone для старых экранов (авторизация, регистрация)
- Bottom Navigation: Сфера, Чат, Профиль

## Стек

- Kotlin, Jetpack Compose + View Binding
- Hilt 2.55, Toothpick 3.1.0
- Retrofit 2.9 + OkHttp + Gson
- RxJava 2 + Coroutines
- Socket.IO Client 2.1.2
- Flashphoner WCS SDK (локальный .aar)
- Firebase (Analytics, Crashlytics, Messaging, RemoteConfig)
- Glide + Coil (Compose)

## Сборка

- `compileSdk` / `targetSdk`: 35
- Build variants: `debug` (stage), `release` (stage), `prod` (production)
- Stage API: `https://stage.naukotheka.ru/api/`
- Prod API: `https://naukotheka.ru/api/`

## Команды

```bash
./gradlew assembleDebug          # Debug сборка (stage)
./gradlew assembleRelease        # Release сборка (stage)
./gradlew assembleProd           # Production сборка
```
