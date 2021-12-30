import pl.piotrpodbielski.pw.mini.mlog.algorithms.BitSwitchHillClimbingAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.various.ValuedSample;
import pl.piotrpodbielski.pw.mini.mlog.enums.Functions;
import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.functions.RastriginFunction;
import pl.piotrpodbielski.pw.mini.mlog.functions.RosenbrockFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.FileOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import static pl.piotrpodbielski.pw.mini.mlog.enums.Algorithms.BIT_SWITCH_HILL_CLIMBING;

public class VisualiseBitSwitchHillClimbingAlgorithm {

    static void runExperiment(Namespace ns) {
        Functions qualityFunction = ns.get("quality_function");

        QualityFunction functionToOptimize;
        if (qualityFunction.equals(Functions.RASTRIGIN)) {
            functionToOptimize = new RastriginFunction(2);
        }
        else if (qualityFunction.equals(Functions.ROSENBROCK)) {
            functionToOptimize = new RosenbrockFunction(2);
        }
        else {
            throw new RuntimeException();
        }

        int noOfBitsForGridMappingPerDim = ns.getInt("no_of_bits_for_grid_mapping_per_dim");

        final String fileName = String.format("%s-%s-%s-log.csv", Main.parseAlgorithmShortName(BIT_SWITCH_HILL_CLIMBING.getShortName()), qualityFunction, noOfBitsForGridMappingPerDim).toLowerCase();
        final FileOptimizationLogger optimizationLogger = new FileOptimizationLogger(fileName, BIT_SWITCH_HILL_CLIMBING.getShortName(), ns.getAttrs());
        optimizationLogger.resetLogger();

        BitSwitchHillClimbingAlgorithm optimizationAlgorithm = new BitSwitchHillClimbingAlgorithm(functionToOptimize, optimizationLogger, 5, 10000, 1, true, noOfBitsForGridMappingPerDim);
        long[] x = optimizationAlgorithm.randomSample();

        double[] x_mapped = optimizationAlgorithm.mapSampleToFunctionBounds(x);

        ValuedSample valuedSample = new ValuedSample(x_mapped, functionToOptimize, 0);
        optimizationLogger.logSample(valuedSample);

        for (int i = 0; i < functionToOptimize.getDimension(); ++i) {
            for (int j = 0; j < noOfBitsForGridMappingPerDim; ++j) {
                long[] x_prime = optimizationAlgorithm.flipBitInSample(x, i, j);
                double[] x_prime_mapped = optimizationAlgorithm.mapSampleToFunctionBounds(x_prime);
                ValuedSample valuedSample_prime = new ValuedSample(x_prime_mapped, functionToOptimize, 0);
                optimizationLogger.logSample(valuedSample_prime);
            }
        }

        optimizationLogger.flushSamples();
    }

    public static void main(String[] args) {
        Utilities.setRandomGenerator(1);

        ArgumentParser parser = ArgumentParsers.newFor("MLOG").build();
        parser.addArgument("quality-function").type(Functions.class);
        parser.addArgument("--no-of-bits-for-grid-mapping-per-dim").type(Integer.class).setDefault(1);

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        runExperiment(ns);
    }
}
