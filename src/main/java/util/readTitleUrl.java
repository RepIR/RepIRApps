package util;

import io.github.repir.Extractor.DefaultTokenizer;
import io.github.repir.Extractor.HtmlTitleExtractor;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.WebTools;
import java.util.ArrayList;

public class readTitleUrl {

   public static Log log = new Log(readTitleUrl.class);
   
   public static void main(String[] args) throws Exception {
       HtmlTitleExtractor titleextractor = new HtmlTitleExtractor();
       DefaultTokenizer tokenizer = new DefaultTokenizer();
       
       byte content[] = WebTools.getUrlByteArray(args[0]);
       
       ArrayList<String> extract = titleextractor.extract(content);
       if (extract.size() > 0) {
           log.info("literal: %s", extract.get(0));
           log.info("tokenized: %s", tokenizer.tokenize(extract.get(0)));
       }
   }
}
