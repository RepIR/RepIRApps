package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.TestSet.TestSet;
import io.github.repir.TestSet.Topic.TestSetTopic;
import io.github.repir.TestSet.Topic.TestSetTopicSession;
import io.github.repir.tools.Lib.Log;
import java.util.Map;
import java.util.TreeMap;

/**
 * Shows the topics from the test set in the config file
 * parameters: <configfile>
 * @author jeroen
 */
public class ShowTopicsSession {

   public static Log log = new Log(ShowTopicsSession.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args);
      TestSet testset = new TestSet(repository);
      TreeMap<Integer, TestSetTopic> sorted = new TreeMap<Integer, TestSetTopic>();
      sorted.putAll(testset.topics);
      for (Map.Entry<Integer, TestSetTopic> topic : sorted.entrySet()) {
         TestSetTopicSession s = (TestSetTopicSession) topic.getValue();
         log.printf("%d %s", topic.getKey(), topic.getValue().query.trim());
         log.info("queries: %s", s.priorqueries);
         log.info("clicked: %s", s.clickeddocuments);
         log.info("unclicked: %s", s.unclickeddocuments);
         log.info("unseen: %s", s.unseen);
      }
   }
}
