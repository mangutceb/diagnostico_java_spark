package minsait.ttaa.datio.common;

public final class Common {

    public static final String SPARK_MODE = "local[*]";
    public static final String HEADER = "header";
    public static final String INFER_SCHEMA = "inferSchema";
    public static final String INPUT_PATH = Configuration.getInputFileName();
    public static final String OUTPUT_PATH = Configuration.getOutputFileName();
    public static final int FILTER_AGE = Configuration.getFilterAge();

}
