package io.github.repir.apps.Pig;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class CreateDoc {

   public static Log log = new Log(CreateDoc.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      NullPigJob job = new NullPigJob(repository);
      job.setMapperClass(DocMap.class);
      job.submit();
      job.waitForCompletion(true);
   }
}
