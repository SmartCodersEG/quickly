package sk.ttomovcik.quickly.db;

import sk.ttomovcik.quickly.BuildConfig;

public class Constants
{
    static final String DB_NAME = BuildConfig.APPLICATION_ID + ".todo.db";
    static final String DB_TABLE = "todo";
    static final int DB_VERSION = 1;
}
