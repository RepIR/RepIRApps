package io.github.repir.apps.Retrieve;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import io.github.repir.tools.Lib.StrTools;

/**
 * Retrieves a single query over the MR, and outputs the results to the console
 * @author jeroen
 */
public class QueryFromArgs2 extends Configured implements Tool {

   public static Log log = new Log(QueryFromArgs2.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "{query}");
      HDTools.setPriorityHigh(conf);
      System.exit(HDTools.run(conf, new QueryFromArgs2()));
   }

   @Override
   public int run(String[] args) throws Exception {
      Configuration conf = (Configuration)this.getConf();
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      Query q1 = retriever.constructQueryRequest(StrTools.concat(' ', getConf().get("query")));
      q1.addFeature("DocLiteral:literaltitle");
      q1 = retriever.retrieveQuery(q1);
      int rank = 1;
      for (Document d : q1.queryresults) {
         log.info("%d %5d#%3d %f %s", rank++, d.docid, d.partition, d.score,
                 d.getLiteral("DocLiteral:literaltitle"));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.getConfigurationInt("retriever.reportlimit", 10))
            break;
      }
      return 0;
   }
}
