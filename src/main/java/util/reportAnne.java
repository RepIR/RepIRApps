package util;

import java.util.ArrayList;
import java.util.Map;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.Excel.ExcelDoc;
import static io.github.repir.tools.Lib.PrintTools.*;
import static io.github.repir.tools.Excel.ExcelSheet.*;
import io.github.repir.tools.Excel.ExcelSheet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.TestSet;
import java.util.TreeMap;
import java.util.TreeSet;
import io.github.repir.TestSet.Topic;
import io.github.repir.tools.Excel.ExcelCell;
import io.github.repir.tools.Excel.ExcelRange;
import io.github.repir.tools.Lib.ArrayTools;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class reportAnne {

   public static Log log = new Log(reportAnne.class);
   int cps = 4;
   ExcelDoc workbook = new ExcelDoc("repAnne.xlsx");
   public String systems[];
   public ResultSet results;

   public reportAnne(String filename, String collection, String systems[]) {
      workbook = new ExcelDoc(filename);
      ExcelSheet mainsheet = workbook.getSheet("map");

      Repository repository = new Repository(HDTools.readConfig(collection));
      ArrayList<String> syslist = new ArrayList<String>();
      ArrayList<ExcelSheet> mappen = new ArrayList<ExcelSheet>();
      ArrayList<Integer> topics = new TestSet(repository).getTopicIDs();
      for (int coll = 0; coll < systems.length; coll++) {
         String collection1 = systems[coll].substring(0, systems[coll].lastIndexOf('.'));
         String suffix = systems[coll].substring( systems[coll].lastIndexOf('.') );
         Repository repo1 = new Repository(HDTools.readConfig(collection1));
         String sysname = repo1.getTestsetName() + suffix;
         sysname = sysname.substring(sysname.indexOf('.')+1);
         syslist.add( sysname );
         log.info("%s %s", repo1.getTestsetName(), suffix);
         ExcelSheet sheet = workbook.getSheet( sysname );
         mappen.add(sheet);
         sheet.getCell(0, 0).setCellValue("topic nr");
         sheet.getCell(0, 1).setCellValue("query");
         TestSet ts = new TestSet( repo1 );
         for (int i = 0; i < topics.size(); i++) {
            sheet.getCell( i + 1, 0).setCellValue(topics.get(i));
            sheet.getCell( i + 1, 1).setCellValue(ts.topics.get(topics.get(i)).query);
         }
      }
      systems = syslist.toArray( new String[ syslist.size() ]);
      
         results = new ResultSet(new QueryMetricAP(), new TestSet(repository), systems);
         results.calulateMeasure();
         results.calculateSig();
      
      for (int sys = 0; sys < systems.length; sys++) {
         ExcelSheet sheet = mappen.get(sys);
         for (int t = 0; t < topics.size(); t++) {
            ExcelCell baselineavg = sheet.createCell( 1 + t, 2);
            baselineavg.setFormatDouble(results.result[0].queryresult[t]);
            ExcelCell sysavg = sheet.createCell( 1 + t, 3);
            sysavg.setFormatDouble(results.result[sys].queryresult[t]);
            ExcelCell gain = sysavg.right();
            gain.setCellFormula("(%s - %s)/%s", sysavg, baselineavg, baselineavg);
            gain.format("perc2", "0.00%");
         }
         ExcelRange range = sheet.createRange(1, 2, topics.size(), 2);
         ExcelCell baselineavg = range.end.below();
         range.setAvg(baselineavg);
         ExcelRange sysrange = sheet.createRange(1, 3, topics.size(), 3);
         ExcelCell sysavg = sysrange.end.below();
         sysrange.setAvg(sysavg);
         sysrange.setAvg(mainsheet.getCell(1, 1 + sys));
      }

      for (int sys = 0; sys < systems.length; sys++) {
         mainsheet.getCell(0, 1 + sys).setCellValue(systems[sys]);
      }
      workbook.write();
   }

   public static void main(String[] args) {
      String systems[] = ArrayTools.subArray(args, 1);
      new reportAnne("repAnne.xlsx", args[0], systems);
   }
}
