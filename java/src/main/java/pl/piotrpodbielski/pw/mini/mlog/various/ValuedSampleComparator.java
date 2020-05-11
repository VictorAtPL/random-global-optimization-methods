package pl.piotrpodbielski.pw.mini.mlog.various;

import java.util.Comparator;

public class ValuedSampleComparator implements Comparator<ValuedSample> {
    @Override
    public int compare(ValuedSample a, ValuedSample b) {
        if (b.getValue() == a.getValue()) {
            return 0;
        }

        return b.getValue() - a.getValue() > 0 ? 1 : -1; // ASC: a - b or DESC: b - a
    }
}
