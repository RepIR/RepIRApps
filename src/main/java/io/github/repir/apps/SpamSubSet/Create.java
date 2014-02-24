package io.github.repir.apps.SpamSubSet;

import io.github.repir.tools.Content.HDFSDir;
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
import org.apache.hadoop.util.ToolRunner;

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
      String inputfile = conf.getSubString("splitspamrank.inputfile", "input/clueweb/clueweb09spam.Fusion");
      String outputdir = conf.getSubString("splitspamrank.outputdir", "dummy");
      HDTools.getFS().delete(new Path(outputdir), true);
      Job job = new Job(conf, "Waterloo Clueweb Spam ranking");
      job.setNumReduceTasks(0);
      job.setJarByClass(Map.class);
      job.setMapperClass(Map.class);
      FileInputFormat.setMinInputSplitSize(job, Long.MAX_VALUE);
      FileInputFormat.addInputPath(job, new Path(inputfile));
      FileOutputFormat.setOutputPath(job, new Path(outputdir));
      job.waitForCompletion(true);
      return 0;
   }
}
