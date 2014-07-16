package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * Mapper for the extraction of tokens, to reduce into {@link DictionaryFeature}s
 * along with their collection statistics.
 * @author jer
 */
public class RecoverMap extends Mapper<IntWritable, NullWritable, Text, LongWritable> {

   public static Log log = new Log(RecoverMap.class);
   private Repository repository;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
   }

   @Override
   public void map(IntWritable inkey, NullWritable invalue, Context context) throws IOException, InterruptedException {
      Text term = new Text("###doccount###");
      LongWritable value = new LongWritable(repository.getDocumentCount());
      context.write(term, value);
      
      VocTFFile tffile = new VocTFFile(CreateRecover.getVocTFInFile(repository));
      tffile.openRead();
      while (tffile.next()) {
          term.set(tffile.term.value);
          value.set(tffile.cf.value);
          context.write(term, value);
          value.set(-tffile.df.value);
          context.write(term, value);
      }
      tffile.closeRead();
   }
}
