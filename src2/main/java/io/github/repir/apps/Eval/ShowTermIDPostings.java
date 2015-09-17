package io.github.repir.apps.Eval;

import io.github.repir.Retriever.MapReduce.Retriever;
import io.github.repir.Repository.Repository;
import io.github.repir.Repository.Term;
import io.github.repir.Repository.TermDF;
import io.github.repir.Repository.TermString;
import io.github.repir.Repository.TermCF;
import io.github.repir.Repository.TermInverted;
import io.github.repir.Retriever.Document;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.hadoop.Conf;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Shows the top-10000 terms in the vocabulary
 */
public class ShowTermIDPostings {

    public static Log log = new Log(ShowTermIDPostings.class);

    public static void main(String args[]) {
        Repository repository = new Repository(args, "partition termid");
        Conf conf = repository.getConf();
        Retriever retriever = new Retriever(repository);
        TermString termstring = TermString.get(repository);
        TermDF df = TermDF.get(repository);
        TermCF cf = TermCF.get(repository);
        int termid = repository.configuredInt("termid");
        Term term = repository.getTerm(termid);
        TermInverted ti = TermInverted.get(repository, "all", term);
        ti.setPartition(conf.getInt("partition", 0));
        log.printf("Term %d is %s df %d cf %d postings %s", termid, termstring.readValue(termid), df.readValue(termid), cf.readValue(termid), getPostings(ti));
    }

    public static TreeMap<Integer, ArrayList<Integer>> getPostings(TermInverted ti) {
        TreeMap<Integer, ArrayList<Integer>> map = new TreeMap();
        ti.openRead();
        while (ti.next()) {
            Document doc = new Document(ti.docid, ti.partition);
            ArrayList<Integer> positions = ArrayTools.toList(ti.getValue(doc));
            map.put(ti.docid, positions);
        }
        return map;
    }
}
