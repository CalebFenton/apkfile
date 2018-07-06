package org.cf.apkfile.analysis;

import java.util.Arrays;
import java.util.zip.Adler32;

public class NGram {

    private final int[] data;
    private int hashCode;

    NGram(int n) {
        data = new int[n];
    }

    void set(int index, int value) {
        data[index] = value;
        hashCode = 0;
    }

    public int get(int index) {
        return data[index];
    }

    public int size() {
        return data.length;
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            Adler32 chk = new Adler32();
            for (int d : data) {
                chk.update(d);
            }
            hashCode = (int) chk.getValue();
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object rhs) {
        if (rhs == this) {
            return true;
        }
        
        if (rhs instanceof NGram) {
            NGram other = (NGram) rhs;
            if (hashCode() == other.hashCode()) {
                return Arrays.equals(data, other.data);
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int d : data) {
            output.append(Integer.toHexString(d));
            output.append(' ');
        }
        output.setLength(output.length() - 1);

        return output.toString();
    }


    public static NGram create(int order, int... args) {
        NGram ngram = new NGram(order);
        for (int ix = 0; ix < order; ++ix) {
            ngram.set(ix, args[ix]);
        }

        return ngram;
    }

}
