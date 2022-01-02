package edu.policy.model.test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import edu.policy.model.data.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

public class TestFileParser {

    private static final Logger logger = LogManager.getLogger(User.class);

    public static TestCase[] testParser(String testFileName) throws FileNotFoundException {

        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(testFileName));

        TestCase[] testCases = gson.fromJson(reader,TestCase[].class);

        logger.debug(Arrays.toString(testCases));

        return testCases;
    }
}
