package io.github.repir.apps.Retrieve;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Reusable.Retriever;
import io.github.htools.lib.Log;
import java.util.ArrayList;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.Retriever.MapReduce.QueueIterator;
import io.github.repir.TestSet.TestSet;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 * @author jeroen
 */
public class RunTestSetV {

   public static Log log = new Log(RunTestSetV.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args);
      repository.getConf().setBoolean("inputformat.cansplit", true);
      Retriever retriever = new Retriever(repository);
      TestSet testset = new TestSet( repository );
      ArrayList<Variant> variants = new ArrayList<Variant>();
      variants.add(new Variant("RetrievalModel", "ScoreFunctionKLD", "kld.mu=1000"));
      variants.add(new Variant("RetrievalModel", "ScoreFunctionKLD", "kld.mu=2000"));
      retriever.addQueue(testset.getQueries(retriever));
      for (Query q : retriever.queue) {
         for (Variant v : variants)
            q.addVariant(v);
      }
      QueueIterator results = retriever.retrieveQueueIterator();
      for (int i = 0; i < variants.size(); i++) {
         ArrayList<Query> list = results.nextVariant();
         for (Query q : list) {
            if (q.getQueryResults().length > 0)
               log.info("%d %s %d %f", q.id, q.originalquery, q.getQueryResults()[0].docid, q.getQueryResults()[0].score);
         }
      }
   }
}
