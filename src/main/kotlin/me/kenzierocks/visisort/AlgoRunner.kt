package me.kenzierocks.visisort

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Runs the given algorithm.
 */
class AlgoRunner(
        private val output: MutableList<Data>,
        private val sortAlgo: SortAlgo,
        private val operationChannel: SendChannel<OpResult<*>>,
        private val coroutineScope: CoroutineScope
) {
    private class Task(
            val id: String,
            val channel: OpChannel
    ) {
        override fun toString() = id
    }

    private val ids = IdTracker("Task")
    private val tasks = mutableListOf<Task>()
    private val arraysInternal = mutableMapOf<String, VisiArray>()

    val arrays: Map<String, VisiArray> = arraysInternal

    fun start() {
        val array = VisiArray(0, 0, output)
        arraysInternal[array.id] = array
        launch(array, sortAlgo)
    }

    private fun launch(array: VisiArray, sortAlgo: SortAlgo): CoroutineContext {
        val channel = OpChannel()
        val id = ids.newId()
        val job = CoroutineName(id) + coroutineScope.launch(context = CoroutineName(id)) {
            try {
                with(sortAlgo) {
                    channel.sort(array)
                }
            } finally {
                channel.close()
            }
        }
        tasks.add(Task(id, channel))
        return job
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend fun pulse(): Boolean {
        tasks.removeIf { t -> t.channel.isClosedForProcessing }
        if (tasks.isEmpty()) {
            return false
        }
        var processedAny = false
        while (!processedAny && tasks.size > 0) {
            val remove = tasks.toList().filter { task ->
                val result = task.channel.processOp {
                    val result = processOperation(it)
                    @Suppress("UNCHECKED_CAST")
                    val opResult = OpResult(it as Op<Any>, result)
                    operationChannel.send(opResult)
                    result
                }
                var shouldKeep = true
                when (result) {
                    OpChannel.ProcessResult.PROCESSED -> processedAny = true
                    OpChannel.ProcessResult.CLOSED -> shouldKeep = false
                    OpChannel.ProcessResult.NO_OPERATION -> {
                    }
                }

                !shouldKeep
            }
            tasks.removeAll(remove)
        }
        return true
    }

    private fun processOperation(it: Op<*>): Any {
        return when (it) {
            is Op.Idle -> Unit
            is Op.Compare -> Integer.compare(it.a.value.value, it.b.value.value)
            is Op.Fork -> launch(it.array, it.algo)
            is Op.Get -> it.array.data[it.index]
            is Op.Copy -> it.to.array.data[it.to.index] = it.from.value
            is Op.Slice -> {
                val newArray = it.array.data.slice(it.range).toMutableList()
                val visiArray = VisiArray(it.array.level + 1, it.array.offset + it.range.first, newArray)
                arraysInternal[visiArray.id] = visiArray
                visiArray
            }
            is Op.Swap -> {
                val d = it.array.data
                val tmp = d[it.a]
                d[it.a] = d[it.b]
                d[it.b] = tmp

                Unit
            }
        }
    }

}