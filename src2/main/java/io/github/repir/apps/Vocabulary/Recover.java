package io.github.repir.apps.Vocabulary;

import io.github.repir.Repository.DictionaryFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import static io.github.repir.apps.Vocabulary.Reduce.getDictionaryFeatures;
import static io.github.repir.apps.Vocabulary.Reduce.writeVoc;
import io.github.htools.lib.Log;
import io.github.htools.io.struct.StructuredFileSort;
import io.github.htools.io.struct.StructuredFileSortReader;
import java.util.ArrayList;

public class Recover {

   public static Log log = new Log(Recover.class);
   
    public static void main(String[] args) {
      Repository repository = new Repository(args);
      int mincf = repository.configuredInt("vocabulary.mincf", 0);
      VocTFFile tffile = VocTFFile.getVocTFFile(repository); // temporary file, that is self-sorting, and used to construct the DIctionary features afterwards.
      tffile.setDatafile(tffile.getTempfile());
      
      SegmentsFile segments = new SegmentsFile(tffile);
      segments.openRead();
      while (segments.nextRecord()) {
          log.info("%s %s %s", segments.segment.get(), segments.offset.get(), segments.ceiling.get());
          StructuredFileSort segment = (StructuredFileSort) tffile.clone();
          segment.setOffset(segments.offset.get());
          segment.setCeiling(segments.ceiling.get());
          log.info("segment %d offset %d ceiling %d", segments.segment.get(), segment.getOffset(), segment.getCeiling());
          tffile.getReader().add(new StructuredFileSortReader(segment, segments.segment.get()));
      }
      tffile.merge();

      ArrayList<DictionaryFeature> features = getDictionaryFeatures( repository );
      writeVoc( repository, features );
   }
}
