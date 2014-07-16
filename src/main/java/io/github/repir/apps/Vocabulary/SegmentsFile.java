package io.github.repir.apps.Vocabulary;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Structure.StructuredTextTSV;

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
