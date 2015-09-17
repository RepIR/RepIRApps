package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 * shows MAP of a resultsfile
 * arguments <configfile> <result_file_extension>
 * @author jeroen
 */
public class ShowMAP {

   public static Log log = new Log(ShowMAP.class);

   public static void main(String args[]) throws IOException {
      ArgsParser parsedargs = new ArgsParser(args, "configfile resultext");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet(repository);
      ResultSet resultset = new ResultSet( new QueryMetricAP(), testset, parsedargs.get("resultext"));
      log.printf("%f", resultset.getMean());
   }
}
