'''
Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data (VLDB 2022)

Experiments on Hospital10k dataset: 
    code for plotting
    Figure 6b, 10 in paper
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


# parse dataframe load from report csv
def data_process(report):
    # x axis: no. of sensitive cells
    num_sen_cells = report["numSenCells"].unique()
    print(num_sen_cells)

    # y axis: no. of hidden cells
    num_hidden_cells = []

    for i in num_sen_cells:
        num_hidden_cells_i = report[report["numSenCells"] == i]["numHiddenCells"].to_list()
        num_hidden_cells.append(num_hidden_cells_i)

    num_cuesets = []

    for i in num_sen_cells:
        num_cuesets_i = report[report["numSenCells"] == i]["numCuesets"].to_list()
        num_cuesets.append(num_cuesets_i)

    time_cost = []

    for i in num_sen_cells:
        time_cost_i = report[report["numSenCells"] == i]["executionTime (HH:mm:ss.SSS)"].to_list()
        converted_time_i = []
        for timestamp_i in time_cost_i:
            dt_obj = datetime.strptime(timestamp_i,
                           '%H:%M:%S.%f')
            millisec = dt_obj.hour * 3600 * 1000 + dt_obj.minute * 60 * 1000 + dt_obj.second * 1000 + dt_obj.microsecond / 1000
            converted_time_i.append(millisec/1000)
        time_cost.append(converted_time_i)
    
    return num_sen_cells, num_hidden_cells, num_cuesets, time_cost


report_dir_path = './eva/hospital/server_test_hospital_*'

extension = 'csv'

all_filenames = glob.glob(os.path.join(report_dir_path, '*.{}'.format(extension)))
print(all_filenames)

#combine all files in the list
combined_report = pd.concat([pd.read_csv(f, sep='\s*;\s*', error_bad_lines=False) for f in all_filenames ])

# split data
full_den_part = combined_report[combined_report["usingAlgorithm"] == "full-den"]
full_den_part_noObl = full_den_part[full_den_part["useOblCueset"] == False]
full_den_part_Obl = full_den_part[full_den_part["useOblCueset"] == True]

k_den_part = combined_report[combined_report["usingAlgorithm"] == "k-den"]
k_den_part_noObl = k_den_part[k_den_part["useOblCueset"] == False]
k_den_part_Obl = k_den_part[k_den_part["useOblCueset"] == True]

# get database size
attrSize = 15
tupleSize = combined_report['DBSize'].to_list()[0]
database_size = attrSize * tupleSize

# process data
full_high_MVC_noObl = full_den_part_noObl[(full_den_part_noObl["policySenLevel"] == "high") & (full_den_part_noObl["useMVC"] == True)]
full_high_noMVC_noObl = full_den_part_noObl[(full_den_part_noObl["policySenLevel"] == "high") & (full_den_part_noObl["useMVC"] == False)]
full_high_MVC_Obl = full_den_part_Obl[(full_den_part_Obl["policySenLevel"] == "high") & (full_den_part_Obl["useMVC"] == True)]
full_high_noMVC_Obl = full_den_part_Obl[(full_den_part_Obl["policySenLevel"] == "high") & (full_den_part_Obl["useMVC"] == False)]

k_high = k_den_part_noObl[k_den_part_noObl["policySenLevel"] == "high"]
k_0_high = k_high[k_high["k_value"] == 0.0]
k_003_high = k_high[k_high["k_value"] == 0.03]

num_sen_cells, full_high_num_hidden_cells_MVC_noObl, full_high_num_cuesets_MVC_noObl, full_high_time_cost_MVC_noObl = data_process(full_high_MVC_noObl)
num_sen_cells, full_high_num_hidden_cells_MVC_Obl, full_high_num_cuesets_MVC_Obl, full_high_time_cost_MVC_Obl = data_process(full_high_MVC_Obl)
num_sen_cells, k_0_high_num_hidden_cells, k_0_high_num_cuesets, k_0_high_time_cost = data_process(k_0_high)
num_sen_cells, k_003_high_num_hidden_cells, k_003_high_num_cuesets, k_003_high_time_cost = data_process(k_003_high)


'''
Experiment 1: Baseline comparison
Metric: data utility
Figure 10a
'''
x_axis = [i for i in num_sen_cells]
y_axis_full_high_MVC = [str(round(i)) + '(' + str(round(i/database_size * 100, 1)) + "\%)" for i in np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)

plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1), marker="x", label="Our approach", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)+error, alpha=0.3)


plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1), marker="v", label="Oblivious Cueset", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_Obl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1)+error, alpha=0.3)


plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("No. of Hidden Cells")
plt.grid(False)

fig.savefig('hospital10k_data_utility.png', dpi=500)
print("Figure 10a")


'''
Experiment 1: Baseline comparison
Metric: efficiency
Figure 10b
'''
x_axis = [i for i in num_sen_cells]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)

plt.plot(x_axis, np.mean(full_high_time_cost_MVC_noObl, axis=1), linestyle='--', marker="x", label="Our approach", linewidth=2.5)
error = np.std(full_high_time_cost_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_time_cost_MVC_noObl, axis=1)-error, np.mean(full_high_time_cost_MVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(full_high_time_cost_MVC_Obl, axis=1), linestyle='--', marker="o", label="Oblivious Cueset", linewidth=2.5)
error = np.std(full_high_time_cost_MVC_Obl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_time_cost_MVC_Obl, axis=1)-error, np.mean(full_high_time_cost_MVC_Obl, axis=1)+error, alpha=0.3)



plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("Second")
plt.grid(False)

fig.savefig('hospital10k_efficiency.png', dpi=500)
print("Figure 10b")


'''
Experiment 3: Extensions - k-den
Metric: data utility
Figure 6b
'''
x_axis = [i for i in num_sen_cells]
y_axis_full_high_MVC = [str(round(i)) + '(' + str(round(i/database_size * 100, 1)) + "\%)" for i in np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)

plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1), marker="x", label="Our approach (full-den)", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(k_003_high_num_hidden_cells, axis=1), marker=".", label="k-den: k=0.03", linewidth=2.5)
error = np.std(k_003_high_num_hidden_cells, axis=1)
plt.fill_between(x_axis, np.mean(k_003_high_num_hidden_cells, axis=1)-error, np.mean(k_003_high_num_hidden_cells, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(k_0_high_num_hidden_cells, axis=1), marker="X", label="Unconstrained: k=0", linewidth=2.5)
error = np.std(k_0_high_num_hidden_cells, axis=1)
plt.fill_between(x_axis, np.mean(k_0_high_num_hidden_cells, axis=1)-error, np.mean(k_0_high_num_hidden_cells, axis=1)+error, alpha=0.3)


plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("No. of Hidden Cells")
plt.grid(False)

fig.savefig('hospital10k_k_data_utility.png', dpi=500)
print("Figure 6b")