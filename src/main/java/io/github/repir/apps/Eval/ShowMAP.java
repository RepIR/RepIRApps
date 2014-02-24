package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.ResultStat;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;

/**
 * shows MAP of a resultsfile
 * arguments <configfile> <result_file_extension>
 * @author jeroen
 */
public class ShowMAP {

   public static Log log = new Log(ShowMAP.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile resultext");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet(repository);
      ResultStat resultset = new ResultStat( new QueryMetricAP(), testset, parsedargs.get("resultext"));
      resultset.calculateMeasure();
      log.printf("%f", resultset.avg);
   }
}
