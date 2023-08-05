package com.example.livenativerppg.component.db.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.livenativerppg.component.db.models.VitalSign;

import java.util.Collection;
import java.util.List;

@Dao
public interface MeasuredVitalSignDao {

    @Insert
    void InsertVitalSign(VitalSign vitalSign);

    @Insert
    void InsertVitalSigns(Collection<VitalSign> vitalSigns);

    @Query("select * from WholeVitalSigns where recordType = :type order by CaptureDate asc")
    PagingSource<Integer , VitalSign> getVitalSignWithType(String type);
}
