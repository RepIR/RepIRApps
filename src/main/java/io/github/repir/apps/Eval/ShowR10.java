package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.QueryMetricRecall;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;

/**
 * Show Recall@10 for set of results files to same test set
 * parameters: <configfile> { results_file_ext }
 * @author jeroen
 */
public class ShowR10 {

   public static Log log = new Log(ShowR10.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {resultsext}");
      TestSet testset = new TestSet(new Repository(parsedargs.get("configfile")));
      ResultSet resultset = new ResultSet( new QueryMetricRecall( 10 ), testset, parsedargs.getRepeatedGroup());
      resultset.calulateMeasure();
      resultset.calculateSig();
      log.printf("baseline %f", resultset.result[0].avg);
      for (int i = 1; i < args.length; i++) {
         log.printf("%s %f gain %f%% sig %f", args[i], resultset.result[i].avg, 
                 100 * (resultset.result[i].avg - resultset.result[0].avg)/resultset.result[0].avg,
                 resultset.result[i].sig);
      }
   }
}
