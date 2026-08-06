import re

file_path = "./app/build.gradle.kts"
with open(file_path, "r") as f:
    content = f.read()

# Make sure we add biometric dependency since they've created a file that uses it
if "libs.androidx.biometric" not in content:
    content = re.sub(
        r"(implementation\(libs\.androidx\.material\.icons\.extended\))",
        r"\1\n    implementation(libs.androidx.biometric)",
        content
    )
    with open(file_path, "w") as f:
        f.write(content)

toml_path = "./gradle/libs.versions.toml"
with open(toml_path, "r") as f:
    toml_content = f.read()

if "androidx-biometric" not in toml_content:
    toml_content = toml_content.replace(
        "[versions]",
        "[versions]\nbiometric = \"1.1.0\""
    )
    toml_content = toml_content.replace(
        "[libraries]",
        "[libraries]\nandroidx-biometric = { group = \"androidx.biometric\", name = \"biometric\", version.ref = \"biometric\" }"
    )
    with open(toml_path, "w") as f:
        f.write(toml_content)
