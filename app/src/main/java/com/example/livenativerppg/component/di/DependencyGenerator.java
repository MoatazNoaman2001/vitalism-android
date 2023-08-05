package com.example.livenativerppg.component.di;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.example.livenativerppg.R;
import com.example.livenativerppg.component.db.AppDatabase;
import com.example.livenativerppg.component.db.converters.DateConverter;
import com.example.livenativerppg.component.db.converters.StringListConverter;
import com.example.livenativerppg.component.utility.Variables;
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.network.GeoApi;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class DependencyGenerator {

    @Provides
    @Singleton
    @IpAddrRetrofitBuilder
    public Retrofit GeoRetorfit() {
        return new Retrofit.Builder()
                .baseUrl(GeoApi.Companion.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public Task<DocumentSnapshot> getMainUserInfo() {
        return FirebaseFirestore.getInstance()
                .collection(Variables.FireStoreUsersRoot)
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get();
    }


    @Provides
    @Singleton
    @IpAddrRetrofit
    public GeoApi getGeoApi(@IpAddrRetrofitBuilder Retrofit retrofit) {
        return retrofit.create(GeoApi.class);
    }

    @Provides
    @Singleton
    public RequestManager getImageLoader(@ApplicationContext Context context) {
        return Glide.with(context).applyDefaultRequestOptions(
                new RequestOptions()
                        .placeholder(R.drawable.ic_downloading_24)
                        .error(R.drawable.image_not_found)
        );
    }

    @Provides
    @Singleton
    public AppDatabase getInstance(Application application) {
        return Room.databaseBuilder(application, AppDatabase.class, "app Database")
                .fallbackToDestructiveMigration()
                .addTypeConverter(new DateConverter())
                .addTypeConverter(new StringListConverter())
                .build();
    }


    @Provides
    @Singleton
    public SharedPreferences userMainEditor(@ApplicationContext Context context) {
        return context.getSharedPreferences(Variables.PROFILE, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    @Named(Variables.PPG_HR_PATH)
    public DatabaseReference PPGHrDocument(){
        return FirebaseDatabase.getInstance().getReference(Variables.FireStoreUsersRoot)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(Variables.MEASUREMENT)
                .child(Variables.PPG)
                .child(Variables.HR);
    }


}
