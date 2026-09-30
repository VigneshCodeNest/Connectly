# Connectly Backend Architecture & REST/WebSocket API

Enterprise-grade Spring Boot 3 & MySQL backend built for the **Connectly Messaging Platform**.

---

## 📌 Architecture Overview

Connectly backend follows a clean, decoupled, layered enterprise architecture:

```text
[ React Frontend ] (HTTP / WebSocket)
        ↓
[ Security & CORS Layer ] (Spring Security, JWT Filters)
        ↓
[ Controller Layer ] (REST API & STOMP WebSocket Endpoints)
        ↓
[ Service Layer ] (Business Logic, Authorization & Transactions)
        ↓
[ Repository Layer ] (Spring Data JPA & Specifications)
        ↓
[ MySQL Database ] (Normalized Relational Schema)
```

---

## 🛠 Tech Stack

* **Language**: Java 21 (LTS)
* **Framework**: Spring Boot 3.2.x
* **Security**: Spring Security + JWT (JSON Web Tokens) + BCrypt
* **Persistence**: Spring Data JPA / Hibernate (ORM)
* **Database**: MySQL 8.0 (with H2 in-memory profile for isolated unit/integration tests)
* **Real-time**: Spring WebSocket with STOMP message broker
* **Build Tool**: Maven 3.9+
* **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)

---

## 🗄️ Normalized Database Schema (Phase 1)

The backend implements 13 core relational entities mapped to MySQL:

```text
┌──────────────┐       ┌──────────────────────┐       ┌──────────────┐
│    users     │◀─────▶│  connection_requests │◀─────▶│    users     │
└──────────────┘       └──────────────────────┘       └──────────────┘
       ▲
       │ 1:N
       ├─────────────────────────────────────────┐
       ▼                                         ▼
┌──────────────┐                          ┌──────────────┐
│   statuses   │                          │blocked_users │
└──────────────┘                          └──────────────┘
       │
       ▼ 1:N
┌──────────────┐
│ status_views │
└──────────────┘

┌──────────────┐       ┌──────────────────────┐       ┌──────────────┐
│    chats     │◀─────▶│  chat_participants   │◀─────▶│    users     │
└──────────────┘       └──────────────────────┘       └──────────────┘
       ▲                                                 ▲
       │ 1:1                                             │
       ▼                                                 │
┌──────────────┐                                         │
│ groups_table │◀────────────────────────────────────────┤
└──────────────┘                                         │
       ▲                                                 │
       │ 1:N                                             │
       ▼                                                 │
┌──────────────┐                                         │
│group_members │◀────────────────────────────────────────┘
└──────────────┘

┌──────────────┐       ┌──────────────────────┐       ┌──────────────┐
│    chats     │◀─────▶│       messages       │◀─────▶│    users     │
└──────────────┘       └──────────────────────┘       └──────────────┘
                               ▲      ▲
                               │      │
                      1:N ┌────┘      └────┐ 1:N
                          ▼                ▼
             ┌─────────────────────┐  ┌───────────────────┐
             │ message_attachments │  │ starred_messages  │
             └─────────────────────┘  └───────────────────┘
```

### Core Entities Summary

1. **`User` (`users`)**: User identity, email, username, BCrypt password hash, avatar URL, bio, accent color, online status, privacy flags (`last_seen_privacy`, `profile_photo_privacy`).
2. **`Chat` (`chats`)**: Container for direct 1-to-1 conversations and multi-user groups.
3. **`ChatParticipant` (`chat_participants`)**: Join table mapping users to chats with unique composite constraint (`chat_id`, `user_id`).
4. **`Message` (`messages`)**: Message records supporting `TEXT`, `IMAGE`, `VIDEO`, `STICKER`, with statuses (`SENT`, `DELIVERED`, `READ`), reply threads (`reply_to_id`), and forwarding flags.
5. **`MessageAttachment` (`message_attachments`)**: Media metadata, file size, MIME type, and storage URI.
6. **`Group` (`groups_table`)**: Group chat metadata, title, description, and group owner reference.
7. **`GroupMember` (`group_members`)**: Group membership with roles (`ADMIN`, `MEMBER`).
8. **`ConnectionRequest` (`connection_requests`)**: Friend connection state machine (`PENDING`, `ACCEPTED`, `REJECTED`) enforcing server-side authorization before messaging.
9. **`Status` (`statuses`)**: Ephemeral stories (Text / Image) with automatic 24-hour expiration (`expires_at`).
10. **`StatusView` (`status_views`)**: Real-time receipt tracking of who viewed which status.
11. **`StarredMessage` (`starred_messages`)**: User-specific starred/bookmarked messages.
12. **`ChatNotificationSetting` (`chat_notification_settings`)**: Per-chat mute settings (`is_muted`, `muted_until`).
13. **`BlockedUser` (`blocked_users`)**: User blocklist preventing communication.

---

## ⚙️ Configuration & Environment Variables

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `SERVER_PORT` | `8080` | Port for Spring Boot HTTP & WebSocket |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/connectly_db?...` | MySQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Database password |
| `JWT_SECRET` | `404E6352...` | 256-bit secret key for signing JWTs |
| `UPLOAD_DIR` | `./uploads` | Local media storage directory |

---

## 🚀 Running the Backend

### Prerequisites
* Java 21 LTS (`java -version`)
* MySQL Server 8.0 running on `localhost:3306`
* Maven 3.9+

### Build & Run Tests
```bash
# Run all unit and integration tests (using test profile)
mvn test

# Package the application JAR
mvn clean package -DskipTests
```

### Start the Application
```bash
mvn spring-boot:run
```

Once started, the backend is accessible at:
* REST API: `http://localhost:8080/api/v1`
* Health Check: `http://localhost:8080/api/v1/health`

---

## 📡 Real-Time WebSocket & STOMP Architecture (Phase 6)

Connectly provides full-duplex, low-latency messaging powered by Spring WebSocket and STOMP message broker:

### Endpoints & Handshake
* **STOMP Endpoint**: `/ws` (supports native WebSocket & SockJS fallback)
* **Application Destination Prefix**: `/app`
* **Broker Destination Prefixes**: `/topic`, `/queue`, `/user`
* **User Destination Prefix**: `/user`

### WebSocket Security & Destination Authorization
* **Handshake / CONNECT**: Client provides JWT token in STOMP headers: `Authorization: Bearer <jwt_token>` (or `token: <jwt_token>`).
* **Inbound Channel Interceptor** (`WebSocketAuthInterceptor`): Authenticates `UserPrincipal` on connection.
* **Destination Access Control**:
  * `/topic/chat.{chatId}`: The interceptor verifies that the authenticated user is a verified participant of `chatId`. Unauthorized subscriptions are immediately rejected.
  * `/topic/presence`: Authenticated contacts receive real-time presence events (`ONLINE` / `OFFLINE` with last seen).

### Real-Time Message Events & Receipts
| STOMP Channel / Mapping | Direction | Payload | Description |
| :--- | :--- | :--- | :--- |
| `/app/chat.sendMessage` | Client → Server | `WsChatMessagePayload` | Send message in real-time, persists to MySQL with `SENT` state and broadcasts to `/topic/chat.{chatId}` |
| `/app/chat.markDelivered` | Client → Server | `WsStatusUpdatePayload` | Mark message(s) as `DELIVERED`, updates DB and broadcasts delivery receipt |
| `/app/chat.markRead` | Client → Server | `WsStatusUpdatePayload` | Mark chat messages as `READ`, updates DB and broadcasts read receipt |
| `/app/chat.typing` | Client → Server | `WsTypingPayload` | Broadcasts typing indicator to chat participants |
| `/topic/chat.{chatId}` | Server → Client | `WsEvent<MessageResponse>` / `WsEvent<WsStatusUpdatePayload>` | Real-time stream of incoming messages and status receipts |
| `/topic/presence` | Server → Client | `WsEvent<WsPresencePayload>` | Online/Offline status changes |

---

## 🖼️ Media & Stickers Architecture (Phase 7)

Connectly supports rich media communication including images, videos, and stickers with secure storage and streaming:

### Storage & Security
* **Abstract Storage Interface** (`StorageService`): Decouples local filesystem storage from cloud object stores (S3, GCS).
* **Directory Isolation**: Automatically isolates media into categorized paths (`/images`, `/videos`, `/stickers`, `/documents`, `/avatars`).
* **Path Traversal Protection**: Rejects file names containing `..` or attempting to escape target storage roots.
* **Executable File Blocking**: Strictly forbids `.exe`, `.bat`, `.sh`, `.php`, `.js`, `.py`, `.dll`, `.scr`, `.msi`, `.bin`, etc.
* **MIME & Size Validation**:
  * **Images**: JPG, JPEG, PNG, GIF, WEBP, SVG, BMP (Max 10MB)
  * **Videos**: MP4, WEBM, MOV, MKV, 3GP, AVI (Max 50MB)
  * **Stickers**: PNG, WEBP, GIF, SVG (Max 5MB)
  * **Documents**: PDF, audio files, general attachments (Max 25MB)

### Media & Sticker Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/media/upload` | Bearer JWT | Secure multipart upload with MIME & size validation, returns metadata & `fileUrl` |
| `GET` | `/api/v1/media/{category}/{filename}` | Public | Stream media content with inline `Content-Disposition` and cache headers |
| `GET` | `/api/v1/stickers` | Public | List available sticker packs (Quick Reactions, Cute Animals, etc.) |

---

## 💬 Advanced Messaging Features (Phase 8)

Connectly provides full support for message replies, multi-destination forwarding, soft deletes, and starred messages:

### Key Rules & Behaviors
* **Replying**: A message can reference another message (`replyToId`) strictly within the **same conversation**. Cross-chat replies are validated and rejected.
* **Forwarding**: Users can forward messages to multiple connected recipients or authorized groups. Forwards preserve original media attachments, mark `isForwarded = true`, and broadcast in real-time.
* **Soft Delete**: Senders can soft-delete their own messages. Unauthorized deletion attempts by other users are forbidden (`UnauthorizedException`). Deletes trigger a real-time `MESSAGE_DELETED` WebSocket event.
* **Starred Messages**: Users can star and unstar messages in chats they participate in. A dedicated endpoint retrieves all bookmarked messages.

### Message Feature Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/chats/{chatId}/messages/{messageId}/forward` | Bearer JWT | Forward message to target chat IDs or user IDs |
| `DELETE` | `/api/v1/chats/{chatId}/messages/{messageId}` | Bearer JWT | Soft-delete message (sender-only authorization) |
| `POST` | `/api/v1/chats/{chatId}/messages/{messageId}/star` | Bearer JWT | Star / bookmark a message in a chat |
| `DELETE` | `/api/v1/chats/{chatId}/messages/{messageId}/star` | Bearer JWT | Unstar a message |
| `GET` | `/api/v1/messages/starred` (or `/api/v1/chats/starred`) | Bearer JWT | Retrieve all starred messages for the current user |

---

## 👥 Group Messaging & Roles Architecture (Phase 9)

Connectly supports rich group conversations with role-based member administration and real-time broadcasting:

### Roles & Access Control
* **Roles**: `ADMIN`, `MEMBER`.
* **Creator / Admin Privileges**:
  * Create groups with name, description, avatar, and initial members.
  * Add new members (`POST /api/v1/groups/{groupId}/members`).
  * Remove members (`DELETE /api/v1/groups/{groupId}/members/{userId}`).
  * Update group information & settings (`PUT /api/v1/groups/{groupId}`).
  * Promote / demote member roles (`PUT /api/v1/groups/{groupId}/members/{userId}/role`).
* **Member Capabilities**:
  * Send group messages (text, image, video, sticker).
  * Access group message history.
  * Leave group (`POST /api/v1/groups/{groupId}/leave`).
* **Non-Member Restrictions**:
  * Cannot access group details or message history.
  * Cannot send messages to group.
  * WebSocket destination authorization blocks unauthorized subscriptions to group topics.

### Group Management Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/groups` | Bearer JWT | Create new group (creator becomes `ADMIN`) |
| `GET` | `/api/v1/groups` | Bearer JWT | Retrieve all groups the current user belongs to |
| `GET` | `/api/v1/groups/{groupId}` | Bearer JWT | Get group details, settings, and member summary |
| `PUT` | `/api/v1/groups/{groupId}` | Bearer JWT | Update group name, description, avatar (**Admin only**) |
| `GET` | `/api/v1/groups/{groupId}/members` | Bearer JWT | List all group members and roles |
| `POST` | `/api/v1/groups/{groupId}/members` | Bearer JWT | Add members to group (**Admin only**) |
| `DELETE` | `/api/v1/groups/{groupId}/members/{userId}` | Bearer JWT | Remove member (**Admin only** or self-leave) |
| `POST` | `/api/v1/groups/{groupId}/leave` | Bearer JWT | Leave group |
| `PUT` | `/api/v1/groups/{groupId}/members/{userId}/role` | Bearer JWT | Change member role (`ADMIN` / `MEMBER`) (**Admin only**) |

---

---

## 📸 Ephemeral Status Stories (Phase 10)

Connectly provides an ephemeral 24-hour status system adhering to privacy and ownership boundaries:

### Status Features & Rules
* **Supported Status Types**: `TEXT` (with custom background color and formatted text) and `IMAGE` (with secure image URL and optional caption).
* **Excluded Types**: `VIDEO`, `AUDIO`, and `MUSIC` are explicitly disallowed and rejected at validation layer with `400 Bad Request`.
* **24-Hour Expiration Lifecycle**: Every created status is automatically assigned an expiration timestamp `expiresAt = createdAt + 24 Hours`. Expired statuses are hidden from feeds and view queries, and automatically purged via `@Scheduled` background tasks.
* **Feed & Privacy**: Status feeds aggregate active statuses from connected contacts and the authenticated user. Non-connected users cannot access private statuses.
* **View Tracking Receipts**: When a user views a contact's status, a `StatusView` entry is recorded with timestamp. Self-views are excluded from inflating view receipts.
* **Ownership & Security Enforcement**:
  * Only the status author/owner can view the viewer list (`GET /api/v1/statuses/{id}/viewers`). Non-owners receive `401 Unauthorized`.
  * Only the status author/owner can delete a status (`DELETE /api/v1/statuses/{id}`). Non-owners receive `401 Unauthorized`.

### Status REST Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/statuses` | Bearer JWT | Create new `TEXT` or `IMAGE` status (expires in 24h) |
| `GET` | `/api/v1/statuses/feed` | Bearer JWT | Retrieve active status feed from connected contacts |
| `GET` | `/api/v1/statuses/my` | Bearer JWT | Retrieve current user's active statuses with viewers |
| `GET` | `/api/v1/statuses/{id}` | Bearer JWT | Get active status details by ID |
| `POST` | `/api/v1/statuses/{id}/view` | Bearer JWT | Record view receipt on status |
| `GET` | `/api/v1/statuses/{id}/viewers` | Bearer JWT | List users who viewed status (**Owner only**) |
| `DELETE` | `/api/v1/statuses/{id}` | Bearer JWT | Delete own status (**Owner only**) |

---

## ⚙️ Chat Preferences, Notification Muting & User Blocking (Phase 11)

Connectly provides granular per-user chat preferences and backend-enforced communication blocking:

### Key Features & Rules
* **Per-User Chat Muting**: Muting is strictly isolated to `User + Chat`. User A muting a conversation has zero effect on User B.
* **Mute Duration Options**:
  * `1 Hour` (`ONE_HOUR`)
  * `8 Hours` (`EIGHT_HOURS`)
  * `1 Week` (`ONE_WEEK`)
  * `Until manually unmuted` / `Indefinitely` (`UNTIL_MANUALLY_UNMUTED`)
* **Starred Messages**: Pin/bookmark important messages in conversations with dedicated retrieval endpoint.
* **Backend Communication Blocking**:
  * Self-blocking is blocked (`400 Bad Request`).
  * If User A blocks User B (or vice versa), private messaging (`sendMessage`), private chat creation, and connection requests are immediately rejected at the backend.
  * Status feed automatically filters out ephemeral stories from blocked users.
  * Unblocking immediately restores normal communication eligibility.

### Chat Preferences & Blocking REST Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/chats/{chatId}/mute` | Bearer JWT | Mute chat notifications with specified duration |
| `POST` | `/api/v1/chats/{chatId}/unmute` | Bearer JWT | Unmute chat notifications |
| `DELETE` | `/api/v1/chats/{chatId}/mute` | Bearer JWT | Unmute chat notifications (RESTful alias) |
| `GET` | `/api/v1/chats/{chatId}/preferences` | Bearer JWT | Retrieve chat preferences (isMuted, mutedUntil, isBlocked) |
| `POST` | `/api/v1/users/{userId}/block` | Bearer JWT | Block a user |
| `DELETE` | `/api/v1/users/{userId}/block` | Bearer JWT | Unblock a user |
| `GET` | `/api/v1/users/blocked` | Bearer JWT | List all blocked users |
| `GET` | `/api/v1/users/{userId}/block-status` | Bearer JWT | Check bidirectional block status |

---

## 🔒 Change Password & Account Settings (Phase 12)

Connectly provides secure password rotation and account/privacy configuration:

### Password Security & Validation Rules
* **Authentication**: Requires valid Bearer JWT.
* **Current Password Verification**: Verified against the BCrypt hash stored in the database. Incorrect current passwords reject with `400 Bad Request`.
* **Confirmation Verification**: New password and confirmation must match exactly.
* **Complexity Requirements**:
  * Minimum 8 characters.
  * At least one uppercase letter (`[A-Z]`).
  * At least one lowercase letter (`[a-z]`).
  * At least one numeric digit (`[0-9]`).
  * At least one special character (`[^A-Za-z0-9]`).
* **BCrypt Hashing**: Passwords are never stored in plaintext and never returned in API payloads.

### Password & Settings Endpoints
| HTTP Method | Endpoint | Auth | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/users/change-password` | Bearer JWT | Change password with current password verification |
| `PUT` | `/api/v1/users/me/password` | Bearer JWT | Change password (RESTful alias) |
| `PUT` | `/api/v1/users/me/privacy` | Bearer JWT | Update privacy settings (`lastSeenPrivacy`, `profilePhotoPrivacy`) |
| `PUT` | `/api/v1/users/me/settings` | Bearer JWT | Update account settings (name, bio, colorAccent, privacy) |

---

## 🗺️ Incremental Development Roadmap

* [x] **Phase 1**: Backend foundation, layered structure, normalized JPA entities, repositories, MySQL schema, DTOs, global exception handling, and health endpoints.
* [x] **Phase 2**: Authentication & Security (Registration, Login, JWT Provider, BCrypt, Auth Filter).
* [x] **Phase 3**: User profile management & user search by email.
* [x] **Phase 4**: Connections system & request authorization pipeline.
* [x] **Phase 5**: Private chat & conversation lifecycle.
* [x] **Phase 6**: Real-time messaging with WebSocket / STOMP & receipt state machine.
* [x] **Phase 7**: Media upload, validation, and sticker support.
* [x] **Phase 8**: Advanced message features (Reply, Forward, Delete, Star, Status updates).
* [x] **Phase 9**: Group chat & membership roles.
* [x] **Phase 10**: Ephemeral Status stories (24h expiration & views).
* [x] **Phase 11**: Chat preferences, mute notifications, and user blocking.
* [x] **Phase 12**: Change password & account settings.
* [ ] **Phase 13**: React frontend integration with live REST/WS endpoints.
* [ ] **Phase 14**: Security hardening & end-to-end integration testing.
* [ ] **Phase 15**: Production optimization & containerization.
