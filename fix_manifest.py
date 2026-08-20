import sys

manifest_file = "Messaging Service/app/src/main/AndroidManifest.xml"
with open(manifest_file, "r") as f:
    content = f.read()

content = content.replace('android:dataExtractionRules="@xml/data_extraction_rules"', '')
content = content.replace('android:fullBackupContent="@xml/backup_rules"', '')
content = content.replace('android:icon="@mipmap/ic_launcher"', '')
content = content.replace('android:roundIcon="@mipmap/ic_launcher_round"', '')
content = content.replace('android:theme="@style/Theme.MessagingService"', '')

with open(manifest_file, "w") as f:
    f.write(content)
