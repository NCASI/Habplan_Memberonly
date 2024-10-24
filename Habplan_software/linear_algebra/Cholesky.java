/*
    Cholesky.java copyright claim:

    Copyright (C) 1996 by Steve Verrill.

    This class is free software; you can redistribute it and/or 
    modify it under the terms of version 2 of the GNU General 
    Public License as published by the Free Software Foundation.

    This class is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with these programs in the file COPYING; if not, write to 
    the Free Software Foundation, Inc., 675 Mass Ave, Cambridge, 
    MA 02139, USA.

    The author's mail address is:

    Steve Verrill 
    USDA Forest Products Laboratory
    1 Gifford Pinchot Drive
    Madison, Wisconsin
    53705

    The author's e-mail address is:

    steve@ws10.fpl.fs.fed.us


***********************************************************************

DISCLAIMER OF WARRANTIES:

THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. 
THE AUTHOR DOES NOT WARRANT, GUARANTEE OR MAKE ANY REPRESENTATIONS 
REGARDING THE SOFTWARE OR DOCUMENTATION IN TERMS OF THEIR CORRECTNESS, 
RELIABILITY, CURRENTNESS, OR OTHERWISE. THE ENTIRE RISK AS TO 
THE RESULTS AND PERFORMANCE OF THE SOFTWARE IS ASSUMED BY YOU. 
IN NO CASE WILL ANY PARTY INVOLVED WITH THE CREATION OR DISTRIBUTION 
OF THE SOFTWARE BE LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE 
OF THIS SOFTWARE.

Sorry about that.

***********************************************************************


History:

Date        Author            Changes

11/30/96    Steve Verrill     Created

*/


package linear_algebra;

import linear_algebra.*;


/**
*
*<p>
*This class contains:
*<ol>
*<li> a method that obtains the Cholesky
*factorization RR&#180, where R is a lower triangular matrix, 
*of a symmetric positive definite matrix A.
*<li> a method to invert a symmetric positive
*definite matrix.
*<li> a method to solve Ax = b where A is a symmetric
*positive definite matrix.
*</ol>
*
*<p>
*This class was written by a statistician rather than
*a numerical analyst.  I have tried to check the code carefully,
*but it may still contain bugs.  Further, its stability and
*efficiency do not meet the standards of high quality
*numerical analysis software.  When public domain Java numerical 
*analysis routines become available from numerical analysts 
*(e.g., the people who produce LAPACK), then <b>THE CODE PRODUCED
*BY THE NUMERICAL ANALYSTS SHOULD BE USED</b>.
*
*<p>
*Meanwhile, if you have suggestions for improving this
*code, please contact Steve Verrill at <a
*href="mailto:steve@ws10.fpl.fs.fed.us">steve@ws10.fpl.fs.fed.us</a>.
*
*@author Steve Verrill
*@version .5 --- November 30, 1996
* 
*/

public class Cholesky extends Object {

/**
*
*<p>
*This method factors the n by n symmetric positive definite
*matrix A as RR&#180 where R is a lower triangular matrix.
*The method assumes that at least the lower triangle
*of A is filled on entry.  On exit, the lower triangle
*of A has been replaced by R.
*
*@param  a[&#32][&#32]  The positive definite matrix to factor.  The method assumes
*that at least the lower triangle of a[&#32][&#32] is filled on entry.  On exit
*the lower triangle of a[&#32][&#32] has been replaced by R such that RR&#180 = A.
*@param  n  The order of the matrix a[&#32][&#32].
*@throws NotPosDefException if the factorization cannot be completed.
*
*/

   public void factorPosDef (double a[][], int n) throws NotPosDefException {


  
      double sum,diff;

      int i,j,l;

      for (i = 0; i < n; i++) {

         sum = 0.0;

         for (j = 0; j < i; j++) {

            sum += a[i][j]*a[i][j];

         }

         diff = a[i][i] - sum;

         if (diff <= 0.0) {

            System.out.print("\nCholesky.factorPosDef error:" +
            "  A is not positive definite.\n" +
            "The i value at which a non-positive a[i][i] - sum value\n" + 
            "was encountered is " + i + ".\n\n");

            throw new NotPosDefException();

         }

         a[i][i] = Math.sqrt(diff);

         for (l = i+1; l < n; l++) {

            sum = 0.0;

            for (j = 0; j < i; j++) {

               sum += a[l][j]*a[i][j];

            }

            a[l][i] = (a[l][i] - sum)/a[i][i];

         }

      }   

      return;

   }             


/**
*
*<p>
*This method solves the equation
*<pre>
*
*      Ax = b
*
*</pre>
*where A is a known n by n symmetric positive definite matrix,
*and b is a known vector of length n.<br><br>
*
*<dl>
*
*   <dt>On entrance:
*
*      <dd>If factored == false, the lower triangle of a[&#32][&#32] should
*      contain the lower triangle of A.<br>
*
*      If factored == true, the lower triangle of a[&#32][&#32] should
*      contain a lower triangular matrix R such that RR&#180 = A.
*
*   <dt>On exit:
*
*      <dd>The elements of b have been replaced by the elements of x.
*
*</dl>
*
*<p>
*The method proceeds by first factoring A as RR&#180 where R is a lower
*triangular matrix.  Thus Ax = b is equivalent to R(R&#180x) = b.  Then the
*method performs two additional operations.  First it solves Ry = b for y.
*Then it solves R&#180x = y for x.  It stores x in b.
*
*@param  a[&#32][&#32]  On entrance, if the boolean variable factored is false,
*the lower triangle of a[&#32][&#32] should contain the lower triangle of A.
*If factored is true, the lower triangle of a[&#32][&#32] should
*contain a lower triangular matrix R such that RR&#180 = A.
*@param  b  On entrance b must contain the known b of Ax = b.  On exit
*it contains the solution x to Ax = b.
*@param  y  A work vector of order at least n.
*@param  n  The order of A and b.
*@param  factored  On entrance, factored should be set to true if A 
*already has been factored, false
*if A has not yet been factored.
*@throws NotPosDefException if A cannot be factored as RR&#180 for a
*full rank lower triangular matrix R.
*
*/

   public void solvePosDef (double a[][], double b[], double y[],
                     int n, boolean factored) 
                     throws NotPosDefException {

      Cholesky cholesky = new Cholesky();
      Triangular triangular = new Triangular();




      int i,j;


      if (factored == false) {

/*

   A has not yet been factored.  Factor it.
   The lower triangle of a[][] will contain R where 
   RR' = A.

*/

         try {

            cholesky.factorPosDef(a,n);

         } catch (NotPosDefException npd) {

            System.out.print("\nCholesky.solvePosDef error:" +
            "  The matrix was not positive definite\n" +
            "so it was not possible" +
            "to use the Cholesky decomposition\n" +
            " to solve Ax = b.\n\n");

            throw npd;

         }

      }

/*

   Solve Ry = b.

*/

      try {

         triangular.solveLower(a, y, b, n);

      } catch (NotFullRankException nfr) {

         System.out.print("\nTriangular.solveLower error:" +
         "  The lower triangular factor of A\n" +
         "was not of full rank.\n\n");

         throw new NotPosDefException();

      }

/*

   Fill the upper triangle of a[][] with R'
   where RR' = A.

*/

      for (i = 0; i < n; i++) {

         for (j = i; j < n; j++) {

            a[i][j] = a[j][i];

         }

      }


/*

   Solve R'x = y.  Put x in the b vector.

*/

      try {

         triangular.solveUpper(a, b, y, n);

      } catch (NotFullRankException nfr) {

         System.out.print("\nTriangular.solveUpper error:" +
         "  The upper triangular factor of A\n" +
         "was not of full rank.\n\n");

         throw new NotPosDefException();

      }

      return;

   }             


/**
*
*<p>
*This method obtains the inverse of an n by n
*symmetric positive definite matrix A.<br><br>
*<dl>
*   <dt>On entrance:
*
*      <dd>If factored == false, the lower triangle of a[&#32][&#32] should
*      contain the lower triangle of A.<br>
*
*      If factored == true, the lower triangle of a[&#32][&#32] should
*      contain a lower triangular matrix R such that RR&#180 = A.
*
*   <dt>On exit:
*
*      <dd>The lower triangle of a[&#32][&#32] has been replaced by the
*      lower triangle of the inverse of A.
*</dl>
*
*@param  a[&#32][&#32]  On entrance, if the boolean variable factored is false,
*the lower triangle of a[&#32][&#32] should contain the lower triangle of A.
*If factored is true, the lower triangle of a[&#32][&#32] should
*contain a lower triangular matrix R such that RR&#180 = A.
*@param  n  The order of A.
*@param  factored  On entrance, factored should be set to true if A 
*already has been factored, false
*if A has not yet been factored.
*@throws NotPosDefException if A cannot be factored as RR&#180 for a
*full rank lower triangular matrix R.
*
*/


   public void invertPosDef (double a[][], int n, boolean factored) 
                      throws NotPosDefException {

      Cholesky cholesky = new Cholesky();
      Triangular triangular = new Triangular();





      double sum;

      int i,j,k;



      if (factored == false) {

/*

   A has not yet been factored.  Factor it.
   The lower triangle of a[][] will contain R where 
   RR' = A.

*/

         try {

            cholesky.factorPosDef(a,n);

         } catch (NotPosDefException npd) {

            System.out.print("\nCholesky.invertPosDef error:" +
            "  The matrix was not positive definite\n" +
            "so it was not possible" +
            "to use the Cholesky decomposition\n" +
            " to obtain an inverse.\n\n");

            throw npd;

         }

      }

/*

   Invert the lower triangle of a[][].
   The lower triangle will contain 
   the inverse of R
   where RR' = A.

*/


      try {

         triangular.invertLower(a,n);

      } catch (NotFullRankException nfr) {

         System.out.print("\nCholesky.invertPosDef error:" +
         "  The matrix was not positive definite\n" +
         "so it was not possible" +
         "to use the Cholesky decomposition\n" +
         " to obtain an inverse.\n\n");

         throw new NotPosDefException();

      }

/*

   The inverse of A is (R^{-1})'(R^{-1}).
   Fill the lower triangle of a[][] with
   the lower triangle of (R^{-1})'(R^{-1}).

*/

      for (j = 0; j < n; j++) {

         for (i = j; i < n; i++) {

            sum = 0.0;

            for (k = i; k < n; k++) {

               sum += a[k][i]*a[k][j];

            }

            a[i][j] = sum;

         }

      }

      return;

   }

}
