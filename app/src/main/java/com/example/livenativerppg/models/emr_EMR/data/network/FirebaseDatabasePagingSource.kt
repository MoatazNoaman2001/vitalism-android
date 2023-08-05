package com.example.livenativerppg.models.emr_EMR.data.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livenativerppg.commons.rppgDateFormat
import com.example.livenativerppg.component.natives.RPPGResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import org.apache.commons.lang3.tuple.ImmutablePair
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "FirebaseDatabasePagingS"

class FirebaseDatabasePagingSource(
    private val queryRPPG: Query,
    val interval: ArrayList<Date>,
) : PagingSource<DataSnapshot, ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>>() {
    var currentList = kotlin.collections.ArrayList<Date>()

    override fun getRefreshKey(state: PagingState<DataSnapshot, ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>>): DataSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DataSnapshot>): LoadResult<DataSnapshot, ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> {
        return try {
            val currentPage: DataSnapshot
            if (interval.isEmpty()) {
                currentPage = params.key ?: queryRPPG.get().await()
            } else {
                currentList = if (currentList.isEmpty()) {
                    Log.d(TAG, "load: 1 called")
                    currentPage = queryRPPG.startAt(rppgDateFormat.format(interval.first()))
                        .get().await()
                    interval
                } else if (currentList != interval){
                    Log.d(TAG, "load: ${rppgDateFormat.format(interval.first())}")
                    currentPage = queryRPPG.startAt(rppgDateFormat.format(interval.first()))
                        .get().await()
                    Log.d(TAG, "load: 2 called")
                    interval
                }else{
                    currentPage = params.key ?: queryRPPG.startAt(rppgDateFormat.format(interval.first()))
                        .get().await()
                    Log.d(TAG, "load: 3 called")
                    currentList
                }
            }
            if (currentPage.children.asFlow().toList().isEmpty()) {
                Log.d(TAG, "load: current page empty")
                return LoadResult.Page(
                    nextKey = null,
                    prevKey = null,
                    data = emptyList()
                )
            }
            Log.d(
                TAG,
                "load: key ${currentPage.key}, keys: ${
                    currentPage.children.asFlow().toList()[0].key.toString()
                }"
            )

            val nextKey = currentPage.children.asFlow().toList()[0].key.toString()
            Log.d(TAG, "load: next key: $nextKey")
            val nextPage = queryRPPG
                .startAfter(nextKey)
                .get().await()
            val results: ArrayList<ImmutablePair<String, ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>>> =
                ArrayList()

            currentPage.children.forEach {
                results.add(
                    ImmutablePair(it.key,
                        ArrayList<ImmutablePair<String, ArrayList<RPPGResult>>>().apply {
                            for (child in it.children) {
                                add(
                                    ImmutablePair<String, ArrayList<RPPGResult>>(child.key,
                                        ArrayList<RPPGResult>().apply {
                                            for (child in child.children) {
                                                add(child.getValue(RPPGResult::class.java)!!)
                                            }
                                        })
                                )
                            }
                        })
                )
            }
            Log.d(TAG, "load: results size: ${results.size}")
            return LoadResult.Page(
                data = results,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    }

}