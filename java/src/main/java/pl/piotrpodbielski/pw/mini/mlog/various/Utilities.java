package pl.piotrpodbielski.pw.mini.mlog.various;

import java.util.Random;

public class Utilities {
    Utilities() {
        throw new RuntimeException("Cannot initialize instance of class Utilities.");
    }

    static Random randomGenerator = new Random(1);

    public static Random getRandomGenerator() {
        return randomGenerator;
    }

    public static void setRandomGenerator(int seed) {
        Utilities.randomGenerator = new Random(seed);
    }

    public static double generateGaussian(double mean, double stdDev) {
        double u1 = 1.0 - randomGenerator.nextDouble(); // uniform(0,1] random doubles
        double u2 = 1.0 - randomGenerator.nextDouble();
        double randStdNormal = Math.sqrt(-2.0 * Math.log(u1)) *
                Math.sin(2.0 * Math.PI * u2); // random normal(0, 1)
        return mean + stdDev * randStdNormal; // random normal(mean, stdDev^2)
    }

    public static double getDoubleBetween(double rangeMin, double rangeMax) {
        return rangeMin + (rangeMax - rangeMin) * randomGenerator.nextDouble();
    }
}
