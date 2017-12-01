package me.kenzierocks.visisort.op;

import java.util.List;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Compare implements SortOp {

    public final int arrayA;
    public final int a;
    public final int arrayB;
    public final int b;

    public Compare(int arrayA, int a, int arrayB, int b) {
        this.arrayA = arrayA;
        this.a = a;
        this.arrayB = arrayB;
        this.b = b;
    }

    @Override
    public Integer process(List<VisiArray> arrays) {
        return Integer.compare(arrays.get(arrayA).getData()[a], arrays.get(arrayB).getData()[b]);
    }

    @Override
    public String toString() {
        return "int result = compare(arrays[" + arrayA + "][" + a + "], arrays[" + arrayB + "][" + b + "])";
    }

}
