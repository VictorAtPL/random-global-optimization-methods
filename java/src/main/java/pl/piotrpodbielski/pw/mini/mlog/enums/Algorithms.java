package pl.piotrpodbielski.pw.mini.mlog.enums;

public enum Algorithms {
    MONTE_CARLO("MC"),
    GRID_SEARCH("GS"),
    HILL_CLIMBING("HC"),
    HILL_CLIMBING_AD_SS("HC + AdSS"),
    BIT_SWITCH_HILL_CLIMBING("BSHC"),
    SIMULATED_ANNEALING("SA"),
    BIT_SWITCH_HILL_CLIMBING_VNS("BSHC + VNS"),
    EVOLUTION_STRATEGY("ES"),
    BIOLOGICAL_EVOLUTION("BE"),
    PARTICLE_SWARM_OPTIMIZATION("PSO"),
    DIFFERENTIAL_EVOLUTION("DE");

    private final String shortName;

    Algorithms(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }
}
