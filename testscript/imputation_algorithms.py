# some codes refer to Holoclean evaluation function
# https://github.com/HoloClean/holoclean

import pandas as pd
import numpy as np
import logging
import random
import argparse

parser = argparse.ArgumentParser(description='Predict on many examples')
parser.add_argument("--dataset", type=str, help="dataset path")
parser.add_argument("--ground_truth", type=str, help="ground truth path")
parser.add_argument("--ground_truth_2", type=str, help="ground truth path")
args = parser.parse_args()

NULL_REPR = '_nan_'
exclude_attr = ['_tid_', 'FName', 'LName']

class DataCleaningAsAdv:
    def __init__(self, csv_fpath) -> None:
        # load dataset with missing values
        self.load_dataset(csv_fpath)

        # associate with domain
        self.get_domain_knowledge()

    def load_dataset(self, fpath, na_values=None) -> None:
        try:
            # Do not include TID and source column as trainable attributes
            exclude_attr_cols = ['_tid_']

            self.df = pd.read_csv(fpath, dtype=str, na_values=na_values, encoding='utf-8')
            # Normalize the dataframe: drop null columns, convert to lowercase strings, and strip whitespaces.
            for attr in self.df.columns.values:
                if self.df[attr].isnull().all():
                    logging.warning("Dropping the following null column from the dataset: '%s'", attr)
                    self.df.drop(labels=[attr], axis=1, inplace=True)
                    continue
                if attr not in exclude_attr_cols:
                    self.df[attr] = self.df[attr].str.strip().str.lower()
                    
            # Add _tid_ column to dataset that uniquely identifies an entity.
            self.df.insert(0, '_tid_', range(0,len(self.df)))

            # Use NULL_REPR to represent NULL values
            self.df.fillna(NULL_REPR, inplace=True)

            # print(self.df.head())

            logging.info("Loaded %d rows with %d cells", self.df.shape[0], self.df.shape[0] * self.df.shape[1])

        except Exception:
            logging.error('loading data for missing data table %s', fpath)
            raise

    def load_ground_truth(self, fpath, tid_col, attr_col, val_col, na_values=None) -> None:
        try:
            self.gt_data = pd.read_csv(fpath, na_values=na_values, encoding='utf-8')
            # We drop any ground truth values that are NULLs since we follow
            # the closed-world assumption (if it's not there it's wrong).
            # TODO: revisit this once we allow users to specify which
            # attributes may be NULL.
            self.gt_data.dropna(subset=[val_col], inplace=True)
            self.gt_data.fillna(NULL_REPR, inplace=True)
            self.gt_data.rename({tid_col: '_tid_',
                             attr_col: '_attribute_',
                             val_col: '_value_'},
                            axis='columns',
                            inplace=True)
            self.gt_data = self.gt_data[['_tid_', '_attribute_', '_value_']]
            # Normalize string to whitespaces.
            self.gt_data['_value_'] = self.gt_data['_value_'].str.strip().str.lower()

        except Exception:
            logging.error('load_data for ground truth table %s', fpath)
            raise

    def get_domain_knowledge(self) -> None:
        # get the domain of each column 
        # and the frequency of each value in the domain
        self.domain = {}
        self.weight = {}
        for attr in self.df.columns.values:
            if attr in exclude_attr:
                continue
            domain = self.df[attr].unique()
            if NULL_REPR in domain:
                domain = domain[domain != NULL_REPR]
            self.domain[attr] = domain
            attr_gb_count_df = self.df.groupby([attr])[attr].count()
            # print(attr_gb_count_df)
            self.weight[attr] = [attr_gb_count_df[val] for val in domain]
            # print(self.weight[attr])

    def fill_in_random_value(self) -> None:
        self.random_repair = self.df.copy()
        for attr in self.df.columns.values:
            if attr in exclude_attr:
                continue

            # fill in the missing values
            indices = self.random_repair[self.random_repair[attr]==NULL_REPR].index.tolist()
            # print(indices)
            for index in indices:
                if self.random_repair.loc[index][attr] is not NULL_REPR:
                    logging.error("index not match")
                    raise
                self.random_repair.at[index, attr] = np.random.choice(self.domain[attr])
                # print(self.random_repair.loc[index][attr], self.df.loc[index][attr])
        

    def fill_in_popular_value(self) -> None:
        self.popular_repair = self.df.copy()
        for attr in self.df.columns.values:
            if attr in exclude_attr:
                continue

            # sort the zipped list to get the most popular item
            # in each column in the ascending order
            zipped = zip(self.domain[attr], self.weight[attr])
            sorted_zip = sorted(zipped, key=lambda x: x[1])
            # print(sorted_zip[-1])

            # fill in the missing values
            indices = self.popular_repair[self.popular_repair[attr]==NULL_REPR].index.tolist()
            for index in indices:
                if self.popular_repair.loc[index][attr] is not NULL_REPR:
                    logging.error("index not match")
                    raise
                self.popular_repair.at[index, attr] = sorted_zip[-1][0]
                # print(self.popular_repair.loc[index][attr], self.df.loc[index][attr])

    def fill_in_by_weighted_sampling(self) -> None:
        self.weighted_repair = self.df.copy()
        for attr in self.df.columns.values:
            if attr in exclude_attr:
                continue

            # fill in the missing values
            indices = self.weighted_repair[self.weighted_repair[attr]==NULL_REPR].index.tolist()
            # print(indices)
            for index in indices:
                if self.weighted_repair.loc[index][attr] is not NULL_REPR:
                    logging.error("index not match")
                    raise
                self.weighted_repair.at[index, attr] = random.choices(self.domain[attr], weights=self.weight[attr], k=1)[0]
                # print(self.weighted_repair.loc[index][attr], self.df.loc[index][attr])


    def evaluate(self, gt_fpath, tid_col, attr_col, val_col, file) -> None:
        self.load_ground_truth(gt_fpath, tid_col, attr_col, val_col)
        total_repairs = self.gt_data.shape[0]

        def _evaluate(df) -> int:
            correct_repair = 0
            for _, row in self.gt_data.iterrows():
                if df.loc[row['_tid_']][row['_attribute_']] == row['_value_']:
                    if self.df.loc[row['_tid_']][row['_attribute_']] is not NULL_REPR:
                        logging.error("index not match when evaluating")
                        raise
                    correct_repair += 1
            return correct_repair

        # evaluate random filling
        self.fill_in_random_value()
        correct_repair = _evaluate(self.random_repair)
        print("Precision of random filling: {}, correct_repairs: {}, total_repairs: {}".format(correct_repair/total_repairs, correct_repair, total_repairs), file=file)

        # evaluate popular filling
        self.fill_in_popular_value()
        correct_repair = _evaluate(self.popular_repair)
        print("Precision of popular filling: {}, correct_repairs: {}, total_repairs: {}".format(correct_repair/total_repairs, correct_repair, total_repairs), file=file)

        # evaluate weighted filling
        self.fill_in_by_weighted_sampling()
        correct_repair = _evaluate(self.weighted_repair)
        print("Precision of weighted filling: {}, correct_repairs: {}, total_repairs: {}".format(correct_repair/total_repairs, correct_repair, total_repairs), file=file)


if __name__ == "__main__":

    # load dataset
    adv = DataCleaningAsAdv(args.dataset)
    
    f = open("baseline_cleaning_report_1", "a")

    print(args.dataset, file=f)

    # evaluate
    adv.evaluate(gt_fpath=args.ground_truth,
            tid_col='tid',
            attr_col='attribute',
            val_col='correct_val', file=f)

    if args.ground_truth_2 is not None:
        adv.evaluate(gt_fpath=args.ground_truth_2,
            tid_col='tid',
            attr_col='attribute',
            val_col='correct_val', file=f)

