package io.github.repir.apps.Retrieve;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.QueryMetricAP;
import io.github.repir.TestSet.ResultSet;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * retrieve a query from test set
 * arguments: <configfile> <topicid> [alternative query]
 * @author jeroen
 */
public class QueryFromTestSet2 extends Configured implements Tool {

   public static Log log = new Log(QueryFromTestSet2.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "topicid {query}");
      HDTools.setPriorityHigh(conf);
      System.exit( HDTools.run( conf, new QueryFromTestSet2()));
   }
   
   @Override
   public int run(String[] args) throws Exception {
      Repository repository = new Repository((Configuration)getConf());
      RetrieverMR retriever = new RetrieverMR(repository);
      TestSet testset = new TestSet(repository);
      int topic = repository.getConfigurationInt("topicid", 0);
      Query q = testset.getQuery(topic, retriever);
      q.addFeature("DocLiteral:literaltitle");
      if (repository.getConfiguration().getStrings("query") != null) {
         q.originalquery = io.github.repir.tools.Lib.StrTools.concat(' ', repository.getConfiguration().getStrings("query"));
      }
      retriever.addQueue(q);
      ArrayList<Query> results = retriever.retrieveQueue();
      Query result = results.get(0);
      
      Document dd = result.queryresults[0];
      log.info("%d", dd.reportdata.length);
      dd.getLiteral("DocLiteral:collectionid");
      dd.getLiteral("DocLiteral:literaltitle");
      int rank = 1;
      for (Document d : result.queryresults) {
         log.printf("%d %d#%d %s %f %s", rank++, d.docid, d.partition,
                 d.getLiteral("DocLiteral:collectionid"), d.score,
                 d.getLiteral("DocLiteral:literaltitle"));
         if (d.report != null)
            log.printf("%s", d.report);
         if (rank > repository.getConfigurationInt("retriever.reportlimit", 10))
            break;
      }
      return 0;
   }
}
