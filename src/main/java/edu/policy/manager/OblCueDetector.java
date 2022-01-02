package edu.policy.manager;

import edu.policy.helper.Utils;
import edu.policy.model.AttributeType;
import edu.policy.model.constraint.*;
import edu.policy.model.cue.CueSet;
import edu.policy.model.data.Session;
import edu.policy.model.data.Table;
import edu.policy.model.data.Tuple;
import org.apache.commons.text.StringSubstitutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static java.util.stream.Collectors.toList;


public class OblCueDetector {

    Session session;

    private static final Logger logger = LogManager.getLogger(OblCueDetector.class);

    final String temporaryRelationName = "temp";

    // Query template
    final String multiQueryTemplate = "SELECT T2.tid AS senCellID, T1.tid AS tid, ${depAttributes}" +
            "FROM ${databaseName}.${relationName} AS T1 JOIN ${databaseName}.${temporaryRelationName} AS T2 ON (T1.tid <> T2.tid) ";

    final String dropQueryTemplate = "DROP TABLE IF EXISTS `${databaseName}`.`${temporaryRelationName}`;";

    final String createTableQueryTemplate = "CREATE TABLE ${databaseName}.${temporaryRelationName} (\n" +
                                            "tid int not null,\n" +
                                            "${otherAttributes}\n" +
                                            ");";

    final String insertTableQueryTemplate = "INSERT INTO ${databaseName}.${temporaryRelationName} \n" +
                                            "(${attributeNames})\n" +
                                            "VALUES\n" +
                                            "${rows};";

    public OblCueDetector(Session session) {
        this.session = session;
    }

    public List<CueSet> detect(List<DataDependency> dependencies, List<Cell> senCells) {

        logger.info(String.format("Generating cuesets for %d dependencies and %d sensitive cells.", dependencies.size(), senCells.size()));

        List<CueSet> cueSetListReturn = new ArrayList<>();

        // instantiate the sensitive cell
        for (Cell senCell: senCells)
            senCellInstantiation(senCell);

        // get the sensitive tuples
        Table sensitiveTuples = getSensitiveTuples(senCells);

        // substitute sensitive cell val with null
        substituteSensitiveVal(sensitiveTuples, senCells);

        // create a new table with all sensitive tuples
        sensTableGen(sensitiveTuples);

        long startTime_cueDetector = new Date().getTime();
        // for each schema-level dependency
        for (DataDependency schemaDep: dependencies) {

            // Modify the query template based on the dependencies
            String queryStatement = queryCompilation(schemaDep);

            long startTime_queryExecution = new Date().getTime();
            // query execution
            PreparedStatement preStatement = queryExecution(queryStatement);

            // Project the query results to the cuesets
            cueSetListReturn.addAll(projectQueryResultsToCuesets(schemaDep, senCells, preStatement, sensitiveTuples));

            long endTime_queryExecution = new Date().getTime();
            long timeElapsed = endTime_queryExecution - startTime_queryExecution;
            logger.info(String.format("Finished executing one query; use time: %d ms.", timeElapsed));
        }
        long endTime_cueDetector = new Date().getTime();
        long timeElapsed = endTime_cueDetector - startTime_cueDetector;
        logger.info(String.format("Finishing execution of cue detector; use time: %d ms.", timeElapsed));

        return cueSetListReturn;

    }


    public List<CueSet> detect(List<DataDependency> dependencies, Cell senCell) {

        logger.info(String.format("Generating cuesets for %d dependencies and sensitive cells %s", dependencies.size(), senCell));

        List<CueSet> cueSetListReturn = new ArrayList<>();

        // instantiate the sensitive cell
        senCellInstantiation(senCell);

        // get the sensitive tuples
        Table sensitiveTuples = getSensitiveTuples(Collections.singletonList(senCell));

        // substitute sensitive cell val with null
        substituteSensitiveVal(sensitiveTuples, Collections.singletonList(senCell));

        // create a new table with all sensitive tuples
        sensTableGen(sensitiveTuples);

        // for each schema-level dependency
        for (DataDependency schemaDep: dependencies) {

            // Modify the query template based on the dependencies
            String queryStatement = queryCompilation(schemaDep);

            PreparedStatement statement = queryExecution(queryStatement);

            // Project the query results to the cuesets
            cueSetListReturn.addAll(projectQueryResultsToCuesets(schemaDep, senCell, statement, sensitiveTuples));
        }

        logger.info(String.format("Finishing execution of cue detector."));

        return cueSetListReturn;
    }

    private void senCellInstantiation(Cell senCell) {

        PreparedStatement query = null;


        // get the datatype of the sensitive cell using attribute name
        AttributeType dataType = senCell.getCellType();

        // assign values to sensitive cell
        try {
            // query database
            query = session.getUser().queryOnSingleCell(session.getDatabaseName(), session.getRelationName(),
                    senCell.getTupleID(), senCell.getAttributeName());
            ResultSet rs = null;

            try {
                rs = query.executeQuery();
                while (rs.next()) {
                    switch (dataType) {
                        case INTEGER:
                            senCell.setIntCellValue(rs.getInt(senCell.getAttributeName()));
                            break;
                        case DOUBLE:
                            senCell.setDoubleCellValue(rs.getDouble(senCell.getAttributeName()));
                            break;
                        case STRING:
                            senCell.setStringCellValue(rs.getString(senCell.getAttributeName()));
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + dataType);
                    }
                }
            } finally {
                if (rs != null)
                    try { rs.close(); } catch (SQLException e) {e.printStackTrace();}
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (query != null)
                try { query.close(); } catch (SQLException e) { e.printStackTrace(); }
        }


    }

    private List<String> queryCompilation(List<DataDependency> dependencies) {

        List<String> queries = new ArrayList<>();

        for (DataDependency dep: dependencies) {
            // switch dependency type
            if (dep.getDependencyType() == ConstraintType.DC) {
                boolean isUnary = dep.getSchemaPredicates().size() == 1;

                if (!isUnary)
                    queries.add(multiQueryGeneration(dep));

            }
        }

        return queries;
    }

    private String queryCompilation(DataDependency schemaDep) {

        String queryStatement = null;

        // switch dependency type
        if (schemaDep.getDependencyType() == ConstraintType.DC) {
            boolean isUnary = schemaDep.getSchemaPredicates().size() == 1;

            if (!isUnary)
                queryStatement = multiQueryGeneration(schemaDep);

        }

        return queryStatement;
    }


    /**
     * This function is to modify the query template
     *          final String multiQueryTemplate = "T2.tid, T1.tid, ${depAttributes}" +
     *                  "FROM ${databaseName}.${relationName} AS T1 JOIN ${databaseName}.${temporaryRelationName} AS T2 ON (T1.tid <> T2.tid) " +
     *                  "WHERE ${conds};";
     * to generate a SQL query.
     *
     * Notice the symmetry property of DCs.
     * An real example on DC (\neg (State=State) \land (Salary > Salary) \land (Rate < Rate)) would be:
     *      SELECT T1.*, T2.tid
     *      FROM taxdb.taxes AS T1 JOIN taxdb.temp AS T2 ON (T1.tid <> T2.tid)
     *      WHERE ( (T2.State is null OR T1.State=T2.State) AND (T2.Salary is null OR T1.Salary>T2.Salary) And (T2.Rate is null OR T1.Rate<T2.Rate) )
     *            OR ( (T2.State is null OR T1.State=T2.State) AND (T2.Salary is null OR T1.Salary<T2.Salary) And (T2.Rate is null OR T1.Rate>T2.Rate) );
     *
     * Optimization:
     *      if a dependency
     */
    private String multiQueryGeneration(DataDependency dependency) {

        logger.debug("Generating CueSet detection queries.");

        // processing depAttributes
        StringBuilder depAttributes = new StringBuilder();

        List<String> attributeNamesInDep = dependency.getAttributeNames();

        for (String attribute: attributeNamesInDep) {

            if (Boolean.TRUE) {
                depAttributes.append("T1.").append(Utils.escapeReserved(attribute));

                if (attributeNamesInDep.indexOf(attribute) != (attributeNamesInDep.size() - 1))
                    depAttributes.append(", ");
                else
                    depAttributes.append(" \n");
            }
        }

        // remove the last ", "
        if (depAttributes.length() > 1) {
            depAttributes.setLength(depAttributes.length() - 1);
        }


        // template code
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("databaseName", session.getDatabaseName());
        valuesMap.put("relationName", session.getRelationName());
        valuesMap.put("temporaryRelationName", temporaryRelationName);
        valuesMap.put("depAttributes", depAttributes.toString());

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String query = sub.replace(multiQueryTemplate);

        return query;
    }


    private List<PreparedStatement> queryExecution(List<String> queryStatements){
        return session.getUser().multiQueryExecution(queryStatements);
    }

    private PreparedStatement queryExecution(String queryStatement){
        return session.getUser().queryExecution(queryStatement);
    }


    private Table getSensitiveTuples(List<Cell> senCells) {

        Table sensitiveTuples = new Table();

        List<Integer> tupleIDs = senCells.stream().map(Cell::getTupleID).distinct().collect(toList());

        for (int tupleID: tupleIDs) {

            Tuple senTuple = session.getUser().retrieveTuple(session.getDatabaseName(), session.getRelationName(),
                    tupleID, session.getSchema());

            sensitiveTuples.addToTable(senTuple);

        }

        sensitiveTuples.setRelationName(session.getRelationName());
        sensitiveTuples.setSchema(session.getSchema());
        sensitiveTuples.setSchemaType(session.getSchemaType());
        sensitiveTuples.setAttrDomSizeDict(session.getAttrDomSizeDict());

        return sensitiveTuples;
    }

    private void substituteSensitiveVal(Table sensitiveTuples, List<Cell> senCells) {

        logger.debug("Substituting sensitive value with null.");

        for (Cell senCell: senCells) {
            // get sensitive cell id
            int senCellTupleID = senCell.getTupleID();

            Tuple tuple = sensitiveTuples.getTable().stream().filter(tuplex -> tuplex.getTupleID() == senCellTupleID).findFirst().orElse(null);

            assert tuple != null;
            tuple.substituteValInTuple(senCell.getAttributeName(), "null");

        }
    }

    private void sensTableGen(Table sensitiveTuples) {

        long startTime_manipulation = new Date().getTime();

        // drop temp table if exists
        dropTempTable();

        // create temp table
        createTempTable();

        // insert sensitive tuples while setting sensitive cell values as NULL
        insertSenTuples(sensitiveTuples);

        long endTime_manipulation = new Date().getTime();
        long timeElapsed = endTime_manipulation - startTime_manipulation;
        logger.info(String.format("Finished executing data manipulation queries; use time: %d ms.", timeElapsed));

    }


    /**
     * Drop temporary table if it exists.
     * Modify the following template query code:
     *          final String dropQueryTemplate = "DROP TABLE IF EXISTS `${databaseName}`.`${temporaryRelationName}`;";
     */
    private void dropTempTable(){
        // template code
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("databaseName", session.getDatabaseName());
        valuesMap.put("temporaryRelationName", temporaryRelationName);

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String query = sub.replace(dropQueryTemplate);

        // drop temp table
        session.getUser().manipulationExecution(query);

    }

    /**
     * Create the temporary table.
     * Modify the following template query code:
     *          final String createTableQueryTemplate = "CREATE TABLE ${databaseName}.${temporaryRelationName} (\n" +
     *                                             "tid int not null,\n" +
     *                                             "${otherAttributes}\n" +
     *                                             ");";
     */
    private void createTempTable() {

        // processing attributes and schema
        StringBuilder otherAttributes = new StringBuilder();

        List<String> schema = new ArrayList<>(session.getSchema());
        schema.remove("tid");
        System.out.println(schema);
        System.out.println(session.getSchema());

        for (String attribute: schema) {
            if (!attribute.equals("tid")) {
                otherAttributes.append(Utils.escapeReserved(attribute));
                otherAttributes.append(" ");

                int index = session.getSchema().indexOf(attribute);
                String attributeType = session.getSchemaType().get(index);
                otherAttributes.append(attributeType);
                if (attributeType.equals("VARCHAR"))
                    otherAttributes.append("(255)");

                if (schema.indexOf(attribute) != (schema.size() - 1))
                    otherAttributes.append(",\n");
            }
        }

        // template code
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("databaseName", session.getDatabaseName());
        valuesMap.put("temporaryRelationName", temporaryRelationName);
        valuesMap.put("otherAttributes", otherAttributes.toString());

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String query = sub.replace(createTableQueryTemplate);

        if (session.getUseMVC())
            logger.info(String.format("Start to execute query: %s", query));

        // execute the query
        session.getUser().manipulationExecution(query);

    }

    /**
     * Insert sensitive tuples.
     * Modify the following template query code:
     *          final String insertTableQueryTemplate = "INSERT INTO ${databaseName}.${temporaryRelationName} \n" +
     *                                             "(${attributeNames})\n" +
     *                                             "VALUES\n" +
     *                                             "${rows};";
     */
    private void insertSenTuples(Table sensitiveTuples) {

        // processing attributenames and rows
        StringBuilder attributeNames = new StringBuilder();
        StringBuilder rows = new StringBuilder();

        List<String> schema = session.getSchema();

        Hashtable<String, AttributeType> schemaTypeDict = session.getSchemaTypeDict();

        for (String attributeName: schema) {
            attributeNames.append(Utils.escapeReserved(attributeName));

            if (schema.indexOf(attributeName) != (schema.size() - 1))
                attributeNames.append(", ");
        }

        List<Tuple> senTuples = sensitiveTuples.getTable();

        for (Tuple senTuple: senTuples) {
            rows.append("(");

            for (String attribute: schema) {

                // check schema type, if string then add ``
                if (schemaTypeDict.get(attribute).equals(AttributeType.STRING) && !senTuple.getTupleWithNull().get(attribute).equals("null"))
                    rows.append("\"").append(senTuple.getTupleWithNull().get(attribute)).append("\"");
                else
                    rows.append(senTuple.getTupleWithNull().get(attribute));
                if (schema.indexOf(attribute) != (schema.size() - 1))
                    rows.append(", ");
            }
            rows.append(")");

            if (senTuples.indexOf(senTuple) != (senTuples.size() - 1))
                rows.append(",\n");
        }


        // template code
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("databaseName", session.getDatabaseName());
        valuesMap.put("temporaryRelationName", temporaryRelationName);
        valuesMap.put("attributeNames", attributeNames.toString());
        valuesMap.put("rows", rows.toString());

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String query = sub.replace(insertTableQueryTemplate);

        if (session.getUseMVC())
            logger.info(String.format("Start to execute query: %s", query));

        // execute the query
        session.getUser().manipulationExecution(query);
    }


    private List<CueSet> projectQueryResultsToCuesets(List<DataDependency> dependencies, List<Cell> senCells,
                                                      List<ResultSet> resultSetList, Table sensitiveTuples) {
        List<CueSet> cueSetList = new ArrayList<>();

        for (DataDependency dep: dependencies) {
            try {
                cueSetList.addAll(projectQueryResultToCueset(dep, senCells, resultSetList.get(dependencies.indexOf(dep)), sensitiveTuples));
            }
            catch (SQLException e) {
                e.printStackTrace();
            }

        }

        return cueSetList;

    }

    private List<CueSet> projectQueryResultsToCuesets(DataDependency schemaDep, Cell senCell,
                                                      PreparedStatement preparedStatement, Table sensitiveTuples) {

        List<CueSet> cueSetList = new ArrayList<>();
        try {
            ResultSet resultSet = preparedStatement.executeQuery();

            try {

                // ** project query to cuesets **
                cueSetList.addAll(projectQueryResultToCueset(schemaDep, senCell, resultSet, sensitiveTuples));
            }
            catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                try { preparedStatement.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return cueSetList;

    }

    private List<CueSet> projectQueryResultsToCuesets(DataDependency schemaDep, List<Cell> senCells,
                                                      PreparedStatement preparedStatement, Table sensitiveTuples) {
        List<CueSet> cueSetList = new ArrayList<>();
        try {
            ResultSet resultSet = preparedStatement.executeQuery();

            try {

                // ** project query to cuesets **
                cueSetList.addAll(projectQueryResultToCueset(schemaDep, senCells, resultSet, sensitiveTuples));
            }
            catch (SQLException e) {
                e.printStackTrace();
            } finally {
                if (resultSet != null) try { resultSet.close(); } catch (SQLException e) {e.printStackTrace();}
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (preparedStatement != null)
                try { preparedStatement.close(); } catch (SQLException e) {e.printStackTrace();}
        }

        return cueSetList;

    }

    /**
     * Function to get the list of cuesets for a specific dependency, given the query results.
     * 1. Retrieve the cueset;
     * 2. Instantiate the dependency;
     * 3. Add the instantiated dep to the cueset object;
     * 4. Return the list of cuesets.
     *
     * For Instantiation:
     * Schema cell --> Instance cell:
     * a. set tuple ID;
     * b. set cell value.
     * Schema pred --> Instance pred:
     * a. set instance cells;
     * b. set sensitive Flag;
     * b. set reverse Flag.
     * Schema dep  --> Instance dep:
     * a. set instance cells and predicates;
     * b. set sensitive predicate index
     * c. set sensitive cell index.
     *
     *
     * @param dep dependency
     * @param senCell sensitive cell
     * @param resultSet return result from the query
     * @param sensitiveTuples sensitive table
     * @return List of cuesets w.r.t the dependency dep.
     * @throws SQLException throw exception
     */
    private List<CueSet> projectQueryResultToCueset(DataDependency dep, Cell senCell,
                                              ResultSet resultSet, Table sensitiveTuples) throws SQLException {

        logger.debug("Projecting query results to CueSets.");

        List<CueSet> cueSetList = new ArrayList<>();

        if (!resultSet.isBeforeFirst() ) {
            logger.warn("No data");
        }
        else {
            while (resultSet.next()) {
                List<Cell> cueCells = new ArrayList<>();

                DependencyInstance instance = new DependencyInstance(dep);

                List<Predicate> predicates = instance.getPredicates();

                Boolean isSymmetric = Boolean.FALSE;

                for (Predicate pred: predicates) {
                    // IF not unary dependency AND not sensitive predicate
                    if (predicates.size() > 1 && !pred.getAttributeName().equals(senCell.getAttributeName())) {

                        // first instantiate the nonsensitive cell in the sensitive tuple
                        instantiateNonSenCellInSenTuple(pred.getComponent_1(), sensitiveTuples, senCell.getTupleID());
                        cueCells.add(pred.getComponent_1());

                        // then instantiate the nonsensitive cell
                        instantiateNonSenCell(pred.getComponent_2(), resultSet);
                        cueCells.add(pred.getComponent_2());

                    }
                    // IF not unary dependency AND sensitive predicate
                    else if (predicates.size() > 1 && pred.getAttributeName().equals(senCell.getAttributeName())) {

                        //first set instantiated sensitive cell
                        pred.instantiateCell_1(senCell);

                        // then instantiate the nonsensitive cell
                        instantiateNonSenCell(pred.getComponent_2(), resultSet);

                        // For predicate instantiation
                        // set sensitive
                        pred.setSensitive(Boolean.TRUE);

                        // set sensitive cell ref
                        pred.setSenCellRef(1);

                        // For dependency instantiation
                        // set sensitive predicate index
                        instance.setSenPredID(pred.getPredID());

                    }

                    // set reverse flag
                    Boolean isReverse = pred.setReverse();

                    // If one predicate is detected to be reverse, the dependency is symmetric and other predicates
                    // should be set reverse as well.
                    if (isReverse.equals(Boolean.TRUE)) {
                        isSymmetric = Boolean.TRUE;
                    }


                }

                // set other predicate reverse flag as true.
                if (isSymmetric.equals(Boolean.TRUE))
                    for (Predicate pred: predicates)
                        pred.setReverse(Boolean.TRUE);


                // create cueset and set the dependency instance
                CueSet cueSet = new CueSet(cueCells, senCell, ConstraintType.DC);

                if (session.getAlgo().equals("k-den"))
                    cueSet.setDependencyInstance(instance);

                cueSetList.add(cueSet);
            }

        }


        return cueSetList;

    }

    /**
     * Overload function for projectQueryResultToCueset:
     * Enable taking in multiple sensitive cells as input
     * Result set has multiple rows, use senCellID to identify which row is corresponding to which sensitive cell.
     *
     * @param dep schema level dependency
     * @param senCells sensitive cells
     * @param resultSet result from query
     * @param sensitiveTuples sensitive table
     * @return a list of cuesets
     * @throws SQLException throw sql exception
     */
    private List<CueSet> projectQueryResultToCueset(DataDependency dep, List<Cell> senCells,
                                                    ResultSet resultSet, Table sensitiveTuples) throws SQLException {

        logger.debug("Projecting query results to CueSets.");

        List<CueSet> cueSetList = new ArrayList<>();

        if (!resultSet.isBeforeFirst() ) {
            logger.warn("No data");
        }
        else {
            while (resultSet.next()) {

                // more than one cell being marked as sensitive in this dep
                int senTID = resultSet.getInt("senCellID");

                // optimization: more than one sensitive cells in the dependency
                if (Utils.hasMoreThan2SenCellsInDep(dep.getSchemaComponents(), senCells, senTID))
                    continue;

                int senCellTID = resultSet.getInt("senCellID");

                Cell senCell = senCells.stream().filter(cell -> cell.getTupleID()==senCellTID).findFirst().orElse(null);

                List<Cell> cueCells = new ArrayList<>();

                DependencyInstance instance = new DependencyInstance(dep);

                List<Predicate> predicates = instance.getPredicates();

                Boolean isSymmetric = Boolean.FALSE;

                for (Predicate pred: predicates) {
                    // IF not unary dependency AND not sensitive predicate
                    if (predicates.size() > 1 && !pred.getAttributeName().equals(Objects.requireNonNull(senCell).getAttributeName())) {

                        // first instantiate the nonsensitive cell in the sensitive tuple
                        instantiateNonSenCellInSenTuple(pred.getComponent_1(), sensitiveTuples, senCell.getTupleID());
                        cueCells.add(pred.getComponent_1());

                        // then instantiate the nonsensitive cell
                        instantiateNonSenCell(pred.getComponent_2(), resultSet);
                        cueCells.add(pred.getComponent_2());

                    }
                    // IF not unary dependency AND sensitive predicate
                    else if (predicates.size() > 1 && pred.getAttributeName().equals(senCell.getAttributeName())) {

                        //first set instantiated sensitive cell
                        pred.instantiateCell_1(senCell);

                        // then instantiate the nonsensitive cell
                        instantiateNonSenCell(pred.getComponent_2(), resultSet);

                        // For predicate instantiation
                        // set sensitive
                        pred.setSensitive(Boolean.TRUE);

                        // set sensitive cell ref
                        pred.setSenCellRef(1);

                        // For dependency instantiation
                        // set sensitive predicate index
                        instance.setSenPredID(pred.getPredID());


                    }

                    // set reverse flag
                    Boolean isReverse = pred.setReverse();

                    // If one predicate is detected to be reverse, the dependency is symmetric and other predicates
                    // should be set reverse as well.
                    if (isReverse.equals(Boolean.TRUE)) {
                        isSymmetric = Boolean.TRUE;
                    }


                }

                // set other predicate reverse flag as true.
                if (isSymmetric.equals(Boolean.TRUE))
                    for (Predicate pred: predicates)
                        pred.setReverse(Boolean.TRUE);


                // create cueset and set the dependency instance
                CueSet cueSet = new CueSet(cueCells, senCell, ConstraintType.DC);

                if (session.getAlgo().equals("k-den"))
                    cueSet.setDependencyInstance(instance);

                cueSetList.add(cueSet);
            }

        }


        return cueSetList;

    }

    /**
     * Instantiate cell:
     * 1. set cell value;
     * 2. set tuple ID.
     *
     * @param cell the cell to be instantiated
     * @param val the value to instantiate the cell
     * @param tupleID the corresponding tuple id
     */
    private void instantiateCell(Cell cell, String val, int tupleID) {
        AttributeType dataType = cell.getCellType();

        switch (dataType) {
            case INTEGER:
                cell.setIntCellValue(Integer.parseInt(val));
                break;
            case DOUBLE:
                cell.setDoubleCellValue(Double.parseDouble(val));
                break;
            case STRING:
                cell.setStringCellValue(val);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + dataType);
        }

        cell.setTupleID(tupleID);

    }

    /**
     *
     * Instantiate the cell which is not sensitive but in the sensitive tuple.
     * e.g.     A        B
     *         c_0       *    <-- sensitive tuple
     *         c_2      c_3
     *  FD: A->B, this function is to instantiate c_0.
     *
     * @param tupleID the sensitive cell tupleID, i.e. the sensitive tuple ID
     */
    private void instantiateNonSenCellInSenTuple(Cell cell, Table sensitiveTuples, int tupleID) {

        String val = sensitiveTuples.getTuple(tupleID).get(cell.getAttributeName());

        instantiateCell(cell, val, tupleID);

    }

    /**
     *
     * Instantiate the cell which is in the non-sensitive predicate and in the non-sensitive tuple.
     * e.g.     A        B
     *         c_0       *
     *         c_2      c_3     <-- non-sensitive tuple
     *  FD: A->B, this function is to instantiate c_2.
     *
     */
    private void instantiateNonSenCell (Cell cell, ResultSet resultSet) {

        String val = null;
        int tid = -1;

        try {
            val = resultSet.getString(cell.getAttributeName());
            tid = resultSet.getInt("tid");
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (val != null && tid != -1)
            instantiateCell(cell, val, tid);
        else
            logger.error("Cannot instantiate non sensitive cell in the non sensitive tuple.");
    }

    private void instantiateDependency() {

    }


    // intersection function: from https://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    private <T> List<T> intersection(List<T> list1, List<T> list2) {
        List<T> list = new ArrayList<>();

        for (T t : list1) {
            if(list2.contains(t)) {
                list.add(t);
            }
        }

        return list;
    }

}


