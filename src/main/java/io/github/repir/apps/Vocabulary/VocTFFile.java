package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.Repository;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredFileSort;
import static io.github.repir.tools.Structure.StructuredFileSort.log;
import io.github.repir.tools.Structure.StructuredFileSortReader;
import io.github.repir.tools.Structure.StructuredFileSortRecord;
import java.util.ArrayList;

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

    @Override
    protected int spillThreshold() {
        return 1000000;
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
   
   protected void checkCount() {
       tempfile = this.getTempfile();
       setDatafile(tempfile);
       openRead();
       long count = 0;
       while (next()) {
           log.printf("%s %d", this.term.value, this.cf.value);
           count++;
       }
       log.printf("count %d", count);
   }
   
   protected void checkCount2() {
       openRead();
       long count = 0;
       while (next()) {
           log.printf("%s %d", this.term.value, this.cf.value);
           count++;
       }
       log.printf("count %d", count);
   }
   
   public ArrayList<StructuredFileSortReader> getReader() {
       return readers;
   }
   
   @Override
   public void setDatafile(Datafile df) {
       super.setDatafile(df);
   }
   
   protected void recover() {
       tempfile = getTempfile();
         setDatafile(tempfile);
         openRead();
         long lastoffset = 0, segmentoffset = getOffset();
         long lastmin = 0;
         segments = 0;
         while (next()) {
             if (this.cf.value > lastmin && lastoffset > segmentoffset) {
                StructuredFileSort segment = (StructuredFileSort) this.clone();
                segment.setOffset(segmentoffset);
                segment.setCeiling(lastoffset);
                log.info("segment %d offset %d ceiling %d", segments, segment.getOffset(), segment.getCeiling());
                readers.add(new StructuredFileSortReader(segment, segments++));
                segmentoffset = lastoffset;
             }
             lastmin = cf.value;
             lastoffset = getOffset();
         }
         StructuredFileSort segment = (StructuredFileSort) this.clone();
         segment.setOffset(segmentoffset);
         segment.setCeiling(lastoffset);
         log.info("segment %d offset %d ceiling %d", segments, segment.getOffset(), segment.getCeiling());
         readers.add(new StructuredFileSortReader(segment, segments++));
         segmentoffset = lastoffset;
         setDatafile(null);
         merge();
    }
   
    public static void main(String[] args) {
        Datafile df = new Datafile(args[0]);
        VocTFFile vtf = new VocTFFile( df );
        vtf.recover();
    }
}
