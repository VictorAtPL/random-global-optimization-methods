package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;

public class HillClimbingAlgorithm extends OptimizationAlgorithm {

    final IOptimizationLogger logger;
    final int failuresToReset;
    final int maxSamplesCount;
    double step;
    final boolean resetResetsFailuresCounter;

    public HillClimbingAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int failuresToReset,
                                 int maxSamplesCount, double step, boolean resetResetsFailuresCounter) {
        super(functionToOptimize);

        this.logger = logger;
        this.failuresToReset = failuresToReset;
        this.maxSamplesCount = maxSamplesCount;
        this.step = step;
        this.resetResetsFailuresCounter = resetResetsFailuresCounter;
    }


    @Override
    public ValuedSample optimize() {
        int samplesCount = 1;
        // How many tries between last success and now?
        int lastSuccess = 0;
        // Get random starting point
        double[] x = this.randomSampleWithinFunctionBounds();
        double[] x_prime;

        // Evaluate starting point and log
        ValuedSample bestSample = new ValuedSample(x, functionToOptimize, getIteration());
        logger.logSample(bestSample);

        // In bound checker counter
        int[] drawRetries = new int[functionToOptimize.getDimension()];
        Arrays.fill(drawRetries, 15);

        // Until all iterations done
        while (samplesCount < maxSamplesCount)
        {
            // Set from where we start to looking
            x_prime = Arrays.copyOf(x, functionToOptimize.getDimension());

            // Update every dim of point with std dev `step`
            for (int dim = 0; dim < functionToOptimize.getDimension(); ++dim)
            {
                int cnt = 0;
                do {
                    if (cnt == drawRetries[dim]) {
                        if (drawRetries[dim] > 1) {
                            drawRetries[dim] -= 1;
                        }
                        x_prime[dim] = x[dim];
                        break;
                    }

                    x_prime[dim] = Utilities.generateGaussian(x[dim], step);

                    cnt += 1;
                } while (x_prime[dim] < functionToOptimize.getLowerBoundary()[dim] || x_prime[dim] > functionToOptimize.getUpperBoundary()[dim]);

                if (cnt < drawRetries[dim]) {
                    drawRetries[dim] = 15;
                }
            }

            // Evaluate new sample
            ValuedSample testSample = new ValuedSample(x_prime, functionToOptimize, getIteration());
            samplesCount++;
            logger.logSample(testSample);

            // Test new sample if it is better or not
            if (testSample.getValue() < bestSample.getValue())
            {
                lastSuccess = 0;
                bestSample = testSample;
                x = Arrays.copyOf(x_prime, x_prime.length);
            }
            else
            {
                lastSuccess++;
            }

            if (lastSuccess > failuresToReset)
            {
                if (resetResetsFailuresCounter) {
                    lastSuccess = 0;
                }

                // Generate new random point
                x = this.randomSampleWithinFunctionBounds();
            }
        }
        logger.flushSamples();

        return bestSample;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public double getStep() {
        return step;
    }
}
