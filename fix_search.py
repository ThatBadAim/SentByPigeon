import sys

filepath = "Messaging Service/app/src/main/java/com/hybrid/messaging/feature/search/ui/SearchScreen.kt"
with open(filepath, "r") as f:
    content = f.read()

content = content.replace("Icons.AutoMirrored.Filled.ArrowBack", "Icons.Default.ArrowBack")

with open(filepath, "w") as f:
    f.write(content)
