package com.example.livenativerppg.models.searchMain.data.network

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livenativerppg.component.natives.RPPGResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Query
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.tasks.await
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple

private const val TAG = "FirebaseSearchEmr"

class FirebaseSearchEmr(val searchQuery: Query, val txt: String) :
    PagingSource<DataSnapshot, ImmutableTriple<String ,String , ArrayList<RPPGResult>>>() {
    override fun getRefreshKey(state: PagingState<DataSnapshot, ImmutableTriple<String ,String , ArrayList<RPPGResult>>>): DataSnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<DataSnapshot>): LoadResult<DataSnapshot, ImmutableTriple<String ,String , ArrayList<RPPGResult>>> {
        return try {
            val currentPage =
                params.key ?: searchQuery.startAt(txt).endAt(txt + '\uf8ff').get().await()
            if (currentPage.children.asFlow().toList().isEmpty()) {
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
            val nextPage = searchQuery
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
            val flatten_res: ArrayList<ImmutableTriple<String ,String , ArrayList<RPPGResult>>> = ArrayList()
            results.forEach {date->
                date.right.forEach { time->
                    flatten_res.add(ImmutableTriple(date.left , time.left , time.right))
                }
            }
            Log.d(TAG, "load: results size: ${results.size}")
            return LoadResult.Page(
                data = flatten_res,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


}