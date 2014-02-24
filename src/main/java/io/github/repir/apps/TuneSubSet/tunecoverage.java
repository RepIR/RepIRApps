package io.github.repir.apps.TuneSubSet;

import java.io.EOFException;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.fs.FileSystem;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.ResultFile;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;

/**
 *
 * @author Jeroen Vuurens
 */
public class tunecoverage {

   public static Log log = new Log(tunecoverage.class);

   public static void main(String[] args) {
      String reps[] = args[1].split(",");
      String sys[] = args[2].split(",");
      ByteRegex cluewebidpattern = new ByteRegex("clueweb\\S*?(?=\\s)");
      HashSet<String> ids = new HashSet<String>();
      FileSystem fs = HDTools.getFS();
      Datafile subset = new Datafile(fs, args[0]);
      subset.setBufferSize(100000000);
      subset.openRead();
      try {
         while (subset.hasMore()) {
            String readString = subset.read(cluewebidpattern);
            ids.add(readString);
         }
      } catch (EOFException ex) { }
      subset.close();
      log.info("count %d", ids.size());
      log.info("present %b", ids.contains("clueweb09-en0072-93-17757"));
      int countin = 0;
      int countout = 0;
      for (String r : reps) {
         Repository repository = new Repository(HDTools.readConfig(r));
         TestSet ts = new TestSet(repository);
         for (String file : sys) {
            log.info("results %s%s", r, file);
            Collection<Query> results = new ResultFile(ts, file).getResults();
            for (Query q : results) {
              for (Document d : q.queryresults) {
                 if (ids.contains(d.getCollectionID()))
                    countin++;
                 else
                    countout++;
              }
            }
         }
      }
      log.info("in %d out %d", countin, countout);
   }
}
