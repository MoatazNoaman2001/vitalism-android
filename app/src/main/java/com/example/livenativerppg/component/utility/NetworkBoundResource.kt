package com.example.livenativerppg.component.utility

import android.util.Log
import kotlinx.coroutines.flow.*


inline fun <ResultType , RequestType> networkBoundResource(
    crossinline init: () -> Unit = {},
    crossinline query : () -> Flow<ResultType>,
    crossinline fetch : suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline isFetched : (ResultType) -> Boolean = { true }
)  = flow {
    init()
    val data = query().first()
    val flow = if (isFetched(data)){
        emit(Resource.Loading(data))
        try {
            saveFetchResult(fetch())
            query().map { Resource.Success(it) }
        }catch (throwable: Throwable){
            query().map { Resource.Failed(throwable , it) }
        }
    }else{
        query().map { Resource.Success(it) }
    }
    emitAll(flow)
}




