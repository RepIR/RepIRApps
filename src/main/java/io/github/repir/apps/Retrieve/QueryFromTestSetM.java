package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import java.util.ArrayList;
import io.github.repir.Retriever.MapOnly.RetrieverM;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.Lib.StrTools;

/**
 * retrieve a query from test set
 * arguments: <configfile> <topicid> [alternative query]
 * @author jeroen
 */
public class QueryFromTestSetM {

   public static Log log = new Log(QueryFromTestSetM.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "topicid {query}");
      RetrieverM retriever = new RetrieverM(repository);
      TestSet testset = new TestSet(repository);
      int topic = repository.configuredInt("topicid", 0);
      Query q = testset.getQuery(topic, retriever);
      DocLiteral literaltitle = (DocLiteral)repository.getFeature(DocLiteral.class, "literaltitle");
      q.addFeature(literaltitle);
      if (repository.configuredStrings("query").length > 0) {
         q.originalquery = StrTools.concat(' ', repository.configuredStrings("query"));
         q.query = retriever.tokenizeString(q.originalquery);
       }
      retriever.addQueue(q);
      ArrayList<Query> results = retriever.retrieveQueue();
      Query result = results.get(0);
      
      QueryMetricAP ap = new QueryMetricAP();
      ResultSet resultstat = new ResultSet( ap, testset, result);

      log.info("query %d '%s' MAP=%f\n%s", q.id, q.query, resultstat.queryresult[0], io.github.repir.tools.Lib.ArrayTools.concat(ap.curve));
      
      int rank = 1;
      for (Document d : result.getQueryResults()) {
         log.printf("%d %d#%d %s %f %s", rank++, d.docid, d.partition,
                 d.getString(repository.getCollectionIDFeature()), d.score,
                 d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
