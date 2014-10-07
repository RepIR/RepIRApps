package util;

import org.apache.hadoop.fs.FileSystem;
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
public class mvIndex {

   public static Log log = new Log(mvIndex.class);

   public static void main(String[] args) {
      Configuration conf = new Configuration(args, "newindex");
      String newname = conf.get("newindex");
      Repository repository = new Repository(conf);
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (newrepository.exists()) {
         log.fatal("Directory %s exists, please remove", newrepository.getBaseDir().toString());
      }
      HDFSDir sourcedir = repository.getBaseDir();
      HDFSDir destdir = newrepository.getBaseDir();
      sourcedir.move(destdir);
      mv(destdir, repository.getPrefix(), newrepository.getPrefix());
      newrepository.writeConfiguration();
      Datafile fsfilein = Configuration.configfile(args[0]);
      String content = fsfilein.readAsString();
      ByteSearch needle = ByteSearch.create("(?<=\\W)" + repository.getPrefix() + "($|(?=\\W))");
      String content1 = needle.replaceAll(content, newrepository.getPrefix());
      fsfilein.close();
      Datafile fsfileout = new Datafile(fsfilein.getFullPath().replaceAll(repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content1);
      fsfileout.close();
   }
   
   public static void mv(HDFSDir dir, String sourceprefix, String destprefix) {
      dir.move(dir, sourceprefix + "*", destprefix + "*");
      for (HDFSDir d : dir.getSubDirs()) {
         mv(d, sourceprefix, destprefix);
      }
   }

}
