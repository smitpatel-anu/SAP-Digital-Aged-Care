package com.sap.shared;

import androidx.room.TypeConverter;

public class TremorSeverityTypeConverters {
    @TypeConverter
    public static int fromTremorSeverityToInt(TremorSeverity tremorSeverity) {
        return tremorSeverity.ordinal();
    }

    @TypeConverter
    public static TremorSeverity fromIntToTremorSeverity(int severity) {
        return TremorSeverity.values()[severity];
    }
}
