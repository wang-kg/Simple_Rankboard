package com.wangdai.rankboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RankBoardMod implements ModInitializer {
    static final String MOD_ID = "rankboard";
    static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private RankBoardManager manager;

    @Override
    public void onInitialize() {
        RankBoardCommands.register(this);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            manager = new RankBoardManager(server);
            manager.start();
            LOGGER.info("RankBoard started.");
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (manager != null) {
                manager.tick();
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            if (manager != null) {
                manager.stop();
            }
        });
    }

    RankBoardManager manager() {
        if (manager == null) {
            throw new IllegalStateException("RankBoard is not ready yet");
        }
        return manager;
    }
}
