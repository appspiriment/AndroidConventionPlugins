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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch


/*********************************************************
 * Class   :  FlowUtils
 * Author  :  Appspiriment Labs
 * Created :  12/01/2025
 *******************************************************
 * Purpose :
 *******************************************************
 * Rework Details:
 * 1) {Author} :  {Date} : {Details}
 *********************************************************/

/**
 * Converts any object to a [Flow] emitting that object as a single value,
 * with emission occurring on the specified [dispatcher]. The flow is cancellable.
 *
 * @param dispatcher The [CoroutineDispatcher] for the emission.
 * @return A [Flow] emitting the object on the given [dispatcher].
 *
 * @sample
 */
fun <T> T.asFlow(dispatcher: CoroutineDispatcher): Flow<T> {
    return flowOf(this).cancellable().flowOn(dispatcher)
}


/**
 * Returns a [Flow] that emits the results of applying the given [transform] function to each value
 * emitted by the original [Flow].
 *
 * <p>This function is a flow transformation operator. For each value emitted by the source [Flow],
 * the [transform] function is invoked. The results emitted by the [Flow] returned by the transform function are emitted by the returned flow.
 *
 * @param <T> The type of values emitted by the original flow.
 * @param <R> The type of values emitted by the transformed flow (a List).
 * @param transform A function that takes a value of type T and returns a [Flow] of [List]<R>.
 * @return A [Flow] that emits the transformed values.
 */
inline fun <T, R> Flow<T>.flatMap(crossinline transform: suspend (value: T) -> Flow<List<R>>): Flow<List<R>> {
    return arrayListOf<Flow<List<R>>>().apply {
        this@flatMap.map {
            add(transform(it))
        }
    }.merge()
}


/**
 * Collects values from a [Flow] and updates various state flows based on the emitted values, errors, and loading states.
 *
 * <p>This function provides a convenient way to collect from a [Flow] and manage common states such as success,
 * error, and loading. It allows you to optionally update [MutableStateFlow] instances or invoke custom
 * listener functions for each state.
 *
 * <p>The collection is performed within the given [CoroutineScope] on the specified [dispatcher].
 *
 * @param <R> The type of values emitted by the flow.
 * @param scope The [CoroutineScope] in which the collection will be performed.
 * @param successState An optional [MutableStateFlow] to update with successful values.
 * @param errorState An optional [MutableStateFlow] to update with error messages.
 * @param loadingState An optional [MutableStateFlow] to update with loading state (true/false).
 * @param onNext An optional listener function invoked when a new value is successfully collected.
 * @param onError An optional listener function invoked when an error occurs during collection.
 * @param onLoading An optional listener function invoked when the loading state changes.
 * @param cancelAfterFirst If true, the collection will be canceled after the first successful value is received.
 * @param dispatcher The [CoroutineDispatcher] on which the collection will be performed. Defaults to [Dispatchers.IO].
 * @throws RuntimeException if both `successState` and `onNext` are null.
 */
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