package io.github.repir.apps.Vocabulary;

import io.github.repir.EntityReader.EntityWritable;
import io.github.repir.Extractor.EntityAttribute;
import io.github.repir.Extractor.Extractor;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermInverted;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import io.github.repir.tools.DataTypes.ByteArrayPos;

public class VMap extends Mapper<LongWritable, EntityWritable, Text, LongWritable> {

   public static Log log = new Log(VMap.class);
   private Extractor extractor;
   private Repository repository;
   private FileSystem fs;
   private FileSplit filesplit;
   private long doccount = 0;
   private HashMap<String, Term> voc = new HashMap<String, Term>();
   private TermInverted all;
   //private englishStemmer stemmer;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      fs = HDTools.getFS();
      filesplit = ((FileSplit) context.getInputSplit());
      extractor = new Extractor(repository);
      all = (TermInverted) repository.getFeature("TermInverted:all");
      //log.info("%s", filesplit.getPath().toString());
   }

   @Override
   public void map(LongWritable key, EntityWritable value, Context context) throws IOException, InterruptedException {
      extractor.process(value.entity);
      if (value.entity.size() > 0) {
         doccount++;
         HashSet<String> termsindocument = new HashSet<String>();
         for (Map.Entry<String, EntityAttribute> e : value.entity.entrySet()) {
            //log.info( "match %s %s %d", e.getKey(), all.getField(), e.getValue().size() );
            if (e.getKey().equals(all.getField())) {
               for (String chunk : e.getValue()) {
                  termsindocument.add(chunk);
                  Term t = voc.get(chunk);
                  if (t == null) {
                     t = new Term();
                     voc.put(chunk, t);
                  } else {
                     t.tf++;
                  }
               }
            } else {
               //log.info("document %s %s", e.getKey(), e.getValue().getContentStr());
            }
         }
         for (String t: termsindocument)
            voc.get(t).df++;
         if (voc.size() > 1000000) {
            write(context, 5);
         }
         context.progress();
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      Text term = new Text("###doccount###");
      LongWritable value = new LongWritable(doccount);
      context.write(term, value);
      for (Map.Entry<String, Term> t : voc.entrySet()) {
         //log.info("%s %d", t.getKey(), t.getValue());
         term.set(t.getKey());
         value.set(t.getValue().tf);
         context.write(term, value);
         value.set(-t.getValue().df);
         context.write(term, value);
      }
      Log.reportProfile();
   }

   protected void write(Context context, long threshold) throws IOException, InterruptedException {
      Iterator<Entry<String, Term>> iter = voc.entrySet().iterator();
      Text term = new Text();
      LongWritable value = new LongWritable(0);
      while (iter.hasNext()) {
         Entry<String, Term> t = iter.next();
         if (t.getValue().tf < threshold) {
            term.set(t.getKey());
            value.set(t.getValue().tf);
            context.write(term, value);
            value.set(-t.getValue().df);
            context.write(term, value);
            iter.remove();
         }
      }
      System.gc();
   }
   
   class Term {
      long tf = 1;
      int df;
   }
}
