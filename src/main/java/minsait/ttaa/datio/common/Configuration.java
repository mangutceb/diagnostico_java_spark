package minsait.ttaa.datio.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;


public class Configuration {
    private Configuration(){}
    private static final Properties properties = new Properties();

    public static final String DEFAULT_INPUT_PATH = "src/test/resources/data/players_21.csv";
    public static final String DEFAULT_OUTPUT_PATH = "src/test/resources/data/outputDefault";
    public static final String DEFAULT_AGE_FILTER = "0";
    public static final String CONFIG_TEXT_FILE = "src/main/resources/aplication.properties";

    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());

    public static void initConfiguration() {
        try {
            InputStream readFile = new FileInputStream(CONFIG_TEXT_FILE);
            properties.load(readFile);
            LOG.info("[Configuration] Configuration file read successfully.");
        } catch (Exception e) {
            LOG.warning("[Configuration] Error while reading the file.");
            properties.setProperty("path.input", DEFAULT_INPUT_PATH);
            properties.setProperty("path.output", DEFAULT_OUTPUT_PATH);
            properties.setProperty("age.filter", DEFAULT_AGE_FILTER);
        }
    }

    public static String getInputFileName() {
        return properties.getProperty("path.input");
    }

    public static String getOutputFileName() {
        return properties.getProperty("path.output");
    }

    public static int getFilterAge() {
        try {
            return Integer.parseInt(properties.getProperty("age.filter"));
        } catch (Exception e) {
            LOG.warning("[Configuration] Error while parsing age.filter value. Please see configuration file.");
            return 0;
        }
    }
}
