package io.github.repir.apps.TuneSubSet;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.MapReduce.Job;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * This class creates a set of files that indicate which files are to be indexed 
 * to build a Repository. We used this to construct a Repository containing the 
 * top-10k documents of all retrieved models, and the documents in the TREC qrels,
 * to tune their parameters. The input consists of Qrel-like files, where every line
 * is a whitespace separated entry, that has one clueweb-id, which is the only word
 * that starts with a 'c'. These ClueWeb ID's are used to create {@link SubSetFile}s.
 * These subset files can be configured setting a directory "repository.subsetinput", to use only
 * the documents registered in those files.
 * <p/>
 * To create a repository for a subset of the collection, we concatenated the TREC
 * qrels with the top-10k result files. We used this tool to create the SubSetFiles. 
 * We then created a new folder for the Repository used for tuning. We copied the 
 * {@link DictionaryFeature}s and master file from the full repository to this folder. 
 * Renaming the Repository name in the master file's contents (flat text) and file names.
 * We then configured "repository.subsetinput", and ran the default 
 * {@link io.github.repir.apps.Repository.Create}. We then copied the {@link ProximityStats}
 * to the new {@link Repository}. This ensures that the new Repository uses the 
 * same statistics as the full collection, but is much faster for retrieval because 
 * of the much smaller size (0.1%-1%).
 * @author jeroen
 */
public class Create {

   public static Log log = new Log(Create.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args[0]);
      String inputdir = repository.configuredString("repository.subsetinput", "input/clueweb/subset.input");
      String outputdir = "dummy";
      repository.getFS().delete(new Path(outputdir), true);
      Job job = new Job(repository, "subset list generator");
      job.setMapperClass(Map.class);
      job.setNumReduceTasks(0);
      FileInputFormat.setMinInputSplitSize(job, Long.MAX_VALUE);
      FileInputFormat.addInputPath(job, new Path(inputdir));
      FileOutputFormat.setOutputPath(job, new Path(outputdir));
      job.waitForCompletion(true);
   }
}
