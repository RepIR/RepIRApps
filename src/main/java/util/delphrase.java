package util;

import java.util.ArrayList;
import io.github.repir.Strategy.Collector.CollectorPhrase;
import io.github.repir.Repository.PhraseStats;
import io.github.repir.Repository.PhraseStats.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.FeatureProximity;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class delphrase {

   public static Log log = new Log(delphrase.class);
   
   public static void main(String[] args) {
      Configuration configuration = HDTools.readConfig(args, "{query}");
      Repository repository = new Repository(configuration);
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', configuration.getStrings("query")));
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.prepareAggregation();
      ArrayList<Record> list = new ArrayList<Record>();
      for (GraphNode n : rm.root.containedfeatures) {
         if (n instanceof FeatureProximity) {
            log.info("phrase s", n);
            //n.resetFeatureValues();
            ((FeatureProximity)n).setupCollector();
            CollectorPhrase collector = ((FeatureProximity)n).collector;
            Record r = collector.createRecord();
            list.add(r);
         }
      }
      PhraseStats f = (PhraseStats) repository.getFeature("PhraseStats");
      f.remove(list);
      list(f);
   }
   
   public static void list(PhraseStats f) {
      f.openRead();
      for (PhraseStats.Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
