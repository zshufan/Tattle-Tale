import json
import random
import numpy as np
import os

high = ['HospitalName'] 
med = ['State', 'CountyName', 'HospitalType', 'PhoneNumber', 'HospitalOwner']
low = ['MeasureCode', 'MeasureName']


algo_list = ['full-den', 'k-den']

DCFileName = "/testdata/hospital_constraints.txt" # path to constraints file

os.makedirs('../testdata/testcases/')


def testcase_gen(curPolicyArray, policySenLevel, testcase_count, limit, runs, database_name, relation_name, test_name, is_monotonic, no_sen_policies, testfanout, test_obl_cueset, tuple_start, tuple_end, is_pagination, binning_tuples, merging_size, if_sampling_ratio):


    # full-den with MVC
    test = []

    for k in range(0, runs):

        for i in range(0, testcase_count):

            if if_sampling_ratio is False:
                np.random.seed(42+k)
                # without replacement sample for tid's
                if is_monotonic:
                    tid = np.random.choice(range(1, (i+1) * binning_tuples), no_sen_policies, replace=False)
                else:
                    tid = np.random.choice(range(1, (i+1) * binning_tuples), no_sen_policies, replace=True)

                np.random.seed(42+k)
                attributes_sample = np.random.choice(curPolicyArray, no_sen_policies, replace=True)
            else:
                np.random.seed(42+k)
                # without replacement sample for tid's
                if is_monotonic:
                    tid = np.random.choice(range(1, (i+1) * binning_tuples), (i+1) * no_sen_policies, replace=False)
                else:
                    tid = np.random.choice(range(1, (i+1) * binning_tuples), (i+1) * no_sen_policies, replace=True)

                np.random.seed(42+k)
                attributes_sample = np.random.choice(curPolicyArray, (i+1) * no_sen_policies, replace=True)

            testcase = {}
            testcase['expID'] = k
            testcase['userID'] = "38400000-8cf0-11bd-b23e-10b96e4ef00d"
            testcase['userName'] = "Samus"
            testcase['databaseName'] = database_name
            testcase['relationName'] = relation_name
            testcase['tuple_start'] = tuple_start
            testcase['tuple_end'] = (i+1) * binning_tuples
            testcase['isPagination'] = is_pagination
            testcase['binning_size'] = (i+1)  # because the database size is (i+1) * 10k
            testcase['merging_size'] = merging_size
            testcase['purpose'] = "analytics"
            testcase['DCFileName'] = DCFileName
            testcase['algo'] = "full-den"

            testcase['k_value'] = 0

            testcase['limit'] = (i+1) * binning_tuples
            testcase['isAscend'] = True
            testcase['policySenLevel'] = policySenLevel

            policies = []

            no_policies = (i+1) * no_sen_policies if if_sampling_ratio is True else no_sen_policies

            for j in range(0, no_policies):
                policy = {}
                policy['databaseName'] = database_name
                policy['relationName'] = relation_name
                
                policy['tupleID'] = int(tid[j])
                policy['attributeName'] = attributes_sample[j]

                policies.append(policy)

            randomFlag = {}
            randomFlag['seed'] = 42 + k
            randomFlag['randomCuesetChoosing'] = True
            randomFlag['randomHiddenCellChoosing'] = True

            testcase['policies'] = policies
            testcase['randomFlag'] = randomFlag
            testcase['testname'] = test_name + "_full_MVC"
            testcase['useMVC'] = True
            testcase['testOblCueset'] = test_obl_cueset
            test.append(testcase)

    with open('../testdata/testcases/testcases_full_MVC_'+ policySenLevel + "_" + relation_name + "_obl_" + str(test_obl_cueset) + "_ratio_" + str(if_sampling_ratio) +'.json', 'w') as f:
        json.dump(test, f, ensure_ascii=False)


    if test_obl_cueset is False:
        # full-den without MVC
        test = []
        for k in range(0, runs):


            for i in range(0, testcase_count):

                if if_sampling_ratio is False:
                    np.random.seed(42+k)
                    # without replacement sample for tid's
                    if is_monotonic:
                        tid = np.random.choice(range(1, (i+1) * binning_tuples), no_sen_policies, replace=False)
                    else:
                        tid = np.random.choice(range(1, (i+1) * binning_tuples), no_sen_policies, replace=True)

                    np.random.seed(42+k)
                    attributes_sample = np.random.choice(curPolicyArray, no_sen_policies, replace=True)
                else:
                    np.random.seed(42+k)
                    # without replacement sample for tid's
                    if is_monotonic:
                        tid = np.random.choice(range(1, (i+1) * binning_tuples), (i+1) * no_sen_policies, replace=False)
                    else:
                        tid = np.random.choice(range(1, (i+1) * binning_tuples), (i+1) * no_sen_policies, replace=True)

                    np.random.seed(42+k)
                    attributes_sample = np.random.choice(curPolicyArray, (i+1) * no_sen_policies, replace=True)


                testcase = {}
                testcase['expID'] = k
                testcase['userID'] = "38400000-8cf0-11bd-b23e-10b96e4ef00d"
                testcase['userName'] = "Samus"
                testcase['databaseName'] = database_name
                testcase['relationName'] = relation_name
                testcase['tuple_start'] = tuple_start
                testcase['tuple_end'] = (i+1) * binning_tuples
                testcase['isPagination'] = is_pagination
                testcase['binning_size'] = (i+1)
                testcase['merging_size'] = merging_size
                testcase['purpose'] = "analytics"
                testcase['DCFileName'] = DCFileName
                testcase['algo'] = "full-den"

                testcase['k_value'] = 0

                testcase['limit'] = (i+1) * binning_tuples
                testcase['isAscend'] = True
                testcase['policySenLevel'] = policySenLevel

                policies = []

                no_policies = (i+1) * no_sen_policies if if_sampling_ratio is True else no_sen_policies

                for j in range(0, no_policies):
                
                    policy = {}
                    policy['databaseName'] = database_name
                    policy['relationName'] = relation_name
                    
                    policy['tupleID'] = int(tid[j])
                    policy['attributeName'] = attributes_sample[j]

                    policies.append(policy)


                randomFlag = {}
                randomFlag['seed'] = 42 + k # TODO: involving some randomness
                randomFlag['randomCuesetChoosing'] = True
                randomFlag['randomHiddenCellChoosing'] = True

                testcase['policies'] = policies
                testcase['randomFlag'] = randomFlag
                testcase['testname'] = test_name + "_full_noMVC"
                testcase['useMVC'] = False
                testcase['testFanOut'] = testfanout
                testcase['testOblCueset'] = test_obl_cueset
                test.append(testcase)

        with open('../testdata/testcases/testcases_full_noMVC_'+ policySenLevel + "_" + relation_name + "_obl_" + str(test_obl_cueset) + "_ratio_" + str(if_sampling_ratio) +'.json', 'w') as f:
                json.dump(test, f, ensure_ascii=False)


if __name__ == "__main__":

    curPolicyArray = high
    policySenLevel = "high"

    # if set as True, monotonically selecting policies in different experiments
    is_monotonic = True

    database_name = "hospitaldb"
    relation_name = "hospital"
    tuple_start = 0
    tuple_end = 100000

    is_pagination = True
    binning_tuples = 10000
    merging_size = 5

    testcase_count = 10  # no. of testcases in each test
    no_sen_policies = 30  # no. of sensitive cells in testcases
    if_sampling_ratio = False # enabling the ratio sampling, i.e., sample no_sen_policies policies per bins

    limit = tuple_end # no. of tuples
    runs = 4   # no. of runs

    testfanout = True

    test_obl_cueset = False

    test_name_base="server_test_hospital_scalability"

    testcase_gen(curPolicyArray, policySenLevel, testcase_count, limit, runs, database_name, relation_name, test_name_base, is_monotonic, no_sen_policies, testfanout, test_obl_cueset, tuple_start, tuple_end, is_pagination, binning_tuples, merging_size, if_sampling_ratio)