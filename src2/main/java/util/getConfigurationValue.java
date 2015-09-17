package util;

import io.github.repir.Repository.Repository;
import io.github.htools.lib.Log;

public class getConfigurationValue {

   public static Log log = new Log(getConfigurationValue.class);
   
   public static void main(String[] args) {
      Repository repository = new Repository(args[0]);
      System.out.println(repository.configuredString(args[1]));
   }
}
