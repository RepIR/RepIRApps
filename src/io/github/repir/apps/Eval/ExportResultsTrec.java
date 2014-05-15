package io.github.repir.apps.Eval;

import io.github.repir.Repository.DocLiteral;
import io.github.repir.Repository.Repository;
import io.github.repir.Retriever.Document;
import io.github.repir.Retriever.Query;
import io.github.repir.TestSet.ResultFile;
import io.github.repir.TestSet.TestSet;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredTextCSV;
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
      public FolderNode top = this.addRoot("qrel", "", "($|\n)", "", "\n");
      public IntField topic = this.addInt(top, "topic", "", "\\s", "", " ");
      public StringField Q0 = this.addString(top, "Q0", "", "\\s", "", " ");
      public StringField collectionid = this.addString(top, "collectionid", "", "\\s", "", " ");
      public IntField rank = this.addInt(top, "rank", "", "\\s", "", " ");
      public DoubleField score = this.addDouble(top, "topic", "", "\\s", "", " ");
      public StringField identifier = this.addString(top, "identifier", "", "($|\\s)", "", "");

      public TRECFile(Datafile df) {
         super(df);
      }
   }
}
