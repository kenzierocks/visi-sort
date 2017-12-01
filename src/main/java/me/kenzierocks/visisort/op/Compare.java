package me.kenzierocks.visisort.op;

import me.kenzierocks.visisort.SortOp;
import me.kenzierocks.visisort.VisiArray;

public class Compare implements SortOp {

    public final VisiArray arrayA;
    public final int a;
    public final VisiArray arrayB;
    public final int b;

    public Compare(VisiArray arrayA, int a, VisiArray arrayB, int b) {
        this.arrayA = arrayA;
        this.a = a;
        this.arrayB = arrayB;
        this.b = b;
    }

    @Override
    public Integer process() {
        return Integer.compare(arrayA.getData()[a], arrayB.getData()[b]);
    }

    @Override
    public String toString() {
        return "int result = compare(arrays[" + arrayA + "][" + a + "], arrays[" + arrayB + "][" + b + "])";
    }

}
