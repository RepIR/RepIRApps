package io.github.repir.apps.Eval;

import io.github.repir.Repository.CollectionID;
import io.github.repir.Repository.DocForward;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.DocTF;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.TermString;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.Retriever.Retriever;
import io.github.repir.Strategy.RetrievalModel;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Shows the StoredFeatures and tokenized contents of a document for inspection
 * arguments: <configfile> <docid> <partition>
 */
public class ShowDocument {

   public static Log log = new Log(ShowDocument.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "documentid partitionnr");
      TermString termstring = (TermString) repository.getFeature(TermString.class);
      termstring.loadMem(100000);
      Retriever retriever = new Retriever(repository);
      int docid = repository.configuredInt("documentid");
      int partition = repository.configuredInt("partitionnr");
      Query q = retriever.constructQueryRequest("test");
      DocTF doctf = (DocTF)repository.getFeature(DocTF.class, "all");
      CollectionID collectionid = repository.getCollectionIDFeature();
      DocLiteral title = (DocLiteral)repository.getFeature(DocLiteral.class, "literaltitle");
      q.addFeature(collectionid);
      q.addFeature(title);
      q.addFeature(doctf);
      RetrievalModel rm = (RetrievalModel)retriever.constructStrategy(q);
      Document doc = rm.createDocument(docid, partition);
      ArrayList<Document> docs = new ArrayList<Document>();
      docs.add(doc);
      DocForward fw = (DocForward)repository.getFeature(DocForward.class, "all");
      fw.read(doc);
      int[] value = fw.getValue();
      retriever.readReportedStoredFeatures(docs, rm.getReportedStoredFeatures(), partition);
      log.printf("doctf=%d", doc.getInt(doctf));
      log.printf("doc %5d#%3d id %s title %s", doc.docid, doc.partition,
              doc.getString(collectionid),
              doc.getString(title));
      log.printf("%s", ArrayTools.concat( value ));
      log.printf("%s", termstring.getContentStr( value ));
   }
}