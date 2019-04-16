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

import com.flowpowered.math.vector.Vector2d
import com.flowpowered.math.vector.Vector2i
import com.google.common.collect.ImmutableListMultimap
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
import kotlinx.coroutines.channels.Channel
import org.jcolorbrewer.ColorBrewer
import org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback
import java.util.concurrent.TimeUnit

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

            ctx.pen.uncap()
            ctx.pen.scale(scale)
            drawArray(runner.arrays)
            if (running) {
                running = runner.pulse()
                if (!running) {
                    startTime = java.lang.Long.MAX_VALUE
                }
            }
            while (true) {
                val next = operationInput.poll() ?: break
                drawOperation(next.op)
            }
            ctx.pen.cap()

            ctx.swapBuffers()
            needsRedraw = false

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

    private fun drawArray(arrays: List<VisiArray>) {
        if (arrays.isEmpty()) {
            return
        }
        val pen = window.graphicsContext.pen
        val byLevel = collectByLevel(arrays)
        val size = arrays[0].data.size
        val arrayHeight = (SIZE.y - BORDER_Y * 2) / byLevel.keySet().size.toFloat()
        for (i in byLevel.keySet()) {
            for (va in byLevel.get(i)) {
                drawLevel(arrayHeight, (arrayHeight + 1) * i, va.offset, size, va.data)
            }
            pen.color = Color.RED
            pen.fill { pen.rect(0f, (arrayHeight + 1) * i - 1, SIZE.x.toFloat(), 1f) }
        }
    }

    private fun collectByLevel(arrays: List<VisiArray>): ImmutableListMultimap<Int, VisiArray> {
        val levels = ImmutableListMultimap.builder<Int, VisiArray>()
        for (array in arrays) {
            levels.put(array.level, array)
        }
        return levels.build()
    }

    private fun drawLevel(allocatedHeight: Float,
                          offsetY: Float,
                          offset: Int,
                          fullSize: Int,
                          data: List<Data>) {
        val barWidth = (SIZE.x - BORDER_X * 2 - SEPARATION_X * fullSize) / fullSize.toFloat()
        val barHeight = allocatedHeight / fullSize.toFloat()
        // assume array represents colors
        val pen = window.graphicsContext.pen
        for (i in data.indices) {
            val datum = data[i]
            pen.color = COLORS[datum.originalIndex % COLORS.size]
            pen.fill {
                val x = BORDER_X + (SEPARATION_X + barWidth) * (offset + i)
                val y = allocatedHeight + offsetY - barHeight - (BORDER_Y + barHeight * datum.value)
                pen.rect(x, y, barWidth, barHeight)
            }
        }
    }

    private interface OpDraw<O : Op<*>> {

        fun draw(pen: DigitalPen, op: O)

    }

    private fun drawOperation(nextOp: Op<*>) {
//        opDraw.draw(window.graphicsContext.pen, )
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
