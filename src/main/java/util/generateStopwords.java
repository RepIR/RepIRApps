package util;
import java.util.HashSet;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Stopwords.StopwordsCache;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.Stopwords.StopWords;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class generateStopwords {
  public static Log log = new Log( generateStopwords.class ); 

   public static void main(String[] args) {
      Repository repository = new Repository(args);
      RRConfiguration conf = repository.getConfiguration();
      StopwordsCache sw = StopwordsCache.get(repository);
      sw.openRead();
      HashSet<Integer> list = sw.getStopwords();
      list.clear();
      StopWords slist = StopWords.get(repository);
      HashSet<String> stemmedset = slist.getStemmedFilterSet();
      TermString termstring = TermString.get(repository);
      termstring.loadMem(500000);
      for (int i = repository.getVocabularySize() - 1; i >= 0 ; i--) {
         String t = termstring.readValue(i);
         if (stemmedset.contains(t) || (t.length() < 3 && t.charAt(0) >= '0' && t.charAt(0) <= '9')) {
            list.add(i);
         }
      }
      sw.write();
   }

}
