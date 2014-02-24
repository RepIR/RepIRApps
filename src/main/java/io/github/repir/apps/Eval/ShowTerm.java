package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermTF;
import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Stemmer.englishStemmer;
import io.github.repir.Repository.TermID;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Shows stored data for a single stemmed term 
 * arguments: <configfile> <term>
 */
public class ShowTerm {

   public static Log log = new Log(ShowTerm.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser( args, "configfile term");
      Configuration conf = HDTools.readConfig(parsedargs.get("configfile"));
      Repository repository = new Repository(conf);
      RetrieverMR retriever = new RetrieverMR(repository);
      String termstring = parsedargs.get("term");
      Query q = retriever.constructQueryRequest(termstring);
      String stemmedterm = retriever.tokenizeString(q);
      log.info("%s %s", q.query, stemmedterm);

      int termid = repository.termToID(stemmedterm);
      log.info("termid %d", termid);
      TermDF df = (TermDF) repository.getFeature("TermDF");
      TermTF tf = (TermTF) repository.getFeature("TermTF");
      df.openRead();
      tf.openRead();
      log.info("Term %d string %s stem %s df %d tf %d", termid, termstring, stemmedterm, df.readValue(termid), tf.readValue(termid));
   }
}
