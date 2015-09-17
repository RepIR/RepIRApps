package util;

import org.apache.hadoop.fs.FileSystem;
import io.github.repir.Repository.Repository;
import io.github.htools.search.ByteSearch;
import io.github.htools.io.Datafile;
import io.github.htools.io.HDFSPath;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.htools.lib.Log;
import java.io.IOException;

/**
 *
 * @author Jeroen Vuurens
 */
public class mvIndex {

   public static Log log = new Log(mvIndex.class);

   public static void main(String[] args) throws IOException {
      Repository repository = new Repository(args, "newindex");
      RRConfiguration conf = repository.getConf();
      String newname = conf.get("newindex");
      FileSystem fs = repository.getFS();
      Repository newrepository = new Repository(conf);
      newrepository.changeName(newname);
      if (newrepository.exists()) {
         log.fatal("Directory %s exists, please remove", newrepository.getBaseDir().toString());
      }
      HDFSPath sourcedir = repository.getBaseDir();
      HDFSPath destdir = newrepository.getBaseDir();
      sourcedir.move(destdir);
      mv(destdir, repository.getPrefix(), newrepository.getPrefix());
      newrepository.writeConfiguration();
      Datafile fsfilein = RRConfiguration.configfile(args[0]);
      String content = fsfilein.readAsString();
      ByteSearch needle = ByteSearch.create("(?<=\\W)" + repository.getPrefix() + "($|(?=\\W))");
      String content1 = needle.replaceAll(content, newrepository.getPrefix());
      fsfilein.close();
      Datafile fsfileout = new Datafile(fsfilein.getCanonicalPath().replaceAll(repository.getPrefix(), newrepository.getPrefix()));
      fsfileout.printf("%s", content1);
      fsfileout.close();
   }
   
   public static void mv(HDFSPath dir, String sourceprefix, String destprefix) throws IOException {
      dir.move(dir, sourceprefix + "*", destprefix + "*");
      for (HDFSPath d : dir.getDirs()) {
         mv(d, sourceprefix, destprefix);
      }
   }

}
