package com.sap.shared;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;

@Entity
public class TremorRecord {
    @PrimaryKey
    @ColumnInfo(name = "start_timestamp")
    public long startTimestamp;

    @ColumnInfo(name = "end_timestamp")
    public long endTimestamp;

    @ColumnInfo(name = "tremor_severity")
    public int tremorSeverity;

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public int getTremorSeverity() {
        return tremorSeverity;
    }

    public void setTremorSeverity(int tremorSeverity) {
        this.tremorSeverity = tremorSeverity;
    }

    TremorRecord(long startTimestamp, long endTimestamp, int tremorSeverity) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.tremorSeverity = tremorSeverity;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("startTimestamp: ");
        sb.append(new java.util.Date(startTimestamp).toString());
        sb.append(", ");
        sb.append("endTimestamp: ");
        sb.append(new java.util.Date(endTimestamp).toString());
        sb.append(", ");
        sb.append("tremorSeverity: ");
        sb.append(tremorSeverity);
        sb.append("}");
        return sb.toString();
    }
}
