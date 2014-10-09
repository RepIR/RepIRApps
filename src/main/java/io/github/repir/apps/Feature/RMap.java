package io.github.repir.apps.Feature;

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Feature;
import io.github.repir.Repository.ReduciblePartitionedFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

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
   TermEntityKey outkey;
   TermEntityValue outvalue = new TermEntityValue();
   ArrayList<ReduciblePartitionedFeature> documentfeatures = new ArrayList();
   ArrayList<AutoTermDocumentFeature> termdocfeatures = new ArrayList();
   DocLiteral collectionid;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      collectionid = repository.getCollectionIDFeature();
      for (String featurename : repository.configuredStrings("repository.constructfeatures")) {
         Feature f = repository.getFeature(featurename);
            if (f instanceof ReduciblePartitionedFeature) {
               documentfeatures.add((ReduciblePartitionedFeature) f);
            } else if (f instanceof AutoTermDocumentFeature) {
               termdocfeatures.add((AutoTermDocumentFeature) f);
            }
      }
      extractor = new Extractor(repository.getConfiguration());
      fs = repository.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
   }

   @Override
   public void map(LongWritable inkey, EntityWritable value, Context context) throws IOException, InterruptedException {
      extractor.process(value.entity);
      if (value.entity.size() > 0) {
         String docid = collectionid.extract(value.entity);
         int partition = Repository.getPartition(docid, repository.getPartitions());
            for (int feature = 0; feature < documentfeatures.size(); feature++) {
               ReduciblePartitionedFeature sf = documentfeatures.get(feature);
               sf.writeMap(context, partition, feature, docid, value.entity);
            }
            for (int feature = 0; feature < termdocfeatures.size(); feature++) {
               AutoTermDocumentFeature tdf = termdocfeatures.get(feature);
               tdf.writeMap(context, partition, feature, docid, value.entity);
            }
      }
      context.progress();
   }
}
