package io.github.repir.apps.Eval;

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
import io.github.repir.tools.Excel.ExcelCell;
import io.github.repir.tools.Excel.ExcelRange;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class SigToExcel {

   public static Log log = new Log(SigToExcel.class);
   int cps = 4;
   ExcelDoc workbook = new ExcelDoc("sig.xlsx");
   public String collections[];
   public String systems[];
   public ResultSet sets[];
   public String bas;

   public SigToExcel(String filename, String collections[], String systems[]) {
      workbook = new ExcelDoc(filename);
      this.bas = bas;
      this.collections = collections;
      this.systems = systems;
      sets = new ResultSet[collections.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(HDTools.readConfig(collections[coll]));
         sets[coll] = new ResultSet(new QueryMetricAP(), new TestSet(repository), systems);
         sets[coll].calulateMeasure();
         sets[coll].calculateSig();
      }

      ExcelSheet mapsheet = createSheetTable("map");

      TreeMap<Integer, String> output = new TreeMap<Integer, String>();
      for (int coll = 0; coll < collections.length; coll++) {
         ExcelCell baselineavg = mapsheet.createCell(coll + 1, 1);
         baselineavg.setFormatDouble(sets[coll].result[0].avg);
         for (int sys = 1; sys < systems.length; sys++) {
            ExcelCell sysavg = mapsheet.createCell(coll + 1, sys * cps + 2);
            sysavg.setFormatDouble(sets[coll].result[sys].avg);
            ExcelCell gain = sysavg.right();
            gain.setCellFormula("(%s - %s)/%s", sysavg, baselineavg, baselineavg);
            gain.format("perc2", "0.00%");
            ExcelCell sig = gain.right();
            sig.setFormatDouble(sets[coll].result[sys].sig);
            StringBuilder sb = new StringBuilder();
            for (int o = 1; o < systems.length; o++) {
               if (o != sys && sets[coll].result[sys].avg > sets[coll].result[o].avg && sets[coll].sigOver(o, sys) < 0.05) {
                  sb.append(systems[o]).append(" ");
               }
            }
            ExcelCell sysimprove = sig.right();
            sysimprove.set(sb.toString());
         }
      }

      ExcelRange range = mapsheet.createRange(1, 1, collections.length, 1);
      ExcelCell baselineavg = range.end.below();
      range.setAvg( baselineavg );
      for (int sys = 0; sys < systems.length; sys++) {
         int row = collections.length + 1;
         ExcelRange rangesys = mapsheet.createRange(1, (sys * cps) + 2, collections.length, (sys * cps) + 2 );
         ExcelCell rangeavg = rangesys.end.below();
         rangesys.setAvg(rangeavg);
         rangeavg.right().setCellFormula("(%s - %s)/%s", rangeavg, baselineavg, baselineavg);
      }
      workbook.write();
   }

   public static void main(String[] args) {
      ArgsParser ar = new ArgsParser( args , "collections systems");
      String collections[] = ar.get("collections").split(",");
      String systems[] = ar.get("systems").split(",");
      new SigToExcel("sig.xlsx", collections, systems);
   }

   public ExcelSheet createSheetTable(String sheetname) {
      ExcelSheet mapsheet = workbook.getSheet(sheetname);
      mapsheet.createCell(0, 0).set("testset");
      mapsheet.createCell(0, 1).set(systems[0]);
      for (int s = 1; s < systems.length; s++) {
         mapsheet.createCell(0, s * cps + 2).set( systems[s] );
      }
      for (int c = 0; c < collections.length; c++) {
         mapsheet.createCell(c + 1, 0).set( collections[c] );
      }
      return mapsheet;
   }
}
