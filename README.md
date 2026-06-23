# RealDeath

RealDeath is a client-side Fabric mod for Minecraft 26.2. On death, it immediately cuts all sound and replaces the world with a pitch-black screen. After three seconds, the normal death interface fades in over two seconds while the background stays black.

On the first death in hardcore mode, the only interface shown is a **Give Up** button. It exits without deleting the world. Rejoining that dead world shows a faster-fading prompt with **Observe** and **Move On**: Observe requests the vanilla hardcore spectator respawn, while Move On leaves the world or server.

The bundled `death-ambient.ogg` fades in with each death interface.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or newer
- Fabric API 0.153.0+26.2
- Java 25

## Build

```powershell
$env:JAVA_HOME = 'C:\path\to\java-25'
.\gradlew.bat build
```

The distributable JAR is written to `build/libs/realdeath-1.0.0.jar`.
