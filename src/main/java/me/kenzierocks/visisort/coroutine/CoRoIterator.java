package me.kenzierocks.visisort.coroutine;

import java.util.function.Function;

public interface CoRoIterator<I, O> {

    boolean hasNext();

    void processNext(Function<I, O> func);

}