package me.kenzierocks.visisort.algo;

import me.kenzierocks.visisort.SortAlgo;
import me.kenzierocks.visisort.VisiArray;

public class MergeSort implements SortAlgo {

    private static final class MergeArray {

        private final VisiArray data;
        private final MergeArray left;
        private final MergeArray right;

        MergeArray(VisiArray array) {
            data = array;
            if (array.getSize() > 1) {
                int leftAmt = array.getSize() / 2;
                this.left = new MergeArray(array.slice(0, leftAmt));
                this.right = new MergeArray(array.slice(leftAmt, array.getSize()));
            } else {
                left = right = null;
            }
        }

        public int size() {
            return data.getSize();
        }

        public int get(int index) {
            return data.get(index);
        }

        public void mergeLR() {
            if (left == null || right == null) {
                return;
            }
            left.mergeLR();
            right.mergeLR();

            int index = 0;
            int indexLeft = 0;
            int indexRight = 0;
            while (indexLeft < left.size() && indexRight < right.size()) {

                int append;
                if (left.data.compare(indexLeft, right.data, indexRight) <= 0) {
                    append = left.get(indexLeft);
                    indexLeft++;
                } else {
                    append = right.get(indexRight);
                    indexRight++;
                }
                data.set(index, append);
                index++;
            }

            while (indexLeft < left.size()) {
                data.set(index, left.get(indexLeft));
                indexLeft++;
                index++;
            }

            while (indexRight < right.size()) {
                data.set(index, right.get(indexRight));
                indexRight++;
                index++;
            }
        }

    }

    @Override
    public String getName() {
        return "Merge Sort";
    }

    @Override
    public void sort(VisiArray array) {
        MergeArray merger = new MergeArray(array);
        merger.mergeLR();
    }

}
