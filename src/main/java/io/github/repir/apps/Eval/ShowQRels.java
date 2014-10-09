package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.Qrel.QRel;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.Log;
import io.github.repir.MapReduceTools.RRConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Shows the topics from the test set in the config file parameters:
 * <configfile>
 *
 * @author jeroen
 */
public class ShowQRels {

   public static Log log = new Log(ShowQRels.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args, "[topic]");
      RRConfiguration conf = repository.getConfiguration();
      TestSet testset = new TestSet(repository);
      TreeMap<Integer, QRel> sorted = new TreeMap();
      sorted.putAll(testset.getQrels());
      if (conf.containsKey("topic")) {
         TreeSet<String> s = new TreeSet<String>(sorted.get(conf.getInt("topic", -1)).relevance.keySet());
         log.printf("%s %s", conf.get("topic"), s);
      } else {
         for (Map.Entry<Integer, QRel> topic : sorted.entrySet()) {
            log.printf("%d %s", topic.getKey(), topic.getValue().relevance);
         }
      }
   }
}
