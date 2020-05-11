package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;

public class EvolutionStrategyAlgorithm extends OptimizationAlgorithm {

    final IOptimizationLogger logger;
    private final int maxSamplesCount;
    private final int improvementsLoopIterations;
    private final double stepMutationCoefficient;
    private double step;

    public EvolutionStrategyAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger,
                                      int maxSamplesCount, int improvementsLoopIterations, double step, double stepMutationCoefficient) {
        super(functionToOptimize);

        this.logger = logger;
        this.maxSamplesCount = maxSamplesCount;
        this.improvementsLoopIterations = improvementsLoopIterations;
        this.step = step;

        if (stepMutationCoefficient < .85 || stepMutationCoefficient >= 1) {
            throw new Error("Step mutation coefficient can not be less than 0.85, equal to 1. or more than 1.");
        }

        this.stepMutationCoefficient = stepMutationCoefficient;
    }


    @Override
    public ValuedSample optimize() {
        int samplesCount = 1;
        // Get random starting point
        double[] x = this.randomSampleWithinFunctionBounds();

        // Evaluate starting point and log
        ValuedSample bestSample = new ValuedSample(x, functionToOptimize, getIteration());
        logger.logSample(bestSample);

        int[] drawRetries = new int[functionToOptimize.getDimension()];
        Arrays.fill(drawRetries, 15);

        // Until all iterations done
        boolean runFlag = true;
        while (runFlag)
        {
            int improvements = 0;
            for (int i = 0; i < this.improvementsLoopIterations; ++i) {
                // Set from where we start to looking
                double[] x_prime = Arrays.copyOf(x, functionToOptimize.getDimension());

                // Update every dim of point with std dev `step`
                for (int dim = 0; dim < functionToOptimize.getDimension(); ++dim) {
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
                    bestSample = testSample;
                    x = Arrays.copyOf(x_prime, x_prime.length);
                    improvements += 1;
                }

                // Stop if maxSampleCount almost exceeded
                if (samplesCount == maxSamplesCount) {
                    runFlag = false;
                    break;
                }
            }

            if ((float)improvements / this.improvementsLoopIterations < 1. / 5) {
                this.step /= this.stepMutationCoefficient;
            }
            else {
                this.step *= this.stepMutationCoefficient;
            }

        }
        logger.flushSamples();

        return bestSample;
    }
}
