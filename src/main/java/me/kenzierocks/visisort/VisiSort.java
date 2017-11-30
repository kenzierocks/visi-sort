package me.kenzierocks.visisort;

import java.util.List;
import java.util.Map;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector2i;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import com.techshroom.unplanned.blitter.GraphicsContext;
import com.techshroom.unplanned.blitter.pen.DigitalPen;
import com.techshroom.unplanned.blitter.pen.PenInk;
import com.techshroom.unplanned.core.util.Color;
import com.techshroom.unplanned.core.util.Sync;
import com.techshroom.unplanned.event.window.WindowResizeEvent;
import com.techshroom.unplanned.window.Window;
import com.techshroom.unplanned.window.WindowSettings;

import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Get;
import me.kenzierocks.visisort.op.NewArray;
import me.kenzierocks.visisort.op.Set;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.Swap;

public class VisiSort {

    private static final Vector2i SIZE = Vector2i.from(4096);

    private final Window window;
    private final SortAlgo algo;
    private Vector2d scale;

    public VisiSort(SortAlgo algo) {
        window = WindowSettings.builder()
                .title("VisiSort - " + algo.getName())
                .msaa(true)
                .screenSize(320, 200)
                .build().createWindow();
        window.getEventBus().register(this);
        this.algo = algo;
    }

    public void run(int[] data) {
        GraphicsContext ctx = window.getGraphicsContext();
        ctx.makeActiveContext();
        window.setVsyncOn(true);
        window.setVisible(true);
        Vector2i size = window.getSize();
        window.getEventBus().post(WindowResizeEvent.create(window, size.getX(), size.getY()));

        AlgoRunner runner = new AlgoRunner(data, algo, this::drawOperation);

        Sync sync = new Sync();
        boolean running = true;

        while (!window.isCloseRequested()) {
            sync.sync(60);
            ctx.clearGraphicsState();
            window.processEvents();

            ctx.getPen().uncap();
            ctx.getPen().scale(scale);
            drawArray(runner.getArrays());
            if (running) {
                running &= runner.pulse();
            }
            ctx.getPen().cap();

            ctx.swapBuffers();
        }

        window.setVisible(false);
    }

    @Subscribe
    public void onResize(WindowResizeEvent event) {
        scale = event.getSize().toDouble().div(SIZE.toDouble());
    }

    private static final int BORDER_X = 5;
    private static final int BORDER_Y = 5;
    private static final int SEPARATION_X = 2;

    private void drawArray(List<int[]> arrays) {
        float arrayHeight = SIZE.getY() / (float) arrays.size();
        for (int i = 0; i < arrays.size(); i++) {
            drawArray(arrayHeight, arrayHeight * i, arrays.get(i));
        }
    }

    private void drawArray(float allocatedHeight, float offsetY, int[] array) {
        float barWidth = (SIZE.getX() - BORDER_X * 2 - SEPARATION_X * array.length) / (float) array.length;
        float barHeight = (allocatedHeight - BORDER_Y * 2) / (float) array.length;
        // assume array represents colors
        DigitalPen pen = window.getGraphicsContext().getPen();
        for (int i = 0; i < array.length; i++) {
            int barIndex = i;
            PenInk ink = pen.getInk(Color.hashed(array[i]));
            pen.fill(ink, () -> {
                float x = BORDER_X + (SEPARATION_X + barWidth) * barIndex;
                float y = (allocatedHeight + offsetY) - barHeight - (BORDER_Y + barHeight * array[barIndex]);
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
            .put(NewArray.class, (pen, op) -> {
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
