package io.github.repir.apps.Retrieve;

import edu.emory.mathcs.backport.java.util.Collections;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Query;
import io.github.repir.RetrieverMR.RetrieverMRInputFormat;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;
import java.util.Iterator;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Repository.ModelParameters;
import io.github.repir.Repository.ModelParameters.Record;
import io.github.repir.Repository.Tools.Parameter;
import io.github.repir.Repository.Tools.ParameterGrid;
import io.github.repir.Retriever.Query.Variant;
import io.github.repir.RetrieverTuner.RetrieverTuner;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Lib.ArrayTools;

/**
 * Retrieve all topics from the RunTestSet, and store in an output file.
 * arguments: <configfile> <outputfileextension>
 *
 * @author jeroen
 */
public class RunTestSetE {

   public static Log log = new Log(RunTestSetE.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfig(args, "");
      HDTools.setPriorityHigh(conf);
      Repository repository = new Repository(conf);
      RetrieverTuner retriever = new RetrieverTuner(repository);
      TestSet testset = new TestSet(repository);
      ArrayList<Variant> variants = getVariants(repository);
      if (variants.size() > 0) {
         retriever.addQueue(testset.getQueries(retriever));
         for (Query q : retriever.queue) {
            for (Variant v : variants) {
               q.addVariant(v);
            }
         }
         for (Query q : retriever.queue) {
            log.info("variants %d", q.variantCount());
         }
         RetrieverMRInputFormat.setSplitable(true);
         RetrieverMRInputFormat.setIndex(repository);
         retriever.doJobDontWait(retriever.queue);
      }
      log.info("%s started", conf.get("iref.conf"));
   }

   public static ArrayList<Variant> getVariants(Repository repository) {
      ModelParameters modelparameters = (ModelParameters) repository.getFeature("ModelParameters");
      modelparameters.setBufferSize(1000000);
      ArrayList<Parameter> parameters = getParameters(repository);
      ArrayList<Variant> variants = new ArrayList<Variant>();
      ArrayList<String> settings = generatePoints(parameters);
      if (!repository.getConfigurationBoolean("tuner.overwrite", false))
         settings = removeKnownSettings(repository, settings);
      for (String conf : settings) {
         Variant v = new Variant();
         v.configuration = conf;
         v.retrievalmodelclass = repository.getConfigurationString("retriever.strategy");
         v.scorefunctionclass = repository.getConfigurationString("retriever.scorefunction");
         variants.add(v);
      }
      return variants;
   }

   public static ArrayList<Parameter> getParameters(Repository repository) {
      ArrayList<Parameter> parameters = new ArrayList<Parameter>();
      for (String p : repository.getFreeParameters()) {
         parameters.add(new ParameterGrid(p));
      }
      Collections.sort(parameters);
      for (int i = 0; i < parameters.size(); i++) {
         Parameter p = parameters.get(i);
         p.index = i;
         p.reset();
         p.generatePoints();
      }
      return parameters;
   }

   public static ArrayList<String> generatePoints(ArrayList<Parameter> parameters) {
      ArrayList<String> settings = new ArrayList<String>();
      int parami[] = new int[parameters.size()];
      for (int i = 0; i < parameters.size(); i++) {
         parami[i] = parameters.get(i).getPoints().size() - 1;
      }
      while (parami[0] >= 0) {
         settings.add(getSettings(parameters, parami));
         for (int i = parameters.size() - 1; i >= 0; i--) {
            if (i < parameters.size() - 1) {
               parami[i + 1] = parameters.get(i + 1).getPoints().size() - 1;
            }
            if (--parami[i] >= 0) {
               break;
            }
         }
      }
      return settings;
   }

   public static String getSettings(ArrayList<Parameter> parameters, int settings[]) {
      ArrayList<String> list = new ArrayList<String>();
      for (Parameter p : parameters) {
         String pstr = p.parameter + "=" + p.getPoints().get(settings[p.index]).toString();
         list.add(pstr);
      }
      return ArrayTools.toString(",", list);
   }

   public static ArrayList<String> removeKnownSettings(Repository repository, ArrayList<String> settings) {
      String[] storedparameters = repository.getStoredFreeParameters();
      repository.getConfiguration().setInt("fold", 0);
      ModelParameters modelparameters = (ModelParameters) repository.getFeature("ModelParameters");
      modelparameters.setBufferSize(1000000);
      modelparameters.openRead();
      Iterator<String> iter = settings.iterator();
      while (iter.hasNext()) {
         String s = iter.next();
         repository.addConfiguration(s);
         Record newRecord = modelparameters.newRecord(storedparameters);
         Record found = modelparameters.read(newRecord);
         if (found != newRecord) {
            iter.remove();
         }
      }
      return settings;
   }
}
