package pl.piotrpodbielski.pw.mini.mlog.algorithms.simple;

import pl.piotrpodbielski.pw.mini.mlog.algorithms.OptimizationAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;
import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.NotImplementedException;

public class GridSearchAlgorithm extends OptimizationAlgorithm {

    private final IOptimizationLogger logger;
    private final int gridDensityPerDimension;

    public GridSearchAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int gridDensityPerDimension) {
        super(functionToOptimize);

        this.logger = logger;
        this.gridDensityPerDimension = gridDensityPerDimension;
    }


    @Override
    public ValuedSample optimize() throws NotImplementedException {
        throw new NotImplementedException();
    }
}
