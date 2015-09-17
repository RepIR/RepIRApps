package io.github.repir.apps.Vocabulary;

import io.github.htools.io.Datafile;
import io.github.htools.io.struct.StructuredTextTSV;

/**
 * The segments written for VocTFFile, that enable recovery.
 * @author jeroen
 */
public class SegmentsFile extends StructuredTextTSV {
    VocTFFile voctffile;
    IntField segment = this.addInt("segment");
    LongField offset = this.addLong("offset");
    LongField ceiling = this.addLong("ceiling");
    
    public SegmentsFile(VocTFFile voctffile) {
        super(voctffile.getDatafile().getDir().getFile("segments"));
        this.voctffile = voctffile;
    }  
}
