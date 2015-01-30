package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigDoc;
import io.github.repir.Repository.Pig.PigTerm;
import io.github.repir.Repository.Pig.PigTermDoc;
import io.github.repir.Repository.Pig.PigTermDocPos;
import io.github.repir.Repository.Repository;
import static io.github.repir.apps.Pig.CreateTermDoc.getKeywords;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.FSPath;
import io.github.repir.tools.lib.Log;
import java.util.HashSet;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class CreateLoadLocal {

   public static Log log = new Log(CreateLoadLocal.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      FSPath dir = new FSPath(repository.configuredString("rr.localdir") + "pig/" + repository.getPrefix());
      PigTerm term = PigTerm.get(repository);
      Datafile scriptfile = dir.getFile("terms");
      scriptfile.printf("terms = %s", term.loadLocalScript());
      scriptfile.closeWrite();
      PigDoc doc = PigDoc.get(repository);
      scriptfile = dir.getFile("docs");
      scriptfile.printf("docs = %s", doc.loadLocalScript());
      scriptfile.closeWrite();

      HashSet<String> keywords = getKeywords(repository);
      for (String w : keywords) {
         PigTermDoc termdoc = PigTermDoc.get(repository, w);
            scriptfile = dir.getFile("postings_" + w);
            scriptfile.printf("postings_%s = %s", w, termdoc.loadLocalScript());
            scriptfile.closeWrite();
         PigTermDocPos termdocpos = PigTermDocPos.get(repository, w);
            scriptfile = dir.getFile("pospostings_" + w);
            scriptfile.printf("pospostings_%s = %s", w, termdocpos.loadLocalScript());
            scriptfile.closeWrite();
      }
   }
}
