package org.cf.apkfile.dex;

import java.util.HashMap;
import java.util.Set;


public class NGramTable {

    private HashMap<NGram, Integer> nGramToCount;

    public NGramTable() {
        this.nGramToCount = new HashMap<>();
    }

    public Set<NGram> gramSet() {
        return this.nGramToCount.keySet();
    }

    public boolean contains(NGram gram) {
        return nGramToCount.containsKey(gram);
    }

    public int get(NGram ngram) {
        Integer value = nGramToCount.get(ngram);
        if (value == null) {
            return 0;
        }
        return value;
    }

    public void add(NGram ngram) {
        add(ngram, 1);
    }

    public void add(NGram ngram, int count) {
        nGramToCount.merge(ngram, count, (a, b) -> a + b);
    }

    public void remove(NGram term) {
        nGramToCount.remove(term);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (NGram gram : nGramToCount.keySet()) {
            builder.append(gram).append(' ').append(get(gram)).append('\n');
        }
        return builder.toString();
    }
}
