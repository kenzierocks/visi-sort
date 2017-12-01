package me.kenzierocks.visisort.op;

import java.util.concurrent.Future;

import com.google.common.base.Throwables;

public class UFJF<V> implements UncheckedFuture<V> {

    private final Future<V> completion;

    public UFJF(Future<V> completion) {
        this.completion = completion;
    }

    @Override
    public boolean complete() {
        return completion.isDone();
    }

    @Override
    public V get() {
        try {
            return completion.get();
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

}
