package com.wangdai.rankboard;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

final class RankBoardCommands {
    private RankBoardCommands() {
    }

    static void register(RankBoardMod mod) {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(literal("rankboard")
                        .requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))
                        .then(literal("reload").executes(context -> {
                            mod.manager().reload();
                            feedback(context.getSource(), "RankBoard 配置已重载。");
                            return 1;
                        }))
                        .then(literal("refresh").executes(context -> {
                            mod.manager().refresh();
                            feedback(context.getSource(), "RankBoard 数据已刷新。");
                            return 1;
                        }))
                        .then(literal("next").executes(context -> {
                            mod.manager().nextPage();
                            feedback(context.getSource(), "RankBoard 已切换到下一页。");
                            return 1;
                        }))
                        .then(literal("show")
                                .then(argument("page", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            builder.suggest("placed");
                                            builder.suggest("mined");
                                            builder.suggest("moved");
                                            builder.suggest("deaths");
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String pageId = StringArgumentType.getString(context, "page");
                                            BoardPage page = BoardPage.byId(pageId);
                                            if (page == null) {
                                                feedback(context.getSource(), "未知页面，可用：placed, mined, moved, deaths");
                                                return 0;
                                            }
                                            mod.manager().show(page);
                                            feedback(context.getSource(), "RankBoard 已切换到 " + page.title() + "。");
                                            return 1;
                                        })))
                        .then(literal("status").executes(context -> {
                            RankBoardConfig config = mod.manager().config();
                            feedback(context.getSource(), "RankBoard: 每页 " + config.entriesPerPage
                                    + " 名，轮播 " + config.pageSeconds
                                    + " 秒，刷新 " + config.refreshSeconds + " 秒。");
                            return 1;
                        }))));
    }

    private static void feedback(ServerCommandSource source, String message) {
        source.sendFeedback(() -> Text.literal(message), false);
    }
}
