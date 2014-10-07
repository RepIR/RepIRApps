package util;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Tuner.Parameter;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jeroen Vuurens
 */
public class StoreSolutionCross {

   public static Log log = new Log(StoreSolutionCross.class);

   public StoreSolutionCross(Repository repository) {
      Repository repositories[] = repository.getTuneRepositories();
      HashMap<Record, Record> results[] = new HashMap[repositories.length];
      for (int i = 0; i < repositories.length; i++) {
         results[i] = new HashMap<Record, Record>();
         ModelParameters modelparameters = ModelParameters.get(repository, repositories[i].configurationName());
         modelparameters.setDataBufferSize(1000000);
         modelparameters.openRead();
         for (Record r : modelparameters.getKeys()) {
            results[i].put(r, r);
         }
         modelparameters.closeRead();
      }

      double max[] = new double[repositories.length];
      Record maxrecord[] = new Record[repositories.length];
      for (int i = 0; i < repositories.length; i++) {
         for (Record r : results[i].values()) {
            double map = 0;
            for (int j = 0; j < repositories.length; j++) {
               if (j != i) {
                  Record rr = results[j].get(r);
                  if (rr != null)
                     map += rr.map;
                  else
                     log.fatal("Not all points tuned: ", rr);
               }
            }
            if (map > max[i]) {
               max[i] = map;
               maxrecord[i] = r;
            }
         }
      }

      for (int subset = 0; subset < repositories.length; subset++) {
         ArrayList<String> list = new ArrayList<String>();
         for (String p : repositories[subset].getFreeParameters().keySet()) {
            list.add(p + "=" + maxrecord[subset].parameters.get(p));
         }
         String confsetting = ArrayTools.concatStr(list, ",");

         Datafile configfile = new Datafile(repositories[subset].getParameterFile());
         configfile.openWrite();
         for (int topic : new TestSet(repositories[subset]).getTopicIDs()) {
            configfile.printf("query.%d=%s\n", topic, confsetting);
         }
         configfile.closeWrite();
      }
   }
}
