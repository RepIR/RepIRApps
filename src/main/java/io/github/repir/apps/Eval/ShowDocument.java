package io.github.repir.apps.Eval;

import io.github.repir.RetrieverMR.RetrieverMR;
import io.github.repir.Repository.DocTF;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Strategy.Strategy;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.ArrayTools;
import java.util.ArrayList;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.tools.Lib.ArgsParser;

/**
 * Shows the StoredFeatures and tokenized contents of a document for inspection
 * arguments: <configfile> <docid> <partition>
 */
public class ShowDocument {

   public static Log log = new Log(ShowDocument.class);

   public static void main(String args[]) {
      ArgsParser parsedargs = new ArgsParser( args, "configfile documentid partitionnr");
      Configuration conf = HDTools.readConfig( parsedargs.get("configfile") );
      Repository repository = new Repository(conf);
      TermString termstring = (TermString) repository.getFeature("TermString");
      termstring.loadMem(100000);
      Retriever retriever = new Retriever(repository);
      int docid = parsedargs.getInt("documentid");
      int partition = parsedargs.getInt("partitionnr");
      Query q = retriever.constructQueryRequest("test");
      q.addFeature("DocLiteral:collectionid");
      q.addFeature("DocLiteral:literaltitle");
      q.addFeature("DocLiteral:url");
      q.addFeature("DocForward:all");
      q.addFeature("DocTF:all");
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      Document doc = rm.createDocument(docid, partition);
      ArrayList<Document> docs = new ArrayList<Document>();
      docs.add(doc);
      retriever.readReportedStoredFeatures(docs, rm.getFeatures().getReportedStoredFeatures(), partition);
      DocTF tf = (DocTF) rm.getFeatures().getReportedFeature("DocTF:all");
      log.printf("doctf=%d", tf.valueReported(doc));
      log.printf("doc %5d#%3d id %s title %s url %s", doc.docid, doc.partition,
              doc.getLiteral("DocLiteral:collectionid"),
              doc.getLiteral("DocLiteral:literaltitle"),
              doc.getLiteral("DocLiteral:url"));
      log.printf("%s", ArrayTools.toString( doc.getReportedForward() ));
      log.printf("%s", termstring.getContentStr(doc.getReportedForward()));
   }
}
