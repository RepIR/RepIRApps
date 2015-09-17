package io.github.repir.apps.Vocabulary;

import io.github.htools.hadoop.io.archivereader.ReaderInputFormat;
import io.github.repir.Repository.Repository;
import io.github.htools.lib.ClassTools;
import io.github.htools.lib.Log;
import io.github.htools.hadoop.Job;
import java.lang.reflect.Constructor;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;

/**
 * Sets up a new Repository, extracts the configured {@link DictionaryFeature}s 
 * from the collection source archives and stores these in the {@link Repository},
 * and sets collection statistics (document count, vocabulary count, collection size). 
 * Using this tool requires the destination folder for the Repository to be created. This 
 * folder will be emptied! The {@link DictionaryFeature}s will not be segmented into
 * partitions (different than {@ReducibleFeature}s), there will be only one shared 
 * file for every of these features, so one Vocabulary for the entire {@link Repository}.
 * <p/>
 * The following configuration settings are used:
 * <ul>
 * <li>repository.prefix: the name of the repository, which is also used as a prefix for all file names.
 * <li>repository.entityreader: class name of the EntityReader used to read the archives
 * <li>repository.assignentityreader: list of "<fileextension> <class extends EntityReader>", that 
 * assign a different EntityReader to be used on files with this extension.
 * <li>repository.inputdir: list of files and/or directories containing the collection
 * archive files. The directories will be searched recursively. See {@link EntityReader}
 * about settings to specify valid/invalid archive files.
 * <li>repository.feature: list of features. The
 * app will only construct {@link StoredFeature}s that extend {@link DictionaryFeature}.
 * </ul>
 * @author jer
 */

public class Create {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args);
      repository.getConf().setReduceSpeculativeExecution(false);
      repository.deleteMasterFile();
      repository.getConf().setMaxSimultaneousMappers(1);
      Job job = new Job(repository.getConf(), repository.configuredString("repository.prefix"));
      job.setNumReduceTasks(1);
      job.setMapOutputKeyClass(Text.class);
      job.setMapOutputValueClass(LongWritable.class);
      job.setOutputKeyClass(NullWritable.class);
      job.setOutputValueClass(NullWritable.class);
      job.setMapperClass(VMap.class);
      job.setReducerClass(Reduce.class);
      repository.getFS().delete(new Path(repository.configuredString("repository.dir")), true);
      repository.getFS().mkdirs(new Path(repository.configuredString("repository.dir")));
      Class clazz = ClassTools.toClass(repository.configuredString("repository.inputformat", ReaderInputFormat.class.getSimpleName()), ReaderInputFormat.class.getPackage().getName());
      Constructor c = ClassTools.getAssignableConstructor(clazz, ReaderInputFormat.class, Job.class);
      ClassTools.construct(c, job);
      job.waitForCompletion(true);
   }
}
