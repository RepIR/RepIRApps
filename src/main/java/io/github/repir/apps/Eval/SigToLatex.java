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
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Excel.ExcelCell;
import io.github.repir.tools.Excel.ExcelRange;
import io.github.repir.tools.Latex.Tabular;
import io.github.repir.tools.Latex.Underline;
import io.github.repir.tools.Latex.Upperline;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.MathTools;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class SigToLatex {

   public static Log log = new Log(SigToLatex.class);
   int cps = 4;
   public String collections[];
   public String systems[];
   public ResultSet sets[];
   public double[] totals;

   public SigToLatex(String filename, String collections[], String systems[]) {
      String h = "|coll:-:-|map:map:" + systems[0] + ":Decimal4";
      for ( int i = 1; i < systems.length; i++ ) 
         h += sprintf("|map%d:map:%s:Decimal4,gain%d:::Percentage1,sig%d:sig::Precision4,comp%d:::Superscript", i, systems[i], i, i, i);
      h+= "|";
      Tabular latex = new Tabular(h);
      this.collections = collections;
      this.systems = systems;
      totals = new double[systems.length];
      sets = new ResultSet[collections.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(HDTools.readConfig(collections[coll]));
         sets[coll] = new ResultSet(new QueryMetricAP(), new TestSet(repository), systems);
         sets[coll].calulateMeasure();
         sets[coll].calculateSig();
      }

      for (int coll = 0; coll < collections.length; coll++) {
         totals[0] += sets[coll].result[0].avg;
         latex.set(coll, "coll", collections[coll]);
         latex.set(coll, "map", sets[coll].result[0].avg);
         String s = collections[coll] + "," + sprintf("%.4f", sets[coll].result[0].avg);
         for (int sys = 1; sys < systems.length; sys++) {
            totals[sys] += sets[coll].result[sys].avg;
            double gain = (sets[coll].result[sys].avg - sets[coll].result[0].avg)/sets[coll].result[0].avg;
            latex.set(coll, "map" + sys, sets[coll].result[sys].avg);
            latex.set(coll, "gain" + sys, gain);
            latex.set(coll, "sig" + sys, sets[coll].result[sys].sig);
            
            StringBuilder sb = new StringBuilder();
            for (int o = 1; o < systems.length; o++) {
               if (o != sys && sets[coll].result[sys].avg > sets[coll].result[o].avg && sets[coll].sigOver(o, sys) < 0.05) {
                  sb.append(o);
               }
            }
            latex.set(coll, "comp" + sys, sb);
         }
      }

      int lastrow = collections.length;
      latex.set(lastrow, "coll", "Total");
      for (int sys = 1; sys < systems.length; sys++) {
         double gain = (totals[sys] - totals[0])/totals[0];
         latex.set(lastrow, "gain" + sys, gain);
         
      }
      latex.getRow(lastrow).addModifier(new Underline());
      latex.getRow(lastrow).addModifier(new Upperline());
      Datafile file = new Datafile(filename);
      file.printf("%s", latex.toString());
      file.close();
   }

   public static void main(String[] args) {
      ArgsParser ar = new ArgsParser( args , "collections systems");
      String collections[] = ar.get("collections").split(",");
      String systems[] = ar.get("systems").split(",");
      new SigToLatex("sig.tex", collections, systems);
   }
}
