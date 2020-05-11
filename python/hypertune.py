import math
import os
import random
import subprocess
import time
from argparse import ArgumentParser

from hyperopt import fmin, tpe, STATUS_OK, Trials, STATUS_FAIL

from common import Functions, STEPS, SEED, Algorithms, get_opt_space
from plot_comparison import get_pd_from_path


def get_subprocess_arguments(algorithm, function, times, hp_args):
    arguments = ['java', '-jar', '../java/build/libs/java.jar', algorithm.name, function.name, str(STEPS[function]), '--times', str(times), ]

    for key, value in hp_args.items():
        if '--reset-resets-failures-counter' in key:
            if value:
                arguments.append(key)
            continue
        elif any([ele in key for ele in ['--improvements-loop-iteration', '--number_of_particles', '--neighbour-looks']]):
            value = int(value)
        elif '--omega' in key:
            if float(value) < 0.0:
                arguments.append(f"{key}={str(value)}")
                continue

        arguments.extend([key, str(value)])

    return arguments


def calculate_args_sum(args):
    return sum([value if type(value) == float else 0. for value in args.values()])


def find_best_params(algorithm, function, times, max_evaluations, fixed_arguments=None):
    if fixed_arguments is None:
        fixed_arguments = {}

    hyper_opt_space = get_opt_space(algorithm, function)

    if type(hyper_opt_space) == tuple:
        hyper_opt_space, max_evaluations = hyper_opt_space

    trials = Trials()

    def objective(args):
        arguments = get_subprocess_arguments(algorithm, function, times, args)
        proc = subprocess.Popen(arguments, stdout=subprocess.PIPE)
        try:
            proc_out, proc_err = proc.communicate(timeout=15)

            file_name = proc_out.decode().split("\n")[0]
            pd, _, _ = get_pd_from_path(file_name, STEPS[function])

            if proc_err and proc_err.decode():
                return {'status': STATUS_FAIL}
            else:
                return {'loss': pd['mean'][::50].mean() * 0.01 + 0.99 * pd['mean'].min(),
                        'status': STATUS_OK,
                        'filename': file_name,
                        'args_sum': calculate_args_sum(args)}
        except subprocess.TimeoutExpired:
            proc.kill()
            return {'status': STATUS_FAIL}

    fmin(objective, hyper_opt_space, algo=tpe.suggest, max_evals=max_evaluations, trials=trials)

    best_trial_filename = trials.best_trial['result']['filename']
    best_trial_args_sum = trials.best_trial['result']['args_sum']
    best_trial_loss = trials.best_trial['result']['loss']

    for result in trials.results:
        if result['status'] is not STATUS_OK:
            continue

        filename = result['filename']
        args_sum = result['args_sum']
        loss = result['loss']

        if math.isclose(best_trial_loss, loss) and args_sum < best_trial_args_sum:
            best_trial_filename = filename
            best_trial_args_sum = args_sum
            best_trial_loss = loss

    for result in trials.results:
        if result['status'] is not STATUS_OK:
            continue

        filename = result['filename']
        if filename != best_trial_filename:
            os.unlink(filename)
            os.unlink(filename.replace('.csv', '.json'))

    print(best_trial_filename)


def main():
    random.seed(SEED)
    start_time = time.time()

    parser = ArgumentParser()
    parser.add_argument("algorithm", type=Algorithms.argparse, choices=list(Algorithms))
    parser.add_argument("function", type=Functions.argparse, choices=list(Functions))
    parser.add_argument("--times", type=int, default=500)
    parser.add_argument("--max-evaluations", type=int, default=350)

    args = parser.parse_args()

    find_best_params(args.algorithm, args.function, args.times, args.max_evaluations)

    print("--- %s seconds ---" % (time.time() - start_time))


if __name__ == '__main__':
    main()
