package com.appspiriment.utils.extensions

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch


/*********************************************************
 * Class   :  FlowUtils
 * Author  :  Arun Nair
 * Created :  27/10/2022
 *******************************************************
 * Purpose :
 *******************************************************
 * Rework Details:
 * 1) {Author} :  {Date} : {Details}
 *********************************************************/

/**
 * Returns a flow containing the results of applying the given [transform] function to each value of the original flow.
 */
inline fun <T, R> Flow<T>.flatMap(crossinline transform: suspend (value: T) -> Flow<List<R>>): Flow<List<R>> {
    return arrayListOf<Flow<List<R>>>().apply {
        this@flatMap.map {
            add(transform(it))
        }
    }.merge()
}


/***************************************
 * Setting Observers
 ***************************************/
fun <R> Flow<R>.collectState(
    scope: CoroutineScope,
    successState: MutableStateFlow<R>? = null,
    errorState: MutableStateFlow<String>? = null,
    loadingState: MutableStateFlow<Boolean>? = null,
    onNext: ((R) -> Unit)? = null,
    onError: ((Throwable?) -> Unit)? = null,
    onLoading: ((Boolean) -> Unit)? = null,
    cancelAfterFirst: Boolean = false,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    scope.launch(dispatcher) {
        collect(
            successListener = {
                if (successState == null && onNext == null) {
                    throw RuntimeException("Both successlistener and state cannot be null")
                }
                successState?.value = it
                onNext?.invoke(it)
                if (cancelAfterFirst) {
                    cancel()
                }
            },
            errorListener = {
                errorState?.value = it?.message ?: "Unknown Error"
                onError?.invoke(it)
            },
            loadingListener = {
                loadingState?.value = it
                onLoading?.invoke(it)
            }
        )
    }
}


/***************************************
 * Setting Observers
 ***************************************/
private suspend fun <R> Flow<R>.collect(
    successListener: (R) -> Unit,
    errorListener: (Throwable?) -> Unit,
    loadingListener: (Boolean) -> Unit
) {
    loadingListener.invoke(true)
    try {
        catch {
            it.printLog()
            errorListener.invoke(it)
            loadingListener.invoke(false)
        }.collect {
            successListener.invoke(it)
            delay(100)
            loadingListener.invoke(false)
        }

    } catch (e: Exception) {
        e.printLog()
        loadingListener.invoke(false)
        errorListener.invoke(e)
    }
}
/***************************************
 * Setting Observers
 ***************************************/
fun <R> Flow<R>.collectFlows(
    scope: CoroutineScope,
    onNext: ((R) -> Unit),
    onError: ((Throwable?) -> Unit),
    onLoading: ((Boolean) -> Unit),
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    scope.launch(dispatcher) {
        collect(successListener = onNext, errorListener = onError, loadingListener = onLoading)
    }
}