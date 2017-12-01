package me.kenzierocks.visisort.coroutine;

public interface Producer<I, O> {

    O yield(I in);

}