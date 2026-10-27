package com.wangdai.rankboard;

record PlayerStatsSnapshot(String name, long placedBlocks, long minedBlocks, long movedMeters, long deaths) {
    long valueFor(BoardPage page) {
        return switch (page) {
            case PLACED -> placedBlocks;
            case MINED -> minedBlocks;
            case MOVED -> movedMeters;
            case DEATHS -> deaths;
        };
    }
}
