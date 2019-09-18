package com.sap.shared;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {TremorRecord.class}, version = 1, exportSchema = false)
@TypeConverters({TremorSeverityTypeConverters.class})
public abstract class TremorDatabase extends RoomDatabase {
    public abstract TremorRecordDao tremorRecordDao();
}
