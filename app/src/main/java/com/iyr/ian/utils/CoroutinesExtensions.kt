package com.iyr.ian.utils

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class CoroutinesExtensions {
}


suspend fun CoroutineContext.isMainContext(): Boolean {
    val dispatcher = coroutineContext[ContinuationInterceptor]!!
    return dispatcher == Dispatchers.Main
}