package util;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class copyIndex {
  public static Log log = new Log( copyIndex.class ); 

   public static void main(String[] args) {
      Configuration conf = HDTools.readConfigNoMR(args, "newindex");
      String newname = conf.get("newindex");
      Repository repository = new Repository(conf);
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      HDFSDir sourcedir = repository.getBaseDir().getSubdir("repository");
      HDFSDir destdir = newrepository.getBaseDir().getSubdir("repository");
      if (!destdir.exists())
         destdir.mkdirs();
      for (Path p : sourcedir.getFiles()) {
         String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
         HDFSDir.copy(fs, p.toString(), destdir.getFilename(newfile));
      }
      sourcedir = sourcedir.getSubdir("temp");
      destdir = destdir.getSubdir("temp");
      if (!destdir.exists())
         destdir.mkdirs();
      for (Path p : sourcedir.getFiles()) {
         String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
         HDFSDir.copy(fs, p.toString(), destdir.getFilename(newfile));
      }
      newrepository.writeConfiguration();
      Datafile fsfilein = HDTools.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      Datafile fsfileout = new Datafile( fsfilein.getFullPath().replaceAll( repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
   }

}
