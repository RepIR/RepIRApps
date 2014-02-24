package util;

import io.github.repir.Repository.PhraseStats;
import io.github.repir.Repository.PhraseStats.Record;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;

public class listphrase {

   public static Log log = new Log(listphrase.class);
   
   public static void main(String[] args) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile");
      Repository repository = new Repository(parsedargs.get("configfile"));
      
      PhraseStats f = (PhraseStats) repository.getFeature("PhraseStats");
      list(f);
   }
   
   public static void list(PhraseStats f) {
      f.openRead();
      for (Record r : f.getKeys()) {
         log.printf("list %s", r);
      }
   }
}
