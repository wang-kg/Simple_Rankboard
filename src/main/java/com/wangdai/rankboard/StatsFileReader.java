package com.wangdai.rankboard;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

final class StatsFileReader {
    private static final Gson GSON = new Gson();

    private static final String STATS = "stats";
    private static final String MINED = "minecraft:mined";
    private static final String USED = "minecraft:used";
    private static final String CUSTOM = "minecraft:custom";
    private static final String DEATHS = "minecraft:deaths";

    private final MinecraftServer server;

    StatsFileReader(MinecraftServer server) {
        this.server = server;
    }

    List<PlayerStatsSnapshot> readAll() {
        Path statsDir = server.getSavePath(WorldSavePath.STATS);
        if (!Files.isDirectory(statsDir)) {
            return List.of();
        }

        List<PlayerStatsSnapshot> snapshots = new ArrayList<>();
        try (Stream<Path> files = Files.list(statsDir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .forEach(path -> read(path).ifPresent(snapshots::add));
        } catch (IOException e) {
            RankBoardMod.LOGGER.warn("Failed to list player stats in {}", statsDir, e);
        }
        RankBoardMod.LOGGER.info("RankBoard loaded {} player stats from {}", snapshots.size(), statsDir);
        return snapshots;
    }

    private Optional<PlayerStatsSnapshot> read(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null || !root.has(STATS) || !root.get(STATS).isJsonObject()) {
                return Optional.empty();
            }

            JsonObject stats = root.getAsJsonObject(STATS);
            String name = resolveName(path);
            long placed = sumPlacedBlocks(stats.getAsJsonObject(USED));
            long mined = sumValues(stats.getAsJsonObject(MINED));
            long moved = sumMovementMeters(stats.getAsJsonObject(CUSTOM));
            long deaths = getLong(stats.getAsJsonObject(CUSTOM), DEATHS);
            return Optional.of(new PlayerStatsSnapshot(name, placed, mined, moved, deaths));
        } catch (IOException | RuntimeException e) {
            RankBoardMod.LOGGER.warn("Failed to read stats file {}", path, e);
            return Optional.empty();
        }
    }

    private String resolveName(Path path) {
        String fileName = path.getFileName().toString();
        String uuidText = fileName.substring(0, fileName.length() - ".json".length());
        try {
            UUID uuid = UUID.fromString(uuidText);
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (player.getUuid().equals(uuid)) {
                    return player.getGameProfile().name();
                }
            }
            var profile = server.getApiServices().nameToIdCache().getByUuid(uuid);
            if (profile.isPresent()) {
                return profile.get().name();
            }
        } catch (IllegalArgumentException ignored) {
            return uuidText;
        }
        return uuidText.substring(0, Math.min(8, uuidText.length()));
    }

    private static long sumPlacedBlocks(JsonObject usedStats) {
        if (usedStats == null) {
            return 0L;
        }

        long total = 0L;
        for (var entry : usedStats.entrySet()) {
            Identifier id = parseId(entry.getKey());
            if (id == null) {
                continue;
            }

            Item item = Registries.ITEM.get(id);
            if (item instanceof BlockItem) {
                total += asLong(entry.getValue());
            }
        }
        return total;
    }

    private static long sumMovementMeters(JsonObject customStats) {
        if (customStats == null) {
            return 0L;
        }

        long centimeters = 0L;
        for (var entry : customStats.entrySet()) {
            if (entry.getKey().endsWith("_one_cm")) {
                centimeters += asLong(entry.getValue());
            }
        }
        return centimeters / 100L;
    }

    private static long sumValues(JsonObject object) {
        if (object == null) {
            return 0L;
        }

        long total = 0L;
        for (JsonElement value : object.asMap().values()) {
            total += asLong(value);
        }
        return total;
    }

    private static long getLong(JsonObject object, String key) {
        if (object == null || !object.has(key)) {
            return 0L;
        }
        return asLong(object.get(key));
    }

    private static long asLong(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return 0L;
        }
        try {
            return element.getAsLong();
        } catch (RuntimeException ignored) {
            return 0L;
        }
    }

    private static Identifier parseId(String id) {
        return Identifier.tryParse(id);
    }
}
