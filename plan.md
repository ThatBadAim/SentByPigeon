1. **Update Models and Room Entity for Sync State:**
   - Modify `EncryptionStatus` in `Models.kt` or introduce `SyncState` enum if it's cleaner (but the instruction implies adding states like `PENDING, SENT, DELIVERED, READ, FAILED`, which feels like `SyncState` distinct from `EncryptionStatus` but the context says `syncState: PENDING, SENT, DELIVERED, READ, FAILED`).
   - Actually, wait, `Models.kt` has `EncryptionStatus` which has `PENDING`. Let's create `enum class SyncState { PENDING, SENT, DELIVERED, READ, FAILED }`.
   - Add `syncState` property to `MessageEntity` in `Entities.kt`.
   - Update `Converters.kt` to handle `SyncState`.
   - Update `MessageDao` queries and methods to handle `syncState` if needed (e.g. `getPendingMessages()`). Actually it currently queries `encryptionStatus = 'PENDING'`.

2. **Update AppDatabase version and Auto-Migrations:**
   - Increase `AppDatabase` version to `2`.
   - Add `@AutoMigration(from = 1, to = 2)` to `@Database`.
   - Ensure the `defaultValue` for `syncState` is set to `'PENDING'` or `'SENT'` with single quotes if required for room columns.

3. **MessageRepositoryImpl Optimistic Updates:**
   - In `MessageRepositoryImpl`, modify `sendTextMessage` (and others) to insert the message with `syncState = SyncState.PENDING` first.
   - Then, attempt to send it via `webSocketManager`.
   - If sending fails, update the message locally to `SyncState.FAILED`.
   - If sending succeeds, update it to `SyncState.SENT`.
   - (The repository will also be updated by WebSocket incoming events for DELIVERED / READ later, but we focus on immediate optimistic send attempts).

4. **MessageSyncWorker implementation:**
   - Implement `MessageSyncWorker` that runs periodically or on connectivity change to retry sending pending messages.
   - It should fetch all messages with `syncState == PENDING` or `FAILED` and attempt to re-send them using `webSocketManager` or a direct HTTP fallback if necessary, but using the Repository/DAO and network manager is good.

5. **Pre-commit step:**
   - Run tests and verifications using `pre_commit_instructions`.

6. **Submit:**
   - Push to branch.
