package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.MapReduceTools.Configuration;
import io.github.repir.Strategy.Strategy;
import io.github.repir.tools.Lib.StrTools;

/**
 * Retrieve the results for one query from only one partition of the repository,
 * this is mainly a utility for test purposes.
 * arguments: <repository> <partition> <query>
 * @author jeroen
 */
public class QueryFromTestSetSinglePartition {

   public static Log log = new Log(QueryFromTestSetSinglePartition.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration(args, "topicid partitionnr {query}");
      Repository repository = new Repository(conf);
      Retriever retriever = new Retriever(repository);
      TestSet bm = new TestSet(repository);
      int topic = conf.getInt("topicid", 0);
      int partition = conf.getInt("partitionnr", 0);
      Query qq = bm.getQuery(topic, retriever);
      if (conf.getStrings("query").length > 0) {
         qq.originalquery = StrTools.concat(' ', conf.getStrings("query"));
         qq.query = retriever.tokenizeString(qq.originalquery);
      }
      DocLiteral literaltitle = DocLiteral.get(repository, "literaltitle");
      qq.addFeature(literaltitle);
      qq.addFeature(repository.getCollectionIDFeature());
      Strategy result = retriever.retrieveSegment(qq, partition);
      for (Document d : (((CollectorDocument)result.collectors.get(0)).getRetrievedDocs()))
         qq.add(d);
      //QueryMetricAP ap = new QueryMetricAP();
      //ResultSet r = new ResultSet( ap, bm, qq);
      //r.calulateMeasure();
      //log.info("query %d '%s' MAP=%f\n%s", qq.id, qq.query, r.result[1].avg, ArrayTools.toString(ap.curve));
      int rank = 1;
      for (Document d : qq.getQueryResults()) {
         log.printf("%d %d#%d %f %s %s", rank++, d.docid, d.partition, d.score,
                 d.getString(repository.getCollectionIDFeature()), d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
