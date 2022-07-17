# Tattle-Tale
Main repository for "Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data" (VLDB 2022)

## Brief Intro

Tattle-tale is the project of studying the problem of answering queries when (part of) the data may be sensitive and should not be leaked to the querier.
We build efficient algorithms to resist leakage (based on our *full-deniability* security model) that suppress minimal number of non-sensitivie cells during query processing.
This open-source repository contains the system/algorithms we develop for the Tattle-tale project, while it also consists of our code for preparing test cases and plotting our research findings.


## Repository Structure

    ├── Tattle-Tale/
    │   ├── src/main/                        *Main directory
    |       ├── java/                        *Java source code
    |       └── resources/                   *Database configuration directory
    │   ├── plot                             *Plotting code
    |       └── /eva/                        *Directory to store output report
    │   ├── testdata/                        *Directory to place test and constraint files
    |       ├── testcases/
    |       └── data constraint files
    |   ├── testscript/                      *Tools and scripts for testing
    |       ├── holoclean_test_script.py     *For generating Holoclean test script
    |       ├── imputation_algorithms.py     *Weighted sampling
    |       ├── testcase_gen_tax.py          *For generating testcases on Tax
    |       └── testcase_gen_hospital.py     *For generating testcases on Hospital
    |   └── pom.xml                          *Project dependency

## Code Guide

#### Step 1: Configure Database

1.  Create database in MySQL ([Export files](https://drive.google.com/drive/folders/1CiCXU08zWgzI2VUKp1vEcadBkTJA6Lbb?usp=sharing), enabling database index and hasing large domain can optimize performance)
2.  Update corresponding database info (*username, password, server and port number*) in the `mysql.properties` file under `resources/credentials/` directory.

#### Step 2: Prepare Testcases

Use the test script **testcase_gen_tax.py** or **testcase_gen_hospital.py** to generate testcases on Tax or Hospital dataset. The generated testcases will be automatically placed under `testdata/testcases/` directory.

> python testcase_gen_tax.py
>
> python testcase_gen_hospital.py

For the scalability experiment (described in the extended version of our paper), we enable the binning-then-merging (*btm*) mode. Testcases can be generated using script **testcase_gen_hospital_scalability.py**.
Our codebase is forward-compatible with test cases without btm mode.

> python testcase_gen_hospital_scalability.py

#### Step 3: Execution Commands

Under the working directory (`Tattle-Tale/`), use the following commands to install required dependencies and execute the program.

**Experimental setting**: requiring at least 64 GB RAM [if not possible for limited computing environment, use *btm* mode (as in the scalability experiment) to reduce the momery requirement]

> mvn clean install
>
> mvn exec:java 

After execution, the output experiment reports can be found under the `plot/eva/` directory.



**Special Note: This project uses log4j version 2.17. Please check the latest update on [log4j official website](https://logging.apache.org/log4j/2.x/security.html#CVE-2021-44832) before running to avoid potential vulnerabilities.**

## How to Cite: 

> ```
> @inproceedings{Pappachan2022tattletale,
>   author={Pappachan, Primal and Zhang, Shufan and He, Xi and Mehrotra, Sharad},
>   title={Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data}, 
>   journal={Proceedings of the VLDB Endowment},
>   volume={15},
>   number={11},
>   year={2022},
>   publisher={VLDB Endowment}
>}
> ```

## Correspondence

[Primal Pappachan](mailto:primal@uci.edu) <br>[Shufan Zhang](mailto:shufan.zhang@uwaterloo.ca) <br>[Xi He](mailto:xihe@uwaterloo.ca)  <br>[Sharad Mehrotra](mailto:sharad@ics.uci.edu) <br>

## License

[BSD-3-Clause License](https://choosealicense.com/licenses/bsd-3-clause/)

