# Tattle-Tale
Main repository for "Don’t Be a Tattle-Tale: Preventing Leakages through Data Dependencies on Access Control Protected Data" (in submission to VLDB 2022)

## Repository Structure

    ├── Tattle-Tale/
    │   ├── src/main/                        *Main directary
    |       ├── java/                        *Java source code
    |       └── resources/                   *Database configuration directory
    │   ├── plot/eva/                        *Directory to store output report
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

1.  Create database in MySQL
2.  Update corresponding database info (*username, password, server and port number*) in the `mysql.properties` file under `resources/credentials/` directory.

#### Step 2: Prepare Testcases

Use the test script **testcase_gen_tax.py** or **testcase_gen_hospital.py** to generate testcases on Tax or Hospital dataset. The generated testcases will be automatically placed under `testdata/testcases/` directory.

#### Step 3: Execution Commands

Under the working directory (`Tattle-Tale/`), use the following commands to install required dependencies and execute the program.

> mvn clean install
>
> mvn exec:java 

After execution, the output experiment reports can be found under the `plot/eva/` directory.



**Special Note: This project uses log4j version 2.17. Please check the latest update on log4j official website before running to avoid potential vulnerabilities.**

## Correspondence

[Primal Pappachan](primal@uci.edu) <br>[Shufan Zhang](mailto:shufan.zhang@uwaterloo.ca) <br>[Xi He](mailto:xihe@uwaterloo.ca )  <br>[Sharad Mehrotra](mailto:sharad@ics.uci.edu ) <br>

## License

[BSD-3-Clause License][https://choosealicense.com/licenses/bsd-3-clause/]

