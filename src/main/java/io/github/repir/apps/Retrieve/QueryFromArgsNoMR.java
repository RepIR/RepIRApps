package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.StrTools;

/**
 * Retrieves a single query without MR, and shows top-10 docs
 * @author jeroen
 */
public class QueryFromArgsNoMR {

   public static Log log = new Log(QueryFromArgsNoMR.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "{query}");
      Retriever retriever = new Retriever(repository);
      int rank = 1;
      Query q1 = retriever.constructQueryRequest(StrTools.concat(' ', repository.configuredStringList("query")));
      q1.addFeature(repository.getCollectionIDFeature());
      DocLiteral literaltitle = DocLiteral.get(repository, "literaltitle");
      q1.addFeature(literaltitle);
      q1 = retriever.retrieveQuery(q1);
      for (Document d : q1.getQueryResults()) {
         log.printf("%d %5d#%3d %f %s %s", rank++, d.docid, d.partition, d.score,
                 d.getString(repository.getCollectionIDFeature()), 
                 d.getString(literaltitle));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.configuredInt("retriever.reportlimit", 10))
            break;
      }
   }
}
