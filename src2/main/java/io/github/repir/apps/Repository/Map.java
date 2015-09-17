package io.github.repir.apps.Repository;

import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.htools.extract.ExtractorConf;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.ReducibleFeature;
import io.github.repir.Repository.ReduciblePartitionedFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.htools.extract.Content;
import io.github.htools.lib.Log;
import java.io.IOException;
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
public class Map extends Mapper<LongWritable, Content, RecordKey, RecordValue> {

   public static Log log = new Log(Map.class);
   private ExtractorConf extractor;
   private Repository repository;
   private FileSystem fs;
   private FileSplit filesplit;
   private int onlypartition;
   RecordKey outkey;
   RecordValue outvalue = new RecordValue();
   AutoFeatures autofeatures;
   DocLiteral collectionid;

   
   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      collectionid = repository.getCollectionIDFeature();
      onlypartition = repository.configuredInt("index.onlypartition", -1);
      autofeatures = new AutoFeatures(repository);

      extractor = new ExtractorConf(repository.getConf());
      fs = repository.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
   }

   @Override
   public void map(LongWritable inkey, Content value, Context context) throws IOException, InterruptedException {
      extractor.process(value);
      if (value.size() > 0) {
         String docid = collectionid.extract(value);
         int partition = Repository.getPartition(docid, repository.getPartitions());
         if (onlypartition == -1 || onlypartition == partition) {
            if (onlypartition >= 0)
               partition = 0;
            for (int feature = 0; feature < autofeatures.reduciblepartitionedfeatures.size(); feature++) {
               ReduciblePartitionedFeature sf = autofeatures.reduciblepartitionedfeatures.get(feature);
               //log.info("write sf partition %d feature %d %s docid %s", partition, feature, ((StoredFeature)sf).getCanonicalName(), docid);
               sf.writeMap(context, partition, feature, docid, value);
            }
            for (int feature = 0; feature < autofeatures.reduciblefeatures.size(); feature++) {
               ReducibleFeature sf = autofeatures.reduciblefeatures.get(feature);
               //log.info("write sf feature %d %s docid %s", feature, ((StoredFeature)sf).getCanonicalName(), docid);
               sf.writeMap(context, feature, docid, value);
            }
            for (int feature = 0; feature < autofeatures.termdocfeatures.size(); feature++) {
               AutoTermDocumentFeature tdf = autofeatures.termdocfeatures.get(feature);
               //log.info("write sf partition %d feature %d %s docid %s", partition, feature, ((StoredFeature)tdf).getCanonicalName(), docid);
               tdf.writeMap(context, partition, feature, docid, value);
            }
         }
      }
      context.progress();
   }
}
