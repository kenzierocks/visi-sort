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
package me.kenzierocks.visisort.algo

import me.kenzierocks.visisort.OpChannel
import me.kenzierocks.visisort.SortAlgo
import me.kenzierocks.visisort.VisiArray
import java.util.concurrent.ThreadLocalRandom

class BogoSort : SortAlgo {

    override val name = "Bogo Sort"

    override suspend fun OpChannel.sort(array: VisiArray) {
        main@ while (true) {
            shuffle(array)
            for (i in 0 until array.size - 1) {
                if (compare(array.ref(i), array.ref(i + 1)) > 0) {
                    continue@main
                }
            }
            break
        }
    }

    private suspend fun OpChannel.shuffle(array: VisiArray) {
        for (i in 0 until array.size - 1) {
            val j = ThreadLocalRandom.current().nextInt(i, array.size)
            array.swap(i, j)
        }
    }

}
