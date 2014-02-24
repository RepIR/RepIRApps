package io.github.repir.apps.Retrieve;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.StrTools;

/**
 * Use the MR retriever to retrieve a single query from the repository and
 * report the document title.
 * <p/>
 * @author jeroen
 */
public class QueryFromArgs3 {
   
   public static Log log = new Log(QueryFromArgs3.class);
   
   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "{query}");
      HDTools.setPriorityHigh(conf);
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', repository.getConfigurationSubStrings("query")));
      q.addFeature("DocLiteral:literaltitle");
      q.addFeature(repository.getCollectionIDFeature());
      retriever.addQueue(q);
      ArrayList<Query> results = retriever.retrieveQueue();
      Query q1 = results.get(0);
      int rank = 1;
      for (Document d : q1.queryresults) {
         log.printf("%d %d#%d %s %f %s", rank++, d.docid, d.partition,
                 d.getLiteral(repository.getCollectionIDFeature()), d.score,
                 d.getLiteral("DocLiteral:literaltitle"));
         if (d.report != null) {
            log.printf("%s", d.report);
         }
         if (rank > repository.getConfigurationInt("retriever.reportlimit", 10)) {
            break;
         }
      }
   }
}
