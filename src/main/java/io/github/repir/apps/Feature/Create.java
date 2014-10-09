package io.github.repir.apps.Feature;

import io.github.repir.EntityReader.MapReduce.EntityReaderInputFormat;
import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.MapReduceTools.Configuration;
import static io.github.repir.tools.Lib.ClassTools.*;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.hadoop.Job;
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
       Repository repository = new Repository(args, "{feature}");
      Configuration conf = repository.getConfiguration();
      conf.setStrings("repository.constructfeatures", conf.getStrings("feature"));
      Job job = new Job(conf, "Repository Builder " + conf.get("repository.prefix"));
      int partitions = conf.getInt("repository.partitions", -1);
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

      Class clazz = toClass(conf.get("repository.inputformat", EntityReaderInputFormat.class.getSimpleName()), EntityReaderInputFormat.class.getPackage().getName());
      Constructor c = getAssignableConstructor(clazz, EntityReaderInputFormat.class, Job.class);
      construct(c, job);

      job.waitForCompletion(true);
      log.info("BuildRepository completed");
   }
}
