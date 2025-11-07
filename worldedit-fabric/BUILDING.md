# Building FastAsyncWorldEdit for Fabric

These instructions walk you through compiling the Fabric edition of FastAsyncWorldEdit (FAWE) from source. They target the latest supported Minecraft version and Fabric API listed in the repository.

## Prerequisites

Before you start, install the following tools:

* **Git** – for cloning the repository.
* **Java Development Kit (JDK) 17 or newer** – required by modern Minecraft versions and Fabric Loom.
  * We recommend [Temurin 17](https://adoptium.net/temurin/releases/?version=17) or another distribution that provides `java` and `javac` on your `PATH`.
* **Gradle** – not required manually. The project bundles the Gradle Wrapper (`gradlew`) that downloads the correct Gradle version automatically.

You can verify your Java installation by running:

```bash
java -version
```

On Windows, replace `java` with the full path to your JDK if it is not on the `PATH` yet.

## Clone the repository

```bash
git clone https://github.com/IntellectualSites/FastAsyncWorldEdit.git
cd FastAsyncWorldEdit
```

If you already have the repository, update it to the latest code:

```bash
git pull --rebase
```

## Build the Fabric mod

Run the Gradle wrapper from the project root to build the Fabric module:

```bash
./gradlew :worldedit-fabric:build
```

On Windows PowerShell or Command Prompt, use:

```powershell
.\gradlew.bat :worldedit-fabric:build
```

This task compiles the code, runs the checks, and remaps the output jar for distribution. When it finishes, you can find the mod jar at:

```
worldedit-fabric/build/libs/fastasyncworldedit-fabric-<version>.jar
```

## Run FAWE in a Fabric development environment (optional)

If you want to launch a Fabric test server with FAWE loaded:

```bash
./gradlew :worldedit-fabric:runServer
```

The first run downloads the Minecraft server and other dependencies, which may take several minutes. Once the server window appears, you can connect from a Fabric client that targets the same Minecraft version.

## Updating mappings or dependencies

To move FAWE to a newer Minecraft or Fabric API release:

1. Open `worldedit-fabric/build.gradle.kts`.
2. Update the Fabric loader, API, and Minecraft coordinates referenced in the `dependencies` block (they are the entries that start with `libs.fabric.*`).
3. If needed, adjust the corresponding version numbers in `gradle/libs.versions.toml` so the catalog points to the desired releases.
4. Re-run the build command to fetch the new dependencies.

Refer to the [Fabric Wiki](https://fabricmc.net/wiki/tutorial:setup) and the official FAWE documentation for guidance on preparing PRs that target new versions.

## Troubleshooting

* **Gradle cannot find Java 17** – ensure `JAVA_HOME` points to a JDK 17 installation and that `java -version` reports version 17 or newer.
* **Build fails due to missing dependencies** – rerun the build; Gradle caches downloads under `~/.gradle`. A transient network issue might have interrupted the download.
* **`runServer` fails on macOS** – grant execute permissions to the Gradle wrapper if necessary: `chmod +x gradlew`.

If you encounter other issues, visit the [FastAsyncWorldEdit Discord](https://discord.gg/intellectualsites) for community support.
