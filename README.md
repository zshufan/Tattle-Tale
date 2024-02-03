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
<font color="red">

**2024 Update**  
**You'll need to change line 77 of `src/main/java/edu/policy/dbms/MySQLConnectionManagerDBCP.java` from**  
```
dataSource.setUrl(String.format("jdbc:mysql://%s:%s/mysql", SERVER, PORT));
```
**to**  
```
dataSource.setUrl(String.format("jdbc:mysql://%s:%s/<YOURDATABASENAME>", SERVER, PORT)); 
```
**Alternatively you could create two databases named hospitaldb and taxdb to store the hospital and taxes tables respectively.**
</font>
2.  Update corresponding database info (*username, password, server and port number*) in the `mysql.properties` file under `resources/credentials/` directory.
<font color="red">

**2024 Update**  
**In addition you need to change the user privileges for the default account named in the mysql.properties file to have all privileges on the database value you set in step 1.**  
</font>

#### Step 2: Prepare Testcases

Use the test script **testcase_gen_tax.py** or **testcase_gen_hospital.py** to generate testcases on Tax or Hospital dataset. The generated testcases will be automatically placed under `testdata/testcases/` directory. 
<font color="red">

**2024 Update**  
**Be sure to run the testscripts from the testscript folder in the GitHub repo.  You will also need to change the python testscript to set the correct database for the java classes to connect to.**  
**If you're using**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**testcase_gen_tax.py**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**change line 13 from**
```
DCFileName = "/testdata/taxdb_constraints.txt"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
DCFileName = "/testdata/taxdb_constraints_noPBD.txt"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**and line 278 from**  
```
database_name = "taxdb"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
database_name = "<YOURDATABASENAME>"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**testcase_gen_tax_btm.py**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**change line 235 from**
```
database_name = "taxdb"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
database_name = "<YOURDATABASENAME>"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**testcase_gen_hospital.py**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**change line 280 from**
```
database_name = "hospitaldb"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
database_name = "<YOURDATABASENAME>"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**Additionally you need to create a table called hospital10k in your given database.  This table can be a duplicate of your hospital table but with only 10,000 of the hospital table records.  This will work if you `DELETE FROM hospital10k WHERE tid > 10000`.  I have not tested if any combination of 10,000 hospital table records are also compatible.  In addition you must set up the jvm environment to have sufficient space on the jvm and the heap.  You will need to allocate over 10 GB (exact size not confirmed) of heap space to complete this test.**  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**testcase_gen_hospital_btm.py**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**change line 236 from**
```
database_name = "hospitaldb"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
database_name = "<YOURDATABASENAME>"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**testcase_gen_hospital_scalability.py**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**change line 192 from**
```
database_name = "hospitaldb"
```
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**to**  
```
database_name = "<YOURDATABASENAME>"
```

</font>

> python testcase_gen_tax.py
>
> python testcase_gen_hospital.py

For the scalability experiment (described in the extended version of our paper), we enable the binning-then-merging (*btm*) mode. Testcases can be generated using script **testcase_gen_hospital_scalability.py**.
Our codebase is forward-compatible with test cases without btm mode.

> python testcase_gen_hospital_scalability.py

#### Step 3: Execution Commands

Under the working directory (`Tattle-Tale/`), use the following commands to install required dependencies and execute the program.

**Experimental setting**: requiring at least 64 GB RAM [if not possible for limited computing environment, use *btm* mode (as in the scalability experiment) to reduce the memory requirement]
<font color="red">

**2024 Update**  
**You'll need to change line 25 of `src/main/java/edu/policy/execution/Experiment.java` from**  
```
private static final File testCaseDir = new File(System.getProperty("user.dir") + "/testdata/testcases");
```
**to**  
```
private static final File testCaseDir = new File(<ExactPathNameToTestCases>);
```
</font>

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

