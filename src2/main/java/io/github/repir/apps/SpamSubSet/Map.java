package io.github.repir.apps.SpamSubSet;

import io.github.htools.hadoop.Conf;
import io.github.htools.hadoop.ContextTools;
import io.github.htools.hadoop.io.archivereader.SpamFile;
import io.github.htools.io.Datafile;
import io.github.repir.MapReduceTools.RRConfiguration;
import io.github.htools.lib.Log;
import java.io.IOException;
import java.util.HashMap;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class Map extends Mapper<LongWritable, Text, NullWritable, NullWritable> {

   public static Log log = new Log(Map.class);
   private HashMap<String, SpamFile> spamfiles = new HashMap<String, SpamFile>();
   Conf conf;
   
   public void setup(Context context) {
       conf = ContextTools.getConfiguration(context);
   }
   
   @Override
   public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
      String line = value.toString();
      int space = line.indexOf(' ');
      if (space > 0) {
         int spamindex = Integer.parseInt(line.substring(0, space));
         String cluewebid = line.substring(space + 1);
         String directory = cluewebid.substring(10, 16);
         SpamFile sf = spamfiles.get(directory);
         if (sf == null) {
            String filename = "/user/jeroenv/input/clueweb/" + directory + ".spam";
            //log.info("spamfile %s %s", directory, filename);
            sf = new SpamFile(new Datafile(conf, filename));
            sf.setBufferSize(100000);
            sf.openWrite();
            spamfiles.put(directory, sf);
         }
         //log.info("write %s %d", cluewebid, spamindex);
         sf.cluewebid.write(cluewebid);
         sf.spamindex.write(spamindex);
      }
   }

   @Override
   protected void cleanup(Context context) throws IOException, InterruptedException {
      for (SpamFile sf : spamfiles.values()) {
         sf.closeWrite();
      }
   }
}
