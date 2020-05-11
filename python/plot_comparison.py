import json
import re
from argparse import ArgumentParser
import time
import multiprocessing as mp

import matplotlib.pyplot as plt
from tabulate import tabulate

from common import Algorithms, get_opt_space, Functions

plt.rcParams['figure.dpi'] = 300
plt.style.use('seaborn-white')
import seaborn as sns
import pandas as pd


def str_list_to_double_list(lst):
    return [float(ele) for ele in lst]


def get_pd_from_path(path, max_steps, compare_same=False, function_name=None):
    df = pd.read_csv(path, sep='\t')

    if max_steps:
        df = df[df['Step'] < max_steps]
    df = df.rename(columns={'Name': 'Algorithm'})
    df = df.astype({'Iteration': 'int32', 'Step': 'int32', 'Best value': 'float32'})

    iteration_count = df['Iteration'].unique().shape[0]
    step_count = int(df['Step'].max())

    # df = df.groupby(['Algorithm', 'Step'])['Best value'].agg(['min', 'mean', 'max']).reset_index()
    df = df.groupby(['Algorithm', 'Step'])['Best value'].agg(['mean', 'std']).reset_index()
    df['min'] = df['mean'] - df['std']
    df['min'] = df['min'].where(df['min'] > 0, 0)
    df['max'] = df['mean'] + df['std']

    df['Filename'] = path.split('/')[-1]

    if compare_same and function_name:
        algorithm = df['Algorithm'].iloc[0]
        space = get_opt_space(Algorithms(algorithm), Functions(function_name.upper()))

        if type(space) == tuple:
            space, _ = space
        hyper_parameters = [parameter[2:].replace('-', '_') for parameter in space.keys()]
        df['Hyperparameters'] = ", ".join(hyper_parameters)

        json_path = path.replace('.csv', '.json')

        algorithm_append = ''
        with open(json_path, 'r') as f:
            data = json.load(f)
            parameters_values = [f"{data[parameter]:.3f}" if type(data[parameter]) == float else str(data[parameter]) for parameter in hyper_parameters]
            algorithm_append += ' (' + '/'.join(parameters_values) + ')'
            df['Algorithm'] = df['Algorithm'] + algorithm_append

    return df, iteration_count, step_count


def plot(args):
    path_list = args.log_file_path
    if not len(path_list):
        raise RuntimeError

    res = re.match(r'.*[a-z]+\-([a-z]+)\-[0-9]{4}\-[0-9]{6}\-log\.csv', path_list[0])
    if not res:
        print("Wrong file name provided")
        return
    groups = res.groups()
    function_name = groups[0]

    pool = mp.Pool(mp.cpu_count())

    results = [pool.apply(get_pd_from_path, args=(path, args.max_steps), kwds={'compare_same': args.compare_same, 'function_name': function_name}) for path in path_list]
    df = pd.concat([result[0] for result in results])
    iteration_count = max([result[1] for result in results])
    step_count = min([result[2] for result in results])

    # Step 3: Don't forget to close
    pool.close()

    print(tabulate(df[df['Step'] == step_count].set_index('Filename'), headers='keys', tablefmt='psql'))

    algorithms_with_bits = []
    for algorithm in df['Algorithm'].unique():
        regex_result = re.match(r'.*\(([0-9]+) bits\).*', algorithm)
        bits = int(regex_result.groups()[0]) if regex_result else 0

        algorithms_with_bits.append((algorithm, bits))

    algorithms_with_bits.sort(key=lambda x: (x[1], x[0]))
    algorithms = [algorithm_with_bit[0] for algorithm_with_bit in algorithms_with_bits]
    fig, ax = plt.subplots()
    sns.lineplot(x="Step", y="mean",
                 hue="Algorithm",
                 hue_order=algorithms,
                 data=df,
                 estimator=None,
                 ci=False,
                 ax=ax,
                 alpha=0.3 if args.no_std_range else 1.)

    algorithm_colors = {}
    for line in ax.get_lines():
        if line.get_label() not in algorithms:
            continue

        line.set_alpha(.3)
        algorithm_colors[line.get_label()] = line.get_color()

    if args.no_std_range:
        for algorithm, color in algorithm_colors.items():
            plt.fill_between('Step', 'min', 'max', alpha=.3, data=df[df['Algorithm'] == algorithm], color=color)

    ax.set_title(f"Algorithms comparison plot\n{function_name.capitalize()} ({iteration_count} runs)")
    ax.set_ylabel('Best value')

    filename_extras = []
    if args.max_value:
        ax.set_ylim([0, args.max_value])
        filename_extras.append("v-{}".format(int(args.max_value)))
    if args.max_steps:
        ax.set_xlim([0, args.max_steps])
        filename_extras.append("s-{}".format(int(args.max_steps)))

    plt.savefig('{}-{}comparison.pdf'.format(function_name.lower(), "{}-".format("-".join(filename_extras)) if filename_extras else ""), dpi=300, transparent=True)
    plt.close()


def main():
    start_time = time.time()

    parser = ArgumentParser()
    parser.add_argument("log_file_path", nargs="+")
    parser.add_argument("--max-value", type=float, default=None, dest="max_value")
    parser.add_argument("--max-steps", type=float, default=None, dest="max_steps")
    parser.add_argument("--compare-same", action="store_true")
    parser.add_argument("--no-std-range", action="store_false")

    plot(parser.parse_args())

    print("--- %s seconds ---" % (time.time() - start_time))


if __name__ == '__main__':
    main()
