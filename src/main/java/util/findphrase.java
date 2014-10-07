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
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class findphrase {

   public static Log log = new Log(findphrase.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {queryterm}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', parsedargs.getStrings("queryterm")));
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.prepareAggregation();
      ArrayList<Record> list = new ArrayList<Record>();
      for (Operator n : rm.root.containednodes) {
         if (n instanceof ProximityOperator) {
            ((ProximityOperator)n).setupCollector();
            CollectorProximity collector = ((ProximityOperator)n).collector;
            Record r = collector.createRecord();
            list.add(r);
         }
      }
      ProximityStats f = ProximityStats.get(repository);
      for (Record r : list) {
         r = (Record)f.find(r);
         if (r != null)
            log.info("found %s", r);
      }
   }
   
   public static void list(ProximityStats f) {
      f.openRead();
      for (Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
