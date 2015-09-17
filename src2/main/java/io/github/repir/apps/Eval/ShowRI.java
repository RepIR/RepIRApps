package io.github.repir.apps.Eval;

import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.htools.lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.TestSet.ResultSets;
import io.github.htools.lib.ArgsParser;
import java.io.IOException;

/**
 * calculates MAP and significance for given results
 * arguments: <configfile> <extension_baselinefile> { extension_resultsfile }
 * @author jeroen
 */
public class ShowRI {

   public static Log log = new Log(ShowRI.class);

   public static void main(String args[]) throws IOException {
      ArgsParser parsedargs = new ArgsParser(args, "configfile baselineext {resultsext}");
      TestSet testset = new TestSet(new Repository(parsedargs.get("configfile")));
      testset.getQrels();
      testset.purgeTopics();
      log.info("valid topics %d", testset.topics.size());
      String systems[] = parsedargs.getStrings("resultsext");
      ResultSets resultset = new ResultSets( new QueryMetricAP(), testset, systems);
      log.printf("baseline %f", resultset.get(0).getMean());
      for (int i = 1; i < systems.length; i++) {
         log.printf("%s %f gain %f%% RI %f queries %d", systems[i], resultset.get(i).getMean(), 
                 100 * (resultset.get(i).getMean() - resultset.get(0).getMean())/resultset.get(0).getMean(),
                 resultset.riOver(0, i), resultset.get(i).validqueries.size());
      }
   }
}
