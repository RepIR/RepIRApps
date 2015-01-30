package util;

import io.github.repir.Repository.Repository;
import java.io.IOException;

/**
 * Store the maximum parameter settings in ModelParameters, using n-fold cross evaluation
 * or leave-a-testset-out.
 * @author Jeroen Vuurens
 */
public class StoreSolution {

   public static void main(String[] args) throws IOException {
      Repository repository = new Repository(args);
      if (repository.configuredString("testset.crossevaluate").equals("fold"))
         new StoreSolutionFold(repository);
      else
         new StoreSolutionCross(repository);
   }
}
