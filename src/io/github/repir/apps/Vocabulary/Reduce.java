package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.DictionaryFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<Text, LongWritable, NullWritable, NullWritable> {

   public static Log log = new Log(Reduce.class);
   Repository repository;
   ArrayList<DictionaryFeature> features;
   int doccount = 0; // number of documents in the collection
   int mincf;        // threshold, terms must have a cf that exceeds mincf to be in the Vocabulary 
   int voccount = 0; // number of terms in vocabulary
   VocTFFile tffile;

   @Override
   protected void setup(Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      mincf = context.getConfiguration().getInt("vocabulary.mincf", 0);
      tffile = VocTFFile.getVocTFFile(repository); // temporary file, that is self-sorting, and used to construct the DIctionary features afterwards.
      tffile.setBufferSize(10000000);
      tffile.openWrite();
      features = getDictionaryFeatures( repository );
   }

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
         doccount += (int) cf;
      } else {
         if (cf >= mincf) {
            voccount++;
            tffile.term.write(term); // write a term with statistics to the temp file
            tffile.cf.write(cf);
            tffile.df.write(df);
         }
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      tffile.closeWrite();
      repository.setDocumentCount(doccount);
      repository.setVocabularySize(voccount);
      writeVoc( repository, features );
      repository.writeConfiguration(); // write masterfile in the repository
   }
   
   public static void writeVoc(Repository repository, ArrayList<DictionaryFeature> features) {
      int doccount = repository.getDocumentCount();
      long voccount = repository.getVocabularySize();
      for (DictionaryFeature f : features) {
         f.startReduce(voccount, doccount);
      }
      long cf = 0;
      int id = 0;
      VocTFFile tffile = VocTFFile.getVocTFFile(repository);
      tffile.setBufferSize(10000000);
      tffile.openRead();
      while (tffile.next()) { // traverse terms in order of cf
         for (DictionaryFeature f : features) { // write to all DictionaryFeatures
            f.reduceInput(id, tffile.term.value, tffile.cf.value, tffile.df.value);
         }
         cf += tffile.cf.value;
         id++;
      }
      for (DictionaryFeature f : features) {
         f.finishReduce();
      }
      repository.setCF(cf); // set collection term size
      tffile.closeRead();
   }

   protected static ArrayList<DictionaryFeature> getDictionaryFeatures(Repository repository) {
      ArrayList<DictionaryFeature> features = new ArrayList<DictionaryFeature>();
      for (StoredFeature f : repository.getConfiguredFeatures())
         if (f instanceof DictionaryFeature)
            features.add((DictionaryFeature) f );
      return features;
   }
}
