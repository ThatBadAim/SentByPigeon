import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Replace javaCompileOptions with kapt
new_content = re.sub(
    r'        javaCompileOptions \{\s*annotationProcessorOptions \{\s*arguments\["room\.schemaLocation"\] = "\$projectDir/schemas"\s*\}\s*\}',
    r'''        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }''',
    content
)

with open("app/build.gradle.kts", "w") as f:
    f.write(new_content)
