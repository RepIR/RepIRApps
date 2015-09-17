package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.TestSet;
import io.github.htools.lib.Log;
import java.util.Map;
import java.util.TreeMap;
import io.github.repir.TestSet.Topic.TestSetTopic;
import io.github.repir.MapReduceTools.RRConfiguration;

/**
 * Shows the topics from the test set in the config file
 * parameters: <configfile>
 * @author jeroen
 */
public class ShowDescriptions {

   public static Log log = new Log(ShowDescriptions.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args, "");
      RRConfiguration conf = repository.getConf();
      repository.getConf().set("testset.topicreader", "TrecTopicDescription");
      TestSet testset = new TestSet(repository);
      TreeMap<Integer, TestSetTopic> sorted = new TreeMap<Integer, TestSetTopic>();
      sorted.putAll(testset.topics);
      for (Map.Entry<Integer, TestSetTopic> topic : sorted.entrySet()) {
         log.printf("%d %s", topic.getKey(), topic.getValue().query.trim());
      }
   }
}
