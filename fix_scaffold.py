import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/feature/main/ui/MainScaffold.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("import androidx.compose.ui.Modifier", "import androidx.compose.ui.Modifier\nimport androidx.compose.ui.unit.dp")

with open(filepath, "w") as f:
    f.write(content)
