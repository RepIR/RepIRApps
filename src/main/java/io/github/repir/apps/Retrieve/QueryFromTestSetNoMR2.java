package io.github.repir.apps.Retrieve;

import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.GraphNode;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.TestSet.QueryMetricPrecision;
import io.github.repir.TestSet.QueryMetricRecall;
import io.github.repir.tools.Lib.StrTools;

/**
 * Retrieves a query from a test set without using MR
 * arguments: <configfile> <topicid> [query that overrides one in test set]
 * @author jeroen
 */
public class QueryFromTestSetNoMR2 {

   public static Log log = new Log(QueryFromTestSetNoMR2.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfigNoMR(args, "topicid {query}");
      Repository repository = new Repository(conf);
      Retriever retriever = new Retriever(repository);
      TestSet bm = new TestSet(repository);
      int topic = conf.getInt("topicid", 0);
      Query qq = bm.getQuery(topic, retriever);
      qq.addFeature("DocLiteral:literaltitle");
      if (conf.getStrings("query")!=null) {
         qq.originalquery = StrTools.concat(' ', conf.getStrings("query"));
      }
      Query q = retriever.retrieveQuery(qq);

      Log.reportProfile();
      
      int rank = 1;
      for (Document d : q.queryresults) {
         log.printf("%b %d %d#%d %f %s %s", bm.isRelevant(topic, d) > 0, rank++, d.docid, d.partition, d.score,
                 d.getLiteral("DocLiteral:collectionid"), 
                 d.getLiteral("DocLiteral:literaltitle"));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.getConfigurationInt("retriever.reportlimit", 10))
            break;
      }
   }
}
