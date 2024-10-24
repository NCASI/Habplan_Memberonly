/*
    Cholesky_f77.java copyright claim:

    This software is based on public domain LINPACK routines.
    It was translated from FORTRAN to Java by a US government employee 
    on official time.  Thus this software is also in the public domain.


    The translator's mail address is:

    Steve Verrill 
    USDA Forest Products Laboratory
    1 Gifford Pinchot Drive
    Madison, Wisconsin
    53705


    The translator's e-mail address is:

    steve@ws10.fpl.fs.fed.us


***********************************************************************

DISCLAIMER OF WARRANTIES:

THIS SOFTWARE IS PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND. 
THE TRANSLATOR DOES NOT WARRANT, GUARANTEE OR MAKE ANY REPRESENTATIONS 
REGARDING THE SOFTWARE OR DOCUMENTATION IN TERMS OF THEIR CORRECTNESS, 
RELIABILITY, CURRENTNESS, OR OTHERWISE. THE ENTIRE RISK AS TO 
THE RESULTS AND PERFORMANCE OF THE SOFTWARE IS ASSUMED BY YOU. 
IN NO CASE WILL ANY PARTY INVOLVED WITH THE CREATION OR DISTRIBUTION 
OF THE SOFTWARE BE LIABLE FOR ANY DAMAGE THAT MAY RESULT FROM THE USE 
OF THIS SOFTWARE.

Sorry about that.

***********************************************************************


History:

Date        Translator        Changes

3/2/97      Steve Verrill     Translated

*/


package linear_algebra;

import linear_algebra.*;


/**
*
*<p>
*This class contains the LINPACK DPOFA (Cholesky decomposition),
*DPOSL (solve), and DPODI (determinant and inverse) routines.
*
*<p>
*<b>IMPORTANT:</b>  The "_f77" suffixes indicate that these routines use
*FORTRAN style indexing.  For example, you will see
*<pre>
*   for (i = 1; i <= n; i++)
*</pre>
*rather than
*<pre>
*   for (i = 0; i < n; i++)
*</pre>
*To use the "_f77" routines you will have to declare your vectors
*and matrices to be one element larger (e.g., v[101] rather than
*v[100], and a[101][101] rather than a[100][100]), and you will have
*to fill elements 1 through n rather than elements 0 through n - 1.
*Versions of these programs that use C/Java style indexing will
*soon be available.  They will end with the suffix "_j".
*
*<p>
*This class was translated by a statistician from FORTRAN versions of 
*the LINPACK routines.  It is NOT an official translation.  It wastes
*memory by failing to use the first elements of vectors.  When 
*public domain Java numerical analysis routines become available 
*from the people who produce LAPACK, then <b>THE CODE PRODUCED
*BY THE NUMERICAL ANALYSTS SHOULD BE USED</b>.
*
*<p>
*Meanwhile, if you have suggestions for improving this
*code, please contact Steve Verrill at steve@ws10.fpl.fs.fed.us.
*
*@author Steve Verrill
*@version .5 --- March 2, 1997
* 
*/

public class Cholesky_f77 extends Object {

/**
*
*<p>
*This method decomposes an p by p symmetric, positive definite 
*matrix X into a product, R&#180R, where R is an upper triangular matrix
*and R&#180 is the transpose of R.
*For details, see the comments in the code.
*This method is a translation from FORTRAN to Java of the LINPACK subroutine
*DPOFA.  In the LINPACK listing DPOFA is attributed to Cleve Moler
*with a date of 8/14/78.
*
*Translated by Steve Verrill, March 2, 1997.
*
*@param  a     The matrix to be decomposed
*@param  n     The order of a
*
*/



   public static void dpofa_f77 (double a[][], int n) throws
                                                  NotPosDefException {

/*

Here is a copy of the LINPACK documentation (from the SLATEC version):

c***begin prologue  dpofa
c***date written   780814   (yymmdd)
c***revision date  861211   (yymmdd)
c***category no.  d2b1b
c***keywords  library=slatec(linpack),
c             type=double precision(spofa-s dpofa-d cpofa-c),
c             linear algebra,matrix,matrix factorization,
c             positive definite
c***author  moler, c. b., (u. of new mexico)
c***purpose  factors a double precision symmetric positive definite
c            matrix.
c***description
c
c     dpofa factors a double precision symmetric positive definite
c     matrix.
c
c     dpofa is usually called by dpoco, but it can be called
c     directly with a saving in time if  rcond  is not needed.
c     (time for dpoco) = (1 + 18/n)*(time for dpofa) .
c
c     on entry
c
c        a       double precision(lda, n)
c                the symmetric matrix to be factored.  only the
c                diagonal and upper triangle are used.
c
c        lda     integer
c                the leading dimension of the array  a .
c
c        n       integer
c                the order of the matrix  a .
c
c     on return
c
c        a       an upper triangular matrix  r  so that  a = trans(r)*r
c                where  trans(r)  is the transpose.
c                the strict lower triangle is unaltered.
c                if  info .ne. 0 , the factorization is not complete.
c
c        info    integer
c                = 0  for normal return.
c                = k  signals an error condition.  the leading minor
c                     of order  k  is not positive definite.


   For the Java version, info 
   is returned as an argument to NotPosDefException
   if the matrix is not positive definite.


c
c     linpack.  this version dated 08/14/78 .
c     cleve moler, university of new mexico, argonne national lab.
c
c     subroutines and functions
c
c     blas ddot
c     fortran dsqrt
c***references  dongarra j.j., bunch j.r., moler c.b., stewart g.w.,
c                 *linpack users  guide*, siam, 1979.
c***routines called  ddot
c***end prologue  dpofa

*/

      double s,t;
      int j,jm1,k,info;
      
      for (j = 1; j <= n; j++) {

         info = j;
         s = 0.0;
         jm1 = j - 1;

         for (k = 1; k <= jm1; k++) {

            t = a[k][j] - Blas_f77.coldot_f77(k-1,a,1,k,j);
            t = t/a[k][k];
            a[k][j] = t;
            s = s + t*t;

         }

         s = a[j][j] - s;

         if (s <= 0.0) {

/*

            System.out.print("\nCholesky_f77.dpofa error:" +
            "  A is not positive definite.\n" +
            "The j value at which a non-positive a[j][j] - sum value\n" + 
            "was encountered is " + j + ".\n\n");

*/

            throw new NotPosDefException(info);

         }

         a[j][j] = Math.sqrt(s);

      }

      return;

   }


/**
*
*<p>
*This method uses the Cholesky decomposition provided by
*DPOFA to solve the equation Ax = b where A is symmetric,
*positive definite.
*For details, see the comments in the code.
*This method is a translation from FORTRAN to Java of the LINPACK subroutine
*DPOSL.  In the LINPACK listing DPOSL is attributed to Cleve Moler
*with a date of 8/14/78.
*
*Translated by Steve Verrill, March 2, 1997.
*
*@param  a     a[][]
*@param  n     The order of a
*@param  b     The vector b in Ax = b
*
*/

   public static void dposl_f77 (double a[][], int n, double b[]) {

/*

Here is a copy of the LINPACK documentation (from the SLATEC version):
      
c***begin prologue  dposl
c***date written   780814   (yymmdd)
c***revision date  861211   (yymmdd)
c***category no.  d2b1b
c***keywords  library=slatec(linpack),
c             type=double precision(sposl-s dposl-d cposl-c),
c             linear algebra,matrix,positive definite,solve
c***author  moler, c. b., (u. of new mexico)
c***purpose  solves the double precision symmetric positive definite
c            system a*x=b using the factors computed by dpoco or dpofa.
c***description
c
c     dposl solves the double precision symmetric positive definite
c     system a * x = b
c     using the factors computed by dpoco or dpofa.
c
c     on entry
c
c        a       double precision(lda, n)
c                the output from dpoco or dpofa.
c
c        lda     integer
c                the leading dimension of the array  a .
c
c        n       integer
c                the order of the matrix  a .
c
c        b       double precision(n)
c                the right hand side vector.
c
c     on return
c
c        b       the solution vector  x .
c
c     error condition
c
c        a division by zero will occur if the input factor contains
c        a zero on the diagonal.  technically this indicates
c        singularity, but it is usually caused by improper subroutine
c        arguments.  it will not occur if the subroutines are called
c        correctly and  info .eq. 0 .
c
c     to compute  inverse(a) * c  where  c  is a matrix
c     with  p  columns
c           call dpoco(a,lda,n,rcond,z,info)
c           if (rcond is too small .or. info .ne. 0) go to ...
c           do 10 j = 1, p
c              call dposl(a,lda,n,c(1,j))
c        10 continue
c
c     linpack.  this version dated 08/14/78 .
c     cleve moler, university of new mexico, argonne national lab.
c
c     subroutines and functions
c
c     blas daxpy,ddot
c***references  dongarra j.j., bunch j.r., moler c.b., stewart g.w.,
c                 *linpack users  guide*, siam, 1979.
c***routines called  daxpy,ddot
c***end prologue  dposl

*/

      double t;
      int k,kb;

//   solve transpose(r)y = b

      for (k = 1; k <= n; k++) {

         t = Blas_f77.colvdot_f77(k-1,a,b,1,k);
         b[k] = (b[k] - t)/a[k][k];

      }

//   solve rx = y

      for (kb = 1; kb <= n; kb++) {

         k = n + 1 - kb;
         b[k] = b[k]/a[k][k];
         t = -b[k];
         Blas_f77.colvaxpy_f77(k-1,t,a,b,1,k);

      }

      return;

   }



/**
*
*<p>
*This method uses the Cholesky decomposition provided by
*DPOFA to obtain the determinant and/or inverse of a symmetric,
*positive definite matrix.
*For details, see the comments in the code.
*This method is a translation from FORTRAN to Java of the LINPACK subroutine
*DPODI.  In the LINPACK listing DPODI is attributed to Cleve Moler
*with a date of 8/14/78.
*
*Translated by Steve Verrill, March 3, 1997.
*
*@param  a     a[][]
*@param  n     The order of a
*@param  det   det[]
*@param  job   Indicates whether a determinant, inverse,
*              or both is desired
*
*/


   public static void dpodi_f77 (double a[][], int n, double det[], int job) {


/*

Here is a copy of the LINPACK documentation (from the SLATEC version):


C***BEGIN PROLOGUE  DPODI
C***DATE WRITTEN   780814   (YYMMDD)
C***REVISION DATE  861211   (YYMMDD)
C***CATEGORY NO.  D2B1B,D3B1B
C***KEYWORDS  LIBRARY=SLATEC(LINPACK),
C             TYPE=DOUBLE PRECISION(SPODI-S DPODI-D CPODI-C),
C             DETERMINANT,INVERSE,LINEAR ALGEBRA,MATRIX,
C             MATRIX FACTORIZATION,POSITIVE DEFINITE
C***AUTHOR  MOLER, C. B., (U. OF NEW MEXICO)
C***PURPOSE  Computes the determinant and inverse of a certain double
C            precision SYMMETRIC POSITIVE DEFINITE matrix (see abstract)
C            using the factors computed by DPOCO, DPOFA or DQRDC.
C***DESCRIPTION
C
C     DPODI computes the determinant and inverse of a certain
C     double precision symmetric positive definite matrix (see below)
C     using the factors computed by DPOCO, DPOFA or DQRDC.
C
C     On Entry
C
C        A       DOUBLE PRECISION(LDA, N)
C                the output  A  from DPOCO or DPOFA
C                or the output  X  from DQRDC.
C
C        LDA     INTEGER
C                the leading dimension of the array  A .
C
C        N       INTEGER
C                the order of the matrix  A .
C
C        JOB     INTEGER
C                = 11   both determinant and inverse.
C                = 01   inverse only.
C                = 10   determinant only.
C
C     On Return
C
C        A       If DPOCO or DPOFA was used to factor  A , then
C                DPODI produces the upper half of INVERSE(A) .
C                If DQRDC was used to decompose  X , then
C                DPODI produces the upper half of inverse(TRANS(X)*X)
C                where TRANS(X) is the transpose.
C                Elements of  A  below the diagonal are unchanged.
C                If the units digit of JOB is zero,  A  is unchanged.
C
C        DET     DOUBLE PRECISION(2)
C                determinant of  A  or of  TRANS(X)*X  if requested.
C                Otherwise not referenced.
C                Determinant = DET(1) * 10.0**DET(2)
C                with  1.0 .LE. DET(1) .LT. 10.0
C                or  DET(1) .EQ. 0.0 .
C
C     Error Condition
C
C        A division by zero will occur if the input factor contains
C        a zero on the diagonal and the inverse is requested.
C        It will not occur if the subroutines are called correctly
C        and if DPOCO or DPOFA has set INFO .EQ. 0 .
C
C     LINPACK.  This version dated 08/14/78 .
C     Cleve Moler, University of New Mexico, Argonne National Lab.
C
C     Subroutines and Functions
C
C     BLAS DAXPY,DSCAL
C     Fortran MOD
C***REFERENCES  DONGARRA J.J., BUNCH J.R., MOLER C.B., STEWART G.W.,
C                 *LINPACK USERS  GUIDE*, SIAM, 1979.
C***ROUTINES CALLED  DAXPY,DSCAL
C***END PROLOGUE  DPODI

*/

      double s,t;
      int i,j,jm1,k,kp1;

//   compute determinant

      if (job/10 != 0) {

         det[1] = 1.0;
         det[2] = 0.0;
         s = 10.0;

         for (i = 1; i <= n; i++) {

            det[1] *= a[i][i]*a[i][i];
            
            if (det[1] == 0.0) break;

            while (det[1] < 1.0) {

               det[1] *= s;
               det[2]--;

            }

            while (det[1] >= s) {

               det[1] /= s;
               det[2]++;

            }

         }

      }

//   compute inverse(R)

      if (job%10 != 0) {

         for (k = 1; k <= n; k++) {

            a[k][k] = 1.0/a[k][k];
            t = -a[k][k];
            Blas_f77.colscal_f77(k-1,t,a,1,k);
            kp1 = k + 1;

            for (j = kp1; j <= n; j++) {

               t = a[k][j];
               a[k][j] = 0.0;
               Blas_f77.colaxpy_f77(k,t,a,1,k,j);

            }

         }

//   form inverse(R) * transpose(inverse(R))

         for (j = 1; j <= n; j++) {

            jm1 = j - 1;

            for (k = 1; k <= jm1; k++) {

               t = a[k][j];
               Blas_f77.colaxpy_f77(k,t,a,1,j,k);

            }

            t = a[j][j];
            Blas_f77.colscal_f77(j,t,a,1,j);

         }   

      }

      return;

   }   
                                    
}
