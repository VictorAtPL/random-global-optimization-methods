package pl.piotrpodbielski.pw.mini.mlog.functions;

import java.util.stream.DoubleStream;

public class RastriginFunction extends QualityFunction {
    final double A = 10.0;

    public RastriginFunction(int dimension) {
        this.dimension = dimension;
        this.lowerBoundary = DoubleStream.generate(() -> -5.12).limit(dimension).toArray();
        this.upperBoundary = DoubleStream.generate(() -> 5.12).limit(dimension).toArray();
    }

    @Override
    public double getValue(double[] x) {
        if (this.dimension != x.length)
            throw new IllegalArgumentException("Wrong argument size");
        double result = 0.0;

        for (int dim = 0; dim < this.dimension; ++dim)
        {
            result += x[dim] * x[dim];
            result -= A * Math.cos(2 * Math.PI * x[dim]);
        }

        result += A * this.dimension;
        return result;
    }
}
