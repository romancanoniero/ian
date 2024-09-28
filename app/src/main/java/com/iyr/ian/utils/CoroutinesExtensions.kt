package com.iyr.ian.utils

import android.content.Context
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutinesExtensions {
}

suspend fun CoroutineContext.isMainContext(): Boolean {
    val dispatcher = coroutineContext[ContinuationInterceptor]!!
    return dispatcher == Dispatchers.Main
}
// Extension function to check if the current context is a coroutine context
fun Context.isCoroutineContext(coroutineContext : CoroutineContext): Boolean {
    return runCatching {
        coroutineContext != EmptyCoroutineContext
    }.getOrDefault(false)
}

// Extension function to check if the current context is a coroutine context
fun CoroutineContext.isCoroutineContext(): Boolean {
    return this != EmptyCoroutineContext
}

// Extension function to get the current dispatcher if the context is a coroutine context
@OptIn(ExperimentalStdlibApi::class)
suspend fun CoroutineContext.currentDispatcher(): CoroutineDispatcher? {
    return withContext(this) {
        when (this.coroutineContext[CoroutineDispatcher.Key]) {
            Dispatchers.Main -> Dispatchers.Main
            Dispatchers.IO -> Dispatchers.IO
            Dispatchers.Default -> Dispatchers.Default
            Dispatchers.Unconfined -> Dispatchers.Unconfined
            else -> null
        }
    }
}