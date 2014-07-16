package io.github.repir.apps.Repository;

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.EntityReader.Entity;
import io.github.repir.Extractor.EntityChannel;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.ReducibleFeature;

/**
 * Mapper for the automatic extraction of {@link StoredFeature}s, as configured
 * for the {@link Repository}. See {@link io.github.repir.apps.Vocabulary.Create} for
 * more information on the configuration settings.
 * <p/>
 * This Mapper needs the {@link DictionaryFeature}s, to convert the extracted tokens
 * to TermID's.
 * @author jer
 */
public class RMap extends Mapper<LongWritable, EntityWritable, TermEntityKey, TermEntityValue> {

   public static Log log = new Log(RMap.class);
   private Extractor extractor;
   private Repository repository;
   private FileSystem fs;
   private FileSplit filesplit;
   private int onlypartition;
   TermEntityKey outkey;
   TermEntityValue outvalue = new TermEntityValue();
   ArrayList<EntityStoredFeature> documentfeatures = new ArrayList<EntityStoredFeature>();
   ArrayList<AutoTermDocumentFeature> termdocfeatures = new ArrayList<AutoTermDocumentFeature>();
   DocLiteral collectionid;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      onlypartition = repository.configuredInt("index.onlypartition", -1);
      collectionid = repository.getCollectionIDFeature();
      for (StoredFeature f : repository.getConfiguredFeatures()) {
         if (f instanceof ReducibleFeature) {
            if (f instanceof EntityStoredFeature) {
               documentfeatures.add((EntityStoredFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
            }
         }
      }
      extractor = new Extractor(repository);
      fs = repository.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
   }

   @Override
   public void map(LongWritable inkey, EntityWritable value, Context context) throws IOException, InterruptedException {
      extractor.process(value.entity);
      if (value.entity.size() > 0) {
         String docid = collectionid.extract(value.entity);
         int partition = Repository.getPartition(docid, repository.getPartitions());
         if (onlypartition == -1 || onlypartition == partition) {
            if (onlypartition >= 0)
               partition = 0;
            for (int feature = 0; feature < documentfeatures.size(); feature++) {
               EntityStoredFeature sf = documentfeatures.get(feature);
               outkey = TermEntityKey.createPreload(partition, feature, docid);
               sf.mapOutput(outvalue, value.entity);
               context.write(outkey, outvalue);
            }
            for (int feature = 0; feature < termdocfeatures.size(); feature++) {
               AutoTermDocumentFeature tdf = termdocfeatures.get(feature);
               HashMap<Integer, ArrayList<Integer>> tokens = getTokens(value.entity, tdf);
               for (Entry<Integer, ArrayList<Integer>> entry : tokens.entrySet()) {
                  outkey = TermEntityKey.createChannel(partition, feature, entry.getKey(), docid);
                  tdf.writeMapOutput(outvalue, value.entity, entry.getValue());
                  context.write(outkey, outvalue);
               }
            }
         }
      }
      context.progress();
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      Log.reportProfile();
   }

   public HashMap<Integer, ArrayList<Integer>> getTokens(Entity doc, AutoTermDocumentFeature channel) {
      HashMap<Integer, ArrayList<Integer>> list = new HashMap<Integer, ArrayList<Integer>>();
      ArrayList<Integer> l;
      int pos = 0;
      EntityChannel attr = doc.get(channel.entityAttribute());
      if (attr.tokenized == null) {
         attr.tokenized = repository.tokenize(attr);
      }
      for (int token : attr.tokenized) {
         l = list.get(token);
         if (l == null) {
            l = new ArrayList<Integer>();
            list.put(token, l);
         }
         l.add(pos++);
      }
      return list;
   }
}
