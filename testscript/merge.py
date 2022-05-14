import json
import random
import numpy as np
import os
import glob
import pandas as pd
import json


report_path = "../plot/eva/server_test_taxdb_full_MVC" # path to output result report

extension = 'csv'

all_filenames = glob.glob(os.path.join(report_path, '*.{}'.format(extension)))

combined_report = pd.concat([pd.read_csv(f, sep='\s*;\s*', error_bad_lines=False) for f in all_filenames ])

policies = []

for index, row in combined_report.iterrows():
    hidden_cells = row['hiddenCells']
    hidden_cells = hidden_cells.strip('[]').split('},')

    for cell in hidden_cells:
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

        sen_cells_dict["databaseName"] = "taxdb"
        sen_cells_dict["relationName"] = "taxes"
        sen_cells_dict['tupleID'] = int(cell[0])
        sen_cells_dict['attributeName'] = cell[1].replace("\'", "").replace(" ", "")
        

        policies.append(sen_cells_dict)

print(len(policies))

with open('merged.txt', 'w') as fout:
    json.dump(policies, fout)