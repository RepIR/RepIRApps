package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.lib.StrTools;

/**
 * Use the MR retriever to retrieve a single query from the repository and
 * report the document title.
 * <p/>
 * @author jeroen
 */
public class QueryFromArgs {
   
   public static Log log = new Log(QueryFromArgs.class);
   
   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "{query}");
      Retriever retriever = new Retriever(repository);
      DocLiteral literaltitle = DocLiteral.get(repository, "literaltitle");
      Query q = retriever.constructQueryRequest(StrTools.concat(' ', repository.configuredStrings("query")));
      q.addFeature(repository.getCollectionIDFeature());
      q.addFeature(literaltitle);
      retriever.addQueue(q);
      ArrayList<Query> results = retriever.retrieveQueue();
      Query q1 = results.get(0);
      int rank = 1;
      for (Document d : q1.getQueryResults()) {
         log.printf("%d %d#%d %s %f %s", rank++, d.docid, d.partition,
                 d.getString(repository.getCollectionIDFeature()), d.score,
                 d.getString(literaltitle));
         if (d.report != null) {
            log.printf("%s", d.report);
         }
         if (rank > repository.configuredInt("retriever.reportlimit", 10)) {
            break;
         }
      }
   }
}
