import re

with open("app/src/main/java/com/hybrid/messaging/core/database/AppDatabase.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import androidx.room.Database\n',
    'import androidx.room.Database\nimport androidx.room.AutoMigration\n'
)

content = content.replace(
    '    version = 1,\n',
    '    version = 2,\n    autoMigrations = [\n        AutoMigration(from = 1, to = 2)\n    ],\n'
)

with open("app/src/main/java/com/hybrid/messaging/core/database/AppDatabase.kt", "w") as f:
    f.write(content)
