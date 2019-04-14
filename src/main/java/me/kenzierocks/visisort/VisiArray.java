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

import java.util.function.Consumer;

import me.kenzierocks.visisort.coroutine.Producer;
import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Fork;
import me.kenzierocks.visisort.op.Get;
import me.kenzierocks.visisort.op.Idle;
import me.kenzierocks.visisort.op.Set;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.Swap;
import me.kenzierocks.visisort.op.UncheckedFuture;

public class VisiArray {

    private final int id;
    private final int parent;
    private final int level;
    private final Data[] data;
    private final int offset;
    private final Producer<Op, Object> coRo;

    public VisiArray(int id, int parent, int level, Data[] data, int offset, Producer<Op, Object> coRo) {
        this.id = id;
        this.parent = parent;
        this.level = level;
        this.data = data;
        this.offset = offset;
        this.coRo = coRo;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return data.length;
    }

    public int getParent() {
        return parent;
    }

    public int getOffset() {
        return offset;
    }

    public int getLevel() {
        return level;
    }

    public Producer<Op, Object> getCoRo() {
        return coRo;
    }

    /**
     * Only for display purposes, do not access directly in algorithms.
     */
    public Data[] getData() {
        return data;
    }

    public Data get(int index) {
        return (Data) coRo.yield(new Get(this, index));
    }

    public void set(int index, Data value) {
        coRo.yield(new Set(this, index, value));
    }

    public int compare(int a, VisiArray other, int b) {
        return (int) coRo.yield(new Compare(this, a, other, b));
    }

    public void swap(int a, int b) {
        coRo.yield(new Swap(this, a, b));
    }

    public VisiArray slice(int from, int to) {
        return (VisiArray) coRo.yield(new Slice(this, from, to));
    }

    @SuppressWarnings("unchecked")
    public UncheckedFuture<Void> fork(Consumer<VisiArray> code) {
        return (UncheckedFuture<Void>) coRo.yield(new Fork(this, code));
    }

    public void idle() {
        coRo.yield(new Idle());
    }

    public void finish() {
        coRo.yield(new Finish());
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

}
