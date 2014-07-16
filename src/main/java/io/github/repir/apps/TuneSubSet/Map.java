package io.github.repir.apps.TuneSubSet;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.HashMap;
import io.github.repir.tools.MapReduce.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import io.github.repir.EntityReader.SubSetFile;

public class Map extends Mapper<LongWritable, Text, NullWritable, NullWritable> {

   public static Log log = new Log(Map.class);
   Configuration configuration;
   private HashMap<String, SubSetFile> spamfiles = new HashMap<String, SubSetFile>();

   @Override
   public void setup(Context context) {
      configuration = Configuration.convert(context.getConfiguration());
   }
   
   @Override
   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String line = value.toString();
      String outputdir = configuration.get("repository.idlist", "input/clueweb");
      String cluewebid = null;
      int space = line.indexOf(' ');
      if (space > 0) {
         String parts[] = line.split(" ");
         for (int i = 2; i < 5; i++) {
            if (i < parts.length && parts[i].length() > 10 && parts[i].charAt(0) == 'c') {
               cluewebid = parts[i];
            }
         }
         if (cluewebid != null) {
            String directory = cluewebid.substring(10, 16);
            SubSetFile sf = spamfiles.get(directory);
            if (sf == null) {
               String filename = outputdir + "/" + directory + ".idlist";
               //log.info("spamfile %s %s", directory, filename);
               sf = new SubSetFile(new Datafile(configuration.FS(), filename));
               sf.setBufferSize(100000);
               sf.openWrite();
               spamfiles.put(directory, sf);
            }
            sf.cluewebid.write(cluewebid);
         }
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      for (SubSetFile sf : spamfiles.values()) {
         sf.closeWrite();
      }
   }
}
