package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricNDCG;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;

/**
 * shows ndcg@10 for a set of resultsfile for the same test set
 * parameters: <configfile> { results_file_extension }
 * @author jeroen
 */
public class ShowNDCG10 {

   public static Log log = new Log(ShowNDCG10.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile resultext");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet(repository);
      ResultSet resultset = new ResultSet( new QueryMetricNDCG(10), testset, parsedargs.get("resultext"));
      log.printf("%f", resultset.getMean());
   }
}
