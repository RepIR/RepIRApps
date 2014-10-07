package io.github.repir.apps.Eval;

import io.github.repir.Repository.DocContents;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermDF;
import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.StrTools;

/**
 * Shows stored data for a single stemmed term 
 * arguments: <configfile> <term>
 */
public class ShowDocContents {

   public static Log log = new Log(ShowDocContents.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "{doctitle}");
      String doctitle = StrTools.concat(' ', repository.configuredStrings("doctitle"));
      log.info("args %s", StrTools.concat(',', args));
      log.info("repo %s title '%s'", repository.configurationName(), doctitle);
      DocContents df = DocContents.get(repository, "all", "literaltitle");
      String[] get = df.get(doctitle);
      log.info("Doc %s contents %s", doctitle, StrTools.concat(' ', get));
   }
}
