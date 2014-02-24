package io.github.repir.apps.Eval;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermTF;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Shows the top-10000 terms in the vocabulary
 */
public class ShowTermID {

   public static Log log = new Log(ShowTermID.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile [termid]");
      Configuration conf = HDTools.readConfig( parsedargs.get("configfile") );
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      TermString termstring = (TermString) repository.getFeature("TermString");
      TermDF df = (TermDF) repository.getFeature("TermDF");
      TermTF tf = (TermTF) repository.getFeature("TermTF");
      if (parsedargs.get("termid") != null) {
          int termid = parsedargs.getInt("termid");
          String s = termstring.readValue(termid);
          log.printf("Term %d is %s df %d tf %d", termid, s, df.readValue(termid), tf.readValue(termid));
      } else {
         for (int termid = 0; termid < 200; termid++) {
          String s = termstring.readValue(termid);
          log.printf("Term %d is %s df %d tf %d", termid, s, df.readValue(termid), tf.readValue(termid));            
         }
      }
   }
}
