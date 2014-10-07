package util;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 *
 * @author Jeroen Vuurens
 */
public class StoreSolutionFold {

   public static Log log = new Log(StoreSolutionFold.class);
   int folds = 10;

   public StoreSolutionFold(Repository repository) {
      HashMap<Record, Record> results[] = new HashMap[folds];
         ModelParameters modelparameters = ModelParameters.get(repository, repository.configurationName());
      modelparameters.setDataBufferSize(1000000);
      modelparameters.openRead();
      for (int fold = 0;fold < folds; fold++) 
         results[fold] = new HashMap<Record, Record>();
      for (Record r : modelparameters.getKeys()) {
         int fold = Integer.parseInt(r.parameters.get("fold"));
         r.parameters.remove("fold");
         results[fold].put(r, r);
      }
      modelparameters.closeRead();

      double max[] = new double[folds];
      Record maxrecord[] = new Record[folds];
      for (int i = 0; i < folds; i++) {
         for (Record r : results[i].values()) {
            double map = 0;
            for (int j = 0; j < folds; j++) {
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

      TestSet testset = new TestSet(repository);
      testset.purgeTopics();
      TreeSet<Integer> topics = new TreeSet<Integer>(testset.topics.keySet());
      Datafile configfile = new Datafile(repository.getParameterFile());
      configfile.openWrite();
      for (int fold = 0; fold < folds; fold++) {
         int topicstart = topics.first() + fold * folds;
         int topicend = topics.first() + (fold + 1) * folds;
         ArrayList<String> list = new ArrayList<String>();
         for (String p : repository.getFreeParameters().keySet()) {
            list.add(p + "=" + maxrecord[fold].parameters.get(p));
         }
         String confsetting = ArrayTools.concatStr(list, ",");

         for (int topic = topicstart; topic < topicend; topic++) {
            configfile.printf("query.%d=%s\n", topic, confsetting);
         }
      }
      configfile.closeWrite();
   }
}
