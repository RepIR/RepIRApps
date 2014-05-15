package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.StructuredFileSort;
import io.github.repir.tools.Content.StructuredFileSortRecord;

/**
 * File used to sort terms according to CF descending, with their collection
 * statistics.
 */
public class VocTFFile extends StructuredFileSort {

   public StringField term = this.addString("term");
   public LongField cf = this.addLong("cf");
   public LongField df = this.addLong("df");

   public VocTFFile(Datafile df) {
      super(df);
   }

   @Override
   public StructuredFileSortRecord createRecord() {
      Record record = new Record(this);
      record.df = df.value;
      record.term = term.value;
      record.cf = cf.value;
      return record;
   }

   @Override
   public int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
      long comp = ((Record) o2).cf - ((Record) o1).cf;
      return (comp < 0) ? -1 : 1;
   }

   @Override
   public int compare(StructuredFileSort o1, StructuredFileSort o2) {
      long comp = ((VocTFFile) o2).cf.value - ((VocTFFile) o1).cf.value;
      return (comp < 0) ? -1 : 1;
   }

   class Record extends StructuredFileSortRecord {

      String term;
      long cf;
      long df;

      public Record(VocTFFile t) {
         super(t);
      }

      @Override
      public void write() {
         writeFinal();
      }

      @Override
      public void writeFinal() {
         ((VocTFFile) file).term.write(term);
         ((VocTFFile) file).cf.write(cf);
         ((VocTFFile) file).df.write(df);
      }
   }
   
   protected static VocTFFile getVocTFFile(Repository repository) {
      String voctffilename = repository.getFilename(".voctf");
      VocTFFile tffile = new VocTFFile(new Datafile(repository.getFS(), voctffilename));
      return tffile;
   }
}
