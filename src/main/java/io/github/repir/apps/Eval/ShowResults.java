package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFileRR;
import io.github.repir.TestSet.TestSet;
import java.util.Collection;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * outputs a set of results files with the same test set to excel. The output is
 * formatted <TopicID> <TopicString> <MAP baseline> <MAP resultsfile1> ....
 * parameters: <configfile> { resultsfileextension }
 * @author jeroen
 */
public class ShowResults {

   public static Log log = new Log(ShowResults.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile resultsext topicid");
      Repository repository = new Repository(parsedargs.get("configfile"));
      TestSet testset = new TestSet( repository );
      DocLiteral title = DocLiteral.get(repository, "literaltitle");
      int topic = parsedargs.getInt("topicid", 0);
      ResultFileRR results = testset.getResults(parsedargs.get("resultsext"));
      Collection<Query> results1 = results.getResults();
      int rank = 1;
      for (Query q : results1) {
         if (q.id == topic) {
            for (Document d : q.getQueryResults()) {
               title.read(d);
               log.printf("%3d %6d#%3d %6f %s", rank++, d.docid, d.partition, d.score, title.getValue());
               if (rank > 50)
                  break;
            }
         }
      }
   }
}
