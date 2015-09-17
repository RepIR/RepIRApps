package io.github.repir.apps.Repository;

import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.ReducibleFeature;
import io.github.repir.Repository.ReduciblePartitionedFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.htools.lib.Log;
import java.util.ArrayList;
/**
 *
 * @author jeroen
 */
public class AutoFeatures {
   public static final Log log = new Log( AutoFeatures.class );
   public ArrayList<ReduciblePartitionedFeature> reduciblepartitionedfeatures = new ArrayList();
   public ArrayList<ReducibleFeature> reduciblefeatures = new ArrayList();
   public ArrayList<AutoTermDocumentFeature> termdocfeatures = new ArrayList();

      public AutoFeatures(Repository repository) {
      for (StoredFeature f : repository.getConfiguredFeatures()) {
            if (f instanceof ReduciblePartitionedFeature) {
               reduciblepartitionedfeatures.add((ReduciblePartitionedFeature) f);
            } else if (f instanceof ReducibleFeature) {
               reduciblefeatures.add((ReducibleFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
            }
      }
   }

}
