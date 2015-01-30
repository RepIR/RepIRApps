package util;

import io.github.repir.Repository.Repository;
import io.github.repir.Repository.SynStats;
import io.github.repir.Repository.SynStats.Record;
import io.github.repir.tools.lib.ArgsParser;
import io.github.repir.tools.lib.Log;

public class listsyn {

   public static Log log = new Log(listsyn.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile");
      Repository repository = new Repository(parsedargs.get("configfile"));
      
      SynStats f = SynStats.get(repository);
      list(f);
   }
   
   public static void list(SynStats f) {
      f.openRead();
      for (Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
