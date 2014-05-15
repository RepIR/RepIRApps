package io.github.repir.apps.Pig;

import io.github.repir.Repository.Pig.PigDoc;
import io.github.repir.Repository.Pig.PigTerm;
import io.github.repir.Repository.Pig.PigTermDoc;
import io.github.repir.Repository.Repository;
import static io.github.repir.apps.Pig.CreateTermDoc.getKeywords;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSDir;
import io.github.repir.tools.Lib.Log;
import java.util.HashSet;

/**
 * Retrieve all topics from the TestSet, and store in an output file. arguments:
 * <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class CreateLoad {

   public static Log log = new Log(CreateLoad.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      FSDir dir = new FSDir(repository.configuredString("rr.localdir") + "pig/" + repository.getPrefix());
      PigTerm term = (PigTerm)repository.getFeature(PigTerm.class);
      Datafile scriptfile = dir.getFile("pigterm");
      scriptfile.printf("%s", term.loadScript());
      scriptfile.closeWrite();
      PigDoc doc = (PigDoc)repository.getFeature(PigDoc.class);
      scriptfile = dir.getFile("pigdoc");
      scriptfile.printf("%s", doc.loadScript());
      scriptfile.closeWrite();

      HashSet<String> keywords = getKeywords(repository);
      for (String w : keywords) {
         PigTermDoc termdoc = (PigTermDoc) repository.getFeature(PigTermDoc.class, w);
         if (termdoc.getFile().exists()) {
            scriptfile = dir.getFile(w);
            scriptfile.printf("%s", termdoc.loadScript());
            scriptfile.closeWrite();
         } else {
            log.info("doesnt exist %s", termdoc.getFile().datafile.getFullPath());
         }
      }
   }
   
   public static String convert( String loadScript ) {
      return loadScript;
   }
}
