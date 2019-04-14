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
package me.kenzierocks.visisort;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import com.techshroom.unplanned.blitter.GraphicsContext;
import com.techshroom.unplanned.blitter.pen.DigitalPen;
import com.techshroom.unplanned.core.util.Color;
import com.techshroom.unplanned.core.util.Sync;
import com.techshroom.unplanned.event.keyboard.KeyState;
import com.techshroom.unplanned.event.keyboard.KeyStateEvent;
import com.techshroom.unplanned.event.window.WindowResizeEvent;
import com.techshroom.unplanned.input.Key;
import com.techshroom.unplanned.window.Window;
import com.techshroom.unplanned.window.WindowSettings;
import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Get;
import me.kenzierocks.visisort.op.Set;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.Swap;
import org.jcolorbrewer.ColorBrewer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.glfwSetWindowRefreshCallback;

public class VisiSort {

    private static final Vector2i SIZE = Vector2i.from(4096);

    private final Window window;
    private final SortAlgo algo;
    private Vector2d scale;
    private volatile boolean needsRedraw = false;

    public VisiSort(SortAlgo algo) {
        window = WindowSettings.builder()
                .title("VisiSort - " + algo.getName())
                .msaa(true)
                .build().createWindow();
        window.getEventBus().register(this);
        this.algo = algo;
    }

    public void run(Data[] data) {
        GraphicsContext ctx = window.getGraphicsContext();
        ctx.makeActiveContext();
        window.setVsyncOn(true);
        window.setVisible(true);
        Vector2i size = window.getSize();
        window.getEventBus().post(WindowResizeEvent.create(window, size.getX(), size.getY()));
        glfwSetWindowRefreshCallback(window.getWindowPointer(), (win) -> {
            needsRedraw = true;
        });

        AlgoRunner runner = new AlgoRunner(data, algo, this::drawOperation);

        Sync sync = new Sync();
        boolean running = false;

        runner.start();
        // wait a bit, for everything to init
        long startTime = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (!window.isCloseRequested()) {
            sync.sync(60);
            ctx.clearGraphicsState();
            window.processEvents();

            ctx.getPen().uncap();
            ctx.getPen().scale(scale);
            drawArray(runner.getArrays());
            if (running) {
                running = runner.pulse();
                if (!running) {
                    startTime = Long.MAX_VALUE;
                }
            }
            ctx.getPen().cap();

            ctx.swapBuffers();
            needsRedraw = false;

            if (!running) {
                // save frames
                while (!window.isCloseRequested() && !needsRedraw) {
                    sync.sync(60);
                    window.processEvents();
                    if (startTime <= System.nanoTime()) {
                        running = true;
                        needsRedraw = true;
                    }
                }
            }
        }

        window.setVisible(false);
    }

    @Subscribe
    public void onResize(WindowResizeEvent event) {
        scale = event.getSize().toDouble().div(SIZE.toDouble());
        needsRedraw = true;
    }

    @Subscribe
    public void onKey(KeyStateEvent event) {
        if (event.is(Key.ESCAPE, KeyState.RELEASED)) {
            window.setCloseRequested(true);
        }
    }

    private static final int BORDER_X = 5;
    private static final int BORDER_Y = 5;
    private static final int SEPARATION_X = 2;

    private void drawArray(List<VisiArray> arrays) {
        if (arrays.isEmpty()) {
            return;
        }
        DigitalPen pen = window.getGraphicsContext().getPen();
        pen.setColor(Color.RED);
        ImmutableListMultimap<Integer, VisiArray> byLevel = collectByLevel(arrays);
        int size = arrays.get(0).getSize();
        float arrayHeight = (SIZE.getY() - BORDER_Y * 2) / (float) byLevel.keySet().size();
        for (int i : byLevel.keySet()) {
            Data[] level = new Data[size];
            for (VisiArray va : byLevel.get(i)) {
                System.arraycopy(va.getData(), 0, level, va.getOffset(), va.getSize());
            }
            drawLevel(arrayHeight, (arrayHeight + 1) * i, size, level);
            pen.fill(() -> {
                pen.rect(0, (arrayHeight + 1) * i - 1, SIZE.getX(), 1);
            });
        }
    }

    private ImmutableListMultimap<Integer, VisiArray> collectByLevel(List<VisiArray> arrays) {
        ImmutableListMultimap.Builder<Integer, VisiArray> levels = ImmutableListMultimap.builder();
        for (VisiArray array : arrays) {
            levels.put(array.getLevel(), array);
        }
        return levels.build();
    }

    private static final List<Color> COLORS = Stream.of(ColorBrewer.Set3.getColorPalette(12))
        .map(c -> Color.fromInt(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()))
        .collect(Collectors.toList());

    private void drawLevel(float allocatedHeight, float offsetY, int size, Data[] array) {
        float barWidth = (SIZE.getX() - BORDER_X * 2 - SEPARATION_X * size) / (float) size;
        float barHeight = (allocatedHeight) / (float) size;
        // assume array represents colors
        DigitalPen pen = window.getGraphicsContext().getPen();
        for (int i = 0; i < array.length; i++) {
            int barIndex = i;
            Data data = array[barIndex];
            if (data == null) {
                continue;
            }
            pen.setColor(COLORS.get(data.originalIndex() % COLORS.size()));
            pen.fill(() -> {
                float x = BORDER_X + (SEPARATION_X + barWidth) * barIndex;
                float y = (allocatedHeight + offsetY) - barHeight - (BORDER_Y + barHeight * data.value());
                pen.rect(x, y, barWidth, barHeight);
            });
        }
    }

    private interface OpDraw<O extends SortOp> {

        void draw(DigitalPen pen, O op);

    }

    private static final Map<Class<?>, OpDraw<?>> opDraws = ImmutableMap.<Class<?>, OpDraw<?>> builder()
            .put(Compare.class, (pen, op) -> {
            })
            .put(Finish.class, (pen, op) -> {
            })
            .put(Get.class, (pen, op) -> {
            })
            .put(Set.class, (pen, op) -> {
            })
            .put(Slice.class, (pen, op) -> {
            })
            .put(Swap.class, (pen, op) -> {
            }).build();

    private void drawOperation(SortOp nextOp) {
        @SuppressWarnings("unchecked")
        OpDraw<SortOp> opDraw = (OpDraw<SortOp>) opDraws.get(nextOp.getClass());
        opDraw.draw(window.getGraphicsContext().getPen(), nextOp);
    }

}
