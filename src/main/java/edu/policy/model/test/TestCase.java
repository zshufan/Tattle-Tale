package edu.policy.model.test;

import edu.policy.helper.Utils;
import edu.policy.model.AttributeType;
import edu.policy.model.constraint.Cell;
import edu.policy.model.data.MetaData;

import java.util.Hashtable;
import java.util.List;

public class TestCase {

    int expID;

    String userID;

    String userName;

    String databaseName;

    String relationName;

    List<Cell> policies;

    String policySenLevel;

    String DCFileName;

    String purpose;

    String algo;

    String testname;

    float k_value;

    RandomFlag randomFlag;

    int limit;
    Boolean isAscend;

    boolean useMVC;

    boolean testFanOut;

    boolean testOblCueset;

    public TestCase(int expID, String userID, String userName, String databaseName, String relationName, List<Cell> policies,
                    String DCFileName, float k_value, long seed, Boolean randomCuesetChoosing,
                    Boolean randomHiddenCellChoosing, String purpose, int limit, boolean isAscend, String policySenLevel,
                    String algo, String testname, boolean useMVC, boolean testFanOut, boolean testOblCueset) {
        this.expID = expID;
        this.userID = userID;
        this.userName = userName;
        this.databaseName = databaseName;
        this.relationName = relationName;
        this.policies = policies;
        this.DCFileName = DCFileName;
        this.k_value = k_value;
        this.purpose = purpose;
        new RandomFlag(seed, randomCuesetChoosing, randomHiddenCellChoosing);
        this.limit = limit;
        this.isAscend = isAscend;
        this.policySenLevel = policySenLevel;
        this.algo = algo;
        this.testname = testname;
        this.useMVC = useMVC;
        this.testFanOut = testFanOut;
        this.testOblCueset = testOblCueset;
    }

    void associateCellTypeToPolicies(List<String> schema, List<String> schemaType) {
        Hashtable<String, AttributeType> schemaTypeDict = Utils.createSchemaTypeDict(schema, schemaType);

        for (Cell policy: policies) {
            policy.setCellType(schemaTypeDict.get(policy.getAttributeName()));
        }

    }

    void associateDomSizeToPolicies(MetaData metaData) {

        Hashtable<String, Integer> categoricalAttrDomSizeDict = metaData.getCategoricalAttrDomSizeDict();

        Hashtable<String, Double> numericalAttrDomMin = metaData.getNumericalAttrDomMin();

        Hashtable<String, Double> numericalAttrDomMax = metaData.getNumericalAttrDomMax();


        for (Cell policy: policies) {

            String attr = policy.getAttributeName();

            switch (policy.getCellType()) {
                case STRING:
                    policy.setAttrDomSize(categoricalAttrDomSizeDict.get(attr));
                    break;
                case INTEGER:
                case DOUBLE:
                    policy.setMinMaxDom(numericalAttrDomMin.get(attr), numericalAttrDomMax.get(attr));
                    break;
            }

        }
    }


    @Override
    public String toString() {
        return "TestCase{" +
                "userID=" + userID +
                ", userName='" + userName + '\'' +
                ", databaseName='" + databaseName + '\'' +
                ", relationName='" + relationName + '\'' +
                ", policies=" + policies +
                ", DCFileName='" + DCFileName + '\'' +
                ", k_value=" + k_value +
                ", randomFlag=" + randomFlag +
                '}';
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return userName;
    }

    public String getRelationName() {
        return relationName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public List<Cell> getPolicies(MetaData metaData) {
        List<String> schema = metaData.getSchema();
        List<String> schemaType = metaData.getSchemaType();


        associateCellTypeToPolicies(schema, schemaType);
        associateDomSizeToPolicies(metaData);
        return policies;
    }

    public int getExpID() {
        return expID;
    }

    public String getAlgo() {
        return algo;
    }

    public List<Cell> getSenCells() {
        return policies;
    }

    public String getDCFileName() {
        return DCFileName;
    }

    public float getK_value() {
        return k_value;
    }

    public RandomFlag getRandomFlag() {
        return randomFlag;
    }

    public long getRandomSeed() {
        return  randomFlag.getSeed();
    }

    public Boolean getRandomCuesetChoosing() {
        return randomFlag.getRandomCuesetChoosing();
    }

    public Boolean getRandomHiddenCellChoosing() {
        return randomFlag.getRandomHiddenCellChoosing();
    }

    public int getLimit() {
        return limit;
    }

    public Boolean getAscend() {
        return isAscend;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getPolicySenLevel() {
        return policySenLevel;
    }

    public String getTestname() {
        return testname;
    }

    public Boolean getUseMVC() {
        return useMVC;
    }

    public boolean isTestFanOut() {
        return testFanOut;
    }

    public boolean isTestOblCueset() {
        return testOblCueset;
    }
}

class RandomFlag {

    long seed;

    Boolean randomCuesetChoosing;

    Boolean randomHiddenCellChoosing;

    public RandomFlag(long seed, Boolean randomCuesetChoosing, Boolean randomHiddenCellChoosing) {
        this.seed = seed;
        this.randomCuesetChoosing = randomCuesetChoosing;
        this.randomHiddenCellChoosing = randomHiddenCellChoosing;
    }

    public long getSeed() {
        return seed;
    }

    public Boolean getRandomCuesetChoosing() {
        return randomCuesetChoosing;
    }

    public Boolean getRandomHiddenCellChoosing() {
        return randomHiddenCellChoosing;
    }

    @Override
    public String toString() {
        return "RandomFlag{" +
                "seed=" + seed +
                ", randomCuesetChoosing=" + randomCuesetChoosing +
                ", randomHiddenCellChoosing=" + randomHiddenCellChoosing +
                '}';
    }
}