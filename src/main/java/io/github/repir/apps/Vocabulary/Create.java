package io.github.repir.apps.Vocabulary;

import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.EntityReader.RepIRInputFormat;
import io.github.repir.tools.Lib.ClassTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.lang.reflect.Constructor;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Create extends Configured implements Tool {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "");
      Repository repository = new Repository(conf);
      repository.deleteMasterFile();
      conf = HDTools.readConfig(args, "");
      System.exit(HDTools.run(conf, new Create()));
   }

   @Override
   public int run(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
      Configuration conf = (Configuration)getConf();
      conf.setInt("mapred.tasktracker.map.tasks.maximum", 1);
      conf.setInt("mapred.map.tasks", 1);
      Job job = new Job(conf, "Vocabulary Builder " + conf.get("repository.prefix"));
      job.setJarByClass(VMap.class);
      job.setNumReduceTasks(1);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(LongWritable.class);
      job.setOutputKeyClass(NullWritable.class);
      job.setOutputValueClass(NullWritable.class);
      job.setMapperClass(VMap.class);
      job.setReducerClass(Reduce.class);
      HDTools.getFS().delete(new Path(conf.get("repository.dir")), true);
      HDTools.getFS().mkdirs(new Path(conf.get("repository.dir")));
      Class clazz = ClassTools.toClass(conf.getSubString("repository.inputformat", RepIRInputFormat.class.getSimpleName()), RepIRInputFormat.class.getPackage().getName());
      Constructor c = ClassTools.getAssignableConstructor(clazz, RepIRInputFormat.class, Job.class, String[].class);
      ClassTools.construct(c, job, conf.get("repository.inputdir").split(","));
      job.waitForCompletion(true);
      return 0;
   }
}
