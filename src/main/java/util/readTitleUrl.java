package util;

import io.github.repir.tools.extract.DefaultTokenizer;
import io.github.repir.tools.extract.HtmlTitleExtractor;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.WebTools;
import io.github.repir.tools.lib.WebTools.UrlResult;
import java.util.ArrayList;

public class readTitleUrl {

   public static Log log = new Log(readTitleUrl.class);
   
   public static void main(String[] args) throws Exception {
       HtmlTitleExtractor titleextractor = new HtmlTitleExtractor();
       DefaultTokenizer tokenizer = new DefaultTokenizer();
       
       UrlResult result = WebTools.getUrlByteArray(args[0]);
       
       ArrayList<String> extract = titleextractor.extract(result.content);
       if (extract.size() > 0) {
           log.info("literal: %s", extract.get(0));
           log.info("tokenized: %s", tokenizer.tokenize(extract.get(0)));
       }
   }
}
