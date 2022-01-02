package edu.policy.manager;

import edu.policy.helper.Utils;
import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;
import edu.policy.model.constraint.ConstraintType;
import edu.policy.model.constraint.Provenance;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/***
 * PBD Parser:
 * parse a provenance based dependency (= function based dependency)
 * based on PBD input strings specified in the constraint file.
 *
 * A PBD string looks like
 * "PBD&taxes&IN(Salary,Rate)&Out(Tax)"
 * meaning the attribute tax is based on salary and rate.
 *
 * This class will return a provenance object to represent this PBD:
 * Provenance: Tax = fn(Salary, Rate)
 */

public class PBDParser {

    Session session;

    private static final Logger logger = LogManager.getLogger(DCParser.class);

    Hashtable<String, Integer> attrDomSizeDict;

    Hashtable<String, Double> numericalAttrDomMin;

    Hashtable<String, Double> numericalAttrDomMax;

    List<String> schema;
    List<String> schemaType;


    public PBDParser(Session session) {
        this.session = session;
        this.attrDomSizeDict = session.getCategoricalAttrDomSizeDict();
        this.numericalAttrDomMax = session.getNumericalAttrDomMax();
        this.numericalAttrDomMin = session.getNumericalAttrDomMin();

        this.schema = session.getSchema();
        this.schemaType = session.getSchemaType();
    }

    public Provenance parsePBD (String[] pbdSplits) throws Exception {

        Provenance pbd = new Provenance();

        // get dependency type
        ConstraintType dependencyType = ConstraintType.valueOf(pbdSplits[0]);
        pbd.setDependencyType(dependencyType);

        // get relation name
        String relationName = pbdSplits[1];
        pbd.setRelationName(relationName);

        // parse predicates
        for (int i=2; i < pbdSplits.length; i++) {
            System.out.println(pbdSplits[i]);

            OperationType operationTypeOfPart = Utils.containsOperation(pbdSplits[i]); // get operation of the predicate
            logger.debug(String.format("pbdSplits[i]: %s, typeOfPart: %s", pbdSplits[i], operationTypeOfPart));

            if (!Objects.isNull(operationTypeOfPart)) {
                String predicateString = pbdSplits[i];

                boolean hasConstant = predicateString.contains("'");

                String[] predSplits = predicateString.split(",|[(]|[)]");

                System.out.println(Arrays.toString(predSplits));

                if (operationTypeOfPart.equals(OperationType.IN)) {
                    for (int j=1; j < predSplits.length; j++) {

                        String attributeName = predSplits[j];

                        if (!session.getSchema().contains(attributeName))
                            throw new Exception(String.format("Attribute %s is not in the retrieved schema %s", attributeName, session.getSchema()));

                        pbd.addInputAttr(attributeName);

                        int attributeIndexOf = schema.indexOf(attributeName);
                        String attributeType = schemaType.get(attributeIndexOf);
                        pbd.addInputAttrType(AttributeType.contains(attributeType));
                    }
                }
                else if (operationTypeOfPart.equals(OperationType.OUT)) {
                    String attributeName = predSplits[1];

                    if (!session.getSchema().contains(attributeName))
                        throw new Exception(String.format("Attribute %s is not in the retrieved schema %s", attributeName, session.getSchema()));

                    pbd.setOutputAttr(attributeName);

                    int attributeIndexOf = schema.indexOf(attributeName);
                    String attributeType = schemaType.get(attributeIndexOf);
                    pbd.setOutputAttrType(AttributeType.contains(attributeType));
                }

            }
            else {
                throw new Exception(String.format("Operation not exists in the predicate: %s", pbdSplits[i]));
            }
        }

        return pbd;
    }

}
