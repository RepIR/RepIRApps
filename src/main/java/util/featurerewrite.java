package util;

import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredDynamicFeature;
import io.github.repir.tools.io.FileIntegrityException;
import io.github.repir.tools.lib.Log;

public class featurerewrite {

   public static Log log = new Log(featurerewrite.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args, "feature");
      StoredDynamicFeature f = (StoredDynamicFeature)repository.getFeature(repository.configuredString("feature"));
      try {
      f.openRead();
      } catch (FileIntegrityException ex) {
         log.exception(ex, "in feature %s", repository.configuredString("feature"));
      }
      f.openWrite();
      f.closeWrite();
   }
}
