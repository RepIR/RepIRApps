package util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.io.HDFSPath;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.tools.lib.Log; 
import java.io.IOException;

/**
 *
 * @author Jeroen Vuurens
 */
public class copyVocIndex {
  public static Log log = new Log( copyVocIndex.class ); 

   public static void main(String[] args) throws IOException {
      Repository repository = new Repository(args, "newindex");
      RRConfiguration conf = repository.getConf();
      ByteSearch numbers = ByteSearch.create("\\.[0-9][0-9][0-9][0-9]");
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (!newrepository.exists()) {
         log.fatal("Directory %s does not exists, please create", newrepository.getBaseDir().toString());
      }
      for (HDFSPath sourcedir : repository.getBaseDir().getDirs()) {
         HDFSPath destdir = newrepository.getBaseDir().getSubdir(sourcedir.getName());
         if (!destdir.exists()) {
            destdir.mkdirs();
         }
         for (String filename : sourcedir.getFilenames()) {
            if (!filename.endsWith(".PartitionLocation") && 
             !filename.endsWith(".temp") && 
             !numbers.exists(filename)) {
            String newfile = filename.replaceAll(repository.getPrefix(), newrepository.getPrefix());
            String filename2 = destdir.getFilename(newfile);
            if (!HDFSPath.exists(fs, new Path(filename2))) {
               log.printf("copying %s %s", filename, destdir.getFilename(newfile));
               HDFSPath.copy(fs, filename, destdir.getFilename(newfile));
            }
            }
         }
      }

      newrepository.writeConfiguration();
      Datafile fsfilein = RRConfiguration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      Datafile fsfileout = new Datafile( fsfilein.getCanonicalPath().replaceAll( repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
   }

}
