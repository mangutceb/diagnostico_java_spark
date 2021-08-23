package minsait.ttaa.datio.engine;

import minsait.ttaa.datio.common.Configuration;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static minsait.ttaa.datio.common.Common.SPARK_MODE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


public class TransformerUnitTest {

    private Transformer engine;
    private SparkSession spark;
    private StructType schema;

    @Before
    public void init() {
        Configuration.initConfiguration();
        spark = SparkSession
                .builder()
                .master(SPARK_MODE)
                .getOrCreate();
        engine = new Transformer(spark);
        List<StructField> inputFields = Arrays.asList(
                DataTypes.createStructField("player_cat", DataTypes.StringType, true),
                DataTypes.createStructField("potential_vs_overall", DataTypes.DoubleType, true)
        );
        schema = DataTypes.createStructType(inputFields);
    }

    @Test
    public void RightFilterFourMethodEmptyDataSet() {
        List<Row> rows = Arrays.asList(
                RowFactory.create("C", 1.15),
                RowFactory.create("C", 1.0),
                RowFactory.create("D", 1.25),
                RowFactory.create("D", 0.9));

        Dataset<Row> testDataset = spark.createDataFrame(rows, schema);
        testDataset.show(4, false);
        testDataset.printSchema();
        testDataset = engine.filterByPlayerAndPotential(testDataset);
        testDataset.printSchema();
        testDataset.show(4, false);
        assertEquals(0L, testDataset.count());
    }

    @Test
    public void RightFilterFourMethodNotEmptyDataSet() {
        List<Row> rows = Arrays.asList(
                RowFactory.create("C", 1.15),
                RowFactory.create("D", 1.25),
                RowFactory.create("C", 1.16),
                RowFactory.create("D", 1.26),
                RowFactory.create("C", 1.25),
                RowFactory.create("D", 1.15),
                RowFactory.create("A", 1.26),
                RowFactory.create("A", 0.5),
                RowFactory.create("B", 0.9),
                RowFactory.create("B", 2.0));

        Dataset<Row> testDataset = spark.createDataFrame(rows, schema);
        testDataset.show();
        testDataset.printSchema();
        testDataset = engine.filterByPlayerAndPotential(testDataset);
        testDataset.printSchema();
        testDataset.show();
        assertEquals(7L, testDataset.count());
    }

}