package util;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Structure.StructuredDataStream;
import io.github.repir.tools.Structure.StructuredFileSort;
import io.github.repir.tools.Structure.StructuredFileSortRecord;
import util.TestSort.SortFile.Record;

/**
 *
 * @author jeroen
 */
public class TestSort {
    public static Log log = new Log(TestSort.class);
    
    public static void main(String[] args) {
        SortFile f = new SortFile(new Datafile("sortfile"));
        f.openWrite();
        Record r = new Record(f);
        for (long i = 0; i < 100000000; i++) {
            r.id = i;
            r.write();
        }
        f.closeWrite();
        int count = 0;
        f.openRead();
        while (f.next())
            count++;
        f.closeRead();
        log.info("count %d", count);
    }

    public static class SortFile extends StructuredFileSort {

        public StructuredDataStream.LongField id = this.addLong("id");

        public SortFile(Datafile df) {
            super(df);
        }

        @Override
        public StructuredFileSortRecord createRecord() {
            Record record = new Record(this);
            record.id = id.value;
            return record;
        }

        @Override
        public int compare(StructuredFileSortRecord o1, StructuredFileSortRecord o2) {
            long comp = ((Record) o2).id - ((Record) o1).id;
            return (comp < 0) ? -1 : 1;
        }

        @Override
        public int compare(StructuredFileSort o1, StructuredFileSort o2) {
            long comp = ((SortFile) o2).id.value - ((SortFile) o1).id.value;
            return (comp < 0) ? -1 : 1;
        }

        @Override
        protected int spillThreshold() {
            return 1000000;
        }

        static class Record extends StructuredFileSortRecord {

            long id;

            public Record(SortFile t) {
                super(t);
            }

            @Override
            public void write() {
                writeFinal();
            }

            @Override
            public void writeFinal() {
                ((SortFile) file).id.write(id);
            }
        }
    }
}
