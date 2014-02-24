package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.DataTypes.Configuration;

/**
 * Retrieves a single query without MR, and shows top-10 docs
 * @author jeroen
 */
public class QueryFromArgsNoMR {

   public static Log log = new Log(QueryFromArgsNoMR.class);

   public static void main(String args[]) {
      Configuration conf = HDTools.readConfig(args[0]);
      args = HDTools.processArgSettings(conf, args);
      Repository repository = new Repository(conf);
      args = ArrayTools.subArray(args, 1);
      Retriever retriever = new Retriever(repository);
      int rank = 1;
      Query q1 = retriever.constructQueryRequest(io.github.repir.tools.Lib.StrTools.concat(' ', args));
      q1.addFeature("DocLiteral:collectionid");
      q1.addFeature("DocLiteral:literaltitle");
      q1 = retriever.retrieveQuery(q1);
      Log.reportProfile();
      for (Document d : q1.queryresults) {
         log.printf("%d %5d#%3d %f %s %s", rank++, d.docid, d.partition, d.score,
                 d.getLiteral("DocLiteral:collectionid"), d.getLiteral("DocLiteral:literaltitle"));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.getConfigurationInt("retriever.reportlimit", 10))
            break;
      }
   }
}
