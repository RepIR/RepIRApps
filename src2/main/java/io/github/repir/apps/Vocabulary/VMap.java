package io.github.repir.apps.Vocabulary;

import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.ExtractorConf;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermInverted;
import io.github.htools.extract.Content;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/**
 * Mapper for the extraction of tokens, to reduce into {@link DictionaryFeature}s
 * along with their collection statistics.
 * @author jer
 */
public class VMap extends Mapper<LongWritable, Content, Text, LongWritable> {

   public static Log log = new Log(VMap.class);
   private ExtractorConf extractor;
   private Repository repository;
   private FileSystem fs;
   private FileSplit filesplit;
   private long doccount = 0;
   private HashMap<String, Term> voclong = new HashMap<String, Term>();
   private HashMap<String, Term> vocshort = new HashMap<String, Term>();
   private TermInverted all;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      fs = repository.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
      extractor = new ExtractorConf(repository.getConf());
      all = TermInverted.get(repository, "all");
   }

   @Override
   public void map(LongWritable key, Content value, Context context) throws IOException, InterruptedException {
      extractor.process(value);
      if (value.size() > 0) {
         doccount++;
         HashSet<String> termsindocument = new HashSet<String>();
         for (Map.Entry<String, ExtractChannel> e : value.entrySet()) {
            if (e.getKey().equals(all.entityAttribute())) {
               for (String chunk : e.getValue()) {
                  termsindocument.add(chunk);
                  Term t = vocshort.get(chunk);
                  if (t == null) {
                     t = new Term();
                     vocshort.put(chunk, t);
                  } else {
                     t.cf++;
                  }
               }
            }
         }
         for (String t: termsindocument)
            vocshort.get(t).df++;
         if (vocshort.size() > 1000000) {
            write(context, 5);
         }
         context.progress();
      }
   }

   /**
    * output consists of a mixed records: (###doccount###, count), (term, cf), 
    * (term, -df), where the sign of the count is used to distinguish between
    * cf and df in the reducer.
    */
   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      write(context, Integer.MAX_VALUE);
      Text term = new Text("###doccount###");
      LongWritable value = new LongWritable(doccount);
      context.write(term, value);
      for (Map.Entry<String, Term> t : voclong.entrySet()) {
         term.set(t.getKey());
         value.set(t.getValue().cf);
         context.write(term, value);
         value.set(-t.getValue().df);
         context.write(term, value);
      }
   }

   protected void write(Context context, long threshold) throws IOException, InterruptedException {
      Text term = new Text();
      LongWritable value = new LongWritable(0);
      for (Map.Entry<String, Term> t : vocshort.entrySet()) {
         Term exists = voclong.get(t.getKey());
         if (exists != null) {
             exists.cf += t.getValue().cf;
             exists.df += t.getValue().df;
         } else if (t.getValue().cf > threshold) {
             voclong.put(t.getKey(), t.getValue());
         } else {
            term.set(t.getKey());
            value.set(t.getValue().cf);
            context.write(term, value);
            value.set(-t.getValue().df);
            context.write(term, value);
         }
      }
      vocshort = new HashMap<String, Term>();
   }
   
   class Term {
      long cf = 1;
      int df;
   }
}
