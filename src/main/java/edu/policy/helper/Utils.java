package edu.policy.helper;

import edu.policy.model.AttributeType;
import edu.policy.model.OperationType;
import edu.policy.model.constraint.Cell;
import edu.policy.model.data.MetaData;
import edu.policy.model.data.Session;
import edu.policy.model.data.User;
import edu.policy.model.test.TestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static final Logger logger = LogManager.getLogger(Utils.class);

    // From https://stackoverflow.com/questions/3833814/java-how-to-write-a-zip-function-what-should-be-the-return-type
    public static <T> List<List<T>> zip(List<T>... lists) {
        List<List<T>> zipped = new ArrayList<List<T>>();
        for (List<T> list : lists) {
            for (int i = 0, listSize = list.size(); i < listSize; i++) {
                List<T> list2;
                if (i >= zipped.size())
                    zipped.add(list2 = new ArrayList<T>());
                else
                    list2 = zipped.get(i);
                list2.add(list.get(i));
            }
        }
        return zipped;
    }

    public static List<List<String>> zipSchemaAndType(List<String> schema, List<String> schemaType) {
        return zip(schema, schemaType);
    }

    public static Hashtable<String, AttributeType> createSchemaTypeDict(List<String> schema, List<String> schemaType) {
        List<List<String>> zippedSchemaType = zipSchemaAndType(schema, schemaType);
        Hashtable<String, AttributeType> schemaTypeDict = new Hashtable<String, AttributeType>();

        for (List<String> zip : zippedSchemaType) {
            String type = zip.get(1);

            if (type.contains("INT")) {
                schemaTypeDict.put(zip.get(0), AttributeType.INTEGER);
            }
            else if (type.contains("CHAR") || type.contains("TEXT"))
                schemaTypeDict.put(zip.get(0), AttributeType.STRING);
            else if (type.contains("FLOAT") || type.contains("DOUBLE"))
                schemaTypeDict.put(zip.get(0), AttributeType.DOUBLE);
            else if (type.contains("DATE"))
                schemaTypeDict.put(zip.get(0), AttributeType.DATE);
        }



        return schemaTypeDict;
    }

    // from https://stackoverflow.com/questions/25417363/java-string-contains-matches-exact-word
    public static boolean isContain(String source, String subItem){
        String pattern = "\\b"+subItem+"\\b";
        Pattern p=Pattern.compile(pattern);
        Matcher m=p.matcher(source);
        return m.find();
    }

    // https://stackoverflow.com/questions/5283047/intersection-and-union-of-arraylists-in-java
    public static List<Cell> unionCells (List<Cell> list1, List<Cell> list2) {
        Set<Cell> set = new HashSet();

        set.addAll(list1);
        set.addAll(list2);

        return new ArrayList<>(set);
    }

    public static List<Cell> intersectionCells (List<Cell> list1, List<Cell> list2) {
        List<Cell> list = new ArrayList<Cell>();

        for (Cell t1 : list1) {
            for (Cell t2: list2) {
                if (t1.equals(t2)) { // since we have customized equals and hashcode implementation
                    list.add(t1);
                }
            }
        }

        return list;
    }

    public static boolean hasMoreThan2SenCellsInDep (List<Cell> schemaCells, List<Cell> senCells, int senTID) {
        int counter = 0;

        for (Cell t1: schemaCells) {
            for (Cell t2: senCells) {
                if (counter/2 > 1)
                    return true;
                if (t1.getAttributeName().equals(t2.getAttributeName()) && t2.getTupleID() == senTID)
                    counter += 1;
            }
        }

        return counter/2 > 1;
    }

    public static int countSenCellInDep (List<Cell> schemaCells, List<Cell> senCells, int senTID) {
        int counter = 0;

        for (Cell t1: schemaCells) {
            for (Cell t2: senCells) {
                if (t1.getAttributeName().equals(t2.getAttributeName()) && t2.getTupleID() == senTID)
                    counter += 1;
            }
        }

        // divided by 2 because each attribute in schemaCells appears two times
        return counter/2;
    }

    public static Session createSessionFromTestCase(TestCase testCase) {

        User user = new User(UUID.fromString(testCase.getUserID()), testCase.getUserName());

        logger.info("The following user is using this program: " + user.toString());

        String database = testCase.getDatabaseName();
        String relation = testCase.getRelationName();

        int expID = testCase.getExpID();

        // Get schema and schematype, and dom size
        List<String> schema = user.retrieveSchema(database, relation); // retrieve schema
        List<String> schemaType = user.retrieveSchemaType(database, relation);

        Hashtable<String, AttributeType> schemaTypeDict = createSchemaTypeDict(schema, schemaType);

        logger.info(String.format("Database schema: %s", schema));
        logger.info(String.format("Database schema data type: %s", schemaType));

        // get categorical attribute schema and domain size
        List<String> categoricalSchema = schema.stream().filter(attr -> schemaTypeDict.get(attr).equals(AttributeType.STRING)).collect(Collectors.toList());

        Hashtable<String, Integer> categoricalAttrDomSizeDict = user.retrieveCategoricalVarDomSize(categoricalSchema, database, relation);

        // get numerical attribute schema and minimum/maximum domain
        List<String> numericalSchema = schema.stream()
                                             .filter(attr ->
                                                (schemaTypeDict.get(attr).equals(AttributeType.INTEGER) || schemaTypeDict.get(attr).equals(AttributeType.DOUBLE)))
                                             .collect(Collectors.toList());

        Hashtable<String, Double> numericalAttrDomMin = user.retrieveNumericalMin(numericalSchema, database, relation);

        Hashtable<String, Double> numericalAttrDomMax = user.retrieveNumericalMax(numericalSchema, database, relation);

        MetaData metaData = new MetaData(schema, schemaType, schemaTypeDict, categoricalAttrDomSizeDict, numericalAttrDomMin, numericalAttrDomMax);

        String algo = testCase.getAlgo();
        float kVal = testCase.getK_value();
        String DCDir = testCase.getDCFileName();

        List<Cell> senCells = testCase.getPolicies(metaData);

        int limit = testCase.getLimit();

        Boolean isAscend = testCase.getAscend();

        Boolean useMVC = testCase.getUseMVC();

        Boolean testOblCueset = testCase.isTestOblCueset();

        // Get random flag
        long seed = testCase.getRandomSeed();
        Boolean randomCuesetChoosing = testCase.getRandomCuesetChoosing();
        Boolean randomHiddenCellChoosing = testCase.getRandomHiddenCellChoosing();

        Session session = new Session(expID, user, database, relation, metaData,
                limit, isAscend, algo, kVal, DCDir, senCells, seed, randomCuesetChoosing, randomHiddenCellChoosing,
                useMVC, testCase.isTestFanOut(), testOblCueset, testCase.getPolicySenLevel());

        logger.debug(String.format("Start the new session: %s", session));

        return session;
    }

    public static String escapeReserved (String attributeName) {
        return "`" + attributeName + "`";
    }

    public static OperationType containsOperation(String part) {
        for (String type: OperationType.getNames(OperationType.class)) {
            if (Utils.isContain(part, type))
                return OperationType.valueOf(type);
        }

        return null;
    }

    public static <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
    }
}
