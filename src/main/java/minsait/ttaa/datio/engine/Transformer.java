package minsait.ttaa.datio.engine;

import minsait.ttaa.datio.common.naming.Field;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static minsait.ttaa.datio.common.Common.*;
import static minsait.ttaa.datio.common.naming.PlayerInput.*;
import static minsait.ttaa.datio.common.naming.PlayerOutput.*;
import static org.apache.spark.sql.functions.*;

public class Transformer extends Writer {
    private final SparkSession spark;
    private static final Logger LOG = Logger.getLogger(Transformer.class.getName());

    public Transformer(@NotNull SparkSession spark) {
        this.spark = spark;
        Dataset<Row> df = readInput();
        df = cleanData(df);
        if(FILTER_AGE == 1){
            df = filterByAge(df);
        }

        df = addPlayerCat(df);
        df = addColumnByDividedTwoFields(df, potentialVsOverall, potential, overall);
        df = filterByPlayerAndPotential(df);
        df = columnSelection(df);
        df.show(100, false);
        df.printSchema();

        write(df);
    }

    private Dataset<Row> columnSelection(@NotNull Dataset<Row> df) {
        return df.select(
                shortName.column(),
                longName.column(),
                age.column(),
                heightCm.column(),
                weightKg.column(),
                nationality.column(),
                clubName.column(),
                overall.column(),
                potential.column(),
                teamPosition.column()
        );
    }

    /**
     * @return a Dataset readed from csv file
     */
    private Dataset<Row> readInput() {
        Dataset<Row> df = spark.read()
                .option(HEADER, true)
                .option(INFER_SCHEMA, true)
                .csv(INPUT_PATH);
        return df;
    }

    /**
     * @param df    is d dataset
     * @return df a Dataset with filter transformation applied
     * column team_position != null && column short_name != null && column overall != null
     */
    private Dataset<Row> cleanData(Dataset<Row> df) {
        df = df.filter(
                teamPosition.column().isNotNull()
                        .and(shortName.column().isNotNull())
                        .and(overall.column().isNotNull())
        );
        return df;
    }

    /**
     * @param df is a Dataset with players information (must have team_position and height_cm columns)
     * @return add to the Dataset the column "cat_height_by_position"
     * by each position value
     * cat A for if is in 20 players tallest
     * cat B for if is in 50 players tallest
     * cat C for the rest
     */
    private Dataset<Row> exampleWindowFunction(Dataset<Row> df) {
        WindowSpec w = Window
                .partitionBy(teamPosition.column())
                .orderBy(heightCm.column().desc());

        Column rank = rank().over(w);

        Column rule = when(rank.$less(10), "A")
                .when(rank.$less(50), "B")
                .otherwise("C");

        df = df.withColumn(catHeightByPosition.getName(), rule);

        return df;
    }

    /**
     * @param   df is a Dataset<Row>
     * @return list of strings with the name of the columns that exist in dataset
     */
    private List<String> getColumns(Dataset<Row> df){
        return Arrays.asList(df.columns());
    }

    /**
     * @param df            is a Dataset where check if exist all columns in columnNames variable
     * @param columnNames   is a List with the name of the columns that we want to know if exist in dataset
     * @return true if exist all columns names in dataset, otherwise return false
     */
    private boolean requiredColumnsExist(Dataset<Row> df, List<String> columnNames){
        return getColumns(df).containsAll(columnNames);
    }

    /**
     * @param df is a Dataset<Row> with players information (must have age column)
     * @return add to the Dataset filter
     *
     */
    private Dataset<Row> filterByAge(Dataset<Row> df){
        List<String> columnsName = Collections.singletonList(age.getName());

        if(requiredColumnsExist(df,columnsName)) {
            LOG.warning("[ENTRA A FILTRAR JUGADORES MENORES DE 23]");
            df = df.filter(
                    age.column().$less(23)
            );
        }
        return df;
    }

    /**
     * @param df is a Dataset<Row> with players information (must have team_position, nationality and overall columns)
     * @return add to the Dataset the column "cat_height_by_position"
     *
     * Agregar una columna player_cat que responderá a la siguiente regla (rank over Window particionada por nationality y team_position y ordenada por overall):
     *
     * A si el jugador es de los mejores 3 jugadores en su posición de su país.
     * B si el jugador es de los mejores 5 jugadores en su posición de su país.
     * C si el jugador es de los mejores 10 jugadores en su posición de su país.
     * D para el resto de jugadores.
     */
    private Dataset<Row> addPlayerCat(Dataset<Row> df) {
        List<String> columnsName = Arrays.asList(
                teamPosition.getName(),
                nationality.getName(),
                overall.getName());

        if(requiredColumnsExist(df,columnsName)) {
            LOG.info("[Start][addPlayerCat method][Entry if]");
            WindowSpec w = Window
                    .partitionBy(nationality.column(),teamPosition.column())
                    .orderBy(overall.column().desc());

            Column rank = row_number().over(w);

            Column rule = when(rank.$less$eq(3), "A")
                    .when(rank.$less$eq(5), "B")
                    .when(rank.$less$eq(10), "C")
                    .otherwise("D");

            df = df.withColumn(playerCat.getName(), rule);

        }else{
            LOG.warning("[addPlayerCat] df doesn't have the required columns to apply transform function properly");
        }
        return df;
    }

    /**
     * @param df                    is a Dataset with players information (must have team_position, nationality and overall columns)
     * @param newColumnName         is a Field of the name of the new column
     * @param dividendColumnName    is a Field that will be the dividend in the division (the type of the column in dataset must be integer, double or float)
     * @param divisorColumnName     is a Field that will be the divisor in the division (the type of the column in dataset must be integer, double or float)
     * @return df with new column
     *
     */
    private Dataset<Row> addColumnByDividedTwoFields(Dataset<Row> df, Field newColumnName, Field dividendColumnName, Field divisorColumnName){
        List<String> columnsName = Arrays.asList(
                dividendColumnName.getName(),
                divisorColumnName.getName());

        if(requiredColumnsExist(df,columnsName)){
            df = df.withColumn(newColumnName.getName(), dividendColumnName.column().divide(divisorColumnName.column()));
        }else{
            LOG.warning("[addColumnByDividedTwoFields] df doesn't have the required columns to filter properly");
        }
        return df;
    }

    /**
     * @param df a Dataset that will be filtered by some conditions. See below
     * @return
     *
     * We will filter according to the player_cat and possible_vs_overall columns with the following conditions:
     * If player_cat is in the following values: A, B
     * If player_cat is C and potential_vs_overall is greater than 1.15
     * If player_cat is D and potential_vs_overall is greater than 1.25
     */
    public Dataset<Row> filterByPlayerAndPotential(Dataset<Row> df){
        List<String> columnsName = Arrays.asList(
                playerCat.getName(),
                potentialVsOverall.getName());

        if(requiredColumnsExist(df,columnsName)){
            df = df.filter(
                    playerCat.column()
                            .equalTo("C").and( potentialVsOverall.column().$greater(1.15))
                            .or( playerCat.column().equalTo("D").and( potentialVsOverall.column().$greater(1.25)))
                            .or( playerCat.column().equalTo("A").or( playerCat.column().equalTo("B")))
            );
        }else{
            LOG.warning("[filterByPlayerAndPotential] df doesn't have the required columns to filter properly");
        }
        return df;
    }


}
