package me.kenzierocks.visisort.op;

public interface UncheckedFuture<V> {
    
    boolean complete();

    V get();

}
