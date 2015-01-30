package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigTermDocPos;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.TestSet;
import io.github.repir.TestSet.Topic.TestSetTopic;
import io.github.repir.tools.lib.Log;
import java.util.HashSet;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class CreateTermDocPos {

   public static Log log = new Log(CreateTermDocPos.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      StringPigJob job = new StringPigJob(repository);
      HashSet<String> keywords = getKeywords(repository);
      for (String w : keywords) {
         PigTermDocPos termdoc = PigTermDocPos.get(repository, w);
         if (!termdoc.getFile().exists()) {
            job.addTerm(w);
         }
      }
      job.setMapperClass(TermDocMapPos.class);
      job.submit();
      job.waitForCompletion(true);
   }

   public static HashSet<String> getKeywords(Repository r) {
      Retriever retriever = new Retriever(r);
      HashSet<String> keywords = new HashSet<String>();
      TestSet testset = new TestSet(r);
      for (TestSetTopic t : testset.topics.values()) {
         Query q = retriever.constructQueryRequest(testset.filterString(t.query));
         for (String w : q.query.split("\\s+")) {
            Term term = r.getTerm(w);
            if (term.exists()) {
               keywords.add(term.getProcessedTerm());
            }
         }
      }
      return keywords;
   }
}
