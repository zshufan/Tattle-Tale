package edu.policy.model.data;

import edu.policy.model.AttributeType;

import java.util.Hashtable;
import java.util.List;

/**
 * Meta data of a database table.
 */
public class MetaData {

    List<String> schema;

    List<String> schemaType;

    Hashtable<String, AttributeType> schemaTypeDict;

    Hashtable<String, Integer> categoricalAttrDomSizeDict;

    Hashtable<String, Double> numericalAttrDomMin;

    Hashtable<String, Double> numericalAttrDomMax;

    public MetaData(List<String> schema, List<String> schemaType, Hashtable<String, AttributeType> schemaTypeDict,
                    Hashtable<String, Integer> categoricalAttrDomSizeDict, Hashtable<String, Double> numericalAttrDomMin,
                    Hashtable<String, Double> numericalAttrDomMax) {
        this.schema = schema;
        this.schemaType = schemaType;
        this.schemaTypeDict = schemaTypeDict;
        this.categoricalAttrDomSizeDict = categoricalAttrDomSizeDict;
        this.numericalAttrDomMin = numericalAttrDomMin;
        this.numericalAttrDomMax = numericalAttrDomMax;
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

    public Hashtable<String, Integer> getCategoricalAttrDomSizeDict() {
        return categoricalAttrDomSizeDict;
    }

    public Hashtable<String, Double> getNumericalAttrDomMin() {
        return numericalAttrDomMin;
    }

    public Hashtable<String, Double> getNumericalAttrDomMax() {
        return numericalAttrDomMax;
    }
}
