package io.github.repir.apps.Eval;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.DocTF;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Query;
import java.util.ArrayList;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Show the contents of a document based on its CollectionID parameters:
 * <configfile> <collectionID>
 */
public class ShowDocumentByCollectionID {

   public static Log log = new Log(ShowDocumentByCollectionID.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser(args, "configfile partitionnr collectionid");
      int partition = parsedargs.getInt("partitionnr");
      Configuration conf = HDTools.readConfig(parsedargs.get("configfile"));
      Repository repository = new Repository(conf);
      TermString termstring = (TermString) repository.getFeature("TermString");
      termstring.loadMem(100000);
      Retriever retriever = new Retriever(repository);
      Query q = retriever.constructQueryRequest("test");
      q.addFeature("DocLiteral:collectionid");
      q.addFeature("DocLiteral:literaltitle");
      q.addFeature("DocLiteral:url");
      q.addFeature("DocForward:all");
      q.addFeature("DocTF:all");
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      rm.buildGraph();
      DocLiteral collectionid = repository.getCollectionIDFeature();
      collectionid.setPartition(partition);
      int docid = collectionid.findLiteral( parsedargs.get("collectionid") );
      if (docid >= 0) {
         Document doc = q.createDocument(rm, docid, partition);
         ArrayList<Document> docs = new ArrayList<Document>();
         docs.add(doc);
         retriever.readReportedStoredFeatures(docs, rm.getFeatures().getReportedStoredFeatures(), partition);
         DocTF tf = (DocTF) rm.getFeatures().getReportedFeature("DocTF:all");
         log.info("%s docfields %d", tf, doc.reportdata.length);
         log.printf("doctf[0]=%d", tf.valueReported(doc));
         log.printf("doc %5d#%3d id %s title %s url %s", doc.docid, doc.partition,
                 args[2],
                 doc.getLiteral("DocLiteral:literaltitle"),
                 doc.getLiteral("DocLiteral:url"));
         log.printf("%s", termstring.getContentStr(doc.getReportedForward()));
      }
   }
}
