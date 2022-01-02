package edu.policy.model.data;

import java.util.*;

/**
 * The class for the data model
 */
public class Tuple {

    String databaseName;

    String relationName;

    int tupleID;

    Map<String, String> tuple = new HashMap<>();

    Map<String, String> tupleWithNull = new HashMap<>();

    String owner;

    public Tuple(String databaseName, String relationName, int tupleID) {
        this.databaseName = databaseName;
        this.relationName = relationName;
        this.tupleID = tupleID;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public int getTupleID() {
        return tupleID;
    }

    public void setTupleID(int tupleID) {
        this.tupleID = tupleID;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public Map<String, String> getTuple() {
        return tuple;
    }

    public void setTuple(Map<String, String> tuple) {
        this.tuple = tuple;
    }

    public void addToTuple(String key, String value) {
        this.tuple.put(key, value);
        this.tupleWithNull.put(key, value);
    }

    public void substituteValInTuple(String key, String newVal) {
        this.tupleWithNull.replace(key, newVal);
    }

    public Map<String, String> getTupleWithNull() {
        return tupleWithNull;
    }

    public void setTupleWithNull(Map<String, String> tupleWithNull) {
        this.tupleWithNull = tupleWithNull;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    // from https://stackoverflow.com/questions/22766888/find-out-if-an-arraylist-contains-more-than-one-of-an-element
    public boolean isMoreThanOneNull() {
        return Collections.frequency(tuple.values(), "null") > 1;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "tupleID=" + tupleID +
                ", tupleWithNull=" + tupleWithNull +
                '}';
    }
}
