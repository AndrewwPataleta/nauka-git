# API / Socket audit — сверка клиента с авторитетной спекой

Дата: 2026-08-11. Источник истины: GitLab wiki `nkt/backend/chat` (страница «Чат») + два OpenAPI-контракта, скачанные со стейджа и сохранённые в `docs/api/`:

- `docs/api/core-openapi.json` — core API (`https://stage.naukotheka.ru/api/core/api-docs`, 128 путей)
- `docs/api/chat-openapi.json` — chat API (`https://stage.naukotheka.ru/api/chat/json`, 61 путь)

Swagger UI: core — `…/api/core/swagger-ui/index.html`, chat — `…/api/chat/static/index.html`.

## Базовые URL (из wiki)

| Назначение | URL |
|---|---|
| REST чат | `https://stage.naukotheka.ru/api/chat/v1` (и `v2`) |
| REST core | `https://stage.naukotheka.ru/api/core` |
| socket.io | `https://stage.naukotheka.ru/api/chat/socket.io` |
| Flashphoner (звонки) | `https://stage.naukotheka.ru:8444/` |

Клиент (`SocketServiceImpl`) использует path `/api/chat/socket.io` — совпадает. ✓

## Socket.io — реальный словарь событий

**Проверено** (по wiki + по живому логу `SocketTraffic` на устройстве): socket.io несёт **только событие `message`**. Полезная нагрузка — объект сообщения (`id, dialog, cType, text, owner, files, action`). Системные действия приходят внутри `action.type`:

- `read` — статус прочтения (`action.messages`, `action.messageStatus`)
- `delete` — удаление (`action.messageId`)
- `update` — изменение (`action.messageId`)
- `changeChat` — изменение атрибутов диалога (`action.name/image/users`)

**Вывод по «печатает…» (typing):** такого события НЕТ — ни в wiki, ни в OpenAPI (`typing`/`печат`/`presence` отсутствуют), ни в живом логе. Индикатор печати потребует **контракта с бэкендом**: новое socket-событие (эмит клиентом + ретрансляция сервером участникам комнаты). Без него фичу на клиенте не сделать.

**Presence (онлайн/был в сети)** существует, но как REST, не как push:
- `POST /api/v1/users/status` — статус пользователя (`{ isOnline, lastSeen }`).
- В поиске пользователей и в глобальном поиске сообщений тоже приходит `status`.

## Данные чужого пользователя (для @упоминаний)

- `GET /api/core/user_profile/{userId}` — полный профиль любого пользователя (та же схема, что свой). **Заведён в клиенте** (`UserProfileApiService.getUserInfoById`), используется при тапе по @тегу. ✓
- `GET /api/chat/v1/users/{id}` — **устаревший** («Получение информации о пользователе (устарело)»). Не использовать, брать core. ✓ (у нас не используется)
- Канонический формат ссылки на профиль: `https://[stage.]naukotheka.ru/user/<UUID>` — бэк строит `linkPreview` (`og:type=person`). Возможный «правильный» формат упоминания вместо plain `@Имя`, если захотим серверные превью.

## Дрейф: наши Retrofit-пути ≠ спека (нужна проверка/правка)

| Наш вызов | Спека | Проблема |
|---|---|---|
| `PATCH chat/v1/dialogs/info/{dialogId}` | `PATCH /api/v1/dialogs/{dialog}` (body `DialogInfoDto`) | В спеке **нет** `dialogs/info/{id}` для PATCH. Редактирование диалога — по `/dialogs/{id}`. |
| `PATCH chat/v1/dialogs/folder/{folderId}` | `PATCH /api/v1/dialogs/folder` (body `UserDialogFolderUpdateDto`) | folderId должен идти **в теле**, не в пути. |
| `DELETE chat/v2/dialogs/chat/{dialogId}` | `DELETE /api/v1/dialogs/chat/{dialog}` | Мы шлём **v2**, спека — **v1**. |
| `PATCH chat/v2/dialogs/leave/{dialogId}` | `PATCH /api/v1/dialogs/leave/{dialog}` | v2 vs v1. |
| `POST chat/v2/dialogs/make-admin/{dialogId}` | `POST /api/v1/dialogs/make-admin/{dialog}` | v2 vs v1. |
| `PATCH chat/v2/dialogs/remove-admin/{dialogId}` | `PATCH /api/v1/dialogs/remove-admin/{dialog}` | v2 vs v1. |
| `PATCH chat/v2/dialogs/{dialogId}` | `PATCH /api/v1/dialogs/{dialog}` | v2 vs v1 (дублирует правку выше). |

**Важно:** в OpenAPI из v2 присутствует ТОЛЬКО `POST /api/v2/dialogs/create`. Остальные v2-пути (leave/make-admin/remove-admin/chat/{id}/dialogs/{id}) в контракте не описаны. Возможные объяснения: (а) сервер их поддерживает, но они не попали в swagger; (б) часть наших вызовов бьёт мимо и молча падает. **Действие:** проверить эмпирически на стейдже (редактирование группы, назначение/снятие админа, выход, удаление группы) с логом ответов; при 404 — перевести на пути из спеки.

## Пробелы: есть в спеке, у нас не используется

Клиентски-полезные (кандидаты на реализацию):

- `POST /api/v1/calls/invite/dialog/{dialog}` — пригласить в звонок (пригодится для «Отправить ссылку»/пригласить из шторки звонка).
- `GET /api/v1/calls/record/list/dialog/{dialog}` + `DELETE /api/v1/calls/record/{record}` — список/удаление записей звонка (для раздела «Записи»).
- `GET /api/v1/calls/dialog/{dialog}` / `…/participant` / `POST …/dialog/{dialog}/stop` / `GET /api/v1/calls/{id}` — доп. чтение/останов звонка по диалогу.
- `DELETE /api/v1/messages` (пакетное) + `DELETE /api/v1/messages/files` — множественное удаление и удаление файлов из сообщений.
- `GET /api/v1/dialogs/poll/{pollId}/stats` — статистика опроса (доступна всем; у нас только `answer-users`).

Служебные, клиенту не нужны: `POST /api/v1/wcs-hooks/roomApp/*` (webhooks Flashphoner→бэк), `PUT /api/v1/users/sync-user/{id}` (внутренняя синхронизация).

## Что уже совпадает и подтверждено ✓

Список диалогов, инфо диалога (+by-peer), медиа, поиск (search-messages, global/search), сообщения (update/read/pin/unpin/delete-one), папки (CRUD/reorder/set-read/dialogs), опросы (create/answer/stop/delete/get/answer-users), звонки (participants/status/permits/state/record start-stop/{id}/stop), профиль (core), загрузка файлов (`POST /api/core/files`, `?raw`), справочники cType (#109), fileType (#41), статусы прочтения (#71), роли (#37)/пермиты (#82).

## TODO по итогам аудита

1. Проверить v2-эндпоинты группового управления на стейдже; при 404 — перевести на v1 из спеки.
2. Починить `PATCH dialogs/info/{id}` → `PATCH dialogs/{id}` и `PATCH folder/{id}` → `PATCH folder` (folderId в body).
3. «Печатает…»: согласовать с бэкендом socket-событие (имя + формат) ИЛИ отложить.
4. Опционально: invite-to-call, список/удаление записей, пакетное удаление сообщений, poll stats.
