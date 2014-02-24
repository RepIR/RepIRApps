package io.github.repir.apps.Repository;

import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.ReportableFeature;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.ReducableFeature;

public class Reduce extends Reducer<TermEntityKey, TermEntityValue, TermEntityKey, IntWritable> {

   public static Log log = new Log(Reduce.class);
   Repository repository;
   int partition;
   int MAXMEMORY = 100000000;
   HashMap<String, Integer> doclist = new HashMap<String, Integer>();
   ArrayList<EntityStoredFeature> documentfeatures = new ArrayList<EntityStoredFeature>();
   ArrayList<AutoTermDocumentFeature> termdocfeatures = new ArrayList<AutoTermDocumentFeature>();

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      partition = repository.getConfigurationInt("repository.onlypartition", HDTools.getReducerId(context));
      for (StoredFeature f : repository.getFeatures()) {
         if (f instanceof ReducableFeature) {
            if (f instanceof EntityStoredFeature) {
               documentfeatures.add((EntityStoredFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
            }
         }
      }
      int mempart = MAXMEMORY / (4096 * (termdocfeatures.size() * 2 + documentfeatures.size()));
      for (EntityStoredFeature dc : documentfeatures) {
         dc.setBufferSize(4096 * mempart);
         dc.startReduce(partition);
      }
      for (AutoTermDocumentFeature tc : termdocfeatures) {
         tc.setBufferSize(4096 * 2 * mempart);
         tc.setDocs(doclist);
         tc.startReduce(partition);
      }
   }

   @Override
   public void reduce(TermEntityKey key, Iterable<TermEntityValue> values, Context context)
           throws IOException, InterruptedException {
      HDTools.reduceReport(context);
      if (key.getType() == TermEntityKey.Type.PRELOADDOC) {
         if (key.feature == 0) {
            //log.info("doc %s %d", key.docname, doclist.size());
            doclist.put(key.docname, doclist.size());
         }
         documentfeatures.get(key.feature).writereduce(key, values);
      } else if (key.getType() == TermEntityKey.Type.CHANNEL) {
         //if (key.docname.equals("clueweb12-0000tw-31-13675"))
         //   log.info("feature %d termid %d doc %s", key.feature, key.termid, key.docname);
         key.docid = doclist.get(key.docname);
         termdocfeatures.get(key.feature).writereduce(key, values);
      }
      context.progress();
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      for (EntityStoredFeature dc : documentfeatures) {
         dc.finishReduce();
      }
      for (AutoTermDocumentFeature tc : termdocfeatures) {
         tc.finishReduce();
      }
//      for (TermPostFeature tc : termpostfeatures) {
//         tc.finishReduce();
//      }
   }
}
