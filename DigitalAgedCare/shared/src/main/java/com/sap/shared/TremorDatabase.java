package com.sap.shared;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TremorRecord.class}, version = 1, exportSchema = false)
public abstract class TremorDatabase extends RoomDatabase {
    public abstract TremorRecordDao tremorRecordDao();
}
