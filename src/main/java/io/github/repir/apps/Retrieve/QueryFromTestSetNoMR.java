package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.lib.Log;
import io.github.repir.TestSet.Metric.QueryMetricAP;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.TestSet.Metric.QueryMetricPrecision;
import io.github.repir.TestSet.Metric.QueryMetricRecall;
import io.github.repir.TestSet.ResultSet;
import io.github.repir.tools.lib.StrTools;

/**
 * Retrieves a query from a test set without using MR
 * arguments: <configfile> <topicid> [query that overrides one in test set]
 * @author jeroen
 */
public class QueryFromTestSetNoMR {

   public static Log log = new Log(QueryFromTestSetNoMR.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "topicid {query}");
      RRConfiguration conf = repository.getConf();
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet(repository);
      int topic = conf.getInt("topicid", 0);
      Query qq = testset.getQuery(topic, retriever);
      int qrelid = testset.getQRelId(qq);
      qq.addFeature(repository.getCollectionIDFeature());
      DocLiteral literaltitle = DocLiteral.get(repository, "literaltitle");
      qq.addFeature(literaltitle);
      if (conf.getStrings("query").length > 0) {
         qq.originalquery = StrTools.concat(' ', conf.getStrings("query"));
         qq.query = retriever.tokenizeString(qq.originalquery);
       }
      Query q = retriever.retrieveQuery(qq);
      QueryMetricAP ap = new QueryMetricAP();
      ResultSet r = new ResultSet( ap, testset, q);
      ResultSet r2 = new ResultSet( new QueryMetricPrecision(1000), testset, q);
      ResultSet r3 = new ResultSet( new QueryMetricRecall(1000), testset, q);
      log.info("query %d '%s' MAP=%f P@1000-%f R@1000=%f\n%s", q.id, q.query, 
              r.queryresult[0], 
              r2.queryresult[0], 
              r3.queryresult[0], 
              io.github.repir.tools.lib.ArrayTools.toString(ap.curve));
      int rank = 1;
      for (Document d : q.getQueryResults()) {
         log.printf("%b %d %d#%d %f %s %s", testset.isRelevant(qrelid, d) > 0, rank++, d.docid, d.partition, d.score,
                 d.getString(repository.getCollectionIDFeature()), 
                 d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
