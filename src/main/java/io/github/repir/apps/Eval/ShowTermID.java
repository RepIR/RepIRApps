package io.github.repir.apps.Eval;

import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermCF;
import io.github.repir.tools.lib.Log;

/**
 * Shows the top-10000 terms in the vocabulary
 */
public class ShowTermID {

   public static Log log = new Log(ShowTermID.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "[termid]");
      Retriever retriever = new Retriever(repository);
      TermString termstring = TermString.get(repository);
      TermDF df = TermDF.get(repository);
      TermCF cf = TermCF.get(repository);
      if (repository.getConf().containsKey("termid")) {
          int termid = repository.configuredInt("termid");
          String s = termstring.readValue(termid);
          log.printf("Term %d is %s df %d cf %d", termid, s, df.readValue(termid), cf.readValue(termid));
      } else {
         for (int termid = 0; termid < 200; termid++) {
          String s = termstring.readValue(termid);
          log.printf("Term %d is %s df %d cf %d", termid, s, df.readValue(termid), cf.readValue(termid));            
         }
      }
   }
}
