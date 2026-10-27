package com.wangdai.rankboard;

import net.minecraft.server.MinecraftServer;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

final class SidebarRenderer {
    private static final String OBJECTIVE = "rankboard";

    private final MinecraftServer server;

    SidebarRenderer(MinecraftServer server) {
        this.server = server;
    }

    void render(BoardPage page, List<PlayerRank> ranks, RankBoardConfig config) {
        List<String> lines = createLines(page, ranks, config);
        ServerScoreboard scoreboard = server.getScoreboard();

        ScoreboardObjective oldObjective = scoreboard.getNullableObjective(OBJECTIVE);
        if (oldObjective != null) {
            scoreboard.removeObjective(oldObjective);
        }

        ScoreboardObjective objective = scoreboard.addObjective(
                OBJECTIVE,
                ScoreboardCriterion.DUMMY,
                Text.literal(formatTitle(page, config)),
                ScoreboardCriterion.RenderType.INTEGER,
                false,
                BlankNumberFormat.INSTANCE
        );

        int score = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            ScoreHolder holder = ScoreHolder.fromName("rb_" + i);
            ScoreAccess access = scoreboard.getOrCreateScore(holder, objective);
            access.setScore(score--);
            access.setDisplayText(Text.literal(lines.get(i)));
            access.setNumberFormat(BlankNumberFormat.INSTANCE);
        }

        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
    }

    void clear() {
        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(OBJECTIVE);
        if (objective != null) {
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);
            scoreboard.removeObjective(objective);
        }
    }

    private static List<String> createLines(BoardPage page, List<PlayerRank> ranks, RankBoardConfig config) {
        List<String> lines = new ArrayList<>();
        if (ranks.isEmpty()) {
            lines.add(config.emptyText);
            return lines;
        }

        for (int i = 0; i < ranks.size(); i++) {
            PlayerRank rank = ranks.get(i);
            String value = NumberFormatter.format(rank.value(), page.unit(), config.compactNumbers);
            lines.add(applyTemplate(config.nameTemplate, page, i + 1, trim(rank.name(), 24), value, config));
            lines.add(applyTemplate(config.valueTemplate, page, i + 1, trim(rank.name(), 24), value, config));
        }
        return lines;
    }

    private static String formatTitle(BoardPage page, RankBoardConfig config) {
        return applyTemplate(config.titleTemplate, page, 0, "", "", config);
    }

    private static String applyTemplate(String template, BoardPage page, int rank, String name, String value, RankBoardConfig config) {
        return template
                .replace("{title}", config.titleFor(page))
                .replace("{page}", Integer.toString(page.pageNumber()))
                .replace("{pages}", Integer.toString(BoardPage.pageCount()))
                .replace("{rank}", Integer.toString(rank))
                .replace("{name}", name)
                .replace("{value}", value);
    }

    private static String trim(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + ".";
    }
}
