package io.github.repir.apps.Retrieve;

import io.github.repir.TestSet.TestSet;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.TestSet.ResultFile;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Retrieve all topics in the test set, and write results to a file
 * parameters: <configfile> <outputfile>
 * @author jeroen
 */
public class TestSetDontSplit extends Configured implements Tool {

   public static Log log = new Log(TestSetDontSplit.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "resultsfileext");
      HDTools.setPriorityHigh(conf);
      System.exit( HDTools.run(conf, new TestSetDontSplit()));
   }

   @Override
   public int run(String[] args) throws Exception {
      Repository repository = new Repository(getConf());
      RetrieverMR retriever = new RetrieverMR(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      ArrayList<Query> results = retriever.retrieveQueue();
      ResultFile out = new ResultFile(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.getConfigurationString("resultsfileext")));
      out.writeresults(results);
      return 0;
   }
}
