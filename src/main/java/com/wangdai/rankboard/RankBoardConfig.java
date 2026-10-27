package com.wangdai.rankboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

final class RankBoardConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> FILTER_MODES = Set.of("off", "blacklist", "whitelist");

    int entriesPerPage = 6;
    int pageSeconds = 10;
    int refreshSeconds = 30;
    boolean compactNumbers = false;
    boolean includeZeroValues = false;
    boolean enabled = true;
    String filterMode = "blacklist";
    List<String> players = new ArrayList<>();
    String placedTitle = "放置榜";
    String minedTitle = "挖掘榜";
    String movedTitle = "移动榜";
    String deathsTitle = "死亡榜";
    String titleTemplate = "§6{title} §e{page}/{pages}";
    String nameTemplate = "§f{rank}. {name}";
    String valueTemplate = "§7{value}";
    String emptyText = "§7暂无数据";

    static RankBoardConfig load() {
        Path path = configPath();
        if (!Files.exists(path)) {
            RankBoardConfig config = new RankBoardConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            RankBoardConfig config = GSON.fromJson(reader, RankBoardConfig.class);
            return config == null ? new RankBoardConfig() : config.sanitize();
        } catch (IOException | RuntimeException e) {
            RankBoardMod.LOGGER.warn("Failed to load RankBoard config, using defaults", e);
            return new RankBoardConfig();
        }
    }

    void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            RankBoardMod.LOGGER.warn("Failed to save RankBoard config", e);
        }
    }

    RankBoardConfig sanitize() {
        entriesPerPage = clamp(entriesPerPage, 1, 7);
        pageSeconds = clamp(pageSeconds, 2, 300);
        refreshSeconds = clamp(refreshSeconds, 5, 3600);
        filterMode = normalizeFilterMode(filterMode);
        players = players == null ? new ArrayList<>() : players.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        placedTitle = fallback(placedTitle, "放置榜");
        minedTitle = fallback(minedTitle, "挖掘榜");
        movedTitle = fallback(movedTitle, "移动榜");
        deathsTitle = fallback(deathsTitle, "死亡榜");
        titleTemplate = fallback(titleTemplate, "§6{title} §e{page}/{pages}");
        nameTemplate = fallback(nameTemplate, "§f{rank}. {name}");
        valueTemplate = fallback(valueTemplate, "§7{value}");
        emptyText = fallback(emptyText, "§7暂无数据");
        return this;
    }

    boolean shouldIncludePlayer(String playerName) {
        String mode = normalizeFilterMode(filterMode);
        if ("off".equals(mode) || players.isEmpty()) {
            return true;
        }

        boolean matched = players.stream().anyMatch(name -> name.equalsIgnoreCase(playerName));
        if ("whitelist".equals(mode)) {
            return matched;
        }
        return !matched;
    }

    String titleFor(BoardPage page) {
        return switch (page) {
            case PLACED -> placedTitle;
            case MINED -> minedTitle;
            case MOVED -> movedTitle;
            case DEATHS -> deathsTitle;
        };
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String normalizeFilterMode(String mode) {
        String normalized = mode == null ? "off" : mode.trim().toLowerCase(Locale.ROOT);
        return FILTER_MODES.contains(normalized) ? normalized : "off";
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("rankboard.json");
    }
}
