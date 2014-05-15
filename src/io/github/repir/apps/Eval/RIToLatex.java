package io.github.repir.apps.Eval;

import io.github.repir.TestSet.ResultSets;
import static io.github.repir.tools.Lib.PrintTools.*;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Latex.Tabular;
import io.github.repir.tools.Latex.Underline;
import io.github.repir.tools.Latex.Upperline;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.MathTools;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class RIToLatex {

   public static Log log = new Log(RIToLatex.class);
   int cps = 4;
   public String collections[];
   public String systems[];
   public ResultSets sets[];
   public double[] totals;

   public RIToLatex(String filename, String collections[], String systems[]) {
      String h = "|coll:-:-";
      for ( int i = 1; i < systems.length; i++ ) 
         h += sprintf("|RI%d:RI:%s:Decimal2", i, systems[i]);
      h+= "|";
      Tabular latex = new Tabular(h);
      this.collections = collections;
      this.systems = systems;
      sets = new ResultSets[collections.length];
      double ri[][] = new double[collections.length][systems.length];
      for (int coll = 0; coll < collections.length; coll++) {
         Repository repository = new Repository(collections[coll]);
         sets[coll] = new ResultSets(new QueryMetricAP(), new TestSet(repository), systems);
         
         for (int s = 1; s < systems.length; s++) 
            ri[coll][s] = sets[coll].riOver(0, s);
      }

      for (int coll = 0; coll < collections.length; coll++) {
         latex.set(coll, "coll", collections[coll]);
         for (int sys = 1; sys < systems.length; sys++) {
            latex.set(coll, "RI" + sys, ri[coll][sys]);
         }
      }

      int lastrow = collections.length;
      latex.set(lastrow, "coll", "Total");
      for (int sys = 1; sys < systems.length; sys++) {
         double avg = MathTools.Avg(ArrayTools.slice( ri, sys ));
         latex.set(lastrow, "RI" + sys, avg);
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
      new RIToLatex("ri.tex", collections, systems);
   }
}
