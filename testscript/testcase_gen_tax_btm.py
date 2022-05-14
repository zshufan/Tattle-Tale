import json
import random
import numpy as np

high = ['State', 'Rate', 'Salary'] 
med = ['AreaCode', 'HasChild', 'SingleExemp', 'Zip']
low = ['MaritalStatus', 'ChildExemp', 'City']


algo_list = ['full-den', 'k-den']

DCFileName = "/testdata/taxdb_constraints_noPBD.txt" # path to constraints file


def testcase_gen(curPolicyArray, policySenLevel, testcase_count, limit, runs, database_name, relation_name, test_name, is_monotonic, step, start, testfanout, test_obl_cueset, tuple_start, tuple_end, is_pagination, binning_size, merging_size):


    # full-den with MVC
    test = []

    for k in range(0, runs):

        np.random.seed(42+k)
        # without replacement sample for tid's
        if is_monotonic:
            tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=False)
        else:
            tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=True)

        np.random.seed(42+k)
        attributes_sample = np.random.choice(curPolicyArray, start+step*(testcase_count), replace=True)

        for i in range(0, testcase_count):
            testcase = {}
            testcase['expID'] = k
            testcase['userID'] = "38400000-8cf0-11bd-b23e-10b96e4ef00d"
            testcase['userName'] = "Link"
            testcase['databaseName'] = database_name
            testcase['relationName'] = relation_name
            testcase['tuple_start'] = tuple_start
            testcase['tuple_end'] = tuple_end
            testcase['isPagination'] = is_pagination
            testcase['binning_size'] = binning_size
            testcase['merging_size'] = merging_size
            testcase['purpose'] = "analytics"
            testcase['DCFileName'] = DCFileName
            testcase['algo'] = "full-den"

            testcase['k_value'] = 0

            testcase['limit'] = limit
            testcase['isAscend'] = True
            testcase['policySenLevel'] = policySenLevel

            policies = []

            for j in range(0, start+step*(i)):
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

    with open('../testdata/testcases/testcases_full_MVC_'+ policySenLevel + "_" + relation_name + "_obl_" + str(test_obl_cueset) +'.json', 'w') as f:
        json.dump(test, f, ensure_ascii=False)

    if test_obl_cueset is False:
        # full-den without MVC
        test = []
        for k in range(0, runs):

            np.random.seed(42+k)
            # without replacement sample for tid's
            if is_monotonic:
                tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=False)
            else:
                tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=True)

            np.random.seed(42+k)
            attributes_sample = np.random.choice(curPolicyArray, start+step*(testcase_count), replace=True)

            for i in range(0, testcase_count):
                testcase = {}
                testcase['expID'] = k
                testcase['userID'] = "38400000-8cf0-11bd-b23e-10b96e4ef00d"
                testcase['userName'] = "Link"
                testcase['databaseName'] = database_name
                testcase['relationName'] = relation_name
                testcase['tuple_start'] = tuple_start
                testcase['tuple_end'] = tuple_end
                testcase['isPagination'] = is_pagination
                testcase['binning_size'] = binning_size
                testcase['merging_size'] = merging_size
                testcase['purpose'] = "analytics"
                testcase['DCFileName'] = DCFileName
                testcase['algo'] = "full-den"

                testcase['k_value'] = 0

                testcase['limit'] = limit
                testcase['isAscend'] = True
                testcase['policySenLevel'] = policySenLevel

                policies = []

                for j in range(0, start+step*(i)):
                
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

        with open('../testdata/testcases/testcases_full_noMVC_'+ policySenLevel + "_" + relation_name + "_obl_" + str(test_obl_cueset) +'.json', 'w') as f:
                json.dump(test, f, ensure_ascii=False)


    if test_obl_cueset is False:
        # k-den with MVC
        k_values = [min((0.1 + 4 * round(i/10, 1)), 1) for i in range(3)]
        print(k_values)

        for t in range(0, len(k_values)):

            test = []

            for k in range(0, runs):

                np.random.seed(42+k)
                # without replacement sample for tid's
                if is_monotonic:
                    tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=False)
                else:
                    tid = np.random.choice(range(tuple_start+1, limit), start+step*(testcase_count), replace=True)

                np.random.seed(42+k)
                attributes_sample = np.random.choice(curPolicyArray, start+step*(testcase_count), replace=True)

                for i in range(0, testcase_count):
                    testcase = {}
                    testcase['expID'] = k
                    testcase['userID'] = "38400000-8cf0-11bd-b23e-10b96e4ef00d"
                    testcase['userName'] = "Link"
                    testcase['databaseName'] = database_name
                    testcase['relationName'] = relation_name
                    testcase['tuple_start'] = tuple_start
                    testcase['tuple_end'] = tuple_end
                    testcase['isPagination'] = is_pagination
                    testcase['binning_size'] = binning_size
                    testcase['merging_size'] = merging_size
                    testcase['purpose'] = "analytics"
                    testcase['DCFileName'] = DCFileName
                    testcase['algo'] = "k-den"

                    testcase['k_value'] = k_values[t]
                    print(testcase['k_value'])

                    testcase['limit'] = limit
                    testcase['isAscend'] = True
                    testcase['policySenLevel'] = policySenLevel

                    policies = []

                    for j in range(0, start+step*(i)):
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
                    testcase['testname'] = test_name + "_k_MVC_" + str(k_values[t]).replace('.', '_')
                    testcase['useMVC'] = True
                    testcase['testOblCueset'] = test_obl_cueset
                    test.append(testcase)

            with open('../testdata/testcases/testcases_k'+ str(k_values[t]).replace('.', '_')+'_MVC_'+ policySenLevel + "_" + relation_name +'.json', 'w') as f:
                json.dump(test, f, ensure_ascii=False)



if __name__ == "__main__":

    curPolicyArray = high
    policySenLevel = "high"

    # if set as True, monotonically selecting policies in different experiments
    is_monotonic = True


    database_name = "taxdb"
    relation_name = "taxes"
    tuple_start = 0
    tuple_end = 9998

    is_pagination = True
    binning_size = 10
    merging_size = 5

    testcase_count = 1  # no. of testcases in each test
    start = 100 # no. of sensitive cells in the starting testcase
    step = 10  # no. of sensitive cells growing in testcases
    limit = tuple_end # no. of tuples
    runs = 1   # no. of runs

    test_name_base="server_test_taxdb"

    testfanout = True

    test_obl_cueset = False

    testcase_gen(curPolicyArray, policySenLevel, testcase_count, limit, runs, database_name, relation_name, test_name_base, is_monotonic, step, start, testfanout, test_obl_cueset, tuple_start, tuple_end, is_pagination, binning_size, merging_size)

