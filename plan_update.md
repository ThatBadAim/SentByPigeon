1. **Fix FTS Join Query**:
   Since `MessageEntity.id` is a UUID String and `Fts4(contentEntity = MessageEntity::class)` links via integer `rowid`, joining `messages.id = messages_fts.rowid` is incorrect. Room creates an implicit `rowid` (int) for `MessageEntity` automatically. Therefore, the FTS query in `MessageDao` should join on `messages.rowid = messages_fts.rowid`. Wait, if Room auto-generates `rowid`, can I just use `messages.rowid` in the query? Yes! `JOIN messages_fts ON messages.rowid = messages_fts.rowid`.
2. **Implement Search in Repository & ViewModel**:
   - Add `searchMessages(query: String)` to `MessageRepository` and its implementation.
   - Create `SearchViewModel.kt` to handle the search logic, filtering, and expose state for `SearchScreen`.
3. **Wire SearchScreen**:
   - Update `SearchScreen` to take `SearchViewModel` and use its state. Wire up the `onSearch` action to trigger the ViewModel's search.
4. **Implement jump-to-message**:
   - Pass the message ID along with room ID when an item is clicked. Update `ChatViewModel` or `ChatScreen` logic to focus the specific message (this might involve scrolling in `LazyColumn`). Since `ChatViewModel` only has `loadRoom`, I might need to add a state for `focusedMessageId`.
5. **Run tests & Check pre commit**.
