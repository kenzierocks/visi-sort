package me.kenzierocks.visisort.dispatcher

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainCoroutineDispatcher
import kotlinx.coroutines.Runnable
import java.util.concurrent.ArrayBlockingQueue
import kotlin.coroutines.CoroutineContext

sealed class MainDispatcher(
        private val runnableQueue: ArrayBlockingQueue<Runnable>
) : MainCoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) = runnableQueue.put(block)

}

class ImmediateMainDispatcher(
        runnableQueue: ArrayBlockingQueue<Runnable>,
        private val mainThread: Thread
) : MainDispatcher(runnableQueue) {
    override val immediate: MainCoroutineDispatcher
        get() = this

    @ExperimentalCoroutinesApi
    override fun isDispatchNeeded(context: CoroutineContext) = Thread.currentThread().id == mainThread.id
}

class NormalMainDispatcher(
        runnableQueue: ArrayBlockingQueue<Runnable>,
        mainThread: Thread
) : MainDispatcher(runnableQueue) {
    override val immediate: MainCoroutineDispatcher = ImmediateMainDispatcher(runnableQueue, mainThread)
}
