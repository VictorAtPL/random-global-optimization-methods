package pl.piotrpodbielski.pw.mini.mlog.various;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;

import java.util.Arrays;

public class ValuedSample {
    private final int iteration;
    double[] x;
    int step;
    double value;
    double bestValue;
    private int idx = 0;
    private static int lastIteration = -1;
    private static int lastStep;
    private static double globalBestValue;

    public ValuedSample(double[] x, QualityFunction function, int iteration) {
        this.x = Arrays.copyOf(x, x.length);
        this.value = function.getValue(x);

        if (lastIteration != iteration) {
            lastStep = 0;
            globalBestValue = Double.MAX_VALUE;
            lastIteration = iteration;
        }
        if (this.value < globalBestValue) {
            globalBestValue = this.value;
        }
        this.step = lastStep++;
        this.bestValue = globalBestValue;
        this.iteration = iteration;
    }

    public ValuedSample(double[] x, QualityFunction function, int iteration, int idx) {
        this(x, function, iteration);
        this.idx = idx;
    }

    public int getStep() {
        return step;
    }

    public double[] getX() {
        return x;
    }

    public double getValue() {
        return value;
    }

    public int getIteration() {
        return iteration;
    }

    public double getBestValue() {
        return bestValue;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }
}
