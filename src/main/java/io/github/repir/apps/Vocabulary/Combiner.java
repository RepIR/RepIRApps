package io.github.repir.apps.Vocabulary;

import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Combiner extends Reducer<Text, LongWritable, Text, LongWritable> {

   public static Log log = new Log(Combiner.class);

   @Override
   public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
      String term = key.toString();
      long cf = 0;
      long df = 0;
      for (LongWritable l : values) {
         long value = l.get();
         if (value >= 0)
            cf += value;
         else
            df -= value;
      }
      if (term.equals("###doccount###")) { // use cf to send the number of documents
         context.write(key, new LongWritable(cf));
      } else {
         context.write(key, new LongWritable(cf));
         context.write(key, new LongWritable(-df));
      }
   }
}
