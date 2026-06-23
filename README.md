# RealDeath - die in an impactful way (in game ofc)

RealDeath is a client-side mod that changes the way of what you see after dying in game for a dramatic effect. Following text hidden under spoiler reveals what actually happens after death. I suggest you trying the mod out first tho, there are no screamers, jumpscares or anything like that istg.
<details>
<summary>What happens after death? SPOILER</summary>

After death the screen goes pitch black, then death screen slowly fades in with windy ambient noise fading in simultaneously.

</details>

## Compatibility

RealDeath is a client-side Fabric mod. It does not need to be installed on the server.

| RealDeath | Minecraft | Fabric Loader | Dependensies            | Java |
|-----------|-----------|---------------|-------------------------|------|
| 1.0.x     | 26.2      | 0.19.3+       | Fabric API 0.153.0+26.2 | 25   |

Backports soon!

# NOTICE

The code was ENTIRELY written by AI. I have ZERO knowledge about how to write code and build minecraft mods.
The assets packaged with mod are entirely made by me without any AI help.

<details>
<summary>SPOILER</summary>

For example `death-ambient.ogg` was made in FL Studio. I generated noise using 3x Osc VST Plugin and put some effect over it.

</details>

## Build (I'm not entirely sure what's that for but looks important. codex left that, propably for building the mod from source code)

```powershell
$env:JAVA_HOME = 'C:\path\to\java-25'
.\gradlew.bat build
```

The distributable JAR is written to `build/libs/realdeath-1.0.0.jar`.

