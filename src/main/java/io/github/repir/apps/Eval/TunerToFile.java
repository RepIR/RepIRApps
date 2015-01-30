package io.github.repir.apps.Eval;

import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Tuner.Retriever;
import io.github.repir.tools.io.Datafile;
import io.github.repir.tools.lib.ArgsParser;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * reports MAP and TTest p-values for a range of test sets and systems into an
 * excel sheet
 * <p/>
 * @author jeroen
 */
public class TunerToFile {

   public static Log log = new Log(TunerToFile.class);
   public String collections[];
   public HashMap<String, Double> totals = new HashMap<String,Double>();

   public TunerToFile(String filename, String collections[]) {

      this.collections = collections;
      String parameters[] = null;
      ArrayList<String> variants = null;
      HashMap<String, Double> results[] = new HashMap[collections.length];
      Repository repository = null;
      for (int coll = 0; coll < collections.length; coll++) {
         results[coll] = new HashMap<String, Double>();
         repository = new Repository(collections[coll]);
         ModelParameters modelparameters = ModelParameters.get(repository, repository.configurationName());
         if (variants == null) {
            parameters = repository.getStoredFreeParameters();
            Retriever retriever = new Retriever(repository);
            variants = retriever.generatePoints(retriever.getParameters());
         }
         for (String v : variants) {
            repository.addConfiguration(v);
            ModelParameters.Record read = modelparameters.read(parameters);
            log.info("%s %s", v, read);
            results[coll].put(v, read.map);
            Double t = totals.get(v);
            if (t == null)
               totals.put(v, read.map);
            else
               totals.put(v, t + read.map);
         }
      }

      Datafile df = new Datafile(filename);
      df.openWrite();
      for (int i = 0 ; i < variants.size(); i++) {
         StringBuilder sb = new StringBuilder();
         String v = variants.get(i);
         repository.addConfiguration(v);
         for (String p : parameters)
            sb.append(repository.configuredString(p)).append(" ");
         for (int coll = 0; coll < collections.length; coll++) {
            sb.append(results[coll].get(v)).append(" ");
         }
         sb.append(totals.get(v));
         df.printf("%s\n", sb);
      }
      df.closeWrite();
   }

   public static void main(String[] args) {
      ArgsParser ar = new ArgsParser( args , "collections");
      String collections[] = ar.get("collections").split(",");
      new TunerToFile("tuner.dat", collections);
   }
}
