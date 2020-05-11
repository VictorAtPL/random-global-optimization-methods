package pl.piotrpodbielski.pw.mini.mlog.algorithms;

import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSampleComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BiologicalEvolutionAlgorithm extends OptimizationAlgorithm {
    private final IOptimizationLogger logger;
    private final int maxSamplesCount;
    private final int population;
    private final int crossoverPopulation;
    private final double mutationProbability;
    private final double mutationStep;
    private final QualityFunction qualityFunction;

    public BiologicalEvolutionAlgorithm(QualityFunction qualityFunction, IOptimizationLogger logger,
                                        int maxSamplesCount, int population, int crossoverPopulation, double mutationProbability, double mutationStep) {
        super(qualityFunction);
        this.qualityFunction = qualityFunction;
        this.logger = logger;
        this.maxSamplesCount = maxSamplesCount;
        this.population = population;
        this.crossoverPopulation = crossoverPopulation;
        this.mutationProbability = mutationProbability;
        this.mutationStep = mutationStep;
    }

    private double[][] crossover(double[][] population) {
        double[][] crossoverPopulation = new double[this.crossoverPopulation][];

        for (int i = 0; i < this.crossoverPopulation; ++i) {
            int firstRandInt = Utilities.getRandomGenerator().nextInt(this.population);
            int secondRandInt = Utilities.getRandomGenerator().nextInt(this.population);

            crossoverPopulation[i] = new double[this.qualityFunction.getDimension()];

            for (int j = 0; j < this.qualityFunction.getDimension(); ++j) {
                crossoverPopulation[i][j] = (population[firstRandInt][j] + population[secondRandInt][j]) / 2;
            }
        }

        return crossoverPopulation;
    }

    private double[][] mutate(double[][] population) {
        List<double[]> mutatePopulationList = new ArrayList<>();

        for (double[] doubles : population) {
            double[] x_prime = new double[this.qualityFunction.getDimension()];

            boolean mutated = false;
            for (int j = 0; j < this.qualityFunction.getDimension(); ++j) {

                if (Utilities.getRandomGenerator().nextDouble() < this.mutationProbability) {
                    x_prime[j] = Utilities.generateGaussian(doubles[j], this.mutationStep);
                    mutated = true;
                } else {
                    x_prime[j] = doubles[j];
                }
            }

            if (mutated) {
                mutatePopulationList.add(x_prime);
            }
        }

        return mutatePopulationList.toArray(new double[mutatePopulationList.size()][]);
    }

    private static double[][] concatArrays(final double[][]... arrays) {
        return Arrays.stream(arrays)
                .flatMap(Arrays::stream)
                .toArray(double[][]::new);
    }

    @Override
    public ValuedSample optimize() {
        int samplesCount = 1;

        // Initialise population of n particles
        ValuedSample bestSample = null;

        ArrayList<ValuedSample> populationSample = new ArrayList<>(this.population);
        double[][] population = new double[this.population][];
        for (int i = 0; i < this.population; ++i) {
            population[i] = this.randomSampleWithinFunctionBounds();

            ValuedSample sample = new ValuedSample(population[i], functionToOptimize, getIteration(), i);
            logger.logSample(sample);
            samplesCount++;

            populationSample.add(i, sample);

            if (bestSample == null || sample.getValue() < bestSample.getValue()) {
                bestSample = sample;
            }
        }

        // Comparator
        ValuedSampleComparator valuedSampleComparator = new ValuedSampleComparator();

        // Until all iterations done
        boolean runFlag = true;
        while (runFlag) {
            double[][] crossoverPopulation = crossover(population);
            double[][] crossoverPopulationMutated = mutate(crossoverPopulation);
            double[][] populationMutated = mutate(population);

            double[][] evaluatePopulation = concatArrays(population, crossoverPopulation, crossoverPopulationMutated, populationMutated);
            ArrayList<ValuedSample> evaluationSample = new ArrayList<>(evaluatePopulation.length);

            for (int i = 0; i < this.population; ++i) {
                evaluationSample.add(i, populationSample.get(i));
            }

            for (int i = this.population; i < evaluatePopulation.length; ++i) {
                ValuedSample sample = new ValuedSample(evaluatePopulation[i], functionToOptimize, getIteration(), i);
                logger.logSample(sample);
                samplesCount++;

                evaluationSample.add(i, sample);

                if (bestSample == null || sample.getValue() < bestSample.getValue()) {
                    bestSample = sample;
                }

                // Stop if maxSampleCount almost exceeded
                if (samplesCount == maxSamplesCount) {
                    runFlag = false;
                    break;
                }
            }

            evaluationSample.sort(valuedSampleComparator);

            for (int i = 0; i < this.population; ++i) {
                ValuedSample valuedSample = evaluationSample.get(i);
                int idx = valuedSample.getIdx();
                population[i] = evaluatePopulation[idx];
                valuedSample.setIdx(i);
                populationSample.set(i, valuedSample);
            }
        }

        logger.flushSamples();

        return bestSample;
    }
}
