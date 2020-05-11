package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.NotImplementedException;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;

public class ParticleSwarmOptimizationAlgorithm extends OptimizationAlgorithm {
    private final IOptimizationLogger logger;
    private final int maxSamplesCount;
    private final int numberOfParticles;
    private final double omega;
    private final double c_1;
    private final double c_2;

    public ParticleSwarmOptimizationAlgorithm(QualityFunction qualityFunction, IOptimizationLogger logger,
                                              int maxSamplesCount, int numberOfParticles, double omega, double c_1, double c_2) {
        super(qualityFunction);
        this.logger = logger;
        this.maxSamplesCount = maxSamplesCount;
        this.numberOfParticles = numberOfParticles;
        this.omega = omega;
        this.c_1 = c_1;
        this.c_2 = c_2;

        if (this.numberOfParticles < 3) {
            throw new Error("Number of particles must be greater than 2 in order to correctly initialize velocity of each particle.");
        }
    }

    private static double calculateEuclideanDistance(double[] firstDouble, double[] secondDouble) {
        double sum = 0;
        for (int i = 0; i < firstDouble.length; ++i) {
            sum += Math.pow(firstDouble[i] - secondDouble[i], 2);
        }

        return Math.sqrt(sum);
    }

    private double[] getClosestNeighbour(double[][] population, int idx) {
        double[] neighbour = {0, 0};
        double neighbourDistance = 10e99;

        for (int i = 0; i < population.length; ++i) {
            if (i == idx) {
                continue;
            }

            double distance = calculateEuclideanDistance(population[i], population[idx]);
            if (distance < neighbourDistance) {
                neighbourDistance = distance;
                neighbour = population[i];
            }
        }

        return neighbour;
    }

    @Override
    public ValuedSample optimize() throws NotImplementedException {
        int samplesCount = 0;

        // Initialise population of n particles
        double[] best = {0, 0};
        ValuedSample bestSample = null;

        double[][] population = new double[numberOfParticles][];

        for (int i = 0; i < numberOfParticles; ++i) {
            population[i] = this.randomSampleWithinFunctionBounds();

            ValuedSample particleSample = new ValuedSample(population[i], functionToOptimize, getIteration());
            logger.logSample(particleSample);
            samplesCount++;

            if (bestSample == null || particleSample.getValue() < bestSample.getValue()) {
                best = population[i];
                bestSample = particleSample;
            }
        }

        double[][] populationVelocity = new double[numberOfParticles][];
        for (int i = 0; i < numberOfParticles; ++i) {
            int firstParticleIdx, secondParticleIdx;

            do {
                firstParticleIdx = Utilities.getRandomGenerator().nextInt(numberOfParticles);
            } while (firstParticleIdx == i);

            do {
                secondParticleIdx = Utilities.getRandomGenerator().nextInt(numberOfParticles);
            } while (secondParticleIdx == i || firstParticleIdx == secondParticleIdx);

            populationVelocity[i] = new double[this.functionToOptimize.getDimension()];
            for (int j = 0; j < this.functionToOptimize.getDimension(); ++j) {
                populationVelocity[i][j] = (population[firstParticleIdx][j] + population[secondParticleIdx][j]) / 2;
            }
        }

        // Until all iterations done
        boolean runFlag = true;
        while (runFlag) {
            for (int i = 0; i < numberOfParticles; ++i) {
                // Update p.x_iter
                for (int j = 0; j < this.functionToOptimize.getDimension(); ++j) {
                    population[i][j] += populationVelocity[i][j];
                }

                // Evaluate
                ValuedSample sample = new ValuedSample(population[i], functionToOptimize, getIteration());
                logger.logSample(sample);
                samplesCount++;

                if (sample.getValue() < bestSample.getValue()) {
                    best = population[i];
                    bestSample = sample;
                }

                // Stop if maxSampleCount almost exceeded
                if (samplesCount == maxSamplesCount) {
                    runFlag = false;
                    break;
                }

                // Get g best
                double[] neighbour = this.getClosestNeighbour(population, i);

                // Update v
                for (int j = 0; j < this.functionToOptimize.getDimension(); ++j) {
                    populationVelocity[i][j] = this.omega * populationVelocity[i][j]
                            + this.c_1 * (best[j] - population[i][j])
                            + this.c_2 * (neighbour[j] - population[i][j]);
                }
            }
        }

        logger.flushSamples();

        return bestSample;
    }
}
