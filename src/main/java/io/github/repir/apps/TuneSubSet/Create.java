package io.github.repir.apps.TuneSubSet;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;

/**
 * Small util to split the Waterloo spamrank list, for use with the extractor of
 * the ClueWeb09 set.
 * @author jeroen
 */
public class Create extends Configured implements Tool {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "");
      System.exit( HDTools.run(conf, new Create()) );
   }

   @Override
   public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
      Configuration conf = (Configuration)getConf();
      String inputdir = conf.getSubString("repository.subsetinput", "input/clueweb/subset.input");
      String outputdir = "dummy";
      HDTools.getFS().delete(new Path(outputdir), true);
      Job job = new Job(conf, "subset list generator");
      job.setJarByClass(Map.class);
      job.setMapperClass(Map.class);
      job.setNumReduceTasks(0);
      FileInputFormat.setMinInputSplitSize(job, Long.MAX_VALUE);
      FileInputFormat.addInputPath(job, new Path(inputdir));
      FileOutputFormat.setOutputPath(job, new Path(outputdir));
      job.waitForCompletion(true);
      return 0;
   }
}
