package me.kenzierocks.visisort.coroutine;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Support for coroutine-like actions. Given to instances of {@link CoRoutine}
 * to handle the transfers.
 * 
 * @param <I>
 *            - input type from {@link CoRoutine}
 * @param <O>
 *            - output type to {@link CoRoutine}
 */
public class CoRo<I, O> implements Producer<I, O>, CoRoIterator<I, O> {

    private final Lock coroutineTransferLock = new ReentrantLock();
    private final Condition condTransI = coroutineTransferLock.newCondition();
    private final Condition condTransO = coroutineTransferLock.newCondition();
    private Optional<I> transI;
    private Optional<O> transO;
    private boolean complete = false;

    /**
     * Call this to complete the coroutine.
     */
    public void complete() {
        coroutineTransferLock.lock();
        try {
            complete = true;
            condTransI.signal();
        } finally {
            coroutineTransferLock.unlock();
        }
    }

    /**
     * Yield a value from the coroutine to the consumer. This will pause the
     * coroutine until the consumer passes back a value.
     */
    @Override
    public O yield(I value) {
        coroutineTransferLock.lock();
        try {
            transI = Optional.ofNullable(value);
            condTransI.signal();
            while (transO == null) {
                condTransO.awaitUninterruptibly();
            }
            Optional<O> val = transO;
            transO = null;
            return val.orElse(null);
        } finally {
            coroutineTransferLock.unlock();
        }
    }

    @Override
    public boolean hasNext() {
        coroutineTransferLock.lock();
        try {
            return awaitTransIOrComplete();
        } finally {
            coroutineTransferLock.unlock();
        }
    }

    /**
     * Process the next value from this coroutine.
     * 
     * @param function
     *            - the function to apply to the next value
     */
    @Override
    public void processNext(Function<I, O> function) {
        coroutineTransferLock.lock();
        try {
            if (!awaitTransIOrComplete()) {
                throw new NoSuchElementException("End of coroutine reached.");
            }
            Optional<I> val = transI;
            transI = null;
            O out = function.apply(val.orElse(null));
            transO = Optional.ofNullable(out);
            condTransO.signal();
        } finally {
            coroutineTransferLock.unlock();
        }
    }

    // following functions assume lock is held

    private boolean awaitTransIOrComplete() {
        while (transI == null && !complete) {
            condTransI.awaitUninterruptibly();
        }
        return transI != null;
    }

}
