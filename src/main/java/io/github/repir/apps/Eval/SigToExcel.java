package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Excel.ExcelCell;
import io.github.repir.tools.Excel.ExcelDoc;
import io.github.repir.tools.Excel.ExcelRange;
import io.github.repir.tools.Excel.ExcelSheet;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import java.util.TreeMap;

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
   public ResultSets sets[];
   public String bas;

   public SigToExcel(String filename, String collections[], String systems[]) {
      workbook = new ExcelDoc(filename);
      this.bas = bas;
      this.collections = collections;
      this.systems = systems;
      sets = new ResultSets[collections.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(collections[coll]);
         sets[coll] = new ResultSets(new QueryMetricAP(), new TestSet(repository), systems);
      }

      ExcelSheet mapsheet = createSheetTable("map");

      TreeMap<Integer, String> output = new TreeMap<Integer, String>();
      for (int coll = 0; coll < collections.length; coll++) {
         ExcelCell baselineavg = mapsheet.createCell(coll + 1, 1);
         baselineavg.setFormatDouble(sets[coll].get(0).getMean());
         for (int sys = 1; sys < systems.length; sys++) {
            ExcelCell sysavg = mapsheet.createCell(coll + 1, sys * cps + 2);
            sysavg.setFormatDouble(sets[coll].get(sys).getMean());
            ExcelCell gain = sysavg.right();
            gain.setCellFormula("(%s - %s)/%s", sysavg, baselineavg, baselineavg);
            gain.format("perc2", "0.00%");
            ExcelCell sig = gain.right();
            sig.setFormatDouble(sets[coll].sigOver(0, sys));
            StringBuilder sb = new StringBuilder();
            for (int o = 1; o < systems.length; o++) {
               if (o != sys && sets[coll].get(sys).getMean() > sets[coll].get(o).getMean() && sets[coll].sigOver(o, sys) < 0.05) {
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
      ArgsParser ar = new ArgsParser( args , "collections systems [filename]");
      String collections[] = ar.get("collections").split(",");
      String systems[] = ar.get("systems").split(",");
      String filename = ar.exists("filename")?ar.get("filename"):"sig.xlsx";
      new SigToExcel(filename, collections, systems);
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
