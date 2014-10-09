package util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.MapReduceTools.Configuration;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class copyVocIndex {
  public static Log log = new Log( copyVocIndex.class ); 

   public static void main(String[] args) {
      Repository repository = new Repository(args, "newindex");
      Configuration conf = repository.getConfiguration();
      ByteSearch numbers = ByteSearch.create("\\.[0-9][0-9][0-9][0-9]");
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (!newrepository.exists()) {
         log.fatal("Directory %s does not exists, please create", newrepository.getBaseDir().toString());
      }
      for (HDFSDir sourcedir : repository.getBaseDir().getSubDirs()) {
         HDFSDir destdir = newrepository.getBaseDir().getSubdir(sourcedir.getName());
         if (!destdir.exists()) {
            destdir.mkdirs();
         }
         for (Path p : sourcedir.getFiles()) {
            if (!p.getName().endsWith(".PartitionLocation") && 
             !p.getName().endsWith(".temp") && 
             !numbers.exists(p.getName())) {
            String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
            String filename = destdir.getFilename(newfile);
            if (!HDFSDir.exists(fs, new Path(filename))) {
               log.printf("copying %s %s", p.toString(), destdir.getFilename(newfile));
               HDFSDir.copy(fs, p.toString(), destdir.getFilename(newfile));
            }
            }
         }
      }

      newrepository.writeConfiguration();
      Datafile fsfilein = Configuration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      Datafile fsfileout = new Datafile( fsfilein.getFullPath().replaceAll( repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
   }

}
