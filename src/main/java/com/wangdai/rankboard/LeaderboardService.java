package com.wangdai.rankboard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

final class LeaderboardService {
    private final Map<BoardPage, List<PlayerRank>> boards = new EnumMap<>(BoardPage.class);

    void rebuild(List<PlayerStatsSnapshot> snapshots, RankBoardConfig config) {
        for (BoardPage page : BoardPage.values()) {
            List<PlayerRank> ranks = new ArrayList<>();
            for (PlayerStatsSnapshot snapshot : snapshots) {
                if (!config.shouldIncludePlayer(snapshot.name())) {
                    continue;
                }
                long value = snapshot.valueFor(page);
                if (value > 0 || config.includeZeroValues) {
                    ranks.add(new PlayerRank(snapshot.name(), value));
                }
            }

            ranks.sort(Comparator.comparingLong(PlayerRank::value)
                    .reversed()
                    .thenComparing(PlayerRank::name, String.CASE_INSENSITIVE_ORDER));
            if (ranks.size() > config.entriesPerPage) {
                ranks = new ArrayList<>(ranks.subList(0, config.entriesPerPage));
            }
            boards.put(page, List.copyOf(ranks));
        }
    }

    List<PlayerRank> get(BoardPage page) {
        return boards.getOrDefault(page, List.of());
    }
}
