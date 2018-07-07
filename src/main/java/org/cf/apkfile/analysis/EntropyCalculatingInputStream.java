package org.cf.apkfile.analysis;

import gnu.trove.map.TByteIntMap;
import gnu.trove.map.hash.TByteIntHashMap;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EntropyCalculatingInputStream extends BufferedInputStream {

    private final TByteIntMap counts = new TByteIntHashMap();

    private long total = 0;

    public EntropyCalculatingInputStream(InputStream in) {
        super(in);
    }

    public EntropyCalculatingInputStream(InputStream in, int size) {
        super(in, size);
    }

    @Override
    public int read() throws IOException {
        int value = super.read();
        if (value != -1) {
            total++;
            counts.adjustOrPutValue((byte) value, 1, 1);
        }

        return value;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int result = super.read(b, off, len);
        if (result > 0) {
            total += result;
            for (byte z : b) {
                counts.adjustOrPutValue(z, 1, 1);
            }
        }

        return result;
    }

    public double entropy() {
        double entropy = 0;
        for (int count : counts.values()) {
            double prob = (double) count / total;
            entropy -= prob * Log2(prob);
        }
        return entropy;
    }

    public double perplexity() {
        return Math.pow(2, entropy());
    }

    public long total() {
        return total;
    }

    private static double Log2(double n) {
        return Math.log(n) / Math.log(2);
    }
}
