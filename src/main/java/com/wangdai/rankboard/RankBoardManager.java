package com.wangdai.rankboard;

import net.minecraft.server.MinecraftServer;

final class RankBoardManager {
    private final MinecraftServer server;
    private final StatsFileReader statsFileReader;
    private final LeaderboardService leaderboardService = new LeaderboardService();
    private final SidebarRenderer renderer;

    private RankBoardConfig config;
    private int currentPageIndex;
    private int pageTicks;
    private int refreshTicks;

    RankBoardManager(MinecraftServer server) {
        this.server = server;
        this.statsFileReader = new StatsFileReader(server);
        this.renderer = new SidebarRenderer(server);
        this.config = RankBoardConfig.load();
    }

    void start() {
        refresh();
        renderCurrentPage();
    }

    void stop() {
        renderer.clear();
    }

    void tick() {
        if (!config.enabled) {
            return;
        }

        pageTicks++;
        refreshTicks++;

        if (refreshTicks >= secondsToTicks(config.refreshSeconds)) {
            refreshTicks = 0;
            refresh();
        }

        if (pageTicks >= secondsToTicks(config.pageSeconds)) {
            pageTicks = 0;
            nextPage();
        }
    }

    void reload() {
        config = RankBoardConfig.load();
        pageTicks = 0;
        refreshTicks = 0;
        refresh();
        if (config.enabled) {
            renderCurrentPage();
        } else {
            renderer.clear();
        }
    }

    void refresh() {
        leaderboardService.rebuild(statsFileReader.readAll(), config);
    }

    void nextPage() {
        currentPageIndex++;
        renderCurrentPage();
    }

    void show(BoardPage page) {
        currentPageIndex = page.pageNumber() - 1;
        pageTicks = 0;
        renderCurrentPage();
    }

    RankBoardConfig config() {
        return config;
    }

    private void renderCurrentPage() {
        if (!config.enabled) {
            renderer.clear();
            return;
        }

        BoardPage page = BoardPage.fromIndex(currentPageIndex);
        renderer.render(page, leaderboardService.get(page), config);
    }

    private static int secondsToTicks(int seconds) {
        return seconds * 20;
    }
}
