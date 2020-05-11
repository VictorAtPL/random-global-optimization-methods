package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.NotImplementedException;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DifferentialEvolutionAlgorithm extends OptimizationAlgorithm {
    private final IOptimizationLogger logger;
    private final int maxSamplesCount;
    private final int population;
    private final double f;

    public DifferentialEvolutionAlgorithm(QualityFunction qualityFunction, IOptimizationLogger logger,
                                          int maxSamplesCount, int population, double f) {
        super(qualityFunction);
        this.logger = logger;
        this.maxSamplesCount = maxSamplesCount;
        this.population = population;
        this.f = f;

        if (this.population < 3) {
            throw new Error("Size of population must be greater than 2 in order to correctly initialize velocity of each particle.");
        }
    }

    private double[] crossover(double[] x, double[] v) {
        double[] u = new double[x.length];

        double probability = 1. / x.length;

        for (int i = 0; i < x.length; ++i) {
            if (Utilities.getRandomGenerator().nextDouble() < probability) {
                u[i] = v[i];
            }
            else {
                u[i] = x[i];
            }
        }

        return u;
    }

    @Override
    public ValuedSample optimize() throws NotImplementedException {
        int samplesCount = 0;

        // Initialise population of n particles
        double[] best = {0, 0};
        ValuedSample bestSample = null;

        Map<Integer, ValuedSample> populationSample = new HashMap<>(this.population);
        double[][] population = new double[this.population][];

        for (int i = 0; i < this.population; ++i) {
            population[i] = this.randomSampleWithinFunctionBounds();

            ValuedSample sample = new ValuedSample(population[i], functionToOptimize, getIteration());
            logger.logSample(sample);
            samplesCount++;

            populationSample.put(i, sample);

            if (bestSample == null || sample.getValue() < bestSample.getValue()) {
                best = population[i];
                bestSample = sample;
            }
        }

        // Until all iterations done
        boolean runFlag = true;
        while (runFlag) {
            for (int i = 0; i < this.population; ++i) {
                int firstParticleIdx, secondParticleIdx;

                do {
                    firstParticleIdx = Utilities.getRandomGenerator().nextInt(this.population);
                } while (firstParticleIdx == i);

                do {
                    secondParticleIdx = Utilities.getRandomGenerator().nextInt(this.population);
                } while (secondParticleIdx == i || firstParticleIdx == secondParticleIdx);

                double[] v = Arrays.copyOf(best, best.length);
                for (int j = 0; j < this.functionToOptimize.getDimension(); ++j) {
                    v[j] += this.f * (population[firstParticleIdx][j] - population[secondParticleIdx][j]);
                }
                double[] u = crossover(population[i], v);

                // Evaluate
                ValuedSample sample = new ValuedSample(u, functionToOptimize, getIteration());
                logger.logSample(sample);
                samplesCount++;

                if (sample.getValue() < populationSample.get(i).getValue()) {
                    population[i] = u;
                    populationSample.put(i, sample);
                }

                if (sample.getValue() < bestSample.getValue()) {
                    bestSample = sample;
                    best = u;
                }

                // Stop if maxSampleCount almost exceeded
                if (samplesCount == maxSamplesCount) {
                    runFlag = false;
                    break;
                }
            }
        }

        logger.flushSamples();

        return bestSample;
    }
}
