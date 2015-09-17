package util;

import io.github.htools.extract.DefaultTokenizer;
import io.github.htools.extract.HtmlTitleExtractor;
import io.github.htools.lib.Log;
import io.github.htools.io.web.WebTools;
import io.github.htools.io.web.WebTools.UrlResult;
import java.util.ArrayList;

public class readTitleUrl {

   public static Log log = new Log(readTitleUrl.class);
   
   public static void main(String[] args) throws Exception {
       HtmlTitleExtractor titleextractor = new HtmlTitleExtractor();
       DefaultTokenizer tokenizer = new DefaultTokenizer();
       
       UrlResult result = WebTools.getUrlResult(args[0]);
       
       ArrayList<String> extract = titleextractor.extract(result.content);
       if (extract.size() > 0) {
           log.info("literal: %s", extract.get(0));
           log.info("tokenized: %s", tokenizer.tokenize(extract.get(0)));
       }
   }
}
