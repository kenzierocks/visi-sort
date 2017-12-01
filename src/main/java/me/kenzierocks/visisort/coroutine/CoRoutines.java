package me.kenzierocks.visisort.coroutine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class CoRoutines {

    private static final ExecutorService coRoThreads = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setDaemon(true).setNameFormat("coro-%d").build());

    /**
     * Runs a co-routine. This handles all of the proper state for managing the
     * {@link CoRo} object.
     * 
     * @param coRoutine
     * @return
     */
    public static <I, O> CoRoIterator<I, O> startCoRoutine(CoRoutine<I, O> coRoutine) {
        CoRo<I, O> coRo = new CoRo<>();
        coRoThreads.execute(() -> {
            try {
                coRoutine.run(coRo);
            } finally {
                coRo.complete();
            }
        });
        return coRo;
    }

}
