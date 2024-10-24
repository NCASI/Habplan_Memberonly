
package linear_algebra;

/**
*
*This is the exception produced by the Cholesky factorization
*if it determines that the matrix to be factored is not
*positive definite.
*
*/

public class NotPosDefException extends Exception {

   public int info;


   public NotPosDefException(int info) {

      this.info = info;

   }

   public NotPosDefException(String problem) {

      super(problem);

   }

   public NotPosDefException() {

   }

}

