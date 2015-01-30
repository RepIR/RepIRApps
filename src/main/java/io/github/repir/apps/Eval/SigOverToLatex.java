package io.github.repir.apps.Eval;

import com.google.common.base.Strings;
import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.Metric.QueryMetricStatAP;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.ArgsParser;
import io.github.repir.tools.lib.Log;
import java.io.IOException;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class SigOverToLatex {

    public static Log log = new Log(SigOverToLatex.class);
    String measures[] = {"MAP", "delta", "p-value"};
    public ResultSets sets[];
    public double[] totals, max;

    public SigOverToLatex(String filename, String collections[], String systems[]) throws IOException {
        totals = new double[systems.length];
        sets = new ResultSets[collections.length];
        max = new double[collections.length];
        for (int coll = 0; coll < collections.length; coll++) {
            Repository repository = new Repository(collections[coll]);
            sets[coll] = new ResultSets(new QueryMetricStatAP(), new TestSet(repository), systems);
        }

        for (int coll = 0; coll < collections.length; coll++) {
            totals[0] += sets[coll].get(0).getMean();
            for (int sys = 1; sys < systems.length; sys++) {
                totals[sys] += sets[coll].get(sys).getMean();
                max[coll] = Math.max(max[coll], sets[coll].get(sys).getMean());
            }
        }

        Datafile df = new Datafile(filename);
        df.printf("\\begin{table}\n");
        df.printf("\\caption{}\n");
        df.printf("\\label{table}\n");
        df.printf("\\begin{tabular}{|l|%s}\n", Strings.repeat("l|", systems.length));

        df.printf("\\hline\n");
        df.printf(" ");
        for (String system : systems) {
            df.printf("& \\multicolumn{1}{c|}{%s}", system);
        }

        df.printf("\\\\\n");
        for (int sys1 = 0; sys1 < systems.length; sys1++) {
            
            df.printf("\\hline\n");
                df.printf("%s ", systems[sys1]);
                for (int sys = 0; sys < systems.length; sys++) {
                    df.printf("& ");
                    if (sys != sys1) {
                       for (int coll = 0; coll < collections.length; coll++) {
                          if (sets[coll].get(sys1).getMean() > sets[coll].get(sys).getMean() && sets[coll].sigOver(sys, sys1) < 0.05)
                              df.printf("%s ", collections[coll]);
                       }
                    }
                }
               df.printf("\\\\\n");

        }

        df.printf("\\hline\n");
        df.printf("\\end{tabular}\n");
        df.printf("\\end{table}\n");
        df.close();
    }

    public static void main(String[] args) throws IOException {
        ArgsParser ar = new ArgsParser(args, "collections systems");
        String collections[] = ar.get("collections").split(",");
        String systems[] = ar.get("systems").split(",");
        new SigOverToLatex("sigover.tex", collections, systems);
    }
}
