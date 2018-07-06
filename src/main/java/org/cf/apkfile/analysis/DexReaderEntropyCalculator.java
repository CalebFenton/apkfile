package org.cf.apkfile.analysis;

import gnu.trove.map.TByteIntMap;
import gnu.trove.map.hash.TByteIntHashMap;
import org.jf.dexlib2.dexbacked.DexReader;

public class DexReaderEntropyCalculator {

    private final DexReader dexReader;
    private final TByteIntMap counts;

    private int size;

    public DexReaderEntropyCalculator(DexReader dexReader) {
        this.dexReader = dexReader;
        counts = new TByteIntHashMap();
    }

    public void calculate(int offset, int size) {
        this.size = size;
        for (int currentOffset = offset; currentOffset < offset + size; currentOffset++) {
            byte b = (byte) dexReader.readByte(currentOffset);
            counts.adjustOrPutValue(b, 1, 1);
        }

    }

    public void reset() {
        counts.clear();
    }

    public double entropy() {
        double entropy = 0;
        for (int count : counts.values()) {
            double prob = (double) count / size;
            entropy -= prob * Log2(prob);
        }
        return entropy;
    }

    public double perplexity() {
        return Math.pow(2, entropy());
    }

    private static double Log2(double n) {
        return Math.log(n) / Math.log(2);
    }

}
