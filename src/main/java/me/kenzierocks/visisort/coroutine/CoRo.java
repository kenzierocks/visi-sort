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
package me.kenzierocks.visisort.coroutine;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

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

    private final String id = UUID.randomUUID().toString();

    private final Lock coroutineTransferLock = new ReentrantLock();
    private final Condition condTransI = coroutineTransferLock.newCondition();
    private final Condition condTransO = coroutineTransferLock.newCondition();
    private Optional<I> transI;
    private Optional<O> transO;
    private boolean complete = false;

    private void inLock(Runnable action) {
        inLock(() -> {
            action.run();
            return null;
        });
    }

    private <T> T inLock(Supplier<T> action) {
        coroutineTransferLock.lock();
        try {
            return action.get();
        } finally {
            coroutineTransferLock.unlock();
        }
    }

    /**
     * Call this to complete the coroutine.
     */
    public void complete() {
        inLock(() -> {
            complete = true;
            condTransI.signal();
        });
    }

    /**
     * Yield a value from the coroutine to the consumer. This will pause the
     * coroutine until the consumer passes back a value.
     */
    @Override
    public O yield(I value) {
        return inLock(() -> {
            transI = Optional.ofNullable(value);
            condTransI.signal();
            while (transO == null) {
                condTransO.awaitUninterruptibly();
            }
            Optional<O> val = transO;
            transO = null;
            return val.orElse(null);
        });
    }

    @Override
    public boolean hasNext() {
        return inLock(this::awaitTransIOrComplete);
    }

    /**
     * Process the next value from this coroutine.
     * 
     * @param function
     *            - the function to apply to the next value
     */
    @Override
    public void processNext(Function<I, O> function) {
        inLock(() -> {
            if (!awaitTransIOrComplete()) {
                throw new NoSuchElementException("End of coroutine reached.");
            }
            Optional<I> val = transI;
            transI = null;
            O out = function.apply(val.orElse(null));
            transO = Optional.ofNullable(out);
            condTransO.signal();
        });
    }

    // following functions assume lock is held

    private boolean awaitTransIOrComplete() {
        while (transI == null && !complete) {
            condTransI.awaitUninterruptibly();
        }
        return transI != null;
    }

}
