package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFileRR;
import java.util.ArrayList;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSet {

   public static Log log = new Log(RunTestSet.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "resultsfileext");
      RRConfiguration conf = repository.getConfiguration();
       Retriever retriever = new Retriever(repository);
       TestSet testset = new TestSet( repository );
       retriever.addQueue(testset.getQueries(retriever));
       repository.getConfiguration().setBoolean("inputformat.cansplit", true);
      ResultFileRR out = new ResultFileRR(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      log.info("outfile %s", out.getDatafile().getFullPath());
      ArrayList<Query> results = retriever.retrieveQueue();
      out.writeresults(results);
   }
}
