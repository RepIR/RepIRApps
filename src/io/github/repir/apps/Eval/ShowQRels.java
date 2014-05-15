package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Configuration;
import io.github.repir.tools.Lib.Log;
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
      Configuration conf = new Configuration(args, "[topic]");
      Repository repository = new Repository(conf);
      TestSet testset = new TestSet(repository);
      TreeMap<Integer, HashMap<String, Integer>> sorted = new TreeMap<Integer, HashMap<String, Integer>>();
      sorted.putAll(testset.getQrels());
      if (conf.containsKey("topic")) {
         TreeSet<String> s = new TreeSet<String>(sorted.get(Integer.parseInt(conf.get("topic"))).keySet());
         log.printf("%s %s", conf.get("topic"), s);
      } else {
         for (Map.Entry<Integer, HashMap<String, Integer>> topic : sorted.entrySet()) {
            log.printf("%d %s", topic.getKey(), topic.getValue());
         }
      }
   }
}
