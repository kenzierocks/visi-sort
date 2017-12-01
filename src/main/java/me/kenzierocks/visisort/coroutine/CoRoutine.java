package me.kenzierocks.visisort.coroutine;

public interface CoRoutine<I, O> {

    void run(Producer<I, O> coro);

}
