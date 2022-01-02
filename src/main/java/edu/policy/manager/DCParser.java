package edu.policy.manager;

import edu.policy.helper.Utils;
import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;
import edu.policy.model.constraint.Cell;
import edu.policy.model.constraint.ConstraintType;
import edu.policy.model.constraint.DataDependency;
import edu.policy.model.constraint.Predicate;
import edu.policy.model.data.Session;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class DCParser {

    Session session;

    private static final Logger logger = LogManager.getLogger(DCParser.class);

    Cell cell_1;
    Cell cell_2;

    Hashtable<String, Integer> attrDomSizeDict;

    Hashtable<String, Double> numericalAttrDomMin;

    Hashtable<String, Double> numericalAttrDomMax;

    List<String> schema;
    List<String> schemaType;

    public DCParser(Session session) {
        this.session = session;
        this.attrDomSizeDict = session.getCategoricalAttrDomSizeDict();
        this.numericalAttrDomMax = session.getNumericalAttrDomMax();
        this.numericalAttrDomMin = session.getNumericalAttrDomMin();

        this.schema = session.getSchema();
        this.schemaType = session.getSchemaType();
    }

    /**
     * Example DC schema level:
     * [FD, Employee, EQ(zip,zip), NEQ(State,State)]
     *
     */
    public DataDependency parseDC(String[] dcSplits) throws Exception {

        DataDependency schemaDC = new DataDependency();

        // get dependency type
        ConstraintType dependencyType = ConstraintType.valueOf(dcSplits[0]);
        schemaDC.setDependencyType(dependencyType);

        // get relation name
        String relationName = dcSplits[1];
        schemaDC.setRelationName(relationName);

        int predRef = 0;
        // parse predicates
        for (int i=2; i < dcSplits.length; i++) {

            Predicate schemaPredicate = null;

            OperationType operationTypeOfPart = Utils.containsOperation(dcSplits[i]); // get operation of the predicate
            logger.debug(String.format("dcSplits[i]: %s, typeOfPart: %s", dcSplits[i], operationTypeOfPart));

            if (!Objects.isNull(operationTypeOfPart)) {
                String predicateString = dcSplits[i];

                boolean hasConstant = predicateString.contains("'");

                String[] predSplits = predicateString.split(",|[(]|[)]");

                // get attribute type and name
                String attributeName = predSplits[1];

                schemaDC.setAttributeNames(attributeName);

                if (!schema.contains(attributeName))
                    throw new Exception(String.format("Attribute %s is not in the retrieved schema %s", attributeName, schema));

                int attributeIndexOf = schema.indexOf(attributeName);
                String attributeType = schemaType.get(attributeIndexOf);

                // get the first component
                if (attributeType.contains("INT")) {
                    this.cell_1 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.INTEGER);
                    this.cell_1.setMinMaxDom(numericalAttrDomMin.get(attributeName), numericalAttrDomMax.get(attributeName));
                }
                else if (attributeType.contains("CHAR")) {
                    this.cell_1 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.STRING);
                    this.cell_1.setAttrDomSize(attrDomSizeDict.get(attributeName));
                }
                else if (attributeType.contains("DOUBLE") || attributeType.contains("FLOAT")) {
                    this.cell_1 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.DOUBLE);
                    this.cell_1.setMinMaxDom(numericalAttrDomMin.get(attributeName), numericalAttrDomMax.get(attributeName));
                }

                // test if not contains constant
                if (!hasConstant) {
                    // no constant, get the second component
                    if (attributeType.contains("INT")) {
                        this.cell_2 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.INTEGER);
                        this.cell_2.setMinMaxDom(numericalAttrDomMin.get(attributeName), numericalAttrDomMax.get(attributeName));
                    }
                    else if (attributeType.contains("CHAR")) {
                        this.cell_2 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.STRING);
                        this.cell_2.setAttrDomSize(attrDomSizeDict.get(attributeName));
                    }
                    else if (attributeType.contains("DOUBLE") || attributeType.contains("FLOAT")) {
                        this.cell_2 = new Cell(session.getDatabaseName(), relationName, attributeName, predRef, AttributeType.DOUBLE);
                        this.cell_2.setMinMaxDom(numericalAttrDomMin.get(attributeName), numericalAttrDomMax.get(attributeName));
                    }

                    schemaPredicate = new Predicate(cell_1, cell_2, operationTypeOfPart, Boolean.TRUE, predSplits[1]);
                } else {
                    // has constant
                    if (attributeType.contains("INT")) {
                        schemaPredicate = new Predicate(cell_1, Integer.parseInt(predSplits[2].replaceAll("'", "")),
                                operationTypeOfPart, Boolean.TRUE, attributeName);
                    }
                    else if (attributeType.contains("CHAR")) {
                        schemaPredicate = new Predicate(cell_1, predSplits[2], operationTypeOfPart, Boolean.TRUE, attributeName);
                    }
                    else if (attributeType.contains("DOUBLE") || attributeType.contains("FLOAT")) {
                        schemaPredicate = new Predicate(cell_1, Double.parseDouble(predSplits[2].replaceAll("'", "")),
                                operationTypeOfPart, Boolean.TRUE, attributeName);
                    }

                }
                assert schemaPredicate != null;
                schemaPredicate.setPredID(predRef);
                schemaDC.addSchemaPredicate(schemaPredicate);

                schemaDC.setSchemaComponents(cell_1, cell_2);

            }
            else {
                throw new Exception(String.format("Operation not exists in the predicate: %s", dcSplits[i]));
            }

            predRef += 1;

        }

        String cnf = StringUtils.join(Arrays.asList(dcSplits).subList(2, dcSplits.length), '&');
        logger.debug(String.format("CNF form of the DC: %s.", cnf));
        schemaDC.setCnf_form(cnf);
        logger.debug(String.format("Operation List of the current dep: %s", schemaDC.getSchemaPredicates().stream().map(Predicate::getOperation).collect(toList())));

        logger.debug(String.format("Finished parsing DC: %s", schemaDC.toString()));

        return schemaDC;

    }

}
