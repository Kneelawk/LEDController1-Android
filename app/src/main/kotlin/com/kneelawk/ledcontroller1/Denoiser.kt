package com.kneelawk.ledcontroller1

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Used to take a whole bunch of requests that would execute a blocking operation and make sure the
 * blocking operation is only executed on the latest request value.
 */
class Denoiser<T>(
    scope: CoroutineScope,
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.(T) -> Unit
) {
    private object NoNew

    private val value = AtomicReference<Any?>(NoNew)

    init {
        scope.launch(context, start) {
            while (true) {
                val cur = value.getAndSet(NoNew)
                if (cur != NoNew) {
                    // update detected, applying
                    block(cur as T)
                }

                // yield at the end of every cycle so as to not hog all the threads
                yield()
            }
        }
    }

    fun send(data: T) {
        value.set(data)
    }
}