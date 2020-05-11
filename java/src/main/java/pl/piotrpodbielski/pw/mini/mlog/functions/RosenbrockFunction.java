package pl.piotrpodbielski.pw.mini.mlog.functions;

import java.util.stream.DoubleStream;

public class RosenbrockFunction extends QualityFunction {

    public RosenbrockFunction(int dimension) {
        this.dimension = dimension;
        this.lowerBoundary = DoubleStream.generate(() -> -2.048).limit(dimension).toArray();
        this.upperBoundary = DoubleStream.generate(() -> 2.048).limit(dimension).toArray();
    }

    @Override
    public double getValue(double[] x) {
        if (this.dimension != x.length)
            throw new IllegalArgumentException("Wrong argument size");
        double result = 0.0;
        for (int dim = 0; dim < dimension - 1; ++dim)
        {
            result += 100 * (x[dim+1]-x[dim]*x[dim]) *
                    (x[dim + 1] - x[dim] * x[dim]);
            result += (1 - x[dim]) * (1 - x[dim]);
        }
        return result;
    }
}
