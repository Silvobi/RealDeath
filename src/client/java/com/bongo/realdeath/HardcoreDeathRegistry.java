package com.bongo.realdeath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelResource;

public final class HardcoreDeathRegistry {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("realdeath-hardcore-deaths.txt");
	private static final String LOCAL_MARKER_DIRECTORY = "realdeath";
	private static final String LOCAL_MARKER_FILE = "hardcore-death.txt";
	private static final String LOCAL_MARKER_CONTENT = """
		This file tells the RealDeath mod that the player has already died in this hardcore world.
		Delete it to reset RealDeath's rejoin screen for this save.
		""";

	private HardcoreDeathRegistry() {
	}

	public static boolean wasAlreadyDeadAndMark(final Minecraft minecraft) {
		IntegratedServer integratedServer = minecraft.getSingleplayerServer();
		if (integratedServer != null) {
			return wasAlreadyDeadInLocalWorld(minecraft, integratedServer);
		}

		String key = currentServerKey(minecraft);
		Set<String> deaths = readEntries();
		boolean recordedDeath = deaths.contains(key);
		if (!recordedDeath) {
			deaths.add(key);
			writeEntries(deaths);
		}
		return recordedDeath;
	}

	private static boolean wasAlreadyDeadInLocalWorld(final Minecraft minecraft, final IntegratedServer integratedServer) {
		Path world = integratedServer.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
		Path marker = world.resolve(LOCAL_MARKER_DIRECTORY).resolve(LOCAL_MARKER_FILE);
		boolean recordedDeath = Files.isRegularFile(marker);

		if (!recordedDeath) {
			try {
				Files.createDirectories(marker.getParent());
				Files.writeString(marker, LOCAL_MARKER_CONTENT, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
			} catch (IOException ignored) {
				// A read-only save should not prevent the death screen from working.
			}
		}

		removeLegacyLocalEntry(minecraft, world);
		return recordedDeath;
	}

	private static void removeLegacyLocalEntry(final Minecraft minecraft, final Path world) {
		if (Files.notExists(FILE)) {
			return;
		}

		String legacyKey = "world|" + minecraft.getUser().getProfileId() + "|" + world;
		Set<String> deaths = readEntries();
		if (deaths.remove(legacyKey)) {
			writeEntries(deaths);
		}
	}

	private static String currentServerKey(final Minecraft minecraft) {
		String player = minecraft.getUser().getProfileId().toString();
		ServerData server = minecraft.getCurrentServer();
		return "server|" + player + "|" + (server == null ? "unknown" : server.ip);
	}

	private static Set<String> readEntries() {
		try {
			if (Files.notExists(FILE)) {
				return new HashSet<>();
			}
			return new HashSet<>(Files.readAllLines(FILE, StandardCharsets.UTF_8));
		} catch (IOException exception) {
			return new HashSet<>();
		}
	}

	private static void writeEntries(final Set<String> entries) {
		try {
			Files.createDirectories(FILE.getParent());
			Files.write(
				FILE,
				entries.stream().sorted().toList(),
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException ignored) {
			// A read-only config directory should not prevent the death screen from working.
		}
	}
}
