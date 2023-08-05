package com.example.livenativerppg.component.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NotificationRetrofitBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NotificationRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IpAddrRetrofitBuilder

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IpAddrRetrofit

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyRequestsRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyChatRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyConnectionsRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyFollowRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyHrMeasurementsRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyBPMeasurementsRef

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MyRealTimeChatDB
