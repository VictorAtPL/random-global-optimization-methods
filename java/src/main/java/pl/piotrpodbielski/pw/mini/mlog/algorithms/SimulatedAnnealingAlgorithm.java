package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;

public class SimulatedAnnealingAlgorithm extends OptimizationAlgorithm {

    public static final double GAMMA = 8;

    final IOptimizationLogger logger;
    final int initTemperature;

    public SimulatedAnnealingAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger, int initTemperature) {
        super(functionToOptimize);

        this.logger = logger;
        this.initTemperature = initTemperature;
    }

    private static double getAcceptanceProbability(ValuedSample bestSample, ValuedSample testSample, double temperature, int initTemperature) {
        if (testSample.getValue() < bestSample.getValue()) {
            return 1.0;
        }

        double alpha = ((initTemperature - temperature) / initTemperature) * Math.E * 2;

        return Math.exp(-alpha * (1 - (bestSample.getValue() - testSample.getValue()) / testSample.getValue()));
    }

    /**
     * Source: https://rosettacode.org/wiki/Map_range#Java
     */
    public static double mapRange(long a1, double a2, double b1, double b2, double s) {
        return b1 + ((s - a1) * (b2 - b1)) /  (a2 - a1);
    }

    private static double getStepBasedOnTemperature(double temperature, double initTemperature, double lowerBoundary, double upperBoundary) {
        final double beta = mapRange(0, initTemperature, 0, Math.E, temperature);
        final double delta = Math.exp(beta - Math.E);
        return delta * (upperBoundary - lowerBoundary) / GAMMA;
    }

    @Override
    public ValuedSample optimize() {
        // How many tries between last success and now?

        double[] x = this.randomSampleWithinFunctionBounds();
        double[] x_prime;

        // Evaluate starting point and log
        ValuedSample bestSample = new ValuedSample(x, functionToOptimize, getIteration());
        logger.logSample(bestSample);

        double temperature = initTemperature;

        // Until all iterations done
        while (temperature > 0)
        {

            // Set from where we start to looking
            x_prime = Arrays.copyOf(x, functionToOptimize.getDimension());

            // Update every dim of point with std dev `step`
            for (int dim = 0; dim < functionToOptimize.getDimension(); ++dim)
            {
                int tries = 5;
                do
                {
                    tries--;
                    x_prime[dim] = Utilities.generateGaussian(x_prime[dim], getStepBasedOnTemperature(temperature, initTemperature, functionToOptimize.getLowerBoundary()[dim], functionToOptimize.getUpperBoundary()[dim]));
                } while (tries > 0 || x_prime[dim] < functionToOptimize.getLowerBoundary()[dim] || x_prime[dim] > functionToOptimize.getUpperBoundary()[dim]);
            }

            // Evaluate new sample
            ValuedSample testSample = new ValuedSample(x_prime, functionToOptimize, getIteration());
            logger.logSample(testSample);

            // Calculate acceptance probability
            final double acceptanceProbability = getAcceptanceProbability(bestSample, testSample, temperature, initTemperature);

            // Test new sample whether should be accepted or not
            if (acceptanceProbability > Utilities.getRandomGenerator().nextDouble())
            {
                bestSample = testSample;
                x = Arrays.copyOf(x_prime, x_prime.length);
            }

            temperature -= 1.0;
        }
        logger.flushSamples();

        return bestSample;
    }
}
