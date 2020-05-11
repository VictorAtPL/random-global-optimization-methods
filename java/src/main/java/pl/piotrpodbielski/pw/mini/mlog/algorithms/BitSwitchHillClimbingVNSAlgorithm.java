package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;

public class BitSwitchHillClimbingVNSAlgorithm extends BitSwitchHillClimbingAlgorithm {

    private int neighbourLooks;

    public BitSwitchHillClimbingVNSAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int samplesCount, int noOfBitsForGridMappingPerDim, int neighbourLooks) {
        super(functionToOptimize, logger, 0, samplesCount, 1, false, noOfBitsForGridMappingPerDim);

        this.neighbourLooks = neighbourLooks;
    }

    @Override
    public ValuedSample optimize() {
        int samplesCount = 1;

        // Get random starting point
        long[] x = this.randomSample();
        long[] x_best = Arrays.copyOf(x, x.length);
        double[] x_best_mapped = this.mapSampleToFunctionBounds(x);

        // Evaluate starting point and log
        ValuedSample bestSample = new ValuedSample(x_best_mapped, functionToOptimize, getIteration());
        logger.logSample(bestSample);

        x = Arrays.copyOf(x_best, x_best.length);

        // Until all iterations done
        boolean stopCriterion = false;
        int neighbourLookRange = 1;
        while (!stopCriterion) {
            boolean improvement = false;
            for (int i = 0; i < neighbourLooks; ++i) {
                improvement = false;

                // Flip `neighbourLookRange` bits
                long[] x_prime = this.flipRandomBitInSample(x);
                double[] x_prime_mapped = this.mapSampleToFunctionBounds(x_prime);

                // Evaluate new sample
                ValuedSample testSample = new ValuedSample(x_prime_mapped, functionToOptimize, getIteration());
                samplesCount++;
                logger.logSample(testSample);

                // Test new sample if it is better or not
                if (testSample.getValue() < bestSample.getValue()) {
                    bestSample = testSample;
                    x_best = x_prime;
                    improvement = true;
                }

                if (samplesCount == maxSamplesCount) {
                    stopCriterion = true;
                    break;
                }
            }

            x = Arrays.copyOf(x_best, x.length);

            if (!improvement) {
                this.setStep(this.getStep() + 1);
            }
            else {
                this.setStep(1);
            }
        }
        logger.flushSamples();

        return bestSample;
    }
}
