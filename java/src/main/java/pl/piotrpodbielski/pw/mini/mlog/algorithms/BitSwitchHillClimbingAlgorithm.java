package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;

public class BitSwitchHillClimbingAlgorithm extends HillClimbingAlgorithm {

    public static final int MAX_BITS_NUMBER = 64;
    protected final int noOfBitsForGridMappingPerDim;

    public BitSwitchHillClimbingAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int failuresToReset, int samplesCount, double step, boolean resetResetsFailuresCounter, int noOfBitsForGridMappingPerDim) {
        super(functionToOptimize, logger, failuresToReset, samplesCount, step, resetResetsFailuresCounter);

        if (noOfBitsForGridMappingPerDim > MAX_BITS_NUMBER) {
            throw new Error("Number of bits for grid mapping can not be more than 64 (long type limitation).");
        }
        this.noOfBitsForGridMappingPerDim = noOfBitsForGridMappingPerDim;
    }

    private long flipBit(long number, int idx) {
        return number ^ (1 << idx);
    }

    public long[] randomSample() {
        long[] x = new long[this.functionToOptimize.getDimension()];
;
        for (int i = 0; i < this.functionToOptimize.getDimension(); ++i) {
            x[i] = Utilities.getRandomGenerator().nextLong();
            x[i] = x[i] & (1L << noOfBitsForGridMappingPerDim - 1);
        }

        return x;
    }

    /**
     * Source: https://rosettacode.org/wiki/Map_range#Java
     */
    public static double mapRange(long a1, long a2, double b1, double b2, long s, int noOfBitsForGridMappingPerDim) {
        if (noOfBitsForGridMappingPerDim <= 32) {
            return b1 + ((double) (s - a1) * (b2 - b1)) /  (double)(a2 - a1);
        }
        return b1 + ((double) (s - a1) * (b2 - b1)) /  ((double)a2 - (double)a1);
    }

    private double mapLongToDoubleWithinBounds(long dim_value, double lowerBoundary, double upperBoundary) {
        long minValue, maxValue;

        // Get first n bits
        if (noOfBitsForGridMappingPerDim == 64) {
            minValue = Long.MIN_VALUE;
            maxValue = Long.MAX_VALUE;
        } else {
            minValue = 0;
            maxValue = (long) Math.pow(2, noOfBitsForGridMappingPerDim) - 1;
        }

        return mapRange(minValue, maxValue, lowerBoundary, upperBoundary, dim_value, noOfBitsForGridMappingPerDim);
    }

    public double[] mapSampleToFunctionBounds(long[] x) {
        double[] x_mapped = new double[functionToOptimize.getDimension()];
        for (int dim = 0; dim < functionToOptimize.getDimension(); dim++) {
            long dim_value = x[dim];

            x_mapped[dim] = this.mapLongToDoubleWithinBounds(dim_value, functionToOptimize.getLowerBoundary()[dim],
                    functionToOptimize.getUpperBoundary()[dim]);
        }

        return x_mapped;
    }

    public long[] flipBitInSample(long[] x, int dimToChange, int bitToChange) {
        long[] x_prime = Arrays.copyOf(x, x.length);

        x_prime[dimToChange] = flipBit(x_prime[dimToChange], bitToChange);

        return x_prime;
    }

    protected long[] flipRandomBitInSample(long[] x) {
        long[] x_prime = Arrays.copyOf(x, x.length);

        for (int i = 0; i < (int)this.getStep(); ++i) {
            int dimToChange = (Utilities.getRandomGenerator().nextInt() & Integer.MAX_VALUE) % this.functionToOptimize.getDimension();
            int bitToChange = (Utilities.getRandomGenerator().nextInt() & Integer.MAX_VALUE) % noOfBitsForGridMappingPerDim;

            x_prime = this.flipBitInSample(x_prime, dimToChange, bitToChange);
        }

        return x_prime;
    }

    @Override
    public ValuedSample optimize() {
        int samplesCount = 1;
        // How many tries between last success and now?
        int lastSuccess = 0;
        // Get random starting point
        long[] x = this.randomSample();
        double[] x_mapped = this.mapSampleToFunctionBounds(x);

        // Evaluate starting point and log
        ValuedSample bestSample = new ValuedSample(x_mapped, functionToOptimize, getIteration());
        logger.logSample(bestSample);

        // Until all iterations done
        while (samplesCount < maxSamplesCount) {
            // Flip `step` bits
            long[] x_prime = this.flipRandomBitInSample(x);
            double[] x_prime_mapped = this.mapSampleToFunctionBounds(x_prime);

            // Evaluate new sample
            ValuedSample testSample = new ValuedSample(x_prime_mapped, functionToOptimize, getIteration());
            samplesCount++;
            logger.logSample(testSample);

            // Test new sample if it is better or not
            if (testSample.getValue() < bestSample.getValue()) {
                lastSuccess = 0;
                bestSample = testSample;
                x = x_prime;
            } else {
                lastSuccess++;
            }

            if (lastSuccess > failuresToReset) {
                if (resetResetsFailuresCounter) {
                    lastSuccess = 0;
                }

                // Generete new random point
                x = this.randomSample();
            }
        }
        logger.flushSamples();

        return bestSample;
    }
}
