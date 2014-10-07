package util;

import java.util.ArrayList;
import io.github.repir.Strategy.Collector.CollectorSynonym;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.Strategy.Operator.SynonymOperator;
import io.github.repir.Strategy.Operator.Operator;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

public class delsyn {

   public static Log log = new Log(delsyn.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile {queryterm}");
      Repository repository = new Repository(parsedargs.get("configfile"));
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', parsedargs.getStrings("queryterm")));
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.prepareAggregation();
      ArrayList<Record> list = new ArrayList<Record>();
      for (Operator n : rm.root.containednodes) {
         if (n instanceof SynonymOperator) {
            //n.resetFeatureValues();
            ((SynonymOperator)n).doSetupCollector();
            CollectorSynonym collector = ((SynonymOperator)n).collector;
            Record r = collector.createRecord();
            list.add(r);
         }
      }
      SynStats f = SynStats.get(repository);
      f.remove(list);
      list(f);
   }
   
   public static void list(SynStats f) {
      f.openRead();
      for (Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
