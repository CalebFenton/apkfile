package org.cf.apkfile.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class NGramTable {

    private final Map<NGram, Integer> nGramToCount;

    public NGramTable() {
        this.nGramToCount = new HashMap<>();
    }

    public Set<NGram> gramSet() {
        return this.nGramToCount.keySet();
    }

    public boolean contains(NGram nGram) {
        return nGramToCount.containsKey(nGram);
    }

    public int get(NGram nGram) {
        Integer value = nGramToCount.get(nGram);
        if (value == null) {
            return 0;
        }
        return value;
    }

    public void add(NGram nGram) {
        add(nGram, 1);
    }

    public void add(NGram nGram, int count) {
        nGramToCount.merge(nGram, count, (a, b) -> a + b);
    }

    public void remove(NGram nGram) {
        nGramToCount.remove(nGram);
    }

    public Map<NGram, Integer> getNGramToCount() {
        return nGramToCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NGram nGram : nGramToCount.keySet()) {
            sb.append(nGram)
                    .append(' ')
                    .append(get(nGram))
                    .append('\n');
        }

        return sb.toString();
    }
}
