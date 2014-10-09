package util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.MapReduceTools.Configuration;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class renamerepository {
  public static Log log = new Log( renamerepository.class ); 

   public static void main(String[] args) {
      Repository repository = new Repository(args, "newindex");
      Configuration conf = repository.getConfiguration();
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      Datafile fsfilein = Configuration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      HDFSDir sourcedir = repository.getBaseDir();
      HDFSDir destdir = newrepository.getBaseDir();
      Datafile fsfileout = fsfilein.getDir().getFile(fsfilein.getFilename().replaceAll( repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
      HDFSDir.rename(fs, sourcedir, destdir);
      destdir = destdir.getSubdir("repository");
      for (Path p : destdir.getFiles()) {
            String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
            HDFSDir.rename(fs, p.toString(), destdir.getFilename(newfile));
      }
      destdir = destdir.getSubdir("dynamic");
      for (Path p : destdir.getFiles()) {
         String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
         HDFSDir.rename(fs, p.toString(), destdir.getFilename(newfile));
      }
      newrepository.writeConfiguration();
   }

}
