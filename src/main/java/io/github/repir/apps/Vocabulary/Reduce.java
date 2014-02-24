package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.DictionaryFeature;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import io.github.repir.Repository.TermID;
import io.github.repir.tools.Lib.HDTools;

public class Reduce extends Reducer<Text, LongWritable, NullWritable, NullWritable> {

   public static Log log = new Log(Reduce.class);
   Repository repository;
   ArrayList<DictionaryFeature> features;
   int doccount = 0;
   int mintf;
   int voccount = 0;
   VocTFFile tffile;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      mintf = context.getConfiguration().getInt("vocabulary.mintf", 0);
      tffile = VocTFFile.getVocTFFile(repository);
      tffile.setBufferSize(1000000);
      tffile.openWrite();
      features = getDictionaryFeatures( repository );
   }

   @Override
   public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
      String term = key.toString();
      long tf = 0;
      long df = 0;
      for (LongWritable l : values) {
         long value = l.get();
         if (value >= 0)
            tf += value;
         else
            df -= value;
      }
      if (term.equals("###doccount###")) { // use tf to send the number of documents
         doccount += (int) tf;
      } else {
         if (tf >= mintf) {
            voccount++;
            tffile.term.write(term);
            tffile.tf.write(tf);
            tffile.df.write(df);
         }
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      tffile.closeWrite();
      repository.setDocumentCount(doccount);
      repository.setVocabularySize(voccount);
      //repository.setHashTableCapacity(voccount);
      
      writeVoc( repository, features );
      repository.writeConfiguration();
      //tffile.getDatafile().delete();
   }
   
   public static void writeVoc(Repository repository, ArrayList<DictionaryFeature> features) {
      int doccount = repository.getDocumentCount();
      long voccount = repository.getVocabularySize();
      //repository.setHashTableCapacity(voccount);
      
      for (DictionaryFeature f : features) {
         f.startReduce(voccount, doccount);
      }
      long tf = 0;
      int id = 0;
      //VocMemTermFile.Record memtermrecord = memtermfile.new Record(null);
      VocTFFile tffile = VocTFFile.getVocTFFile(repository);
      tffile.openRead();
      while (tffile.next()) {
         for (DictionaryFeature f : features) {
            f.reduceInput(id, tffile.term.value, tffile.tf.value, tffile.df.value);
         }
          tf += tffile.tf.value;
         id++;
      }
      for (DictionaryFeature f : features) {
         f.finishReduce();
      }
      repository.setTF(tf);
      tffile.closeRead();
   }

   protected static ArrayList<DictionaryFeature> getDictionaryFeatures(Repository repository) {
      ArrayList<DictionaryFeature> features = new ArrayList<DictionaryFeature>();
      for (StoredFeature f : repository.getFeatures())
         if (f instanceof DictionaryFeature)
            features.add((DictionaryFeature) f );
      return features;
   }
   
   public static void main(String[] args) {
      Configuration conf = HDTools.readConfig(args[0]);
      Repository repository = new Repository( conf );
      ArrayList<DictionaryFeature> features = getDictionaryFeatures(repository);
      ArrayList<DictionaryFeature> features1 = new ArrayList<DictionaryFeature>();
      for (DictionaryFeature f : features)
         if (f instanceof TermID)
            features1.add(f);
      writeVoc(repository, features1);
   }
}
