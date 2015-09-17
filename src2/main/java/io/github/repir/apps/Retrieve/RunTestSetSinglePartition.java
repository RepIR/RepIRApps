package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.htools.lib.Log;
import io.github.repir.TestSet.ResultFileRR;
import java.util.ArrayList;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSetSinglePartition {

   public static Log log = new Log(RunTestSetSinglePartition.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "partition resultsfileext");
      RRConfiguration conf = repository.getConf();
      conf.setBoolean("inputformat.cansplit", false);
      repository.setPartitions(conf.getInt("partition", 1));
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      ResultFileRR out = new ResultFileRR(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      log.info("outfile %s", out.getDatafile().getCanonicalPath());
      ArrayList<Query> results = retriever.retrieveQueue();
      out.writeresults(results);
   }
}
