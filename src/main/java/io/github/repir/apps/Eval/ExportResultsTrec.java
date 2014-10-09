package io.github.repir.apps.Eval;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.ResultFileRR;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextCSV;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.Log;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * outputs a set of results files with the same test set to excel. The output is
 * formatted <TopicID> <TopicString> <MAP baseline> <MAP resultsfile1> ....
 * parameters: <configfile> { resultsfileextension }
 * <p/>
 * @author jeroen
 */
public class ExportResultsTrec {

   public static Log log = new Log(ExportResultsTrec.class);

   public static void main(String args[]) {
      Repository repository = new Repository(args, "resultsext identifier");
      String identifier = repository.configuredString("identifier");
      TestSet testset = new TestSet(repository);
      DocLiteral collectionid = repository.getCollectionIDFeature();
      ResultFileRR results = testset.getResults(repository.configuredString("resultsext"));
      TRECFile trecfile = new TRECFile(new Datafile(identifier));
      trecfile.openWrite();
      Sortedqueries sortedqueries = new Sortedqueries(results.getResults());
      for (Query q : sortedqueries) {
         int rank = 1;
         q.addFeature(collectionid);
         for (Document d : q.getQueryResults()) {
            trecfile.topic.set(q.id);
            trecfile.Q0.set("Q0");
            trecfile.collectionid.set(d.getString(collectionid));
            trecfile.rank.set(rank++);
            trecfile.score.set(d.score);
            trecfile.identifier.set(identifier);
            trecfile.write();
         }
      }
      trecfile.closeWrite();
   }

   public static class Sortedqueries extends TreeSet<Query> {

      public Sortedqueries(Collection<Query> queries) {
         super(new Comparator<Query>() {
            public int compare(Query a, Query b) {
               return (a.id < b.id) ? -1 : 1;
            }
         });
         addAll(queries);
      }
   }

   public static class TRECFile extends StructuredTextCSV {
      public IntField topic = this.addInt("topic", "", "\\s", "", " ");
      public StringField Q0 = this.addString("Q0", "", "\\s", "", " ");
      public StringField collectionid = this.addString("collectionid", "", "\\s", "", " ");
      public IntField rank = this.addInt("rank", "", "\\s", "", " ");
      public DoubleField score = this.addDouble("score", "", "\\s", "", " ");
      public StringField identifier = this.addString("identifier", "", "($|\\s)", "", "");

      public TRECFile(Datafile df) {
         super(df);
      }
   }
}
