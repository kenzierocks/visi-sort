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
import com.techshroom.unplanned.event.window.WindowResizeEvent
import com.techshroom.unplanned.input.Key
import com.techshroom.unplanned.window.Window
import com.techshroom.unplanned.window.WindowSettings
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import me.kenzierocks.visisort.math.angle
import org.jcolorbrewer.ColorBrewer
import org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback
import java.util.concurrent.TimeUnit
import kotlin.math.min

class VisiSort(private val algo: SortAlgo) {

    private val window: Window = WindowSettings.builder()
            .title("VisiSort - " + algo.name)
            .msaa(true)
            .build().createWindow()
    private var scale: Vector2d? = null
    @Volatile
    private var needsRedraw = false

    init {
        window.eventBus.register(this)
    }

    @UseExperimental(ExperimentalCoroutinesApi::class)
    suspend fun run(data: MutableList<Data>) {
        val ctx = window.graphicsContext
        ctx.makeActiveContext()
        window.isVsyncOn = true
        window.isVisible = true
        val size = window.size
        window.eventBus.post(WindowResizeEvent.create(window, size.x, size.y))
        glfwSetWindowRefreshCallback(window.windowPointer) { needsRedraw = true }

        val operationInput = Channel<OpResult<*>>(Channel.UNLIMITED)
        val runner = AlgoRunner(data, algo, operationInput, CoroutineScope(
                Dispatchers.Default + CoroutineName("Algorithm")
        ))

        val sync = Sync()
        var running = false

        runner.start()
        // wait a bit, for everything to init
        var startTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(1)
        while (!window.isCloseRequested) {
            sync.sync(FPS)
            ctx.clearGraphicsState()
            window.processEvents()

            var extraDelay = 0L

            ctx.pen.uncap()
            ctx.pen.scale(scale)
            drawArray(runner.arrays)
            needsRedraw = !operationInput.isEmpty
            while (true) {
                val next = operationInput.poll() ?: break
                extraDelay += drawOperation(next, runner.arrays)
            }
            if (running) {
                running = runner.pulse()
                if (!running) {
                    startTime = java.lang.Long.MAX_VALUE
                }
            }
            ctx.pen.cap()

            ctx.swapBuffers()
            delay(extraDelay)

            if (!running) {
                // save frames
                while (!window.isCloseRequested && !needsRedraw) {
                    sync.sync(FPS)
                    window.processEvents()
                    if (startTime <= System.nanoTime()) {
                        running = true
                        needsRedraw = true
                    }
                }
            }
        }

        window.isVisible = false
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
        val size = arrays.values.first { it.level == 0 }.data.size
        val levels = arrays.values.map { it.level }.distinct()
        val arrayHeight = (SIZE.y - BORDER_Y * 2) / levels.size.toFloat()
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
        val size = arrays.values.first { it.level == 0 }.data.size
        val levels = arrays.values.map { it.level }.distinct()
        val arrayHeight = (SIZE.y - BORDER_Y * 2) / levels.size.toFloat()

        return boxULCorner(arrayHeight, size,
                ref.array.offset + ref.index,
                (arrayHeight + 1) * ref.array.level,
                ref.value)
                .add(barWidth(size) / 2, barHeight(arrayHeight, size) / 2)
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

    private fun drawOperation(opResult: OpResult<*>, arrays: Map<String, VisiArray>): Long {
        val pen = window.graphicsContext.pen
        return when (val op = opResult.op) {
            is Op.Idle, is Op.Fork, is Op.Slice, is Op.Get, is Op.Set -> 0L
            is Op.Swap -> {
                val centerA = findBoxCenter(arrays, op.array.ref(op.a))
                val centerB = findBoxCenter(arrays, op.array.ref(op.b))
                val arrowWidth = min(0.05f * centerA.distance(centerB), 100f)

                with(pen) {
                    pen.color = Color.BLUE
                    fill {
                        lineBetween(centerA.x, centerA.y, centerB.x, centerB.y)
                    }
                    drawLinePointer(centerA, centerB, arrowWidth)
                    drawLinePointer(centerB, centerA, arrowWidth)
                }

                150L
            }
            is Op.Compare -> {
                val centerA = findBoxCenter(arrays, op.a.asOldArrayRef())
                val centerB = findBoxCenter(arrays, op.b.asOldArrayRef())
                val arrowWidth = min(0.05f * centerA.distance(centerB), 100f)

                with(pen) {
                    pen.color = when {
                        (opResult.result as Int == 1) -> Color.GREEN
                        else -> Color.RED
                    }
                    fill {
                        lineBetween(centerA.x, centerA.y, centerB.x, centerB.y)
                    }
                    drawLinePointer(centerA, centerB, arrowWidth)
                    drawLinePointer(centerB, centerA, arrowWidth)
                }

                150L
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
