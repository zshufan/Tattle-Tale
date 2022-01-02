package edu.policy.model.data;

import java.util.*;
import java.util.stream.Collectors;

public class Table {

    String relationName;

    List<Tuple> table = new ArrayList<>();

    List<String> schema;

    List<String> schemaType;

    Hashtable<String, Integer> attrDomSizeDict;

    public Table() {

    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public List<Tuple> getTable() {
        return table;
    }

    public void setTable(List<Tuple> table) {
        this.table = table;
    }

    public List<String> getSchema() {
        return schema;
    }

    public void setSchema(List<String> schema) {
        this.schema = schema;
    }

    public List<String> getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(List<String> schemaType) {
        this.schemaType = schemaType;
    }

    public Hashtable<String, Integer> getAttrDomSizeDict() {
        return attrDomSizeDict;
    }

    public void setAttrDomSizeDict(Hashtable<String, Integer> attrDomSizeDict) {
        this.attrDomSizeDict = attrDomSizeDict;
    }

    public void addToTable (Tuple tuple) {
        table.add(tuple);
    }

    public Map<String, String> getTuple (int tupleID) {
        return Objects.requireNonNull(table.stream().filter(t -> t.getTupleID() == tupleID).findFirst().orElse(null)).getTuple();
    }
}
