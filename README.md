[![arXiv](https://img.shields.io/badge/arXiv-1234.56789-b31b1b.svg)](https://arxiv.org/abs/2207.08757)  [![License](https://img.shields.io/badge/License-BSD_3--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause) [![conference](https://img.shields.io/badge/VLDB--2022-Accepted-success)](https://vldb.org/2022/)

# Tattle-Tale 

<p align="center">
<img src="https://user-images.githubusercontent.com/284107/179824129-4b2a35d9-7dcc-4945-b1bf-5eed595ec23f.png" width=100 height=100>
</p>

Main repository for "[Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data](https://arxiv.org/abs/2207.08757)" (VLDB 2022) [[bibtex](#citation)]


## Brief Intro

Tattle-tale project studies the problem of preventing inferences through data dependencies on sensitive data.
This repository contains the implementation of algorithms for detection and prevention of such inferences along with scripts for preparing test cases and plotting the results.

![arch](https://user-images.githubusercontent.com/284107/179818558-ce0d4cca-6db3-48fa-9d45-207e872051e9.png)

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

1.  Create database in MySQL ([Export files](https://drive.google.com/drive/folders/1CiCXU08zWgzI2VUKp1vEcadBkTJA6Lbb?usp=sharing), enabling database index and creating hash indexes can improve performance)
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

**Experimental setting**: requiring at least 64 GB RAM [if not possible for limited computing environment, use *btm* mode (as in the scalability experiment) to reduce the memory requirement]

> mvn clean install
>
> mvn exec:java 

After execution, the output experiment reports can be found under the `plot/eva/` directory.



**Special Note: This project uses log4j version 2.17. Please check the latest update on [log4j official website](https://logging.apache.org/log4j/2.x/security.html#CVE-2021-44832) before running to avoid potential vulnerabilities.**

## Citation: 

> ```
> @inproceedings{Pappachan2022tattletale,
>   author={Pappachan, Primal and Zhang, Shufan and He, Xi and Mehrotra, Sharad},
>   title={Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data}, 
>   journal={Proceedings of the VLDB Endowment},
>   pages={2437--2449},
>   volume={15},
>   number={11},
>   year={2022},
>   doi={10.14778/3551793.3551805},
>   publisher={VLDB Endowment}
>}
> ```

## Correspondence

[:mailbox_with_mail: Primal Pappachan](mailto:primal@uci.edu) [:scroll: Homepage](https://primalpappachan.com/) <br>
[:mailbox_with_mail: Shufan Zhang](mailto:shufan.zhang@uwaterloo.ca) [:scroll: Homepage](https://cs.uwaterloo.ca/~s693zhan/) <br>
[:mailbox_with_mail: Xi He](mailto:xihe@uwaterloo.ca) [:scroll: Homepage](https://cs.uwaterloo.ca/~xihe/) <br>
[:mailbox_with_mail: Sharad Mehrotra](mailto:sharad@ics.uci.edu) [:scroll: Homepage](https://www.ics.uci.edu/~sharad/) <br>

## License

[BSD-3-Clause License](https://choosealicense.com/licenses/bsd-3-clause/)

