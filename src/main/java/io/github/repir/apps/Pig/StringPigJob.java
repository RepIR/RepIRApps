package io.github.repir.apps.Pig;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import io.github.repir.MapReduceTools.StringInputFormat;
import java.io.IOException;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.lib.output.NullOutputFormat;

/**
 * Extension of Hadoop Job, used by JobManager to start multi-threaded 
 * rerieval.
 * @author jer
 */
public class StringPigJob extends Job {

   public static Log log = new Log(StringPigJob.class);
   public StringInputFormat inputformat;

   public StringPigJob(Repository repository) throws IOException {
      super(repository.getConf(), repository.configuredString("rr.conf"));
      inputformat = new StringInputFormat(repository);
      setMapOutputKeyClass(NullWritable.class);
      setMapOutputValueClass(NullWritable.class);
      setOutputKeyClass(NullWritable.class);
      setOutputValueClass(NullWritable.class);
      setMapperClass(TermDocMap.class);
      setInputFormatClass(inputformat.getClass());
      setOutputFormatClass(NullOutputFormat.class);
      this.setNumReduceTasks(0);
   }

   public void addTerm(String term) {
      inputformat.add(term);
   }
}
