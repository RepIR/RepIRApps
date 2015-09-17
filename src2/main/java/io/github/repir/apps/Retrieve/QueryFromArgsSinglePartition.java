package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;
import io.github.repir.Strategy.Collector.CollectorDocument;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.Strategy.Strategy;
import io.github.htools.lib.StrTools;

/**
 * Retrieve the results for one query from only one partition of the repository,
 * this is mainly a utility for test purposes.
 * arguments: <repository> <topicid> <partition> (query to override test set query with)
 * @author jeroen
 */
public class QueryFromArgsSinglePartition {

   public static Log log = new Log(QueryFromArgsSinglePartition.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "partitionnr query");
      RRConfiguration conf = repository.getConf();
      Retriever retriever = new Retriever(repository);
      int partition = conf.getInt("partitionnr", 0);
      String querystring = StrTools.concat(' ', conf.getStrings("query"));
      Query qq = retriever.constructQueryRequest(querystring);
      DocLiteral literaltitle = DocLiteral.get(repository, "literaltitle");
      qq.addFeature(literaltitle);
      qq.addFeature(repository.getCollectionIDFeature());
      Strategy result = retriever.retrieveSegment(qq, partition);
      for (Document d : ((CollectorDocument)result.collectors.get(0)).getRetrievedDocs())
         qq.add(d);
      int rank = 1;
      for (Document d : qq.getQueryResults()) {
         log.printf("%d %d#%d %f %s %s", rank++, d.docid, d.partition, d.score,
                 d.getString(repository.getCollectionIDFeature()), 
                 d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
