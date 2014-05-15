package io.github.repir.apps.Pig;

import io.github.repir.Repository.CollectionID;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Pig.PigDoc;
import io.github.repir.Repository.Pig.PigDoc.Tuple;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Repository.Configuration;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * The mapper is generic, and collects data for a query request, using the
 * passed retrieval model, scoring function and query string. The common
 * approach is that each node processes all queries for one index partition. The
 * collected results are reshuffled to one reducer per query where all results
 * for a single query are aggregated.
 * <p/>
 * @author jeroen
 */
public class DocMap extends Mapper<IntWritable, NullWritable, NullWritable, NullWritable> {

   public static Log log = new Log(DocMap.class);
   Configuration conf;
   Repository repository;
   int width = 10;

   @Override
   protected void setup(Mapper.Context context) throws IOException, InterruptedException {
      repository = new Repository(context.getConfiguration());
      conf = repository.getConfiguration();
   }

   @Override
   public void map(IntWritable inkey, NullWritable invalue, Context context) throws IOException, InterruptedException {
      ArrayList<Tuple> tuples = new ArrayList<Tuple>();
      DocLiteral title = (DocLiteral) repository.getFeature(DocLiteral.class, "literaltitle");
      CollectionID collid = repository.getCollectionIDFeature();
      DocTF doctf = (DocTF) repository.getFeature(DocTF.class, "all");
      title.setPartition(inkey.get());
      title.readResident();
      collid.setPartition(inkey.get());
      collid.readResident();
      doctf.setPartition(inkey.get());
      doctf.readResident();
      title.find(0);
      doctf.find(0);
      collid.find(0);
      Document doc = new Document();
      doc.partition = inkey.get();
      for (int docid = 0; title.next(); docid++) {
         doctf.next();
         collid.next();
         Tuple t = new Tuple();
         t.collectionid = collid.getValue();
         t.id = docid;
         t.partition = doc.partition;
         t.title = title.getValue();
         t.tf = doctf.getValue();
         tuples.add(t);
      }
      PigDoc d = (PigDoc) repository.getFeature(PigDoc.class);
      d.openAppend();
      for (Tuple t : tuples) {
         d.write(t);
      }
      d.closeWrite();
   }
}
