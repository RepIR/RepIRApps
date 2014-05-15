package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.TestSet.TestSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.Configuration;
import io.github.repir.tools.Lib.StrTools;

/**
 * Retrieves a query from a test set without using MR
 * arguments: <configfile> <topicid> [query that overrides one in test set]
 * @author jeroen
 */
public class QueryFromTestSetNoMR2 {

   public static Log log = new Log(QueryFromTestSetNoMR2.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration(args, "topicid {query}");
      Repository repository = new Repository(conf);
      Retriever retriever = new Retriever(repository);
      TestSet bm = new TestSet(repository);
      int topic = conf.getInt("topicid", 0);
      Query qq = bm.getQuery(topic, retriever);
      DocLiteral literaltitle = (DocLiteral)repository.getFeature(DocLiteral.class, "literaltitle");
      qq.addFeature(literaltitle);
      if (conf.getStrings("query").length > 0) {
         qq.originalquery = StrTools.concat(' ', conf.getStrings("query"));
         qq.query = retriever.tokenizeString(qq.originalquery);
      }
      Query q = retriever.retrieveQuery(qq);

      Log.reportProfile();
      
      int rank = 1;
      for (Document d : q.queryresults) {
         log.printf("%b %d %d#%d %f %s %s", bm.isRelevant(topic, d) > 0, rank++, d.docid, d.partition, d.score,
                 d.getString(repository.getCollectionIDFeature()), 
                 d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
