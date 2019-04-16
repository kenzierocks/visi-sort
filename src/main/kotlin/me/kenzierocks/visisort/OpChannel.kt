package me.kenzierocks.visisort

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel

/**
 * OpChannel acts like a [SendChannel], but strictly works on [Op]s and has a "return" concept.
 */
class OpChannel {

    private val opChannel = Channel<Op<*>>(1)
    private val returnChannel = Channel<Any>(1)

    fun close() {
        opChannel.close()
        returnChannel.close()
    }

    @ExperimentalCoroutinesApi
    val isOpen: Boolean
        get() = !opChannel.isClosedForSend

    @ExperimentalCoroutinesApi
    val isClosedForProcessing: Boolean
        get() = opChannel.isClosedForReceive

    enum class ProcessResult {
        PROCESSED, NO_OPERATION, CLOSED
    }

    @ExperimentalCoroutinesApi
    suspend fun processOp(processor: suspend (Op<*>) -> Any): ProcessResult {
        val op = opChannel.poll() ?: return when {
            isClosedForProcessing -> ProcessResult.CLOSED
            else -> ProcessResult.NO_OPERATION
        }
        val result = processor(op)
        returnChannel.send(result)
        return ProcessResult.PROCESSED
    }

    private suspend fun <R> performOp(op: Op<R>): R {
        opChannel.send(op)
        @Suppress("UNCHECKED_CAST")
        return returnChannel.receive() as R
    }

    suspend fun VisiArray.get(index: Int): Data =
            performOp(Op.Get(this, index))

    suspend fun VisiArray.set(index: Int, value: Data) =
            performOp(Op.Set(this, index, value))

    suspend fun compare(a: VisiArray.Ref, b: VisiArray.Ref) =
            performOp(Op.Compare(a, b))

    suspend fun VisiArray.swap(a: Int, b: Int) =
            performOp(Op.Swap(this, a, b))

    suspend fun VisiArray.slice(range: IntRange) =
            performOp(Op.Slice(this, range))

    suspend fun fork(array: VisiArray, algo: SortAlgo) =
            performOp(Op.Fork(array, algo))

    suspend fun idle() =
            performOp(Op.Idle)

}