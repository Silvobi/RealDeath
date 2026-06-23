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

	private HardcoreDeathRegistry() {
	}

	public static boolean wasAlreadyDeadAndMark(final Minecraft minecraft, final boolean deathScreenShownDuringJoin) {
		String key = currentWorldKey(minecraft);
		Set<String> deaths = readEntries();
		boolean recordedDeath = deaths.contains(key);
		if (!recordedDeath) {
			deaths.add(key);
			writeEntries(deaths);
		}
		return recordedDeath || deathScreenShownDuringJoin;
	}

	private static String currentWorldKey(final Minecraft minecraft) {
		String player = minecraft.getUser().getProfileId().toString();
		IntegratedServer integratedServer = minecraft.getSingleplayerServer();
		if (integratedServer != null) {
			Path world = integratedServer.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
			return "world|" + player + "|" + world;
		}

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
