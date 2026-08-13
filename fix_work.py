import re

with open("gradle/libs.versions.toml", "r") as f:
    content = f.read()

content = content.replace('[versions]\n', '[versions]\nwork = "2.9.0"\nhiltWork = "1.2.0"\n')
content = content.replace('[libraries]\n', '[libraries]\nandroidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }\nandroidx-hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltWork" }\nandroidx-hilt-compiler = { group = "androidx.hilt", name = "hilt-compiler", version.ref = "hiltWork" }\n')

with open("gradle/libs.versions.toml", "w") as f:
    f.write(content)

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace('    implementation(libs.hilt.navigation.compose)\n', '    implementation(libs.hilt.navigation.compose)\n    implementation(libs.androidx.work.runtime.ktx)\n    implementation(libs.androidx.hilt.work)\n    kapt(libs.androidx.hilt.compiler)\n')

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
