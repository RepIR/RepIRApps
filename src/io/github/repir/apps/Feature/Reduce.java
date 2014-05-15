package io.github.repir.apps.Feature;

import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReducibleFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.MapReduce.Job;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

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
      partition = Job.getReducerId(context);
      
      for (String featurename : repository.configuredStrings("repository.constructfeatures")) {
         Feature f = repository.getFeature(featurename);
         if (f instanceof ReducibleFeature) {
            if (f instanceof EntityStoredFeature) {
               documentfeatures.add((EntityStoredFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
               if (doclist == null)
                  doclist = repository.getCollectionIDFeature().getCollectionIDs(partition);
            }
         }
      }
      int mempart = MAXMEMORY / (4096 * (termdocfeatures.size() * 2 + documentfeatures.size()));
      for (EntityStoredFeature dc : documentfeatures) {
         dc.setPartition(partition);
         dc.setBufferSize(4096 * mempart);
         dc.startReduce(partition);
      }
      for (AutoTermDocumentFeature tc : termdocfeatures) {
         tc.setPartition(partition);
         tc.setBufferSize(4096 * 2 * mempart);
         tc.setDocs(doclist);
         tc.startReduce(partition);
      }
   }

   @Override
   public void reduce(TermEntityKey key, Iterable<TermEntityValue> values, Context context)
           throws IOException, InterruptedException {
      Job.reduceReport(context);
      if (key.getType() == TermEntityKey.Type.PRELOAD) {
         documentfeatures.get(key.feature).writereduce(key, values);
      } else if (key.getType() == TermEntityKey.Type.CHANNEL) {
         key.docid = doclist.get(key.collectionid); // convert collectionid to docid
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
   }
}
