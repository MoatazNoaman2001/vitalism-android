package com.example.livenativerppg.component.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.livenativerppg.models.schedule.data.model.Medicine;

import java.util.Date;
import java.util.List;

@Dao
public interface MedicineScheduledDao {

    @Insert
    void InsertMedicine(Medicine medicine);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void InsertMedicines(List<Medicine> medicine);

    @Update
    void UpdateMedicine(Medicine medicine);

    @Delete
    void DeleteMedicine(Medicine medicine);

    @Query("select * from 'medicine table' order by hours asc")
    LiveData<List<Medicine>> getAllMedicine();
}
