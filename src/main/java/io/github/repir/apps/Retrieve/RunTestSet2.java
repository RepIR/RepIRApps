package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFile;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.RetrieverMR.IRHDJobManager;
import io.github.repir.RetrieverMR.IRHDJobThread;
import io.github.repir.RetrieverMR.RetrieverMRCallback;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArrayTools;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSet2 extends IRHDJobThread {

   public static Log log = new Log(RunTestSet2.class);
   boolean done = false;
   
   public RunTestSet2( Configuration conf ) {
      super( conf );
   }
   
   public static void main(String[] args) throws Exception {
      ArrayList<RunTestSet2> runs = new ArrayList<RunTestSet2>();
      for (int i = 0; i < args.length; i+=2) {
         Configuration conf = HDTools.readConfig(ArrayTools.subArray(args, i, 2), "resultsfileext");
         runs.add( new RunTestSet2( conf ));
      }
      boolean done = false;
      while (IRHDJobManager.get().numberRunningJobs() > 0) {
         for (int i = runs.size() -1; i >= 0; i--) {
            RunTestSet2 r = runs.get(i);
            if (r.done)
               runs.remove(i);
         }
         Thread.sleep(200);
      }
      IRHDJobManager.shutdown();
   }

   public void jobWasSuccesful(ArrayList<Query> results) {
      ResultFile out = new ResultFile(repository, io.github.repir.TestSet.TestSet.getResultsFile(repository, configuration.get("resultsfileext")));
      out.writeresults(results);
      log.info("job succesful");
      done = true;
   }

   public void JobFailed() {
      log.info("job failed");
      done = true;
   }
   
   
}
