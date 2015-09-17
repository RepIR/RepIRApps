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
public class RecoverTestSet {

   public static Log log = new Log(RecoverTestSet.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "resultsfileext path");
      RRConfiguration conf = repository.getConf();
      String path = conf.get("retriever.tempdir", "") + repository.configuredString("path");
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet( repository );
      ResultFileRR out = new ResultFileRR(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      ArrayList<Query> results = retriever.recoverQueries(testset.getQueries(retriever), path);
      out.writeresults(results);
   }
}
