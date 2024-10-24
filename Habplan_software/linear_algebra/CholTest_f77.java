
package linear_algebra;

import java.util.*;
import java.lang.*;
import java.io.*;
import linear_algebra.*;
import corejava.Console;

/**
*
*This class tests the Cholesky_f77 classes.
*
*It
*<ol>
*<li> Randomly generates numbers between randlow and randhigh
*and fills a lower triangular matrix R with them.  Note that
*randlow and randhigh should be of the same sign.  Otherwise it
*would be possible for R not to be of full rank.
*<li> Obtains the positive definite matrix A = RR&#180.
*<li> Randomly generates a vector x.
*<li> Calculates the vector b = Ax.
*<li> Performs a Cholesky decomposition of A in an effort to
*recover R. (This tests Cholesky_f77.dpofa_f77.)
*<li> Solves the system Az = b in an effort to recover x. 
*(This tests Cholesky_f77.dposl_f77.)
*<li> Obtains an estimate of A^{-1} and compares A^{-1}A with the
*identity matrix. (This tests Cholesky_f77.dpodi_f77.)
*</ol>
*
*@author Steve Verrill
*@version .5 --- March 12, 1997
*
*/


public class CholTest_f77 extends Object {


   public static void main (String args[]) {


/*

Some of the variables of main:

1.)   randstart --- The integer starting value for the 
                    random number generator.
2.)           n --- The order of the vectors and matrices.
3.)       r[][] --- Holds the lower triangular Cholesky factor of
                    the positive definite matrix A.
4.)       a[][] --- Holds the lower triangle of A.
5.)  aupper[][] --- Holds the upper triangle of A.
6.)   afull[][] --- Holds all of A.
7.)         x[] --- The x in Ax = b.
8.)         y[] --- Work array needed to help solve Ax = b.
9.)         b[] --- The b in Ax = b.

*/ 

      long randstart;

      int n;

      int i,j,k;

      double randlow,randhigh,randdiff;

      double r[][] = new double[101][101];
      double a[][] = new double[101][101];
      double aupper[][] = new double[101][101];
      double afull[][] = new double[101][101];

      double x[] = new double[101];
      double y[] = new double[101];
      double b[] = new double[101];

      double det[] = new double[2];

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/
   
      randstart = Console.readInt("\nWhat is the starting value (an " +
      "integer)\n" +
      "for the random number generator? ");

      n = Console.readInt("\nWhat is the n value? (100 or less) ");

      Random rand01 = new Random(randstart);

      randlow = Console.readDouble("\nWhat is randlow? ");

      randhigh = Console.readDouble("\nWhat is randhigh? ");

      randdiff = randhigh - randlow;

/*

   Generate a positive definite matrix.

*/

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= i; j++) {

            r[i][j] = randlow + rand01.nextDouble()*randdiff;

         }

      }
      System.out.print("\n\n");
      System.out.print("*********************************************\n");
      System.out.print("\nThe r matrix is \n\n");

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= i; j++) {

            System.out.print(r[i][j] + "  ");

         }

         System.out.print("\n");

      }

/*

   The positive definite matrix A = RR'.

*/

      for (j = 1; j <= n; j++) {

         for (i = j; i <= n; i++) {

            a[i][j] = 0.0;

            for (k = 1; k <= j; k++) {

               a[i][j] += r[i][k]*r[j][k];

            }

/*

   a[][] only has the lower triangle filled.
   aupper[][] has only the upper triangle filled.
   afull[][] has both the upper and lower triangles filled.

*/

            aupper[j][i] = a[i][j];

            afull[i][j] = a[i][j];
            afull[j][i] = afull[i][j];


         }

      }




/*

   Generate a vector x

*/

      for (i = 1; i <= n; i++) {

         x[i] = randlow + rand01.nextDouble()*randdiff;

      }


/*

   Calculate Ax = b

*/

      for (i = 1; i <= n; i++) {

         b[i] = 0.0;

         for (j = 1; j <= n; j++) {

            b[i] += afull[i][j]*x[j];

         }

      }


/*   

   This should recover R.

*/

      try {

         Cholesky_f77.dpofa_f77(aupper,n);

      } catch (NotPosDefException npd) {

         System.out.print("\nThe matrix was not positive definite" +
         " so it was not possible\n" +
         "to obtain a Cholesky decomposition.\n\n");  

         System.out.print("\nThe info value was " + npd.info + ".\n\n");  

         System.exit(0);

      }
   


      System.out.print("\n\nThe recovered r matrix is \n\n");

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= i; j++) {

            System.out.print(aupper[j][i] + "  ");

         }

         System.out.print("\n");

      }

      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");

/*   

   This should obtain the solution to Ax = b.
   The solution x will be in the vector b.

*/

      Cholesky_f77.dposl_f77(aupper,n,b);


      System.out.print("\nThe input x vector was\n\n");

      for (i = 1; i <= n; i++) {

         System.out.print(x[i] + "\n");

      }

      System.out.print("\n\nThe recovered x vector was\n\n");

      for (i = 1; i <= n; i++) {

         System.out.print(b[i] + "\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");

/*

   Obtain the inverse of A.

*/

      Cholesky_f77.dpodi_f77(aupper,n,det,1);

/*

   Fill the  lower triangle of aupper so aupper[][] = A^{-1}.

*/
      
      for (i = 2; i <= n; i++) {

         for (j = 1; j < i; j++) {

            aupper[i][j] = aupper[j][i];

         }

      }

/*

   now aupper*afull should equal I

*/

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= i; j++) {

            a[i][j] = 0.0;

            for (k = 1; k <= n; k++) {

               a[i][j] += aupper[i][k]*afull[k][j];

            }

         }

      }

      System.out.print("\nA test of Cholesky_f77.dpodi_f77\n\n" +
      "The lower triangle of the I matrix is:\n\n");

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= i; j++) {

            System.out.print(a[i][j] + "  ");

         }

         System.out.print("\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");


   }

}




