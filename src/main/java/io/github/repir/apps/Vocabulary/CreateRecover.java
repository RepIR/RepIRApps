package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.Log;
import io.github.repir.MapReduceTools.NullInputFormat;
import io.github.repir.tools.hadoop.Job;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

/**
 * Recovers a failed Vocabulary build using the repo.voctf.temp file.
 * @author jer
 */

public class CreateRecover {

   public static Log log = new Log(CreateRecover.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args);

      // rename voctf.temp to voctf.temp.in
      Datafile tempfile = VocTFFile.getVocTFFile(repository).getTempfile();
      if (!tempfile.rename(tempfile.getSubFile(".in")))
         log.fatal("cannot rename %s", tempfile.getCanonicalPath());
      
      Job job = new Job(repository.getConf(), repository.configuredString("repository.prefix"));
      job.setNumReduceTasks(1);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(LongWritable.class);
      job.setOutputKeyClass(NullWritable.class);
      job.setOutputValueClass(NullWritable.class);
      job.setMapperClass(RecoverMap.class);
      job.setReducerClass(Reduce.class);
      job.setOutputFormatClass(NullOutputFormat.class);
      
      // set input to <null, 0>, to execute mapper once
      NullInputFormat inputformat = new NullInputFormat(repository);
      inputformat.addSingle(0); 
      job.setInputFormatClass(inputformat.getClass());
      
      job.waitForCompletion(true);
   }
   
   public static Datafile getVocTFInFile(Repository repository) {
       return VocTFFile.getVocTFFile(repository).getTempfile().getSubFile(".in");
   }
}
