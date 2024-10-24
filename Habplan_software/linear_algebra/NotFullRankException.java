
package linear_algebra;

/**
*
*This is the exception produced by a Triangular method
*if a matrix has at least one zero diagonal element.
*
*/

public class NotFullRankException extends Exception {

   public int info;


   public NotFullRankException(int info) {

      this.info = info;

   }

   public NotFullRankException(String problem) {

      super(problem);

   }

   public NotFullRankException() {

   }

}
