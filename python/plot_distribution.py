import csv
import functools
import math
import os
import re
from _csv import QUOTE_NONE
from argparse import ArgumentParser

import matplotlib.pyplot as plt
import pandas as pd
import time
import numpy as np

plt.rcParams['figure.dpi'] = 300
plt.style.use('seaborn-white')


def get_dict_of_lists(list_of_dicts):
    dict_of_lists = {key: [item[key] for item in list_of_dicts] for key in list(
        functools.reduce(
            lambda x, y: x.union(y),
            (set(dicts.keys()) for dicts in list_of_dicts)
        )
    )}

    dict_of_lists["X1"] = str_list_to_double_list(dict_of_lists["X1"])
    dict_of_lists["X2"] = str_list_to_double_list(dict_of_lists["X2"])
    dict_of_lists["Value"] = str_list_to_double_list(dict_of_lists["Value"])
    dict_of_lists["Best value"] = str_list_to_double_list(dict_of_lists["Best value"])

    return dict_of_lists


def get_title(function_name, algorithm):
    return function_name.capitalize() + ' - ' + algorithm


def rosenbrock(X):
    """The rosenbrock function"""
    X = np.asarray_chkfinite(X)
    x0 = X[:-1]
    x1 = X[1:]
    return (sum( (1 - x0) **2 )
        + 100 * sum( (x1 - x0**2) **2 ))


def rastrigin(X, **kwargs):
    """The Rastrigin function"""
    A = kwargs.get('A', 10)
    return A + sum([(x**2 - A * np.cos(2 * math.pi * x)) for x in X])


def plot(path, title):

    if not os.path.exists(path):
        raise FileNotFoundError

    with open(path, "r") as f:
        reader = csv.DictReader(f, delimiter="\t", quoting=QUOTE_NONE)

        list_of_dicts = [dict(row) for row in reader]

        df = pd.DataFrame(get_dict_of_lists(list_of_dicts))

        if "Iteration" in df.columns:
            df["Iteration"] = df["Iteration"].astype(int)
            df = df[df["Iteration"] == 0]

        fig, ax = plt.subplots()

        res = re.match(r'.*[a-z]+\-([a-z]+)\-([0-9]*)\-?log\.csv', path)
        if not res:
            print("Wrong file name provided")
            return
        groups = res.groups()
        function_name = groups[0]
        bits = int(groups[1])

        if bits:
            if 'rosenbrock' in function_name:
                x = np.linspace(-2.048, 2.048, 100)
                xx, yy = [ele.flatten() for ele in np.meshgrid(x, x)]
                val = rosenbrock([xx, yy])
            else:
                x = np.linspace(-5.12, 5.12, 100)
                xx, yy = [ele.flatten() for ele in np.meshgrid(x, x)]
                val = rastrigin([xx, yy])

            df_real = pd.DataFrame({"X1": xx, "X2": yy, "Value": val})
            # ax.plot_surface(df_real["X1"], df_real["X2"], df_real["Value"], levels=20, linewidths=0.5, colors='k')
            cntr2 = ax.tricontourf(df_real["X1"], df_real["X2"], df_real["Value"], levels=20, cmap="RdBu_r")

            ax.plot(df["X1"][0], df["X2"][0], 'ko', markersize=7, markerfacecolor=(0, 1., 1., 0.8))
            ax.plot(df["X1"][1:], df["X2"][1:], 'ko', markersize=7, markerfacecolor=(1., 1., 0, 0.8))
            title = title or get_title(function_name, df['Name'].values[0])
            ax.set_title(f"Bit flips plot\n{title} ({bits} {'bit' if bits == 1 else 'bits'} per dim)")
        else:

            # Source: https://matplotlib.org/3.2.0/gallery/images_contours_and_fields/irregulardatagrid.html
            # ----------
            # Tricontour
            # ----------
            # Directly supply the unordered, irregularly spaced coordinates
            # to tricontour.
            ax.tricontour(df["X1"], df["X2"], df["Value"], levels=20, linewidths=0.5, colors='k')
            cntr2 = ax.tricontourf(df["X1"], df["X2"], df["Value"], levels=20, cmap="RdBu_r")

            ax.plot(df["X1"], df["X2"], 'ko', markersize=2, markerfacecolor=(0, 0, 0, 0.1))
            title = title or get_title(function_name, df['Name'].values[0])
            ax.set_title(f"Sampling distribution plot\n{title} ({df.shape[0]} points)")

        clb = fig.colorbar(cntr2, ax=ax)
        clb.ax.set_title('Value')
        if 'rosenbrock' in function_name:
            ax.set(xlim=(-2.048, 2.048), ylim=(-2.048, 2.048))
        else:
            ax.set(xlim=(-5.12, 5.12), ylim=(-5.12, 5.12))
        ax.set_xlabel("X1")
        ax.set_ylabel("X2")

        plt.subplots_adjust(hspace=0.5)
        plt.savefig(path.split("/")[-1].replace('.csv', '.pdf'), dpi=300, transparent=True)


def str_list_to_double_list(lst):
    return [float(ele) for ele in lst]


def main():
    start_time = time.time()

    parser = ArgumentParser()
    parser.add_argument("log_file_path")
    parser.add_argument("--title")

    args = parser.parse_args()
    plot(args.log_file_path, args.title)

    print("--- %s seconds ---" % (time.time() - start_time))


if __name__ == '__main__':
    main()
