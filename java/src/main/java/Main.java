import pl.piotrpodbielski.pw.mini.mlog.algorithms.BiologicalEvolutionAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.BitSwitchHillClimbingAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.BitSwitchHillClimbingVNSAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.DifferentialEvolutionAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.EvolutionStrategyAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.ParticleSwarmOptimizationAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.simple.GridSearchAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.HillClimbingAdSSAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.HillClimbingAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.simple.MonteCarloAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.OptimizationAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.algorithms.SimulatedAnnealingAlgorithm;
import pl.piotrpodbielski.pw.mini.mlog.enums.Algorithms;
import pl.piotrpodbielski.pw.mini.mlog.enums.Functions;
import pl.piotrpodbielski.pw.mini.mlog.functions.QualityFunction;
import pl.piotrpodbielski.pw.mini.mlog.functions.RastriginFunction;
import pl.piotrpodbielski.pw.mini.mlog.functions.RosenbrockFunction;
import pl.piotrpodbielski.pw.mini.mlog.loggers.FileOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.loggers.IOptimizationLogger;
import pl.piotrpodbielski.pw.mini.mlog.various.NotImplementedException;
import pl.piotrpodbielski.pw.mini.mlog.various.Utilities;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private static void runExperiment(OptimizationAlgorithm algorithm, int iteration) throws NotImplementedException {
//        System.err.println(String.format("%d iteration", iteration));

        algorithm.setIteration(iteration);
        algorithm.optimize();
    }

    private static OptimizationAlgorithm getOptimizationAlgorithm(Namespace ns) {
        Algorithms algorithm = ns.get("algorithm");
        Functions qualityFunction = ns.get("quality_function");
        int evaluations = ns.get("evaluations");

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

        String fileName = String.format("%s-%s-%s-log.csv", parseAlgorithmShortName(algorithm.getShortName()), qualityFunction, new SimpleDateFormat("MMdd-HHmmss").format(new Date())).toLowerCase();
        IOptimizationLogger optimizationLogger = new FileOptimizationLogger(fileName, algorithm.getShortName(), ns.getAttrs());
        optimizationLogger.resetLogger();

        System.out.println(fileName);
        if (algorithm.equals(Algorithms.MONTE_CARLO)) {
            return new MonteCarloAlgorithm(functionToOptimize, optimizationLogger, evaluations);
        }
        else if (algorithm.equals(Algorithms.GRID_SEARCH)) {
            return new GridSearchAlgorithm(functionToOptimize, optimizationLogger, (int) Math.ceil(Math.pow(evaluations, 1. / functionToOptimize.getDimension())));
        }
        else if (algorithm.equals(Algorithms.HILL_CLIMBING)) {
            return new HillClimbingAlgorithm(functionToOptimize, optimizationLogger, ns.getInt("failures_to_reset"), evaluations, ns.getDouble("step"), ns.getBoolean("reset_resets_failures_counter"));
        }
        else if (algorithm.equals(Algorithms.HILL_CLIMBING_AD_SS)) {
            return new HillClimbingAdSSAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("number_of_particles"));
        }
        else if (algorithm.equals(Algorithms.BIT_SWITCH_HILL_CLIMBING)) {
            return new BitSwitchHillClimbingAlgorithm(functionToOptimize, optimizationLogger, ns.getInt("failures_to_reset"), evaluations, ns.getDouble("step"), ns.getBoolean("reset_resets_failures_counter"), ns.getInt("no_of_bits_for_grid_mapping_per_dim"));
        }
        else if (algorithm.equals(Algorithms.SIMULATED_ANNEALING)) {
            return new SimulatedAnnealingAlgorithm(functionToOptimize, optimizationLogger, evaluations);
        }
        else if (algorithm.equals(Algorithms.BIT_SWITCH_HILL_CLIMBING_VNS)) {
            return new BitSwitchHillClimbingVNSAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("no_of_bits_for_grid_mapping_per_dim"), ns.getInt("neighbour_looks"));
        }
        else if (algorithm.equals(Algorithms.EVOLUTION_STRATEGY)) {
            return new EvolutionStrategyAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("improvements_loop_iteration"), ns.getDouble("step"), ns.getDouble("step_mutation_coefficient"));
        }
        else if (algorithm.equals(Algorithms.BIOLOGICAL_EVOLUTION)) {
            return new BiologicalEvolutionAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("population"), ns.getInt("crossover_population"), ns.getDouble("mutation_probability"), ns.getDouble("step"));
        }
        else if (algorithm.equals(Algorithms.PARTICLE_SWARM_OPTIMIZATION)) {
            return new ParticleSwarmOptimizationAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("number_of_particles"), ns.getDouble("omega"), ns.getDouble("c_1"), ns.getDouble("c_2"));
        }
        else if (algorithm.equals(Algorithms.DIFFERENTIAL_EVOLUTION)) {
            return new DifferentialEvolutionAlgorithm(functionToOptimize, optimizationLogger, evaluations, ns.getInt("population"), ns.getDouble("f"));
        }
        throw new RuntimeException();
    }

    public static String parseAlgorithmShortName(String shortName) {
        return shortName.replaceAll("[ \\+]+", "-");
    }

    public static void main(String[] args) throws NotImplementedException {
        Utilities.setRandomGenerator(1);

        ArgumentParser parser = ArgumentParsers.newFor("MLOG").build();
        parser.addArgument("algorithm").type(Algorithms.class);
        parser.addArgument("quality-function").type(Functions.class);
        parser.addArgument("evaluations").type(Integer.class);
        parser.addArgument("--times").type(Integer.class).setDefault(1);

        parser.addArgument("--number-of-particles").type(Integer.class).setDefault(20); // HC + AdSS, PSO

        parser.addArgument("--step").type(Double.class).setDefault(0.03); // HC, BSHC, ES, BE

        parser.addArgument("--failures-to-reset").type(Integer.class).setDefault(5); // HC, BSHC
        parser.addArgument("--reset-resets-failures-counter").action(Arguments.storeTrue()); // HC, BSHC

        parser.addArgument("--no-of-bits-for-grid-mapping-per-dim").type(Integer.class).setDefault(1); // BSHC, BSHC + VNS

        parser.addArgument("--neighbour-looks").type(Integer.class).setDefault(10); // BSHC + VNS

        parser.addArgument("--improvements-loop-iteration").type(Integer.class).setDefault(100); // ES
        parser.addArgument("--step-mutation-coefficient").type(Double.class).setDefault(0.9); // ES

        parser.addArgument("--population").type(Integer.class).setDefault(10); // BE, DE
        parser.addArgument("--crossover-population").type(Integer.class).setDefault(5); // BE
        parser.addArgument("--mutation-probability").type(Double.class).setDefault(0.5); // BE

        parser.addArgument("--omega").type(Double.class).setDefault(0.6); // PSO
        parser.addArgument("--c-1").type(Double.class).setDefault(1.7); // PSO
        parser.addArgument("--c-2").type(Double.class).setDefault(1.7); // PSO

        parser.addArgument("--f").type(Double.class).setDefault(0.5); // DE



        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        OptimizationAlgorithm algorithm = getOptimizationAlgorithm(ns);

        if (ns.getAttrs().containsKey("times")) {
            for (int i = 0; i < ns.getInt("times"); i++) {
                runExperiment(algorithm, i);
            }
        }
    }
}
