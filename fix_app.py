import re

with open("app/src/main/java/com/hybrid/messaging/MessagingApp.kt", "r") as f:
    content = f.read()

content = content.replace(
    'import dagger.hilt.android.HiltAndroidApp\n',
    'import androidx.hilt.work.HiltWorkerFactory\nimport androidx.work.Configuration\nimport dagger.hilt.android.HiltAndroidApp\nimport javax.inject.Inject\n'
)

content = content.replace(
    'class MessagingApp : Application() {\n',
    'class MessagingApp : Application(), Configuration.Provider {\n\n    @Inject lateinit var workerFactory: HiltWorkerFactory\n\n    override val workManagerConfiguration: Configuration\n        get() = Configuration.Builder()\n            .setWorkerFactory(workerFactory)\n            .build()\n\n'
)

with open("app/src/main/java/com/hybrid/messaging/MessagingApp.kt", "w") as f:
    f.write(content)
