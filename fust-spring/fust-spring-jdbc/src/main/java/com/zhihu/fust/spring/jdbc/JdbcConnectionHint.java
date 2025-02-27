package com.zhihu.fust.spring.jdbc;

public class JdbcConnectionHint {
    protected static class Hint {
        boolean master;
        boolean slave;
        String databaseName;
    }

    private static final ThreadLocal<Hint> MASTER_HINT = ThreadLocal.withInitial(Hint::new);

    public static boolean isMaster() {
        Hint hint = MASTER_HINT.get();
        return hint != null && hint.master;
    }

    public static void setMaster(boolean master) {
        Hint hint = MASTER_HINT.get();
        hint.master = master;
    }

    public static boolean isSlave() {
        Hint hint = MASTER_HINT.get();
        return hint != null && hint.slave;
    }

    public static void setSlave(boolean slave) {
        Hint hint = MASTER_HINT.get();
        hint.slave = slave;
    }

    public static void setDatabaseName(String name) {
        Hint hint = MASTER_HINT.get();
        hint.databaseName = name;
    }

    public static String getDatabaseName() {
        Hint hint = MASTER_HINT.get();
        return hint.databaseName;
    }

    public static void clear() {
        MASTER_HINT.remove();
    }
}
