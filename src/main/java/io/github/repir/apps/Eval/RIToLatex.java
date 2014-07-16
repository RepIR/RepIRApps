package io.github.repir.apps.Eval;

import com.google.common.base.Strings;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
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
    public ResultSets sets[];
    public double[] max;

    public RIToLatex(String filename, String collections[], String systems[]) {
        sets = new ResultSets[collections.length];
        max = new double[collections.length];
        double ri[][] = new double[collections.length][systems.length];
        for (int coll = 0; coll < collections.length; coll++) {
            Repository repository = new Repository(collections[coll]);
            sets[coll] = new ResultSets(new QueryMetricAP(), new TestSet(repository), systems);

            for (int s = 1; s < systems.length; s++) {
                ri[coll][s] = sets[coll].riOver(0, s);
                max[coll] = Math.max(max[coll], ri[coll][s]);
            }
        }

        Datafile df = new Datafile(filename);
        df.printf("\\begin{table}\n");
        df.printf("\\caption{}\n");
        df.printf("\\label{table}\n");
        df.printf("\\begin{tabular}{|l|%s}\n", Strings.repeat("r|", systems.length));

        df.printf("\\hline\n");
        for (int system = 1; system < systems.length; system++) {
            df.printf("& \\multicolumn{1}{c|}{%s}", systems[system]);
        }
        df.printf("\\\\\n");
        df.printf("\\hline\n");
        for (int coll = 0; coll < collections.length; coll++) {
            df.printf("%s", collections[coll]);
            for (int sys = 1; sys < systems.length; sys++) {
                df.printf(" & %s%.2f%s",
                        ri[coll][sys] == max[coll] ? "\\textbf{" : "",
                        ri[coll][sys],
                        ri[coll][sys] == max[coll] ? "}" : "");
            }
            df.printf("\\\\\n");

        }

        df.printf("\\hline\n");
        int lastrow = collections.length;
        df.printf("Average");
        for (int sys = 1; sys < systems.length; sys++) {
            double avg = MathTools.avg(ArrayTools.slice(ri, sys));
            df.printf(" & %.2f", avg);
        }
        df.printf("\\\\\n\\hline\n");
        df.printf("\\end{tabular}\n");
        df.printf("\\end{table}\n");
        df.close();
    }

    public static void main(String[] args) {
        ArgsParser ar = new ArgsParser(args, "collections systems");
        String collections[] = ar.get("collections").split(",");
        String systems[] = ar.get("systems").split(",");
        new RIToLatex("ri.tex", collections, systems);
    }
}
