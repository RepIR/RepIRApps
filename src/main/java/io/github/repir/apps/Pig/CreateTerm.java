package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigTerm;
import io.github.repir.Repository.Pig.PigTerm.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermDF;
import static io.github.repir.apps.Pig.CreateTermDoc.getKeywords;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class CreateTerm {

   public static Log log = new Log(CreateTerm.class);

   public static void main(String[] args) {
      Repository repository = new Repository(args[0]);
      HashSet<String> keywords = getKeywords(repository);
      ArrayList<Tuple> tuples = new ArrayList<Tuple>();
      TermCF termcf = TermCF.get(repository);
      TermDF termdf = TermDF.get(repository);
      termcf.readCache();
      termdf.readCache();
      for (String s : keywords) {
         Term term = repository.getTerm(s);
         Tuple t = new Tuple();
         t.id = term.getID();
         t.term = term.getProcessedTerm();
         t.isstopword = term.isStopword();
         t.df = termdf.readValue(t.id);
         t.cf = termcf.readValue(t.id);
         tuples.add(t);
      }
      PigTerm d = PigTerm.get(repository);
      d.openAppend();
      for (Tuple t : tuples) {
         d.write(t);
      }
      d.closeWrite();
   }
}
