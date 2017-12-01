package me.kenzierocks.visisort;

import me.kenzierocks.visisort.coroutine.Producer;
import me.kenzierocks.visisort.op.Compare;
import me.kenzierocks.visisort.op.Finish;
import me.kenzierocks.visisort.op.Get;
import me.kenzierocks.visisort.op.Set;
import me.kenzierocks.visisort.op.Slice;
import me.kenzierocks.visisort.op.Swap;

public class VisiArray {

    private final int id;
    private final int parent;
    private final int level;
    private final int[] data;
    private final int offset;
    private final Producer<SortOp, Object> coRo;

    public VisiArray(int id, int parent, int level, int[] data, int offset, Producer<SortOp, Object> coRo) {
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

    public Producer<SortOp, Object> getCoRo() {
        return coRo;
    }

    /**
     * Only for display purposes, do not access directly in algorithms.
     */
    public int[] getData() {
        return data;
    }

    public int get(int index) {
        return (int) coRo.yield(new Get(id, index));
    }

    public void set(int index, int value) {
        coRo.yield(new Set(id, index, value));
    }

    public int compare(int a, VisiArray other, int b) {
        return (int) coRo.yield(new Compare(id, a, other.id, b));
    }

    public void swap(int a, int b) {
        coRo.yield(new Swap(id, a, b));
    }

    public VisiArray slice(int from, int to) {
        return (VisiArray) coRo.yield(new Slice(this.id, from, to));
    }

    public void finish() {
        coRo.yield(new Finish());
    }

}
