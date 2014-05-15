package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Latex.Tabular;
import io.github.repir.tools.Latex.Underline;
import io.github.repir.tools.Latex.Upperline;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import static io.github.repir.tools.Lib.PrintTools.*;

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
   public ResultSets sets[];
   public double[] totals;

   public SigToLatex(String filename, String collections[], String systems[]) {
      String h = "|coll:-:-|map:map:" + systems[0] + ":FixedDecimal3";
      for ( int i = 1; i < systems.length; i++ ) 
         h += sprintf("|map%d:map:%s:FixedDecimal3,gain%d:::Percentage1,sig%d:sig::Precision3,comp%d:::Superscript", i, systems[i], i, i, i);
      h+= "|";
      Tabular latex = new Tabular(h);
      this.collections = collections;
      this.systems = systems;
      totals = new double[systems.length];
      sets = new ResultSets[collections.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(collections[coll]);
         sets[coll] = new ResultSets(new QueryMetricAP(), new TestSet(repository), systems);
      }

      for (int coll = 0; coll < collections.length; coll++) {
         totals[0] += sets[coll].get(0).getMean();
         latex.set(coll, "coll", collections[coll]);
         latex.set(coll, "map", sets[coll].get(0).getMean());
         for (int sys = 1; sys < systems.length; sys++) {
            totals[sys] += sets[coll].get(sys).getMean();
            double gain = (sets[coll].get(sys).getMean() - sets[coll].get(0).getMean())/sets[coll].get(0).getMean();
            latex.set(coll, "map" + sys, sets[coll].get(sys).getMean());
            latex.set(coll, "gain" + sys, gain);
            latex.set(coll, "sig" + sys, sets[coll].sigOver(0, sys));
            
            StringBuilder sb = new StringBuilder();
            for (int o = 1; o < systems.length; o++) {
               if (o != sys && sets[coll].get(sys).getMean() > sets[coll].get(o).getMean() && sets[coll].sigOver(o, sys) < 0.05) {
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
