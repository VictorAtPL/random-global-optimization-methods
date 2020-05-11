from enum import Enum

from hyperopt import hp


class Functions(Enum):
    RASTRIGIN = "RASTRIGIN"
    ROSENBROCK = "ROSENBROCK"

    def __str__(self):
        return self.name.lower()

    def __repr__(self):
        return str(self)

    @staticmethod
    def argparse(s):
        try:
            return Functions[s.upper()]
        except KeyError:
            return s


class Algorithms(Enum):
    MONTE_CARLO = "MC"
    GRID_SEARCH = "GS"
    HILL_CLIMBING = "HC"
    HILL_CLIMBING_AD_SS = "HC + AdSS"
    BIT_SWITCH_HILL_CLIMBING = "BSHC"
    SIMULATED_ANNEALING = "SA"
    BIT_SWITCH_HILL_CLIMBING_VNS = "BSHC + VNS"
    EVOLUTION_STRATEGY = "ES"
    BIOLOGICAL_EVOLUTION = "BE"
    PARTICLE_SWARM_OPTIMIZATION = "PSO"
    DIFFERENCE_EVOLUTION = "DE"

    def __str__(self):
        return self.name.upper()

    def __repr__(self):
        return str(self).upper()

    @staticmethod
    def argparse(s):
        try:
            return Algorithms[s.upper()]
        except KeyError:
            return s


STEPS = {
    Functions.RASTRIGIN: 1000,
    Functions.ROSENBROCK: 600,
}
BOUNDARIES = {
    Functions.RASTRIGIN: 5.12,
    Functions.ROSENBROCK: 2.048,
}
SEED = 1


def get_opt_space(algorithm, function):
    if algorithm == Algorithms.HILL_CLIMBING:
        return {
            '--failures-to-reset': hp.uniformint("x_failures_to_reset", 1, 50),
            '--step': hp.uniform("x_step", 0., BOUNDARIES[function] / 3),
            '--reset-resets-failures-counter': hp.choice("x_reset_resets_failures_counter", [True, False]),
        }
    if algorithm == Algorithms.HILL_CLIMBING_AD_SS:
        return {
            '--number-of-particles': hp.uniformint("x_number_of_particles", 5, 40)
        }, 50
    if algorithm == Algorithms.BIT_SWITCH_HILL_CLIMBING:
        return {
            '--failures-to-reset': hp.uniformint("x_failures_to_reset", 1, 50),
            '--step': hp.uniformint("x_step", 1, 8),
            '--reset-resets-failures-counter': hp.choice("x_reset_resets_failures_counter", [True, False]),
            '--no-of-bits-for-grid-mapping-per-dim': hp.uniformint("x_no_of_bits_for_grid_mapping_per_dim", 1, 32)
        }
    if algorithm == Algorithms.BIT_SWITCH_HILL_CLIMBING_VNS:
        return {
            '--no-of-bits-for-grid-mapping-per-dim': hp.uniformint("x_no_of_bits_for_grid_mapping_per_dim", 1, 32),
            '--neighbour-looks': hp.quniform("x_neighbour_looks", 50, STEPS[function], 50)
        }
    if algorithm == Algorithms.EVOLUTION_STRATEGY:
        return {
            '--improvements-loop-iteration': hp.quniform("x_improvements_loop_iteration", 50, STEPS[function], 50),
            '--step': hp.uniform("x_step", 0., BOUNDARIES[function] / 3),
            '--step-mutation-coefficient': hp.uniform("x_step_mutation_coefficient", 0.85, 0.99)
        }
    if algorithm == Algorithms.BIOLOGICAL_EVOLUTION:
        return {
            '--step': hp.uniform("x_step", 0., BOUNDARIES[function] / 3),
            '--population': hp.uniformint("x_population", 5, 40),
            '--crossover-population': hp.uniformint("x_crossover_population", 5, 20),
            '--mutation-probability': hp.quniform("x_mutation_probability", 0., 1., .1)
        }
    if algorithm == Algorithms.PARTICLE_SWARM_OPTIMIZATION:
        return {
            '--number-of-particles': hp.uniformint("x_number_of_particles", 5, 40),
            '--omega': hp.uniform("x_omega", -0.1, 0.837),
            '--c-1': hp.uniform("x_c_1", 0.875, 2.0412),
            '--c-2': hp.uniform("x_c_2", 0.9477, 2.85),
        }
    if algorithm == Algorithms.DIFFERENCE_EVOLUTION:
        return {
            '--population': hp.uniformint("x_population", 5, 40),
            '--f': hp.uniform("x_f", 0., 1.),
        }
