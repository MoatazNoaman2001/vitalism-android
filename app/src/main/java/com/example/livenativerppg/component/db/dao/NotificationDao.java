package com.example.livenativerppg.component.db.dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.airbnb.lottie.L;
import com.example.livenativerppg.component.db.models.Notification;

import java.util.List;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface NotificationDao {

    @Insert
    void insertNotification(Notification notification);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNotifications(List<Notification> notifications);

    @Update
    void updateNotification(Notification notification);
    @Delete
    void deleteNotification(Notification notification);

    @Query("DELETE FROM NotificationTable")
    void deleteAllNotifications();

    @Query("select * from NotificationTable where Noti_SenderId =:senderId and Noti_RequestDate =:dateSent LIMIT 1")
    Notification SearchNotification(String senderId , long dateSent);


    @Query("SELECT * FROM NotificationTable ORDER BY Noti_RequestDate DESC")
    Flow<List<Notification>> getAllNotifications();
}
