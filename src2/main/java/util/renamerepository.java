package util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.repir.Repository.Repository;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.htools.lib.Log; 
import java.io.IOException;

/**
 *
 * @author Jeroen Vuurens
 */
public class renamerepository {
  public static Log log = new Log( renamerepository.class ); 

   public static void main(String[] args) throws IOException {
      Repository repository = new Repository(args, "newindex");
      RRConfiguration conf = repository.getConf();
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      Datafile fsfilein = RRConfiguration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      HDFSPath sourcedir = repository.getBaseDir();
      HDFSPath destdir = newrepository.getBaseDir();
      Datafile fsfileout = fsfilein.getDir().getFile(fsfilein.getName().replaceAll( repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
      HDFSPath.rename(fs, sourcedir, destdir);
      destdir = destdir.getSubdir("repository");
      for (String filename : destdir.getFilenames()) {
            String newfile = filename.replaceAll(repository.getPrefix(), newrepository.getPrefix());
            HDFSPath.rename(fs, destdir.getFilename(filename), destdir.getFilename(newfile));
      }
      destdir = destdir.getSubdir("dynamic");
      for (String filename : destdir.getFilenames()) {
         String newfile = filename.replaceAll(repository.getPrefix(), newrepository.getPrefix());
         HDFSPath.rename(fs, destdir.getFilename(filename), destdir.getFilename(newfile));
      }
      newrepository.writeConfiguration();
   }

}
