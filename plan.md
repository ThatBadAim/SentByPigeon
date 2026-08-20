1. **Implement Android FTS4/FTS5 in `AppDatabase.kt`**:
   - Create `MessageFtsEntity.kt` in `core/database/entity/`. Annotate it with `@Entity(tableName = "messages_fts")` and `@Fts4(contentEntity = MessageEntity::class)`. Include search fields (e.g., `content`, `senderName`, `roomId`, etc.).
   - Add `MessageFtsEntity::class` to `AppDatabase` entities array.
   - Modify `Daos.kt` (`MessageDao` or a new `SearchDao`) to include a search query.

2. **Create Global Search UI (`SearchScreen.kt`)**:
   - Create `SearchScreen.kt` in a new feature package (e.g., `feature/search/ui/`).
   - The UI should have a `SearchBar` / `TextField` for query text.
   - It should have filters for specific user, chat room, server, and file type (Media, Links, Documents).
   - Display list of `SearchResultItem` containing the matched content.

3. **Integrate Search UI in `MainScaffold.kt`**:
   - Add a "Search" tab/icon or accessible button from `MainScaffold.kt`. Let's maybe add a top app bar with a search icon to navigate to `SearchScreen`, or integrate it directly inside the views, or simply add a `NavigationTab.SEARCH` and display `SearchScreen`. The instructions say "Create a global Search UI accessible from `MainScaffold.kt`".

4. **Highlight text matching & Jump-to-message**:
   - Ensure the query string is passed to `SearchScreen` items so we can use `AnnotatedString` to highlight the matches in the UI.
   - Handle clicks on search result items to navigate to the timeline (open specific chat room and focus message).

5. **Complete pre-commit steps**.
6. **Submit**.
