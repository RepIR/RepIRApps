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
public class copyIndex {

   public static Log log = new Log(copyIndex.class);

   public static void main(String[] args) {
      Configuration conf = new Configuration(args, "newindex");
      String newname = conf.get("newindex");
      Repository repository = new Repository(conf);
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (!newrepository.exists()) {
         log.fatal("Directory %s does not exists, please create", newrepository.getBaseDir().toString());
      }
      for (String subdir : new String[]{"repository", "dynamic", "pig"}) {
         HDFSDir sourcedir = repository.getBaseDir().getSubdir(subdir);
         HDFSDir destdir = newrepository.getBaseDir().getSubdir(subdir);
         if (!destdir.exists()) {
            destdir.mkdirs();
         }
         for (Path p : sourcedir.getFiles()) {
            if (!p.getName().endsWith(".PartitionLocation") && !p.getName().endsWith(".temp")) {
               String newfile = p.getName().replaceAll(repository.getPrefix(), newrepository.getPrefix());
               HDFSDir.copy(fs, p.toString(), destdir.getFilename(newfile));
            }
         }
      }
      newrepository.writeConfiguration();
      Datafile fsfilein = Configuration.configfile(args[0]);
      String content = fsfilein.readAsString();
      content = content.replaceAll(repository.getPrefix(), newrepository.getPrefix());
      Datafile fsfileout = new Datafile(fsfilein.getFullPath().replaceAll(repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content);
      fsfileout.close();
   }

}
