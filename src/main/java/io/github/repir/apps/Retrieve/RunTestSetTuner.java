package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.MapReduceTools.Configuration;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.Retriever.Tuner.Retriever;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class RunTestSetTuner {

   public static Log log = new Log(RunTestSetTuner.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration(args, "");
      conf.setBoolean("inputformat.cansplit", false);
      //HDTools.setPriorityHigh(conf);
      Repository repository = new Repository(conf);
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet(repository);
      ArrayList<Variant> variants = retriever.getVariants();
      if (variants.size() > 0) {
         retriever.addQueue(testset.getQueries(retriever));
         for (Query q : retriever.queue) {
            q.initVariants();
            for (Variant v : variants) {
               q.addVariant(v);
            }
         }
         retriever.doJobDontWait(retriever.queue);
         log.info("%s started", conf.get("rr.conf"));
      }
   }
}
