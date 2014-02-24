package io.github.repir.apps.Repository;

import io.github.repir.EntityReader.EntityWritable;
import io.github.repir.Extractor.Entity;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Repository.DocLiteral;
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
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.ReducableFeature;

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
      onlypartition = repository.getConfigurationInt("index.onlypartition", -1);
      collectionid = repository.getCollectionIDFeature();
      for (Map.Entry<String, StoredFeature> entry : repository.storedfeaturesmap.entrySet()) {
         //log.info("%s %s", entry.getKey(), entry.getValue());
         if (entry.getValue() instanceof ReducableFeature) {
            if (entry.getValue() instanceof EntityStoredFeature) {
               documentfeatures.add((EntityStoredFeature) entry.getValue());
            } else if (entry.getValue() instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) entry.getValue());
            }
         }
      }
      extractor = new Extractor(repository);
      fs = HDTools.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
   }

   @Override
   public void map(LongWritable inkey, EntityWritable value, Context context) throws IOException, InterruptedException {
      //log.info("%s", value.entity.get("collectionid").getContentStr());
      //if (value.entity.get("collectionid").getContentStr().equals("clueweb12-0000tw-31-13675")) {
      //   log.info("%s %s", value.entity.get("collectionid").getContentStr(), new String(value.entity.content));
      //}
      extractor.process(value.entity);
      if (value.entity.size() > 0) {
         String docid = collectionid.extract(value.entity);
         //log.info("%s %s", docid, repository);
         int partition = Repository.partition(docid, repository.getPartitions());
         if (onlypartition == -1 || onlypartition == partition) {
            if (onlypartition >= 0)
               partition = 0;
            for (int feature = 0; feature < documentfeatures.size(); feature++) {
               EntityStoredFeature sf = documentfeatures.get(feature);
               outkey = TermEntityKey.createDoc(partition, feature, docid);
               sf.mapOutput(outvalue, value.entity);
               context.write(outkey, outvalue);
            }
            for (int feature = 0; feature < termdocfeatures.size(); feature++) {
               AutoTermDocumentFeature tdf = termdocfeatures.get(feature);
               HashMap<Integer, ArrayList<Integer>> tokens = getTokens(value.entity, tdf);
               //if (value.entity.get("collectionid").getContentStr().equals("clueweb12-0000tw-31-13675")) {
               //   log.info("tokens %s", new TreeSet<Integer>(tokens.keySet()));
               //}
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
      EntityAttribute attr = doc.get(channel.getField());
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
