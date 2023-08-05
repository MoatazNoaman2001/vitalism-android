package com.example.livenativerppg.models.searchMain.data.network

import android.util.Log
import androidx.compose.ui.text.toLowerCase
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livenativerppg.models.startActivity.ui.fragments.signUpFragment.data.model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.tasks.await
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "FirebaseSearchUsersPage"

class FirebaseSearchUsersPager(val searchQuery: Query, val txt: String) :
    PagingSource<QuerySnapshot, UserInfo>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, UserInfo>): QuerySnapshot? {
        return null
    }

    private fun comb4(word: String, accu: CharArray, list: ArrayList<String>, index: Int) {
        if (index == word.length) {
            println(accu)
            list.add(accu.concatToString())
        } else {
            val ch = word[index]
            accu[index] = ch.lowercaseChar()
            val disposable = Observable.just(comb4(word, accu, list, index + 1))
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe({ s ->

                }, { e ->
                    e.printStackTrace()
                }, {
                    Log.d(TAG, "comb4: lower case done")
                })
            accu[index] = ch.uppercaseChar()
            val disposable2 = Observable.just(comb4(word, accu, list, index + 1))
                .observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe({ s ->

                }, { e ->
                    e.printStackTrace()
                }, {
                    Log.d(TAG, "comb4: lower case done")
                })
        }
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, UserInfo> {
        return try {
            val currentPage: QuerySnapshot = params.key ?: searchQuery
                .whereLessThanOrEqualTo("name", txt.lowercase())
                .whereGreaterThanOrEqualTo("name", txt.uppercase())
                .get().await()
            val nextPage = searchQuery.startAfter(currentPage.documents.last()).get().await()

            val results = currentPage.documents
                .map { it.toObject(UserInfo::class.java)!! }
                .filter { it.name.startsWith(txt) && it.uid != FirebaseAuth.getInstance().uid.toString() }
                .toCollection(kotlin.collections.ArrayList())
            Log.d(TAG, "load: result size: ${results.size}")

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