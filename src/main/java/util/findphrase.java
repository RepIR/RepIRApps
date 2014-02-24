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
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class findphrase {

   public static Log log = new Log(findphrase.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {query}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', parsedargs.getRepeatedGroup()));
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.prepareAggregation();
      ArrayList<Record> list = new ArrayList<Record>();
      for (GraphNode n : rm.root.containedfeatures) {
         if (n instanceof FeatureProximity) {
            ((FeatureProximity)n).setupCollector();
            CollectorPhrase collector = ((FeatureProximity)n).collector;
            Record r = collector.createRecord();
            list.add(r);
         }
      }
      PhraseStats f = (PhraseStats) repository.getFeature("PhraseStats");
      for (Record r : list) {
         r = (Record)f.find(r);
         if (r != null)
            log.info("found %s", r);
      }
   }
   
   public static void list(PhraseStats f) {
      f.openRead();
      for (Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
