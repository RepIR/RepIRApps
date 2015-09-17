package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;
import io.github.repir.TestSet.ResultFileRR;
import java.util.ArrayList;
import io.github.repir.Retriever.MapOnly.RetrieverM;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSetM {

   public static Log log = new Log(RunTestSetM.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "resultsfileext");
      RetrieverM retriever = new RetrieverM(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      ResultFileRR out = new ResultFileRR(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      ArrayList<Query> results = retriever.retrieveQueue();
      out.writeresults(results);
   }
}
