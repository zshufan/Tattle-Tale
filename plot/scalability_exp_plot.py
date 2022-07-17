'''
Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data (VLDB 2022)

Scalability experiments for full Hospital dataset (100k tuples): 
    code for plotting
'''

import os
import glob

import pandas as pd
import numpy as np

from datetime import datetime

import matplotlib as mpl
import matplotlib.pyplot as plt


# config matplotlib fonts
rc_fonts = {
    "font.family": "serif",
    "font.size": 24,
    'figure.figsize': (10, 8),
    "text.usetex": True,
    'text.latex.preview': True,
    'text.latex.preamble': [
        r"""
        \usepackage{libertine}
        \usepackage[libertine]{newtxmath}
        """],
}
mpl.rcParams.update(rc_fonts)


# processing the experimental report csv
def process(report):
      
    ret_time_cost = []
    
    for expId in set(report["expID"]):
        time_cost = []

        for index, row in report[report["expID"] == expId].iterrows():
            i = index % 10

            timestamp_i = row["executionTime (HH:mm:ss.SSS)"]
            dt_obj = datetime.strptime(timestamp_i, '%H:%M:%S.%f')
            millisec = dt_obj.hour * 3600 * 1000 + dt_obj.minute * 60 * 1000 + dt_obj.second * 1000 + dt_obj.microsecond / 1000
            converted_time = millisec/1000

            time_cost.append(converted_time) 


        ret_time_cost.append(time_cost)
    
    return ret_time_cost


def plot(report_dir_path, plot_filename):

    extension = 'csv'

    all_filenames = glob.glob(os.path.join(report_dir_path, '*.{}'.format(extension)))

    #combine all files in the list
    combined_report = pd.concat([pd.read_csv(f, sep='\s*;\s*', error_bad_lines=False) for f in all_filenames ])


    time_cost = process(combined_report)


    # plotting
    x_axis = [(i+1) * 10000 for i in range(combined_report[combined_report["expID"] == 0].shape[0])]

    fig = plt.figure(figsize=(16, 9))

    plt.xticks(x_axis)

    plt.plot(x_axis, np.mean(time_cost, axis=0), marker="x", label="Our approach (OG)", linewidth=2.5)
    error = np.std(time_cost, axis=0)
    plt.fill_between(x_axis, np.mean(time_cost, axis=0)-error, np.mean(time_cost, axis=0)+error, alpha=0.3)

    plt.xlabel("No. of Tuples")
    plt.ylabel("Second")
    plt.grid(False)

    fig.savefig(plot_filename, dpi=500)



if __name__ == '__main__':

    report_dir_path_ratio = './eva/scalability/hospital/ratio'
    report_dir_path_fix = './eva/scalability/hospital/fix'

    plot_filename_ratio = 'scalability_ratio.png'
    plot_filename_fix = 'scalability_fix.png'

    plot(report_dir_path_ratio, plot_filename_ratio)
    plot(report_dir_path_fix, plot_filename_fix)