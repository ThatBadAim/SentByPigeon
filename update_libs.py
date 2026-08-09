import sys

def main():
    lines = open("gradle/libs.versions.toml").read().splitlines()
    with open("gradle/libs.versions.toml", "w") as f:
        for line in lines:
            if line.startswith("[libraries]"):
                f.write('work = "2.9.0"\n')
                f.write('hiltWork = "1.2.0"\n')
                f.write(line + "\n")
                f.write('androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "work" }\n')
                f.write('androidx-hilt-work = { group = "androidx.hilt", name = "hilt-work", version.ref = "hiltWork" }\n')
            else:
                f.write(line + "\n")

if __name__ == "__main__":
    main()
