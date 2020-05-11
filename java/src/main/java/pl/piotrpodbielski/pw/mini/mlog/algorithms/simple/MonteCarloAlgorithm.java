package pl.piotrpodbielski.pw.mini.mlog.algorithms.simple;

import pl.piotrpodbielski.pw.mini.mlog.algorithms.OptimizationAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;
import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;

/**
 * @
 */
public class MonteCarloAlgorithm extends OptimizationAlgorithm {

    private final IOptimizationLogger logger;
    private final int samplesCount;

    public MonteCarloAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int samplesCount) {
        super(functionToOptimize);

        this.logger = logger;
        this.samplesCount = samplesCount;
    }

    @Override
    public ValuedSample optimize() {
        ValuedSample bestSample = null;
        for (int sampleIdx = 0; sampleIdx < samplesCount; sampleIdx++)
        {
            double[] x = this.randomSampleWithinFunctionBounds();

            ValuedSample testSample = new ValuedSample(x, functionToOptimize, getIteration());
            logger.logSample(testSample);
            if (bestSample == null || testSample.getValue() < bestSample.getValue())
            {
                bestSample = testSample;
            }
        }

        logger.flushSamples();

        return bestSample;
    }
}
