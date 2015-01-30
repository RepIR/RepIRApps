package io.github.repir.apps.SpamSubSet;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * Small util to split the Waterloo spamrank list, for use with the extractor of
 * the ClueWeb09 set.
 * @author jeroen
 */
public class Create {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      String inputfile = repository.configuredString("splitspamrank.inputfile", "input/clueweb/clueweb09spam.Fusion");
      String outputdir = repository.configuredString("splitspamrank.outputdir", "dummy");
      repository.getFS().delete(new Path(outputdir), true);
      Job job = new Job(repository.getConf(), args[0]);
      job.setNumReduceTasks(0);
      job.setMapperClass(Map.class);
      FileInputFormat.setMinInputSplitSize(job, Long.MAX_VALUE);
      FileInputFormat.addInputPath(job, new Path(inputfile));
      FileOutputFormat.setOutputPath(job, new Path(outputdir));
      job.waitForCompletion(true);
   }
}
