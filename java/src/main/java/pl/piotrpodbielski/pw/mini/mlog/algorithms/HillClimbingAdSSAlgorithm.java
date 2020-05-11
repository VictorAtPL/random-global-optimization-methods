package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HillClimbingAdSSAlgorithm extends OptimizationAlgorithm {

    private final IOptimizationLogger logger;
    private final int maxSamplesCount;
    private int numberOfParticles;

    public HillClimbingAdSSAlgorithm(QualityFunction functionToOptimize, IOptimizationLogger logger,
                                     int maxSamplesCount, int numberOfParticles) {
        super(functionToOptimize);
        this.logger = logger;
        this.maxSamplesCount = maxSamplesCount;
        this.numberOfParticles = numberOfParticles;
    }


    @Override
    public ValuedSample optimize() {
        int samplesCount = 0;

        // Initialise population of n particles
        ValuedSample bestSample = null;

        Map<Integer, ValuedSample> populationSample = new HashMap<>(numberOfParticles);
        double[][] population = new double[numberOfParticles][];
        for (int i = 0; i < numberOfParticles; ++i) {
            population[i] = this.randomSampleWithinFunctionBounds();
            ValuedSample particleSample = new ValuedSample(population[i], functionToOptimize, getIteration());
            populationSample.put(i, particleSample);

            if (bestSample == null || particleSample.getValue() < bestSample.getValue()) {
                bestSample = particleSample;
            }
            logger.logSample(particleSample);
            samplesCount++;
        }

        // In bound checker counter
        int[] drawRetries = new int[functionToOptimize.getDimension()];
        Arrays.fill(drawRetries, 15);

        // While stopping criterion not met
        boolean stopCriterion = false;
        while (!stopCriterion)
        {
            // For every particle p in population
            for (int i = 0; i < this.numberOfParticles; ++i) {
                double[] p = population[i];
                ValuedSample particleSample = populationSample.get(i);

                // Select random particle s =/= p
                int selectedParticleIdx;
                do {
                    // Select randomly one particle
                    selectedParticleIdx = Utilities.getRandomGenerator().nextInt(this.numberOfParticles);
                } while (selectedParticleIdx == i);

                double[] s = population[selectedParticleIdx];

                // For every component p_i in particle p
                double[] p_prime = Arrays.copyOf(p, p.length);
                for (int dim = 0; dim < p.length; ++dim) {
                    int cnt = 0;
                    do {
                        if (cnt == drawRetries[dim]) {
                            if (drawRetries[dim] > 1) {
                                drawRetries[dim] -= 1;
                            }
                            p_prime[dim] = p[dim];
                            break;
                        }


                        double s_max = Math.abs(p[dim] - s[dim]);
                        double r = Utilities.getDoubleBetween(-s_max, s_max);
                        p_prime[dim] = p[dim] + r;

                        cnt += 1;
                    } while (p_prime[dim] < functionToOptimize.getLowerBoundary()[dim] || p_prime[dim] > functionToOptimize.getUpperBoundary()[dim]);

                    if (cnt < drawRetries[dim]) {
                        drawRetries[dim] = 15;
                    }
                }

                // Evaluate new sample
                ValuedSample testSample = new ValuedSample(p_prime, functionToOptimize, getIteration());
                samplesCount++;
                logger.logSample(testSample);

                // Test if f(p_prime) > f(p)
                if (testSample.getValue() < particleSample.getValue())
                {
                    population[i] = Arrays.copyOf(p_prime, p_prime.length);
                    populationSample.put(i, testSample);
                }

                // Check if result better
                if (testSample.getValue() < bestSample.getValue()) {
                    bestSample = testSample;
                }

                if (samplesCount == maxSamplesCount) {
                    stopCriterion = true;
                    break;
                }
            }
        }
        logger.flushSamples();

        return bestSample;
    }
}
