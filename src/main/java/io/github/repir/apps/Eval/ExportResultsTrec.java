package io.github.repir.apps.Eval;

import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.tools.Lib.Log;
import io.github.repir.TestSet.ResultFile;
import io.github.repir.TestSet.TestSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import io.github.repir.Repository.DocLiteral;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordCSV;
import io.github.repir.tools.Lib.ArgsParser;

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
      ArgsParser parsedargs = new ArgsParser(args, "configfile resultsext identifier");
      Repository repository = new Repository(parsedargs.get("configfile"));
      String identifier = parsedargs.get("identifier");
      TestSet testset = new TestSet(repository);
      DocLiteral collectionid = repository.getCollectionIDFeature();
      ResultFile results = testset.getResults(parsedargs.get("resultsext"));
      TRECFile trecfile = new TRECFile(new Datafile(identifier));
      trecfile.openWrite();
      Sortedqueries sortedqueries = new Sortedqueries(results.getResults());
      for (Query q : sortedqueries) {
         int rank = 1;
         q.addFeature(collectionid);
         for (Document d : q.queryresults) {
            trecfile.topic.write(q.id);
            trecfile.Q0.write("Q0");
            trecfile.collectionid.write(d.getLiteral(collectionid));
            trecfile.rank.write(rank++);
            trecfile.score.write(d.score);
            trecfile.identifier.write(identifier);
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

   public static class TRECFile extends RecordCSV {

      public IntField topic = this.addInt("topic");
      public StringField Q0 = this.addString("Q0");
      public StringField collectionid = this.addString("collectionid");
      public IntField rank = this.addInt("rank");
      public DoubleField score = this.addDouble("score");
      public StringField identifier = this.addString("identifier");

      public TRECFile(Datafile df) {
         super(df);
      }
   }
}
