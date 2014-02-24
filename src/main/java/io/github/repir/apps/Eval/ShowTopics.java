package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.TestSet.Topic;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Shows the topics from the test set in the config file
 * parameters: <configfile>
 * @author jeroen
 */
public class ShowTopics {

   public static Log log = new Log(ShowTopics.class);

   public static void main(String[] args) {
      Configuration conf = HDTools.readConfigNoMR( args, "" );
      Repository repository = new Repository(conf);
      TestSet testset = new TestSet(repository);
      TreeMap<Integer, Topic> sorted = new TreeMap<Integer, Topic>();
      sorted.putAll(testset.topics);
      for (Map.Entry<Integer, Topic> topic : sorted.entrySet()) {
         log.printf("%d %s", topic.getKey(), topic.getValue().query.trim());
      }
   }
}
