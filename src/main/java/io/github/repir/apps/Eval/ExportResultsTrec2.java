package io.github.repir.apps.Eval;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.ResultFileRR;
import io.github.repir.TestSet.ResultFileTREC;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * outputs a set of results files with the same test set to excel. The output is
 * formatted <TopicID> <TopicString> <MAP baseline> <MAP resultsfile1> ....
 * parameters: <configfile> { resultsfileextension }
 * <p/>
 * @author jeroen
 */
public class ExportResultsTrec2 {

   public static Log log = new Log(ExportResultsTrec2.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "resultsext identifier");
      String identifier = repository.configuredString("identifier");
      TestSet testset = new TestSet(repository);
      DocLiteral collectionid = repository.getCollectionIDFeature();
      ResultFileRR resultsRR = testset.getResults(repository.configuredString("resultsext"));
      ResultFileTREC resultsTREC = new ResultFileTREC(new Datafile(identifier));
      ArrayList<Query> queries = resultsRR.getResults();
      Collections.sort(queries, new QueryComparator());
      resultsTREC.writeresults(queries);
   }
   
   public static class QueryComparator implements Comparator<Query> {
       @Override
       public int compare(Query a, Query b) {
               return (a.id < b.id) ? -1 : 1;
       }
   }
}
