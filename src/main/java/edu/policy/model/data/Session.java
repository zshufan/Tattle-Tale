package edu.policy.model.data;

import edu.policy.helper.Utils;
import edu.policy.model.AttributeType;
import edu.policy.model.constraint.Cell;
import edu.policy.model.constraint.DataDependency;
import edu.policy.model.constraint.Provenance;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class Session {

    int expID;

    User user;

    DatabaseSetting databaseSetting;

    ExpSetting expSetting;

    RandomFlag randomFlag;

    MetaData metaData;

    Boolean useMVC;

    Boolean testFanOut;

    Boolean testOblCueset;

    String policySenLevel;

    List<DataDependency> dcs = new ArrayList<>();

    List<Provenance> pbds = new ArrayList<>();

    public Session(int expID, User user, String databaseName, String relationName, MetaData metaData,
                   int limit, boolean isAscend, String algo, float k_value, String DCDir, List<Cell> policies,
                   long seed, Boolean randomCuesetChoosing, Boolean randomHiddenCellChoosing, Boolean useMVC,
                   Boolean testFanOut, Boolean testOblCueset, String policySenLevel) {
        this.expID = expID;
        this.user = user;
        this.metaData = metaData;
        this.databaseSetting = new DatabaseSetting(databaseName, relationName, metaData, limit, isAscend);
        this.expSetting = new ExpSetting(algo, k_value, policies, DCDir);
        this.randomFlag = new RandomFlag(seed, randomCuesetChoosing, randomHiddenCellChoosing);
        this.useMVC = useMVC;
        this.testFanOut = testFanOut;
        this.testOblCueset = testOblCueset;
        this.policySenLevel = policySenLevel;
    }

    public int getExpID() {
        return expID;
    }

    public User getUser() {
        return user;
    }

    public String getPolicySenLevel() {
        return policySenLevel;
    }

    public String getDatabaseName() {
        return databaseSetting.getDatabaseName();
    }

    public String getRelationName() {
        return databaseSetting.getRelationName();
    }

    public List<String> getSchema() {
        return databaseSetting.getSchema();
    }

    public List<String> getSchemaType() {
        return databaseSetting.getSchemaType();
    }

    public Hashtable<String, AttributeType> getSchemaTypeDict() {
        return databaseSetting.getSchemaTypeDict();
    }

    public Hashtable<String, Integer> getAttrDomSizeDict() {
        return databaseSetting.getAttrDomSizeDict();
    }

    public String getAlgo() {
        return expSetting.getAlgo();
    }

    public float getK_value() {
        return expSetting.getK_value();
    }

    public List<Cell> getPolicies() {
        return expSetting.getPolicies();
    }

    public String getDCDir() {
        return expSetting.getDCDir();
    }

    public int getLimit() {
        return databaseSetting.getLimit();
    }

    public Boolean getAscend() {
        return databaseSetting.getAscend();
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

    public Boolean getUseMVC() {
        return useMVC;
    }

    public Boolean getTestFanOut() {
        return testFanOut;
    }

    public Boolean getTestOblCueset() {
        return testOblCueset;
    }

    public Hashtable<String, Integer> getCategoricalAttrDomSizeDict() {
        return metaData.getCategoricalAttrDomSizeDict();
    }

    public Hashtable<String, Double> getNumericalAttrDomMin() {
        return metaData.getNumericalAttrDomMin();
    }

    public Hashtable<String, Double> getNumericalAttrDomMax() {
        return metaData.getNumericalAttrDomMax();
    }

    public void setDcs(List<DataDependency> dcs) {
        this.dcs = dcs;
    }

    public void setPbds(List<Provenance> pbds) {
        this.pbds = pbds;
    }

    public List<DataDependency> getDcs() {
        return dcs;
    }

    public List<Provenance> getPbds() {
        return pbds;
    }

    @Override
    public String toString() {
        return "Session{" +
                "user=" + user +
                ", databaseSetting=" + databaseSetting +
                ", expSetting=" + expSetting +
                '}';
    }
}

class DatabaseSetting {

    String databaseName;

    String relationName;

    List<String> schema;

    List<String> schemaType;

    Hashtable<String, AttributeType> schemaTypeDict;

    Hashtable<String, Integer> attrDomSizeDict;

    Hashtable<String, Double> numericalAttrDomMin;

    Hashtable<String, Double> numericalAttrDomMax;

    int limit;

    Boolean isAscend;

    public DatabaseSetting(String databaseName, String relationName, MetaData metaData, int limit, boolean isAscend) {
        this.databaseName = databaseName;
        this.relationName = relationName;
        this.schema = metaData.getSchema();
        this.schemaType = metaData.getSchemaType();
        this.attrDomSizeDict = metaData.getCategoricalAttrDomSizeDict();
        this.schemaTypeDict = metaData.getSchemaTypeDict();
        this.numericalAttrDomMax = metaData.getNumericalAttrDomMax();
        this.numericalAttrDomMin = metaData.getNumericalAttrDomMin();
        this.limit = limit;
        this.isAscend = isAscend;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getRelationName() {
        return relationName;
    }

    public List<String> getSchema() {
        return schema;
    }

    public List<String> getSchemaType() {
        return schemaType;
    }

    public Hashtable<String, AttributeType> getSchemaTypeDict() {
        return schemaTypeDict;
    }

    public Hashtable<String, Integer> getAttrDomSizeDict() {
        return attrDomSizeDict;
    }

    public int getLimit() {
        return limit;
    }

    public Boolean getAscend() {
        return isAscend;
    }

    @Override
    public String toString() {
        return "DatabaseSetting{" +
                "databaseName='" + databaseName + '\'' +
                ", relationName='" + relationName + '\'' +
                ", schema=" + schema +
                ", schemaType=" + schemaType +
                ", attrDomSizeDict=" + attrDomSizeDict +
                '}';
    }
}

class ExpSetting {

    String algo = "k-den";

    float k_value;

    List<Cell> policies;

    String DCDir;

    public ExpSetting(String algo, float k_value, List<Cell> policies, String DCDir) {
        this.algo = algo;
        this.k_value = k_value;
        this.policies = policies;
        this.DCDir = DCDir;
    }

    public String getAlgo() {
        return algo;
    }

    public float getK_value() {
        return k_value;
    }

    public List<Cell> getPolicies() {
        return policies;
    }

    public String getDCDir() {
        return DCDir;
    }

    @Override
    public String toString() {
        return "ExpSetting{" +
                "algo='" + algo + '\'' +
                ", k_value=" + k_value +
                ", policies=" + policies +
                '}';
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
