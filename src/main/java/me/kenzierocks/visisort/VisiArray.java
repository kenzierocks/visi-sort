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
    private final int[] data;
    private final int offset;
    private final Producer<Op, Object> coRo;

    public VisiArray(int id, int parent, int level, int[] data, int offset, Producer<Op, Object> coRo) {
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
    public int[] getData() {
        return data;
    }

    public int get(int index) {
        return (int) coRo.yield(new Get(this, index));
    }

    public void set(int index, int value) {
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
