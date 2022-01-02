package edu.policy.manager;

import edu.policy.model.constraint.ConstraintType;
import edu.policy.model.constraint.DataDependency;
import edu.policy.model.constraint.Provenance;
import edu.policy.model.data.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Parser class:
 * Parse the input dependencies from specified file to Java objects.
 * Support denial constraints (DC) and provenance based dependencies (PBD).
 *
 */

public class Parser {

    Session session;

    PBDParser pbdParser;

    DCParser dcParser;

    private static final Logger logger = LogManager.getLogger(Parser.class);

    List<String> dependencyInputs = new ArrayList<>();

    List<DataDependency> dcs = new ArrayList<>();

    List<Provenance> pbds = new ArrayList<>();

    List<String> schema;
    List<String> schemaType;

    public Parser(Session session) {
        this.session = session;

        // initialize PBD parser and DC parser
        pbdParser = new PBDParser(session);
        dcParser = new DCParser(session);

        schema = session.getSchema();
        schemaType = session.getSchemaType();
    }

    public void loadDependencies(String dir) {

        try {
            logger.info("The directory path of dependencies: " + dir);
            logger.info("Start to load dependencies.");
            File dc_file = new File(dir);
            Scanner dc_file_reader = new Scanner(dc_file);
            while (dc_file_reader.hasNextLine()) {
                dependencyInputs.add(dc_file_reader.nextLine());
            }
            dc_file_reader.close();
            logger.info(String.format("Finish loading %d dependencies.", dependencyInputs.size()));
        } catch (FileNotFoundException e) {
            logger.error("Error in reading dependencies file.");
            e.printStackTrace();
        }
    }

    public void parse() throws Exception {
        logger.info("Start to Parse dependencies.");

        for (String dependencyInput: dependencyInputs) {

            logger.debug(String.format("Parsing dependency: %s", dependencyInput));

            dependencyInput = dependencyInput.replace('"', '\'');  // replace double quote to '' for constants
            dependencyInput = dependencyInput.replaceAll("\\s+",""); // delete whitespace
            String[] depSplits = dependencyInput.split("&");

            // get dependency type
            ConstraintType dependencyType = ConstraintType.valueOf(depSplits[0]);

            if (dependencyType == ConstraintType.DC) {
                dcs.add(dcParser.parseDC(depSplits));
            }
            else if (dependencyType == ConstraintType.PBD) {
                pbds.add(pbdParser.parsePBD(depSplits));
            }

        }

        session.setDcs(dcs);
        session.setPbds(pbds);
    }

    public List<DataDependency> getDcs() {
        return dcs;
    }

    public List<Provenance> getPbds() {
        return pbds;
    }
}
