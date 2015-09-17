package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermDF;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Retriever.Query;
import io.github.htools.lib.Log;

/**
 * Shows stored data for a single stemmed term 
 * arguments: <configfile> <term>
 */
public class ShowTerm {

   public static Log log = new Log(ShowTerm.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "term");
      Retriever retriever = new Retriever(repository);
      String termstring = repository.configuredString("term");
      Query q = retriever.constructQueryRequest(termstring);
      Term term = repository.getTerm(q.query.trim());
      log.info("%s %s", q.query, term.getProcessedTerm());

      log.info("termid %d", term.getID());
      TermDF df = TermDF.get(repository);
      TermCF cf = TermCF.get(repository);
      df.openRead();
      cf.openRead();
      log.info("Term %d string %s stem %s df %d cf %d", term.getID(), term.getOriginalTerm(), term.getProcessedTerm(), df.readValue(term.getID()), cf.readValue(term.getID()));
   }
}
