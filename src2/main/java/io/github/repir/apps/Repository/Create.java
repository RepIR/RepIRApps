package io.github.repir.apps.Repository;

import io.github.htools.hadoop.io.archivereader.ReaderInputFormat;
import io.github.htools.hadoop.io.archivereader.RecordKey;
import io.github.htools.hadoop.io.archivereader.RecordValue;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import static io.github.htools.lib.ClassTools.*;
import io.github.htools.lib.Log;
import io.github.htools.hadoop.Job;
import java.lang.reflect.Constructor;
import org.apache.hadoop.io.NullWritable;

/**
 * Extracts the configured {@link StoredFeature}s from the collection source
 * archives and stores these in the {@link Repository}. Using this tool requires
 * a Repository to be created and the required {@link DictionaryFeature}s to be created (see {@link io.github.repir.apps.Vovcabulary.Create}).
 * <p/>
 * The following configuration settings are used:
 * <ul>
 * <li>repository.prefix: the name of the repository, which is also used as a prefix for all file names.
 * <li>repository.partitions: splits the data over # partitions, allowing retrieval 
 * to split tasks over several mappers.
 * <li>repository.defaultentityreader: class name of the EntityReader used to read the archives
 * <li>repository.assignentityreader: list of "<fileextension> <class extends EntityReader>", that 
 * assign a different EntityReader to be used on files with this extension.
 * <li>repository.inputdir: list of files and/or directories containing the collection
 * archive files. The directories will be searched recursively. See {@link EntityReader}
 * about settings to specify valid/invalid archive files.
 * <li>repository.feature: list of features, with optional parameters appended to
 * their classname, e.g. TermInverted:all to indicate the "all" section of processed
 * entities is used to construct the feature and stored as the "all" channel. The
 * app will only construct {@link StoredFeature}s that implement {@link ReducibleFeature}.
 * </ul>
 * @author jer
 */
public class Create {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args);
      /**
       * These are turned off for a very important reason. Repir will write
       * the output directly to files in the repository. Allowing speculative
       * execution means that two processes will try to generate the same file,
       * which is bound to fail. Therefore, keep this turned off at all times.
       */
      repository.getConf().setMapSpeculativeExecution(false);
      repository.getConf().setBoolean("mapreduce.reduce.speculative", false);
      repository.getConf().setReduceSpeculativeExecution(false);
      
      log.info("mapreduce.job.queuename %s", repository.configuredString("mapreduce.job.queuename"));
      
      Job job = new Job(repository.getConf(), repository.configuredString("repository.prefix"));
      int partitions = repository.configuredInt("repository.onlypartition", -1);
      if (partitions == -1)
         partitions = repository.configuredInt("repository.partitions", -1);
      else
         partitions = 1;
      job.setNumReduceTasks(partitions);
      job.setPartitionerClass(RecordKey.partitioner.class);
      job.setGroupingComparatorClass(RecordKey.FirstGroupingComparator.class);
      job.setSortComparatorClass(RecordKey.SecondarySort.class);
      job.setMapOutputKeyClass(RecordKey.class);
      job.setMapOutputValueClass(RecordValue.class);
      job.setOutputKeyClass(NullWritable.class);
      job.setOutputValueClass(NullWritable.class);
      
      job.setMapperClass(Map.class);
      job.setReducerClass(Reduce.class);

      Class clazz = toClass(repository.configuredString("repository.inputformat", ReaderInputFormat.class.getSimpleName()), ReaderInputFormat.class.getPackage().getName());
      Constructor c = getAssignableConstructor(clazz, ReaderInputFormat.class, Job.class);
      construct(c, job);

      job.waitForCompletion(true);
      log.info("BuildRepository completed");
   }
}
