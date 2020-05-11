package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.various.NotImplementedException;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

public abstract class OptimizationAlgorithm {
    protected QualityFunction functionToOptimize;
    private int iteration;

    protected OptimizationAlgorithm(QualityFunction qualityFunction) {
        this.functionToOptimize = qualityFunction;
    }

    protected double[] randomSampleWithinFunctionBounds()
    {
        double[] x = new double[functionToOptimize.getDimension()];
        for (int dim = 0; dim < functionToOptimize.getDimension(); dim++)
        {
            x[dim] = functionToOptimize.getLowerBoundary()[dim] +
                    (functionToOptimize.getUpperBoundary()[dim] - functionToOptimize.getLowerBoundary()[dim]) * Utilities.getRandomGenerator().nextDouble();
        }

        return x;
    }

    public abstract ValuedSample optimize() throws NotImplementedException;

    public void setIteration(int iteration) {
        this.iteration = iteration;
    };

    public int getIteration() {
        return iteration;
    }
}
