package util;

import io.github.repir.tools.Content.HDFSDir;
import io.github.repir.tools.Lib.HDTools;
import io.github.repir.tools.Lib.Log;
import io.github.repir.Repository.PartitionLocation;
import io.github.repir.Repository.Repository;
import io.github.repir.tools.DataTypes.Configuration;

/**
 * This is a separate util to create a lookup table in the repository for the primary location of
 * each repository partition, to speed up retrieval tasks by assigning it to the correct
 * location.
 * @author jeroen
 */
public class CreatePartitionLocation {

   public static Log log = new Log(CreatePartitionLocation.class);

   public static void main(String[] args) throws Exception {
      Configuration conf = HDTools.readConfigNoMR(args[0]);
      Repository repository = new Repository(conf);
      PartitionLocation partitionlocations = repository.getPartitionLocation();
      partitionlocations.openWrite();
      for (int partition = 0; partition < repository.getPartitions(); partition++) {
         String hosts[] = HDTools.getLocations((HDFSDir)repository.getIndexDir(), partition);
         partitionlocations.write( hosts );
      }
      partitionlocations.closeWrite();
   }
}
