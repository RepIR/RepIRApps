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
public class copyIndex {

   public static Log log = new Log(copyIndex.class);

   public static void main(String[] args) throws IOException {
      Repository repository = new Repository(args, "newindex");
      RRConfiguration conf = repository.getConf();
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (!newrepository.exists()) {
         log.fatal("Directory %s does not exists, please create", newrepository.getBaseDir().toString());
      }
      for (String subdir : new String[]{"repository", "dynamic", "pig"}) {
         HDFSPath sourcedir = repository.getBaseDir().getSubdir(subdir);
         HDFSPath destdir = newrepository.getBaseDir().getSubdir(subdir);
         if (!destdir.exists()) {
            destdir.mkdirs();
         }
         for (String filename : sourcedir.getFilenames()) {
            if (!filename.endsWith(".PartitionLocation") && !filename.endsWith(".temp")) {
               String newfile = filename.replaceAll(repository.getPrefix(), newrepository.getPrefix());
               HDFSPath.copy(fs, filename, destdir.getFilename(newfile));
            }
         }
      }
      newrepository.writeConfiguration();
      Datafile fsfilein = RRConfiguration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      Datafile fsfileout = new Datafile(fsfilein.getCanonicalPath().replaceAll(repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
   }

}
