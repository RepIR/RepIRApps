package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFile;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import io.github.repir.RetrieverM.RetrieverM;
import io.github.repir.RetrieverM.RetrieverMInputFormat;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSetM extends Configured implements Tool {

   public static Log log = new Log(RunTestSetM.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "resultsfileext");
      HDTools.setPriorityHigh(conf);
      System.exit( HDTools.run(conf, new RunTestSetM()));
   }

   @Override
   public int run(String[] args) throws Exception {
      Repository repository = new Repository((Configuration)getConf());
      RetrieverM retriever = new RetrieverM(repository);
      TestSet testset = new TestSet( repository );
      retriever.addQueue(testset.getQueries(retriever));
      RetrieverMInputFormat.setIndex(repository);
      ResultFile out = new ResultFile(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, repository.getConfigurationString("resultsfileext")));
      ArrayList<Query> results = retriever.retrieveQueue();
      out.writeresults(results);
      return 0;
   }
}
