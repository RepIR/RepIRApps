package io.github.repir.apps.Repository;

import io.github.repir.EntityReader.MapReduce.TermEntityKey;
import io.github.repir.EntityReader.MapReduce.TermEntityValue;
import io.github.repir.Repository.AutoTermDocumentFeature;
import io.github.repir.Repository.CollectionID;
import io.github.repir.Repository.EntityStoredFeature;
import io.github.repir.Repository.ReducibleFeature;
import io.github.repir.Repository.ReduciblePartitionedFeature;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.StoredFeature;
import io.github.repir.Repository.StringLookupFeature;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.hadoop.Job;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class Reduce extends Reducer<TermEntityKey, TermEntityValue, TermEntityKey, IntWritable> {

    public static Log log = new Log(Reduce.class);
    Repository repository;
    int partition;
    int MAXMEMORY = 100000000;
    HashMap<String, Integer> doclist = new HashMap<String, Integer>();
    AutoFeatures autofeatures;
    CollectionID collectionid;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        repository = new Repository(context.getConfiguration());
        collectionid = repository.getCollectionIDFeature();
        partition = repository.configuredInt("repository.onlypartition", Job.getReducerId(context));
        autofeatures = new AutoFeatures(repository);
        int mempart = MAXMEMORY / (4096 * (autofeatures.termdocfeatures.size() * 2
                + autofeatures.reduciblepartitionedfeatures.size()
                + autofeatures.reduciblefeatures.size()));
        for (ReduciblePartitionedFeature dc : autofeatures.reduciblepartitionedfeatures) {
            dc.startReduce(partition, 4096 * mempart);
        }
        for (AutoTermDocumentFeature tc : autofeatures.termdocfeatures) {
            tc.setDocs(doclist);
            tc.startReduce(partition, 4096 * 2 * mempart);
        }
        if (partition == 0) {
            for (ReducibleFeature dc : autofeatures.reduciblefeatures) {
                dc.startReduce(4096 * mempart);
            }
        }
    }

    @Override
    public void reduce(TermEntityKey key, Iterable<TermEntityValue> values, Context context)
            throws IOException, InterruptedException {
        Job.reduceReport(context);
        if (key.getType() == TermEntityKey.Type.ENTITYFEATURE) {
            if (key.feature == 0) { // preloads the collection id for the document
                doclist.put(key.collectionid, doclist.size()); //asign docid to collectionid
            }
            ReduciblePartitionedFeature f = autofeatures.reduciblepartitionedfeatures.get(key.feature);
            f.writeReduce(key, values);
        } else if (key.getType() == TermEntityKey.Type.LOOKUPFEATURE) {
            ReducibleFeature f = autofeatures.reduciblefeatures.get(key.feature);
            f.writeReduce(key, values);
        } else if (key.getType() == TermEntityKey.Type.TERMDOCFEATURE) {
            key.docid = doclist.get(key.collectionid); // convert collectionid to docid
            AutoTermDocumentFeature f = autofeatures.termdocfeatures.get(key.feature);
            f.reduceInput(key, values);
        }
        context.progress();
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        for (ReduciblePartitionedFeature dc : autofeatures.reduciblepartitionedfeatures) {
            if (!(dc instanceof StringLookupFeature) || partition != 0) {
                dc.finishReduce();
            }
        }
        for (AutoTermDocumentFeature tc : autofeatures.termdocfeatures) {
            tc.finishReduce();
        }
        if (partition == 0) {
            for (ReducibleFeature dc : autofeatures.reduciblefeatures) {
                dc.finishReduce();
            }
        }
    }
}
