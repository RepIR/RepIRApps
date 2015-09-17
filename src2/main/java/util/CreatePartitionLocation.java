package util;

import io.github.htools.lib.Log;
import io.github.repir.Repository.PartitionLocation;
import io.github.repir.Repository.Repository;
import io.github.repir.MapReduceTools.RRConfiguration;

/**
 * This is a separate util to create a lookup table in the repository for the primary location of
 * each repository partition, to speed up retrieval tasks by assigning it to the correct
 * location.
 * @author jeroen
 */
public class CreatePartitionLocation {

   public static Log log = new Log(CreatePartitionLocation.class);

   public static void main(String[] args) throws Exception {
      Repository repository = new Repository(args, "");
      RRConfiguration conf = repository.getConf();
      PartitionLocation partitionlocations = repository.getPartitionLocation();
      partitionlocations.openWrite();
      for (int partition = 0; partition < repository.getPartitions(); partition++) {
         log.info("partition %d", partition);
         String hosts[] = repository.getPartitionLocation(partition);
         partitionlocations.write( hosts );
      }
      partitionlocations.closeWrite();
   }
}
