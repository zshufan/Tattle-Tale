import json
import random
import numpy as np
import os
import glob
import pandas as pd

high = ['State'] 
med = ['AreaCode', 'HasChild', 'SingleExemp', 'Zip']
low = ['MaritalStatus', 'ChildExemp', 'City']


def holoclean_test_gen(dataset_path, report_path):

    # read tax dataset
    dataset = pd.read_csv(dataset_path)
    tuple_num = dataset.shape[0]

    print(tuple_num)

    extension = 'csv'

    all_filenames = glob.glob(os.path.join(report_path, '*.{}'.format(extension)))

    combined_report = pd.concat([pd.read_csv(f, sep='\s*;\s*', error_bad_lines=False) for f in all_filenames ])

    f = open("run_holoclean.sh", "w+")

    for index, row in combined_report.iterrows():

        if row["useMVC"] == True and row["expID"] == 0 and row["policySenLevel"]=="high":
            num_sen_cell = row['numSenCells']
            sen_cells = row['senCells']
            hidden_cells = row['hiddenCells']
            policySenLevel = row['policySenLevel']
            useObl = row["useOblCueset"]

            sen_cells = sen_cells.strip('[]').split('},')
            hidden_cells = hidden_cells.strip('[]').split('},')

            sen_cells_dict_list = parse_cell_list(sen_cells)
            hidden_cells_dict_list = parse_cell_list(hidden_cells)

            gt_filename, test_filename = produce_filenames("holoclean_tax_test", num_sen_cell, policySenLevel, index, useObl)

            produce_test_files(test_filename, gt_filename, sen_cells_dict_list, dataset)

            produce_test_files(test_filename, gt_filename, hidden_cells_dict_list, dataset)

            write_bash_script(f, test_filename, gt_filename)
    

def parse_cell_list(cell_list):

    ret_dict_list = []

    for cell in cell_list:
        sen_cells_dict = {}

        if "taxes{" in cell:
            cell = cell.replace("taxes{", "")
        if "}" in cell:
            cell = cell.replace("}", "")
        if "hospital10k{" in cell:
            cell = cell.replace("hospital10k{", "")
        if "hospital{" in cell:
            cell = cell.replace("hospital{", "")
        cell = cell.split(",")

        sen_cells_dict['tid'] = int(cell[0])
        sen_cells_dict['attribute'] = cell[1].replace("\'", "").replace(" ", "")
        sen_cells_dict['correct_val'] = cell[2].replace("val:", "").replace("\'", "").replace(" ", "")

        ret_dict_list.append(sen_cells_dict)
    
    return ret_dict_list


def produce_filenames(dir, num_sen_cell, policySenLevel, index, useObl):
    gt_filename = os.path.join(dir, policySenLevel+"_"+str(num_sen_cell)+"_"+str(index) + "_" + str(useObl) +"_gt.csv")
    test_filename = os.path.join(dir, policySenLevel+"_"+str(num_sen_cell)+"_"+str(index) + "_" + str(useObl) +"_test.csv")
    return gt_filename, test_filename


def produce_test_files(test_filename, gt_filename, gt_dict_list, dataset):
    gt_csv = pd.DataFrame(gt_dict_list)
    gt_csv.to_csv(gt_filename, index=None)
    for gt in gt_dict_list:
        dataset.loc[gt['tid']-1, gt['attribute']] = ""

    dataset.to_csv(test_filename, index=None)

def write_bash_script(f, test_filename, gt_filename):
    print("python $script --dataset " + test_filename + " --ground_truth " + gt_filename + " --featurizer 1", file=f)


if __name__ == "__main__":

    dataset_path = "../testdata/hospital10k.csv"

    report_path = "../plot/eva/xxx" # path to output result report

    holoclean_test_gen(dataset_path, report_path)
