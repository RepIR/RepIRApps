package io.github.repir.apps.Eval;

import io.github.repir.tools.Excel.ExcelDoc;
import io.github.repir.tools.Excel.ExcelSheet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.TestSet.Topic;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * outputs a set of results files with the same test set to excel. The output is
 * formatted <TopicID> <TopicString> <MAP baseline> <MAP resultsfile1> ....
 * parameters: <configfile> { resultsfileextension }
 * @author jeroen
 */
public class ResultsToExcel {

   public static Log log = new Log(ResultsToExcel.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {resultsext}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet( repository );
      ResultSet resultset = new ResultSet( new QueryMetricAP(), testset, parsedargs.getRepeatedGroup());
      resultset.calulateMeasure();
      ExcelDoc workbook = new ExcelDoc(repository.getConfigurationString("testset.name") + ".xlsx");
      ExcelSheet querysheet = workbook.getSheet("queries");
      querysheet.setRow(0, 0, "nr", "original query", "system");
      int row = 1;
      TreeMap<Integer, Topic> sortedmap = new TreeMap<Integer, Topic>(testset.topics);
      for (Map.Entry<Integer, Topic> entry : sortedmap.entrySet()) {
         querysheet.setRow(row, 0, entry.getKey(), entry.getValue().query.trim());
         for (int coll = 0; coll < resultset.result.length; coll++) {
             int resultid = resultset.getResultNumber(entry.getKey());
             if (resultid >= 0) {
               querysheet.createCell(row, coll + 2).setFormatDouble(resultset.result[coll].queryresult[resultid]);
             }
         }
         row++;
      }
      for (int coll = 0; coll < resultset.result.length; coll++) {
         // TODO little bug?
         //querysheet.setCellFormula(row, coll + 2, "AVERAGE(%s%d:%s%d)", Character.toString((char) (coll + 67)), 2, Character.toString((char) (coll + 67)), row);
      }
      workbook.write();
   }
}
