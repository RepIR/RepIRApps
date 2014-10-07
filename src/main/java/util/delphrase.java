package util;

import java.util.ArrayList;
import io.github.repir.Strategy.Collector.CollectorProximity;
import io.github.repir.Repository.ProximityStats;
import io.github.repir.Repository.ProximityStats.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.Strategy.Operator.ProximityOperator;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class delphrase {

   public static Log log = new Log(delphrase.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args, "{query}");
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', repository.configuredStrings("query")));
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.prepareAggregation();
      ArrayList<Record> list = new ArrayList<Record>();
      for (Operator n : rm.root.containednodes) {
         if (n instanceof ProximityOperator) {
            log.info("phrase s", n);
            //n.resetFeatureValues();
            ((ProximityOperator)n).setupCollector();
            CollectorProximity collector = ((ProximityOperator)n).collector;
            Record r = collector.createRecord();
            list.add(r);
         }
      }
      ProximityStats f = ProximityStats.get(repository);
      f.remove(list);
      list(f);
   }
   
   public static void list(ProximityStats f) {
      f.openRead();
      for (ProximityStats.Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
