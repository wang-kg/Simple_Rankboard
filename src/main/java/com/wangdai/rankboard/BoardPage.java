package com.wangdai.rankboard;

enum BoardPage {
    PLACED("placed", "放置榜", "个"),
    MINED("mined", "挖掘榜", "个"),
    MOVED("moved", "移动榜", "m"),
    DEATHS("deaths", "死亡榜", "次");

    private static final BoardPage[] ROTATION = {PLACED, MINED, MOVED, DEATHS};

    private final String id;
    private final String title;
    private final String unit;

    BoardPage(String id, String title, String unit) {
        this.id = id;
        this.title = title;
        this.unit = unit;
    }

    String id() {
        return id;
    }

    String title() {
        return title;
    }

    String unit() {
        return unit;
    }

    int pageNumber() {
        for (int i = 0; i < ROTATION.length; i++) {
            if (ROTATION[i] == this) {
                return i + 1;
            }
        }
        return 1;
    }

    static int pageCount() {
        return ROTATION.length;
    }

    static BoardPage fromIndex(int index) {
        return ROTATION[Math.floorMod(index, ROTATION.length)];
    }

    static BoardPage byId(String id) {
        for (BoardPage page : ROTATION) {
            if (page.id.equalsIgnoreCase(id)) {
                return page;
            }
        }
        return null;
    }
}
