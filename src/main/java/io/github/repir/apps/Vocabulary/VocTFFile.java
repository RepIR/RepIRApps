package io.github.repir.apps.Vocabulary;

import io.github.repir.tools.DataTypes.Configuration;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.RecordBinary;
import io.github.repir.tools.Content.RecordSort;
import io.github.repir.tools.Content.RecordSortRecord;
import io.github.repir.tools.Lib.HDTools;

/**
 * File used to sort term according to TF descending.
 */
public class VocTFFile extends RecordSort {

   public StringField term = this.addString("term");
   public LongField tf = this.addLong("tf");
   public LongField df = this.addLong("df");

   public VocTFFile(Datafile df) {
      super(df);
   }

   @Override
   public RecordSortRecord createRecord() {
      Record record = new Record(this);
      record.df = df.value;
      record.term = term.value;
      record.tf = tf.value;
      return record;
   }

   @Override
   public int compare(RecordSortRecord o1, RecordSortRecord o2) {
      long comp = ((Record) o2).tf - ((Record) o1).tf;
      return (comp < 0) ? -1 : 1;
   }

   @Override
   public int compare(RecordSort o1, RecordSort o2) {
      long comp = ((VocTFFile) o2).tf.value - ((VocTFFile) o1).tf.value;
      return (comp < 0) ? -1 : 1;
   }

   class Record extends RecordSortRecord {

      String term;
      long tf;
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
         ((VocTFFile) file).tf.write(tf);
         ((VocTFFile) file).df.write(df);
      }
   }
   
   protected static VocTFFile getVocTFFile(Repository repository) {
      String voctffilename = repository.getFilename(".voctf");
      VocTFFile tffile = new VocTFFile(new Datafile(repository.getFS(), voctffilename));
      return tffile;
   }
   
   public static void main(String[] args) {
      Configuration conf = HDTools.readConfig(args[0]);
      Repository repository = new Repository( conf );
      VocTFFile f = getVocTFFile( repository );
      f.setBufferSize(10000);
      f.openRead();
      int id = 0;
      while (f.next()) {
         log.printf("%d %s %d", id++, f.term.value, f.tf.value);
      }
   }
}
