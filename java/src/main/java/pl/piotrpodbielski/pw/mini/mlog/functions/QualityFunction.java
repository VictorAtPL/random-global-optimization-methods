package pl.piotrpodbielski.pw.mini.mlog.functions;

public abstract class QualityFunction {
    int dimension;
    double[] lowerBoundary;
    double[] upperBoundary;

    public abstract double getValue(double[] x);

    public int getDimension() {
        return dimension;
    }

    public double[] getLowerBoundary() {
        return lowerBoundary;
    }

    public double[] getUpperBoundary() {
        return upperBoundary;
    }
}
