/*
 * This file is part of visi-sort, licensed under the MIT License (MIT).
 *
 * Copyright (c) TechShroom Studios <https://techshroom.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package me.kenzierocks.visisort

import com.flowpowered.math.TrigMath
import com.flowpowered.math.vector.Vector2d
import com.flowpowered.math.vector.Vector2f
import com.flowpowered.math.vector.Vector2i
import com.google.common.eventbus.Subscribe
import com.techshroom.unplanned.blitter.pen.DigitalPen
import com.techshroom.unplanned.core.util.Color
import com.techshroom.unplanned.core.util.Sync
import com.techshroom.unplanned.event.keyboard.KeyState
import com.techshroom.unplanned.event.keyboard.KeyStateEvent
import com.techshroom.unplanned.event.mouse.MouseButtonEvent
import com.techshroom.unplanned.event.window.WindowResizeEvent
import com.techshroom.unplanned.input.Key
import com.techshroom.unplanned.window.Window
import com.techshroom.unplanned.window.WindowSettings
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.selects.whileSelect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import me.kenzierocks.visisort.math.angle
import org.jcolorbrewer.ColorBrewer
import org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback
import org.slf4j.LoggerFactory
import kotlin.math.max

class VisiSort(private val algo: SortAlgo) {

    private val window: Window = WindowSettings.builder()
            .title("VisiSort - " + algo.name)
            .msaa(true)
            .build().createWindow()
    private var scale: Vector2d? = null
    private var needsRedraw = false

    init {
        window.eventBus.register(this)
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.run(data: MutableList<Data>, waitForClick: Boolean): List<Job> {
        val ctx = window.graphicsContext
        ctx.makeActiveContext()
        window.isVsyncOn = true
        window.isVisible = true
        val size = window.size
        window.eventBus.post(WindowResizeEvent.create(window, size.x, size.y))
        glfwSetWindowRefreshCallback(window.windowPointer) { needsRedraw = true }

        val jobs = mutableListOf<Job>()

        val operationInput = Channel<OpResult<*>>(Channel.UNLIMITED)
        val runner = AlgoRunner(data, algo, operationInput, CoroutineScope(newCoroutineContext(
                Dispatchers.Default + CoroutineName("Algorithm")
        )))
        runner.start()

        val delayJob = when {
            waitForClick -> launch {
                val clickChannel = Channel<Unit>()
                val clickListener = object {
                    private var wasDown = false
                    @Subscribe
                    fun onMouseClick(event: MouseButtonEvent) {
                        if (event.isDown) {
                            wasDown = true
                        } else if (wasDown) {
                            GlobalScope.launch {
                                clickChannel.send(Unit)
                                clickChannel.close()
                            }
                        }
                    }
                }
                window.eventBus.register(clickListener)
                whileSelect {
                    clickChannel.onReceiveOrNull {
                        false
                    }
                    onTimeout(5) {
                        true
                    }
                }
                window.eventBus.unregister(clickListener)

            }
            else -> launch { delay(1000L) }
        }
        jobs.add(delayJob)

        jobs.add(launch {
            val sync = Sync()
            var running = true
            while (!window.isCloseRequested) {
                sync.sync(FPS)
                window.processEvents()

                if (needsRedraw) {
                    ctx.clearGraphicsState()
                    ctx.pen.uncap()
                    ctx.pen.scale(scale)
                    drawArray(runner.arrays)

                    var extraDelay = 0L
                    while (true) {
                        val next: OpResult<*> = operationInput.poll() ?: break
                        extraDelay = max(extraDelay, drawOperation(next, runner.arrays))
                        needsRedraw = true
                    }

                    ctx.pen.cap()
                    ctx.swapBuffers()
                    if (extraDelay > 0) {
                        val willRedraw = needsRedraw
                        needsRedraw = false
                        val willRun = running
                        running = false
                        GlobalScope.launch {
                            delay(extraDelay)
                            withContext(this@run.coroutineContext) {
                                needsRedraw = willRedraw
                                running = willRun
                            }
                        }
                    }
                }
                if (running) {
                    if (!delayJob.isCompleted) {
                        running = false
                        GlobalScope.launch {
                            delayJob.join()
                            withContext(this@run.coroutineContext) {
                                running = true
                            }
                        }
                    }
                    if (runner.pulse()) {
                        needsRedraw = true
                    } else {
                        running = false
                    }
                }
                yield()
            }
            window.isVisible = false
        })
        return jobs
    }

    @Subscribe
    fun onResize(event: WindowResizeEvent) {
        scale = event.size.toDouble().div(SIZE.toDouble())
        needsRedraw = true
    }

    @Subscribe
    fun onKey(event: KeyStateEvent) {
        if (event.`is`(Key.ESCAPE, KeyState.RELEASED)) {
            window.isCloseRequested = true
        }
    }

    private fun drawArray(arrays: Map<String, VisiArray>) {
        if (arrays.isEmpty()) {
            return
        }
        val pen = window.graphicsContext.pen
        val size = arraySize(arrays)
        val levels = arrays.values.map { it.level }.distinct()
        val arrayHeight = arrayHeight(arrays)
        for (array in arrays.values) {
            drawLevel(arrayHeight, (arrayHeight + 1) * array.level, array.offset, size, array.data)
        }
        pen.color = Color.RED
        for (level in levels) {
            pen.fill { pen.rect(0f, (arrayHeight + 1) * level - 1, SIZE.x.toFloat(), 1f) }
        }
    }

    private fun barWidth(fullSize: Int) =
            (SIZE.x - BORDER_X * 2 - SEPARATION_X * fullSize) / fullSize.toFloat()

    private fun barHeight(allocatedHeight: Float, fullSize: Int) =
            allocatedHeight / fullSize.toFloat()

    private fun boxULCorner(allocatedHeight: Float,
                            fullSize: Int,
                            offset: Int,
                            offsetY: Float,
                            datum: Data): Vector2f {
        val barWidth = barWidth(fullSize)
        val barHeight = barHeight(allocatedHeight, fullSize)
        return Vector2f.from(
                BORDER_X + (SEPARATION_X + barWidth) * offset,
                allocatedHeight + offsetY - barHeight - (BORDER_Y + barHeight * datum.value)
        )
    }

    private fun arrayHeight(arrays: Map<String, VisiArray>): Float {
        val levels = arrays.values.map { it.level }.distinct()
        return (SIZE.y - BORDER_Y * 2) / levels.size.toFloat()
    }

    private fun arraySize(arrays: Map<String, VisiArray>) =
            arrays.values.first { it.level == 0 }.data.size

    private fun drawLevel(allocatedHeight: Float,
                          offsetY: Float,
                          offset: Int,
                          fullSize: Int,
                          data: List<Data>) {
        // assume array represents colors
        val pen = window.graphicsContext.pen
        for (i in data.indices) {
            val datum = data[i]
            pen.color = COLORS[datum.originalIndex % COLORS.size]
            pen.fill {
                val xy = boxULCorner(allocatedHeight, fullSize, offset + i, offsetY, datum)
                pen.rect(xy.x, xy.y, barWidth(fullSize), barHeight(allocatedHeight, fullSize))
            }
        }
    }

    private fun findBoxCenter(arrays: Map<String, VisiArray>, ref: VisiArray.Ref): Vector2f {
        val size = arraySize(arrays)
        val arrayHeight = arrayHeight(arrays)

        val x = boxULCorner(arrayHeight, size,
                ref.array.offset + ref.index,
                (arrayHeight + 1) * ref.array.level,
                ref.value).x + (barWidth(size) / 2f)
        val y = (arrayHeight + 1) * (ref.array.level + 0.5f)
        return Vector2f.from(x, y)
    }

    private fun DigitalPen.fillBox(arrays: Map<String, VisiArray>, ref: VisiArray.Ref) {
        fill {
            val size = arraySize(arrays)
            val arrayHeight = arrayHeight(arrays)

            val x = boxULCorner(arrayHeight, size,
                    ref.array.offset + ref.index, 0f, ref.value).x
            val y = (arrayHeight + 1) * ref.array.level
            roundedRect(x, y, barWidth(size), arrayHeight, 5f)
        }
    }

    private fun DigitalPen.drawLinePointer(from: Vector2f, to: Vector2f, width: Float) {
        fill {
            val angle = from.sub(to).angle() + TrigMath.PI
            // move back along the line from `from` to `to` `width` px
            val backMidpoint = to.sub(Vector2f.createDirectionRad(angle).mul(width))
            // now move perpendicular to that to get the actual triangle points
            val leftPoint = backMidpoint.add(
                    Vector2f.createDirectionRad(angle + TrigMath.HALF_PI).mul(width / 2))
            val rightPoint = backMidpoint.add(
                    Vector2f.createDirectionRad(angle - TrigMath.HALF_PI).mul(width / 2))

            moveTo(to.x, to.y)
            lineTo(leftPoint.x, leftPoint.y)
            lineTo(rightPoint.x, rightPoint.y)
            closePath()
        }
    }

    private val cyan = Color.fromString("#00FFFF")
    private val lastOperations = mutableMapOf<VisiArray.Ref, OpResult<*>>()

    private fun addOperationToLastOperations(opResult: OpResult<*>) {
        refsOf(opResult).forEach {
            lastOperations[it] = opResult
        }
    }

    private fun refsOf(opResult: OpResult<*>): Set<VisiArray.Ref> {
        return when (val op = opResult.op) {
            is Op.Idle, is Op.Fork, is Op.Slice -> setOf()
            is Op.Get -> setOf(op.oldArray.ref(op.index))
            is Op.Copy -> setOf(op.from.asOldArrayRef(), op.to.asOldArrayRef())
            is Op.Swap -> setOf(op.oldArray.ref(op.a), op.oldArray.ref(op.b))
            is Op.Compare -> setOf(op.a.asOldArrayRef(), op.b.asOldArrayRef())
        }
    }

    private fun lastOpAt(ref: VisiArray.Ref): OpResult<*>? {
        return lastOperations[ref]
    }

    private fun drawOperation(opResult: OpResult<*>, arrays: Map<String, VisiArray>): Long {
        drawLastOps(opResult, arrays)
        addOperationToLastOperations(opResult)
        return rawDrawOp(opResult, arrays, true)
    }

    private fun drawLastOps(opResult: OpResult<*>, arrays: Map<String, VisiArray>) {
        when (opResult.op) {
            is Op.Copy, is Op.Swap -> {
                refsOf(opResult)
                        .mapNotNull { lastOpAt(it) }
                        .filter { it.op is Op.Compare }
                        .forEach {
                            rawDrawOp(it, arrays, false)
                        }
            }
        }
    }

    private fun rawDrawOp(opResult: OpResult<*>, arrays: Map<String, VisiArray>, displayBoxes: Boolean): Long {
        val pen = window.graphicsContext.pen
        val pointerSize = SIZE.y * 0.01f
        return when (val op = opResult.op) {
            is Op.Idle, is Op.Fork, is Op.Slice, is Op.Get -> 0L
            is Op.Copy -> {
                val centerA = findBoxCenter(arrays, op.from.asOldArrayRef())
                val centerB = findBoxCenter(arrays, op.to.asOldArrayRef())

                with(pen) {
                    if (displayBoxes) {
                        pen.color = cyan.withAlpha(64)
                        fillBox(arrays, op.from.asOldArrayRef())
                        fillBox(arrays, op.to.asOldArrayRef())
                    }
                    pen.color = cyan
                    fill {
                        lineBetween(centerA.x, centerA.y, centerB.x, centerB.y)
                    }
                    drawLinePointer(centerA, centerB, pointerSize)
                }

                250L
            }
            is Op.Swap -> {
                val centerA = findBoxCenter(arrays, op.oldArray.ref(op.a))
                val centerB = findBoxCenter(arrays, op.oldArray.ref(op.b))
                val delta = arrayHeight(arrays) * 0.25
                val (topA, topB, botA, botB) = listOf(delta, -delta)
                        .flatMap { listOf(it to centerA, it to centerB) }
                        .map { (delta, vector) -> vector.add(0.toDouble(), delta) }

                with(pen) {
                    if (displayBoxes) {
                        pen.color = Color.BLUE.withAlpha(64)
                        fillBox(arrays, op.oldArray.ref(op.a))
                        fillBox(arrays, op.oldArray.ref(op.b))
                    }
                    pen.color = Color.BLUE
                    fill {
                        lineBetween(topA.x, topA.y, topB.x, topB.y)
                    }
                    fill {
                        lineBetween(botA.x, botA.y, botB.x, botB.y)
                    }
                    drawLinePointer(topA, topB, pointerSize)
                    drawLinePointer(botB, botA, pointerSize)
                }

                250L
            }
            is Op.Compare -> {
                val centerA = findBoxCenter(arrays, op.a.asOldArrayRef())
                val centerB = findBoxCenter(arrays, op.b.asOldArrayRef())

                with(pen) {
                    if (displayBoxes) {
                        val res = opResult.result as Int
                        pen.color = when {
                            res < 0 -> Color.GREEN
                            res == 0 -> Color.GRAY
                            else -> Color.RED
                        }.withAlpha(64)
                        fillBox(arrays, op.a.asOldArrayRef())
                        pen.color = when {
                            res < 0 -> Color.RED
                            res == 0 -> Color.GRAY
                            else -> Color.GREEN
                        }.withAlpha(64)
                        fillBox(arrays, op.b.asOldArrayRef())
                    }
                    pen.color = Color.WHITE
                    fill {
                        lineBetween(centerA.x, centerA.y, centerB.x, centerB.y)
                    }
                    drawLinePointer(centerA, centerB, pointerSize)
                    drawLinePointer(centerB, centerA, pointerSize)
                }

                250L
            }
        }
    }

    companion object {

        private val SIZE = Vector2i.from(4096)

        private const val FPS = 15

        private const val BORDER_X = 5
        private const val BORDER_Y = 5
        private const val SEPARATION_X = 0

        private val COLORS = ColorBrewer.Set3.getColorPalette(12)
                .map { c -> Color.fromInt(c.red, c.green, c.blue, c.alpha) }

    }

}
