package io.github.repir.apps.Retrieve;

import io.github.repir.TestSet.TestSet;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.TestSet.ResultFileRR;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Retrieve all topics in the test set, and write results to a file
 * parameters: <configfile> <outputfile>
 * @author jeroen
 */
public class TestSetDontSplit {

   public static Log log = new Log(TestSetDontSplit.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "resultsfileext");
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      ArrayList<Query> results = retriever.retrieveQueue();
      ResultFileRR out = new ResultFileRR(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      out.writeresults(results);
   }
}
