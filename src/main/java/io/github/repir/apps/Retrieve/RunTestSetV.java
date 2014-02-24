package io.github.repir.apps.Retrieve;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMulti.RetrieverMultiple;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.RetrieverMR.QueueIterator;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSetV {

   public static Log log = new Log(RunTestSetV.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "");
      HDTools.setPriorityHigh(conf);
      Repository repository = new Repository(conf);
      RetrieverMultiple retriever = new RetrieverMultiple(repository);
      TestSet testset = new TestSet( repository );
      ArrayList<Variant> variants = new ArrayList<Variant>();
      variants.add(new Variant("RetrievalModel", "ScoreFunctionKLD", "kld.mu=1000"));
      variants.add(new Variant("RetrievalModel", "ScoreFunctionKLD", "kld.mu=2000"));
      retriever.addQueue(testset.getQueries(retriever));
      for (Query q : retriever.queue) {
         for (Variant v : variants)
            q.addVariant(v);
      }
      RetrieverMRInputFormat.setSplitable(true);
      RetrieverMRInputFormat.setIndex(repository);
      QueueIterator results = retriever.retrieveQueueIterator();
      for (int i = 0; i < variants.size(); i++) {
         ArrayList<Query> list = results.nextVariant();
         for (Query q : list) {
            if (q.queryresults.length > 0)
               log.info("%d %s %d %f", q.id, q.originalquery, q.queryresults[0].docid, q.queryresults[0].score);
         }
      }
   }
}
