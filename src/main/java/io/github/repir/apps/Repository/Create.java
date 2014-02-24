package io.github.repir.apps.Repository;

import io.github.repir.EntityReader.TermEntityKey;
import io.github.repir.EntityReader.TermEntityValue;
import io.github.repir.EntityReader.RepIRInputFormat;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Lib.Log;
import java.lang.reflect.Constructor;
import io.github.repir.tools.DataTypes.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class Create extends Configured implements Tool {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "");
      Repository repository = new Repository(conf);
      System.exit( HDTools.run(conf, new Create()));
   }

   @Override
   public int run(String[] args) throws Exception {
      Configuration conf = (Configuration)getConf();
      Job job = new Job(conf, "Repository Builder " + conf.get("repository.prefix"));
      job.setJarByClass(RMap.class);

      int partitions = conf.getInt("repository.onlypartition", -1);
      if (partitions == -1)
         partitions = conf.getInt("repository.partitions", -1);
      else
         partitions = 1;
      job.setNumReduceTasks(partitions);
      job.setPartitionerClass(TermEntityKey.partitioner.class);
      job.setGroupingComparatorClass(TermEntityKey.FirstGroupingComparator.class);
      job.setSortComparatorClass(TermEntityKey.SecondarySort.class);
      job.setMapOutputKeyClass(TermEntityKey.class);
      job.setMapOutputValueClass(TermEntityValue.class);
      job.setOutputKeyClass(NullWritable.class);
      job.setOutputValueClass(NullWritable.class);

      job.setMapperClass(RMap.class);
      job.setReducerClass(Reduce.class);

      Class clazz = toClass(conf.getSubString("repository.inputformat", RepIRInputFormat.class.getSimpleName()), RepIRInputFormat.class.getPackage().getName());
      Constructor c = getAssignableConstructor(clazz, RepIRInputFormat.class, Job.class, String[].class);
      construct(c, job, conf.get("repository.inputdir").split(","));

      job.waitForCompletion(true);
      log.info("BuildRepository completed");
      return 0;
   }
}
