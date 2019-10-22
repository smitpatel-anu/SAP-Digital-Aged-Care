package com.sap.shared;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

@SuppressWarnings("all")
@Entity(tableName = "tremor_records")
public class TremorRecord {
    private static final String JSON_KEY_START_TIMESTAMP = "startTimestamp";
    private static final String JSON_KEY_END_TIMESTAMP = "endTimestamp";
    private static final String JSON_KEY_TREMOR_SEVERITY = "tremorSeverity";

    @PrimaryKey
    @ColumnInfo(name = "start_timestamp")
    public long startTimestamp;

    @ColumnInfo(name = "end_timestamp")
    public long endTimestamp;

    @ColumnInfo(name = "tremor_severity")
    public TremorSeverity tremorSeverity;

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

    public TremorSeverity getTremorSeverity() {
        return tremorSeverity;
    }

    public void setTremorSeverity(TremorSeverity tremorSeverity) {
        this.tremorSeverity = tremorSeverity;
    }

    TremorRecord(long startTimestamp, long endTimestamp, TremorSeverity tremorSeverity) {
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.tremorSeverity = tremorSeverity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TremorRecord that = (TremorRecord) o;
        return startTimestamp == that.startTimestamp &&
                endTimestamp == that.endTimestamp &&
                tremorSeverity == that.tremorSeverity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTimestamp, endTimestamp, tremorSeverity);
    }

    @Override
    @NonNull
    public String toString() {
        return new StringBuilder(128)
                .append("{")
                .append("startTimestamp: ")
                .append(new java.util.Date(startTimestamp).toString())
                .append(", ")
                .append("endTimestamp: ")
                .append(new java.util.Date(endTimestamp).toString())
                .append(", ")
                .append("tremorSeverity: ")
                .append(tremorSeverity.ordinal())
                .append("}")
                .toString();
    }

    JSONObject toJSONObject() throws JSONException {
        TimeZone timeZone = TimeZone.getTimeZone("UTC");
        DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        iso8601DateFormat.setTimeZone(timeZone);

        return new JSONObject()
                .put(JSON_KEY_START_TIMESTAMP, iso8601DateFormat.format(startTimestamp))
                .put(JSON_KEY_END_TIMESTAMP, iso8601DateFormat.format(endTimestamp))
                .put(JSON_KEY_TREMOR_SEVERITY, tremorSeverity.ordinal());
    }
}
