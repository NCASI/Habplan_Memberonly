
package linear_algebra;

import java.util.*;
import java.lang.*;
import java.io.*;
import linear_algebra.*;
import corejava.Console;

/**
*
*This class tests the Cholesky decomposition class
*and the Triangular solve/invert class.
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
*recover R. (This tests Cholesky.factorPosDef.)
*<li> Solves the system Az = b in an effort to recover x. 
*(This tests Cholesky.solvePosDef, Triangular.solveLower,
* and Triangular.solveUpper.)
*<li> Obtains an estimate of A^{-1} and compares A^{-1}A with the
*identity matrix. (This tests Cholesky.invertPosDef and
*Triangular.invertLower.)
*<li> Obtains the inverse of an upper triangular matrix and compares
*the product of this inverse and the original upper triangular matrix
*with the identity matrix. (This tests Triangular.invertUpper.)
*</ol>
*
*@author Steve Verrill
*@version .5 --- November 30, 1996
*
*/


public class CholTest extends Object {


   public static void main (String args[]) {

      Cholesky cholesky = new Cholesky();
      Triangular triangular = new Triangular();

/*

Some of the variables of main:

1.)   randstart --- The integer starting value for the 
                    random number generator.
2.)           n --- The order of the vectors and matrices.
3.)       r[][] --- Holds the lower triangular Cholesky factor of
                    the positive definite matrix A.
4.)       a[][] --- Holds the lower triangle of A.
5.)      a2[][] --- A copy of a[][].
6.)  aupper[][] --- Holds the upper triangle of A.
7.) aupper2[][] --- A copy of aupper[][].
8.)   afull[][] --- Holds all of A.
9.)         x[] --- The x in Ax = b.
10.)        y[] --- Work array needed to help solve Ax = b.
11.)        b[] --- The b in Ax = b.

*/ 

      long randstart;

      int n;

      int i,j,k;

      double randlow,randhigh,randdiff;

      double r[][] = new double[100][100];
      double a[][] = new double[100][100];
      double a2[][] = new double[100][100];
      double aupper[][] = new double[100][100];
      double aupper2[][] = new double[100][100];
      double afull[][] = new double[100][100];

      double x[] = new double[100];
      double y[] = new double[100];
      double b[] = new double[100];

/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press/Prentice-Hall).

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

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            r[i][j] = randlow + rand01.nextDouble()*randdiff;

         }

      }
      System.out.print("\n\n");
      System.out.print("*********************************************\n");
      System.out.print("\nThe r matrix is \n\n");

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            System.out.print(r[i][j] + "  ");

         }

         System.out.print("\n");

      }

/*

   The positive definite matrix A = RR'.

*/

      for (j = 0; j < n; j++) {

         for (i = j; i < n; i++) {

            a[i][j] = 0.0;

            for (k = 0; k <= j; k++) {

               a[i][j] += r[i][k]*r[j][k];

            }

            a2[i][j] = a[i][j];
/*

   a[][] only has the lower triangle filled.
   a2[][] is a duplicate of a[][].
   aupper[][] has only the upper triangle filled.
   aupper2[][] is a duplicate of aupper[][].
   afull[][] has both the upper and lower triangles filled.

*/
            afull[i][j] = a[i][j];
            afull[j][i] = afull[i][j];

            aupper[j][i] = a[i][j];
            aupper2[j][i] = a[i][j];

         }

      }




/*

   Generate a vector x

*/

      for (i = 0; i < n; i++) {

         x[i] = randlow + rand01.nextDouble()*randdiff;

      }


/*

   Calculate Ax = b

*/

      for (i = 0; i < n; i++) {

         b[i] = 0.0;

         for (j = 0; j < n; j++) {

            b[i] += afull[i][j]*x[j];

         }

      }


/*   

   This should recover R.

*/

      try {

         cholesky.factorPosDef(a,n);

      } catch (NotPosDefException npd) {

         System.out.print("\nThe matrix was not positive definite" +
         " so it was not possible\n" +
         "to obtain a Cholesky decomposition.\n\n");  

         System.exit(0);

      }   


      System.out.print("\n\nThe recovered r matrix is \n\n");

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            System.out.print(a[i][j] + "  ");

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

      try {

         cholesky.solvePosDef(a,b,y,n,true);

      } catch (NotPosDefException npd) {

         System.out.print("\nThe matrix was not positive definite" +
         " so it was not possible\n" +
         "to solve Ax = b.\n\n");  

      }   


      System.out.print("\nThe input x vector was\n\n");

      for (i = 0; i < n; i++) {

         System.out.print(x[i] + "\n");

      }

      System.out.print("\n\nThe recovered x vector was\n\n");

      for (i = 0; i < n; i++) {

         System.out.print(b[i] + "\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");

/*

   Obtain the inverse of A.

*/
      try {

         cholesky.invertPosDef(a2,n,false);

      } catch (NotPosDefException npd) {

         System.out.print("\nThe matrix was not positive definite" +
         " so it was not possible\n" +
         "to use a Cholesky decomposition to obtain an inverse.\n\n");

      }

/*

   Fill the upper triangle of a2 so a2[][] = A^{-1}.

*/
      
      for (i = 0; i < n - 1; i++) {

         for (j = i + 1; j < n; j++) {

            a2[i][j] = a2[j][i];

         }

      }

/*

   now a2*afull should equal I

*/

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            a[i][j] = 0.0;

            for (k = 0; k < n; k++) {

               a[i][j] += a2[i][k]*afull[k][j];

            }

         }

      }

      System.out.print("\nA test of Cholesky.invertPosDef\n\n" +
      "The lower triangle of the I matrix is:\n\n");

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            System.out.print(a[i][j] + "  ");

         }

         System.out.print("\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");

/*

    Test the inversion of upper triangular matrices.

*/

/*

   Obtain the inverse of aupper[][].

*/
      try {

         triangular.invertUpper(aupper,n);

      } catch (NotFullRankException nfr) {

         System.out.print("\nThe upper triangular matrix was not of" +
         " full rank\n" + " so it was not possible " +
         "to invert it.\n\n");

      }

/*

   now aupper*aupper2 should equal I

*/

      for (i = 0; i < n; i++) {

         for (j = i; j < n; j++) {

            a[i][j] = 0.0;

            for (k = 0; k < n; k++) {

               a[i][j] += aupper[i][k]*aupper2[k][j];

            }

         }

      }

      System.out.print("\nA test of Triangular.invertUpper\n\n" +
      "The transpose of the upper triangle of the I matrix is:\n\n");

      for (i = 0; i < n; i++) {

         for (j = 0; j <= i; j++) {

            System.out.print(a[j][i] + "  ");

         }

         System.out.print("\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");

   }

}



