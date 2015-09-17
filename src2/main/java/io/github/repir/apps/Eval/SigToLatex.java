package io.github.repir.apps.Eval;

import com.google.common.base.Strings;
import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.Metric.QueryMetricStatAP;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.TestSet.TestSet;
import io.github.htools.io.Datafile;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class SigToLatex {

    public static Log log = new Log(SigToLatex.class);
    String measures[] = {"MAP", "delta", "p-value"};
    public ResultSets sets[];
    public double[] totals, max;

    public SigToLatex(String filename, String collections[], String systems[]) throws IOException {
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
        df.printf("\\begin{tabular}{|l|l|%s}\n", Strings.repeat("r|", systems.length));

        df.printf("\\hline\n");
        df.printf("Collection & measure ");
        for (String system : systems) {
            df.printf("& \\multicolumn{1}{c|}{%s}", system);
        }
        df.printf("\\\\\n");

        for (int coll = 0; coll < collections.length; coll++) {
            df.printf("\\hline\n");
            for (int measure = 0; measure < measures.length; measure++) {
                if (measure == 0) {
                    df.printf("\\multirow{2}{*}{%s} ", collections[coll]);
                }
                df.printf("& %s ", measures[measure]);
                for (int sys = 0; sys < systems.length; sys++) {
                    switch (measure) {
                        case 0:
                            df.printf("& %s%.4f%s ", sets[coll].get(sys).getMean() == max[coll]?"\\textbf{":"",
                                                     sets[coll].get(sys).getMean(),
                                                     sets[coll].get(sys).getMean() == max[coll]?"}":"");
                            break;
                        case 1:
                            if (sys > 0) {
                                double gain = (sets[coll].get(sys).getMean() - sets[coll].get(0).getMean()) / sets[coll].get(0).getMean();
                                df.printf("& %+.1f\\%% ", 100 * gain);
                            } else 
                                df.printf("& ");
                            break;
                        case 2:
                            if (sys > 0) {
                                df.printf("& %.3f ", sets[coll].sigOver(0, sys));
                            } else 
                                df.printf("& ");
                    }
                }

                df.printf("\\\\\n");
            }
        }
//            StringBuilder sb = new StringBuilder();
//            for (int o = 1; o < systems.length; o++) {
//               if (o != sys && sets[coll].get(sys).getMean() > sets[coll].get(o).getMean() && sets[coll].sigOver(o, sys) < 0.05) {
//                  sb.append(o);
//               }
//            }
//            latex.set(coll, "comp" + sys, sb);
//         }
//      }

        df.printf("\\hline\n");
        df.printf("Average & %s & ", measures[1]);

        for (int sys = 1; sys < systems.length; sys++) {
            double gain = (totals[sys] - totals[0]) / totals[0];
            df.printf("& %+.1f\\%% ", 100 * gain);
        }
        df.printf("\\\\\n\\hline\n");
        df.printf("\\end{tabular}\n");
        df.printf("\\end{table}\n");
        df.close();
    }

    public static void main(String[] args) throws IOException {
        ArgsParser ar = new ArgsParser(args, "collections systems");
        String collections[] = ar.get("collections").split(",");
        String systems[] = ar.get("systems").split(",");
        new SigToLatex("sig.tex", collections, systems);
    }
}
