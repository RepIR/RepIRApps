package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFile;
import java.util.ArrayList;
import io.github.repir.Repository.Configuration;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RecoverTestSet {

   public static Log log = new Log(RecoverTestSet.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration(args, "resultsfileext path");
      Repository repository = new Repository(conf);
      String path = conf.get("retriever.tempdir", "") + repository.configuredString("path");
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet( repository );
      ResultFile out = new ResultFile(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.configuredString("resultsfileext")));
      ArrayList<Query> results = retriever.recoverQueries(testset.getQueries(retriever), path);
      out.writeresults(results);
   }
}
