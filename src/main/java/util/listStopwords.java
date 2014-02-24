package util;
import java.util.HashSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StopwordsCache;
import io.github.repir.Repository.TermString;
import io.github.repir.Strategy.Tools.StopWords;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class listStopwords {
  public static Log log = new Log( listStopwords.class ); 

   public static void main(String[] args) {
      Configuration conf = HDTools.readConfigNoMR(args[0]);
      Repository repository = new Repository( conf );
      StopwordsCache sw = (StopwordsCache)repository.getFeature("StopwordsCache");
      sw.openRead();
      HashSet<Integer> list = sw.getStopwords();
      TermString termstring = (TermString)repository.getFeature("TermString");
      termstring.loadMem(repository.getVocabularySize());
      for (int i : list) {
         String t = termstring.readValue(i);
         log.printf("%s", t);
      }
   }
}
