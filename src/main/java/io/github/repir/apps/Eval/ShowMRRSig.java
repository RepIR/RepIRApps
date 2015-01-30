package io.github.repir.apps.Eval;

import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricRR;
import io.github.repir.TestSet.ResultSets;
import io.github.repir.tools.lib.ArgsParser;
import java.io.IOException;

/**
 * calculates MAP and significance for given results
 * arguments: <configfile> <extension_baselinefile> { extension_resultsfile }
 * @author jeroen
 */
public class ShowMRRSig {

   public static Log log = new Log(ShowMRRSig.class);

   public static void main(String args[]) throws IOException {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {resultsext}");
      TestSet testset = new TestSet(new Repository(parsedargs.get("configfile")));
      testset.getQrels();
      testset.purgeTopics();
      log.info("valid topics %d", testset.topics.size());
      String systems[] = parsedargs.getStrings("resultsext");
      ResultSets resultsets = new ResultSets( new QueryMetricRR(), testset, systems);
      log.printf("baseline %f", resultsets.get(0).getMean());
      for (int i = 1; i < systems.length; i++) {
         log.printf("%s %f gain %f%% sig %f queries %d", systems[i], resultsets.get(i).getMean(), 
                 100 * (resultsets.get(i).getMean() - resultsets.get(0).getMean())/resultsets.get(0).getMean(),
                 resultsets.sigOver(0, i), resultsets.get(i).validqueries.size());
      }
   }
}
