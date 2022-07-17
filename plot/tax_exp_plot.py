'''
Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data (VLDB 2022)

Experiments on Tax dataset: 
    code for plotting
    Figure 3, 4, 6a in paper
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



report_dir_path = './eva/tax/server_test_taxdb_*'

extension = 'csv'

all_filenames = glob.glob(os.path.join(report_dir_path, '*.{}'.format(extension)))
print(all_filenames)

#combine all files in the list
combined_report = pd.concat([pd.read_csv(f, sep='\s*;\s*', error_bad_lines=False) for f in all_filenames])


# split data: evaluation for full-den and k-den
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

num_sen_cells, full_high_num_hidden_cells_MVC_noObl, full_high_num_cuesets_MVC_noObl, full_high_time_cost_MVC_noObl = data_process(full_high_MVC_noObl)
num_sen_cells, full_high_num_hidden_cells_noMVC_noObl, full_high_num_cuesets_noMVC_noObl, full_high_time_cost_noMVC_noObl = data_process(full_high_noMVC_noObl)
num_sen_cells, full_high_num_hidden_cells_MVC_Obl, full_high_num_cuesets_MVC_Obl, full_high_time_cost_MVC_Obl = data_process(full_high_MVC_Obl)

k_high = k_den_part_noObl[k_den_part_noObl["policySenLevel"] == "high"]
k_0_high = k_high[k_high["k_value"] == 0.0]
k_05_high = k_high[k_high["k_value"] == 0.5]
k_01_high = k_high[k_high["k_value"] == 0.1]

num_sen_cells, k_0_high_num_hidden_cells, k_0_high_num_cuesets, k_0_high_time_cost = data_process(k_0_high)
num_sen_cells, k_01_high_num_hidden_cells, k_01_high_num_cuesets, k_01_high_time_cost = data_process(k_01_high)
num_sen_cells, k_05_high_num_hidden_cells, k_05_high_num_cuesets, k_05_high_time_cost = data_process(k_05_high)


'''
Experiment 1: Baseline comparison
Metric: data utility
Figure 3a
'''
x_axis = [i for i in num_sen_cells]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)

plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1), marker="x", label="Our approach", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(full_high_num_hidden_cells_noMVC_noObl, axis=1), marker="o", label="Random Hiding", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_noMVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_noMVC_noObl, axis=1)-error, np.mean(full_high_num_hidden_cells_noMVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1), marker="v", label="Oblivious Cueset", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_Obl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_Obl, axis=1)+error, alpha=0.3)


plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("No. of Hidden Cells")
plt.grid(False)

fig.savefig('tax_data_utility.png', dpi=500)
print("Figure 3a")

'''
Experiment 1: Baseline comparison
Metric: efficiency
Figure 3b
'''
x_axis = [i for i in num_sen_cells]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)

plt.plot(x_axis, np.mean(full_high_time_cost_MVC_noObl, axis=1), linestyle='--', marker="x", label="Our approach", linewidth=2.5)
error = np.std(full_high_time_cost_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_time_cost_MVC_noObl, axis=1)-error, np.mean(full_high_time_cost_MVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(full_high_time_cost_noMVC_noObl, axis=1), linestyle='--', marker="o", label="Random Hiding", linewidth=2.5)
error = np.std(full_high_time_cost_noMVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_time_cost_noMVC_noObl, axis=1)-error, np.mean(full_high_time_cost_noMVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(full_high_time_cost_MVC_Obl, axis=1), linestyle='--', marker="o", label="Oblivious Cueset", linewidth=2.5)
error = np.std(full_high_time_cost_MVC_Obl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_time_cost_MVC_Obl, axis=1)-error, np.mean(full_high_time_cost_MVC_Obl, axis=1)+error, alpha=0.3)


plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("Second")
plt.grid(False)

fig.savefig('tax_efficiency.png', dpi=500)
print("Figure 3b")



'''
Experiment 1: Baseline comparison
Metric: cueset fan-out
Figure 4a
'''
def zero_to_nan(d):
    array = np.array(d)
    array[array == 0] = np.NaN
    return array


full_high_MVC = full_den_part_noObl[(full_den_part_noObl["policySenLevel"] == "high") & (full_den_part_noObl["useMVC"] == True)]
full_high_noMVC = full_den_part_noObl[(full_den_part_noObl["policySenLevel"] == "high") & (full_den_part_noObl["useMVC"] == False)]

fanout_full_high_MVC_10 = full_high_MVC[full_high_MVC['numSenCells']==10]
fanout_full_high_noMVC_10 = full_high_noMVC[full_high_noMVC['numSenCells']==10]

cueset_fanout_MVC_10 = []
cueset_fanout_noMVC_10 = []

for index, row in fanout_full_high_MVC_10[fanout_full_high_MVC_10['useMVC']==True].iterrows():
    cueset_fanout_MVC_10.append(row['cueSetsFanOut'].strip('[]').split(','))
    
for index, row in fanout_full_high_noMVC_10[fanout_full_high_noMVC_10['useMVC']==False].iterrows():
    cueset_fanout_noMVC_10.append(row['cueSetsFanOut'].strip('[]').split(','))


cueset_fanout_MVC_10 = [[int(numeric_string) for numeric_string in cueset_array] for cueset_array in cueset_fanout_MVC_10]
cueset_fanout_noMVC_10 = [[int(numeric_string) for numeric_string in cueset_array] for cueset_array in cueset_fanout_noMVC_10]

# baseline algorithm may not converge, we enforce truncation to plot the figure
truncate = lambda a, i : a[0:i] if len(a) > i else a + [0] * (i-len(a))

cueset_fanout_MVC_10 = np.array([truncate(a, 5) for a in cueset_fanout_MVC_10])


# plot figure 4a
mean = cueset_fanout_MVC_10.sum(0)/(cueset_fanout_MVC_10 != 0).sum(0)
cueset_fanout_MVC_10 = cueset_fanout_MVC_10.astype('float')
stdev = np.nanstd(zero_to_nan(cueset_fanout_MVC_10), axis=0)

fig = plt.figure(figsize=(16, 9))

plt.plot(range(len(cueset_fanout_MVC_10[0])), mean, marker="x", label="Our approach", linewidth=2.5)
error = stdev
plt.fill_between(range(len(cueset_fanout_MVC_10[0])), mean-error, mean+error, alpha=0.3)


plt.plot(range(len(cueset_fanout_noMVC_10[0])), np.mean(cueset_fanout_noMVC_10, axis=0), marker="o", label="Random Hiding", linewidth=2.5)
error = np.std(cueset_fanout_noMVC_10, axis=0)
plt.fill_between(range(len(cueset_fanout_noMVC_10[0])), np.mean(cueset_fanout_noMVC_10, axis=0)-error, np.mean(cueset_fanout_noMVC_10, axis=0)+error, alpha=0.3)


plt.legend()
plt.xticks(range(len(cueset_fanout_noMVC_10)))
plt.xlabel("No. of Iterations")
plt.ylabel("No. of Cuesets")
plt.grid(False)
plt.ticklabel_format(style='plain', useOffset=False)

fig.savefig('tax_cueset_fanout.png', dpi=500)
print("Figure 4a")



'''
Experiment 1: Baseline comparison
Metric: hidden cells fan-out
Figure 4b
'''
hiddencells_fanout_MVC_10 = []
hiddencells_fanout_noMVC_10 = []

for index, row in fanout_full_high_MVC_10[fanout_full_high_MVC_10['useMVC']==True].iterrows():
    hiddencells_fanout_MVC_10.append(row['hiddenCellsFanOut'].strip('[]').split(','))
    
for index, row in fanout_full_high_noMVC_10[fanout_full_high_noMVC_10['useMVC']==False].iterrows():
    hiddencells_fanout_noMVC_10.append(row['hiddenCellsFanOut'].strip('[]').split(','))

hiddencells_fanout_MVC_10 = [[int(numeric_string) for numeric_string in hiddencells_array] for hiddencells_array in hiddencells_fanout_MVC_10]
hiddencells_fanout_noMVC_10 = [[int(numeric_string) for numeric_string in hiddencells_array] for hiddencells_array in hiddencells_fanout_noMVC_10]

# baseline algorithm may not converge, we enforce truncation to plot the figure
truncate = lambda a, i : a[0:i] if len(a) > i else a + [0] * (i-len(a))
hiddencells_fanout_MVC_10 = np.array([truncate(a, 5) for a in hiddencells_fanout_MVC_10])

# plot 4b
mean = hiddencells_fanout_MVC_10.sum(0)/(hiddencells_fanout_MVC_10 != 0).sum(0)
hiddencells_fanout_MVC_10 = hiddencells_fanout_MVC_10.astype('float')
stdev = np.nanstd(zero_to_nan(hiddencells_fanout_MVC_10), axis=0)

fig = plt.figure(figsize=(16, 9))

plt.plot(range(len(hiddencells_fanout_MVC_10[0])), mean, marker="x", label="Our approach", linewidth=2.5)
error = stdev
plt.fill_between(range(len(hiddencells_fanout_MVC_10[0])), mean-error, mean+error, alpha=0.3)


plt.plot(range(len(hiddencells_fanout_noMVC_10[0])), np.mean(hiddencells_fanout_noMVC_10, axis=0), marker="o", label="Random Hiding", linewidth=2.5)
error = np.std(hiddencells_fanout_noMVC_10, axis=0)
plt.fill_between(range(len(hiddencells_fanout_noMVC_10[0])), np.mean(hiddencells_fanout_noMVC_10, axis=0)-error, np.mean(hiddencells_fanout_noMVC_10, axis=0)+error, alpha=0.3)

plt.legend()
plt.xticks(range(len(hiddencells_fanout_noMVC_10)))
plt.xlabel("No. of Iterations")
plt.ylabel("No. of Hidden Cells")
plt.grid(False)

fig.savefig('tax_hiddencells_fanout.png', dpi=500)
print("Figure 4b")


'''
Experiment 3: Extensions k-den
Metric: data utility
Figure 6a
'''
x_axis = [i for i in num_sen_cells]
y_axis_full_high_MVC = [str(round(i)) + '(' + str(round(i/database_size * 100, 1)) + "\%)" for i in np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)]
y_axis_k_0_high = [str(round(i)) + '(' + str(round(i/database_size * 100, 1)) + "\%)" for i in np.mean(k_0_high_num_hidden_cells, axis=1)]
y_axis_k_05_high = [str(round(i)) + '(' + str(round(i/database_size * 100, 1)) + "\%)" for i in np.mean(k_05_high_num_hidden_cells, axis=1)]

fig = plt.figure(figsize=(16, 9))

plt.xticks(x_axis)


plt.plot(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1), marker="x", label="Our approach (full-den)", linewidth=2.5)
error = np.std(full_high_num_hidden_cells_MVC_noObl, axis=1)
plt.fill_between(x_axis, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)-error, np.mean(full_high_num_hidden_cells_MVC_noObl, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(k_05_high_num_hidden_cells, axis=1), marker="v", label="k-den: k=0.5", linewidth=2.5)
error = np.std(k_05_high_num_hidden_cells, axis=1)
plt.fill_between(x_axis, np.mean(k_05_high_num_hidden_cells, axis=1)-error, np.mean(k_05_high_num_hidden_cells, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(k_01_high_num_hidden_cells, axis=1), marker="x", label="k-den: k=0.1", linewidth=2.5)
error = np.std(k_01_high_num_hidden_cells, axis=1)
plt.fill_between(x_axis, np.mean(k_01_high_num_hidden_cells, axis=1)-error, np.mean(k_01_high_num_hidden_cells, axis=1)+error, alpha=0.3)

plt.plot(x_axis, np.mean(k_0_high_num_hidden_cells, axis=1), marker="X", label="Unconstrained: k=0", linewidth=2.5)
error = np.std(k_0_high_num_hidden_cells, axis=1)
plt.fill_between(x_axis, np.mean(k_0_high_num_hidden_cells, axis=1)-error, np.mean(k_0_high_num_hidden_cells, axis=1)+error, alpha=0.3)


plt.legend()
plt.xlabel("No. of Policies")
plt.ylabel("No. of Hidden Cells")
plt.grid(False)

fig.savefig('tax_k_data_utility.png', dpi=500)
print("Figure 6a")