package io.github.repir.apps.Repository;

import io.github.repir.EntityReader.MapReduce.EntityWritable;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.ReducibleFeature;
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
public class Map extends Mapper<LongWritable, EntityWritable, TermEntityKey, TermEntityValue> {

   public static Log log = new Log(Map.class);
   private Extractor extractor;
   private Repository repository;
   private FileSystem fs;
   private FileSplit filesplit;
   private int onlypartition;
   TermEntityKey outkey;
   TermEntityValue outvalue = new TermEntityValue();
   AutoFeatures autofeatures;
   DocLiteral collectionid;

   
   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      collectionid = repository.getCollectionIDFeature();
      onlypartition = repository.configuredInt("index.onlypartition", -1);
      autofeatures = new AutoFeatures(repository);

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
         if (onlypartition == -1 || onlypartition == partition) {
            if (onlypartition >= 0)
               partition = 0;
            for (int feature = 0; feature < autofeatures.reduciblepartitionedfeatures.size(); feature++) {
               ReduciblePartitionedFeature sf = autofeatures.reduciblepartitionedfeatures.get(feature);
               //log.info("write sf partition %d feature %d %s docid %s", partition, feature, ((StoredFeature)sf).getCanonicalName(), docid);
               sf.writeMap(context, partition, feature, docid, value.entity);
            }
            for (int feature = 0; feature < autofeatures.reduciblefeatures.size(); feature++) {
               ReducibleFeature sf = autofeatures.reduciblefeatures.get(feature);
               //log.info("write sf feature %d %s docid %s", feature, ((StoredFeature)sf).getCanonicalName(), docid);
               sf.writeMap(context, feature, docid, value.entity);
            }
            for (int feature = 0; feature < autofeatures.termdocfeatures.size(); feature++) {
               AutoTermDocumentFeature tdf = autofeatures.termdocfeatures.get(feature);
               //log.info("write sf partition %d feature %d %s docid %s", partition, feature, ((StoredFeature)tdf).getCanonicalName(), docid);
               tdf.writeMap(context, partition, feature, docid, value.entity);
            }
         }
      }
      context.progress();
   }
}
