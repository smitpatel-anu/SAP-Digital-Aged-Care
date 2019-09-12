package com.sap.shared;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TremorRecordDao {
    @Query("SELECT * FROM tremorrecord")
    List<TremorRecord> getAll();

    @Query("SELECT * FROM tremorrecord WHERE start_timestamp >= :startTimestampMillis AND end_timestamp <= :endTimestampMillis ")
    List<TremorRecord> findRecords(long startTimestampMillis, long endTimestampMillis);

    @Insert
    void insert(TremorRecord tremorRecord);

//    @Insert
//    void insertAll(TremorRecord... tremorRecords);
//
//    @Delete
//    void delete(TremorRecord tremorRecord);
}
