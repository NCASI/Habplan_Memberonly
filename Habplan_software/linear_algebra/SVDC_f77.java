/*
    SVDC_f77.java copyright claim:

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

3/3/97      Steve Verrill     Translated

*/


package linear_algebra;

import linear_algebra.*;


/**
*
*<p>
*This class contains the LINPACK DSVDC (singular value
*decomposition) routine.
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
*@version .5 --- March 3, 1997
* 
*/

public class SVDC_f77 extends Object {

/**
*
*<p>
*This method decomposes a n by p 
*matrix X into a product UDV&#180 where ...
*For details, see the comments in the code.
*This method is a translation from FORTRAN to Java 
*of the LINPACK subroutine DSVDC.  
*In the LINPACK listing DSVDC is attributed to G.W. Stewart
*with a date of 3/19/79.
*
*Translated by Steve Verrill, March 3, 1997.
*
*@param  x     The matrix to be decomposed
*@param  n     The number of rows of X
*@param  p     The number of columns of X
*@param  s     The singular values of X
*@param  e     See the documentation in the code
*@param  u     The left singular vectors
*@param  v     The right singular vectors
*@param  work  A work array
*@param  job   See the documentation in the code
*@param  info  See the documentation in the code
*
*/



   public static void dsvdc_f77 (double x[][], int n, int p, double s[],
                                 double e[], double u[][], double v[][], 
                                 double work[], int job) 
                                 throws SVDCException {


/*

Here is a copy of the LINPACK documentation (from the SLATEC version):


C***BEGIN PROLOGUE  DSVDC
C***DATE WRITTEN   790319   (YYMMDD)
C***REVISION DATE  861211   (YYMMDD)
C***CATEGORY NO.  D6
C***KEYWORDS  LIBRARY=SLATEC(LINPACK),
C             TYPE=DOUBLE PRECISION(SSVDC-S DSVDC-D CSVDC-C),
C             LINEAR ALGEBRA,MATRIX,SINGULAR VALUE DECOMPOSITION
C***AUTHOR  STEWART, G. W., (U. OF MARYLAND)
C***PURPOSE  Perform the singular value decomposition of a d.p. NXP
C            matrix.
C***DESCRIPTION
C
C     DSVDC is a subroutine to reduce a double precision NxP matrix X
C     by orthogonal transformations U and V to diagonal form.  The
C     diagonal elements S(I) are the singular values of X.  The
C     columns of U are the corresponding left singular vectors,
C     and the columns of V the right singular vectors.
C
C     On Entry
C
C         X         DOUBLE PRECISION(LDX,P), where LDX .GE. N.
C                   X contains the matrix whose singular value
C                   decomposition is to be computed.  X is
C                   destroyed by DSVDC.
C
C         LDX       INTEGER.
C                   LDX is the leading dimension of the array X.
C
C         N         INTEGER.
C                   N is the number of rows of the matrix X.
C
C         P         INTEGER.
C                   P is the number of columns of the matrix X.
C
C         LDU       INTEGER.
C                   LDU is the leading dimension of the array U.
C                   (See below).
C
C         LDV       INTEGER.
C                   LDV is the leading dimension of the array V.
C                   (See below).
C
C         WORK      DOUBLE PRECISION(N).
C                   WORK is a scratch array.
C
C         JOB       INTEGER.
C                   JOB controls the computation of the singular
C                   vectors.  It has the decimal expansion AB
C                   with the following meaning
C
C                        A .EQ. 0    do not compute the left singular
C                                  vectors.
C                        A .EQ. 1    return the N left singular vectors
C                                  in U.
C                        A .GE. 2    return the first MIN(N,P) singular
C                                  vectors in U.
C                        B .EQ. 0    do not compute the right singular
C                                  vectors.
C                        B .EQ. 1    return the right singular vectors
C                                  in V.
C
C     On Return
C
C         S         DOUBLE PRECISION(MM), where MM=MIN(N+1,P).
C                   The first MIN(N,P) entries of S contain the
C                   singular values of X arranged in descending
C                   order of magnitude.
C
C         E         DOUBLE PRECISION(P).
C                   E ordinarily contains zeros.  However see the
C                   discussion of INFO for exceptions.
C
C         U         DOUBLE PRECISION(LDU,K), where LDU .GE. N.
C                   If JOBA .EQ. 1, then K .EQ. N.
C                   If JOBA .GE. 2, then K .EQ. MIN(N,P).
C                   U contains the matrix of right singular vectors.
C                   U is not referenced if JOBA .EQ. 0.  If N .LE. P
C                   or if JOBA .EQ. 2, then U may be identified with X
C                   in the subroutine call.
C
C         V         DOUBLE PRECISION(LDV,P), where LDV .GE. P.
C                   V contains the matrix of right singular vectors.
C                   V is not referenced if JOB .EQ. 0.  If P .LE. N,
C                   then V may be identified with X in the
C                   subroutine call.
C
C         INFO      INTEGER.
C                   The singular values (and their corresponding
C                   singular vectors) S(INFO+1),S(INFO+2),...,S(M)
C                   are correct (here M=MIN(N,P)).  Thus if
C                   INFO .EQ. 0, all the singular values and their
C                   vectors are correct.  In any event, the matrix
C                   B = TRANS(U)*X*V is the bidiagonal matrix
C                   with the elements of S on its diagonal and the
C                   elements of E on its super-diagonal (TRANS(U)
C                   is the transpose of U).  Thus the singular
C                   values of X and B are the same.


   For the Java version, info
   is returned as an argument to SVDCException
   if the decomposition fails.


C
C     LINPACK.  This version dated 03/19/79 .
C     G. W. Stewart, University of Maryland, Argonne National Lab.
C
C     DSVDC uses the following functions and subprograms.
C
C     External DROT
C     BLAS DAXPY,DDOT,DSCAL,DSWAP,DNRM2,DROTG
C     Fortran DABS,DMAX1,MAX0,MIN0,MOD,DSQRT
C***REFERENCES  DONGARRA J.J., BUNCH J.R., MOLER C.B., STEWART G.W.,
C                 *LINPACK USERS  GUIDE*, SIAM, 1979.
C***ROUTINES CALLED  DAXPY,DDOT,DNRM2,DROT,DROTG,DSCAL,DSWAP
C***END PROLOGUE  DSVDC

*/

//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  This is the cause of the following changes:

//      double t,b,c,cs,el,emm1,f,g,scale1,scale2,scale,shift,sl,sm,
//             sn,smm1,t1,test,ztest,fac;

//      int i,iter,j,jobu,k,kase,kk,l,ll,lls,lm1,lp1,ls,lu,
//          m,maxit,mm,mm1,mp1,nct,nctp1,ncu,nrt,nrtp1;

      double t,b,c,cs,f,g,scale,shift,
             sn,t1,test,ztest;

      double rotvec[] = new double[5];

      int i,iter,j,jobu,k,kase,kk,l,ll,lls,lm1,lp1,ls,lu,
          m,maxit,mm,mp1,nct,ncu,nrt;

      boolean wantu,wantv;

//   Java requires that all local variables be initialized before they
//   are used.  This is the reason for the initialization
//   line below:

      ls = 0;

//   set the maximum number of iterations

      maxit = 30;

//   determine what is to be computed

      wantu = false;
      wantv = false;

      jobu = (job%100)/10;

      ncu = n;

      if (jobu > 1) ncu = Math.min(n,p);
      if (jobu != 0) wantu = true;
      if ((job%10) != 0) wantv = true;

//   reduce x to bidiagonal form, storing the diagonal elements
//   in s and the super-diagonal elements in e

      nct = Math.min(n-1,p);
      nrt = Math.max(0,Math.min(p-2,n));
      lu = Math.max(nct,nrt);

//      if (lu >= 1) {       This test is not necessary under Java.
//                           The loop will be skipped if lu < 1 = the
//                           starting value of l.

         for (l = 1; l <= lu; l++) {

            lp1 = l + 1;

            if (l <= nct) {

//   compute the transformation for the l-th column and
//   place the l-th diagonal in s[l]

               s[l] = Blas_f77.colnrm2_f77(n-l+1,x,l,l);

               if (s[l] != 0.0) {

                  if (x[l][l] != 0.0) s[l] = Blas_f77.sign_f77(s[l],x[l][l]);
                  Blas_f77.colscal_f77(n-l+1,1.0/s[l],x,l,l);
                  x[l][l]++;

               }

               s[l] = -s[l];

            }

//            if (p >= lp1) {   This test is not necessary under Java.
//                              The loop will be skipped if p < lp1.

               for (j = lp1; j <= p; j++) {

                  if ((l <= nct) && (s[l] != 0.0)) {

//   apply the transformation

                     t = -Blas_f77.coldot_f77(n-l+1,x,l,l,j)/x[l][l];
                     Blas_f77.colaxpy_f77(n-l+1,t,x,l,l,j);

                  }

//   place the l-th row of x into e for the
//   subsequent calculation of the row transformation

                  e[j] = x[l][j];

               }

//            }   This test is not necessary under Java.  See above.

            if (wantu && (l <= nct)) {

//   place the transformation in u for subsequent 
//   back multiplication

               for (i = l; i <= n; i++) {

                  u[i][l] = x[i][l];

               }

            }

            if (l <= nrt) {

//   compute the l-th row transformation and place the
//   l-th super-diagonal in e[l]

               e[l] = Blas_f77.dnrm2p_f77(p-l,e,lp1);

               if (e[l] != 0.0) {

                  if (e[lp1] != 0.0) e[l] = Blas_f77.sign_f77(e[l],e[lp1]);
                  Blas_f77.dscalp_f77(p-l,1.0/e[l],e,lp1);
                  e[lp1]++;

               }

               e[l] = -e[l];

               if ((lp1 <= n) && (e[l] != 0.0)) {

//   apply the transformation

                  for (i = lp1; i <= n; i++) {

                     work[i] = 0.0;

                  }

                  for (j = lp1; j <= p; j++) {

                     Blas_f77.colvaxpy_f77(n-l,e[j],x,work,lp1,j);

                  }

                  for (j = lp1; j <= p; j++) {

                     Blas_f77.colvraxpy_f77(n-l,-e[j]/e[lp1],work,x,lp1,j);

                  }

               }

               if (wantv) {

//   place the transformation in v for subsequent
//   back multiplication

                  for (i = lp1; i <= p; i++) {

                     v[i][l] = e[i];

                  }

               }

            }

         }

//      }         This test (see above) is not necessary under Java

//   set up the final bidiagonal matrix of order m

      m = Math.min(p,n+1);

//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  This is the cause of the following changes:

//      nctp1 = nct + 1;
//      nrtp1 = nrt + 1;

      if (nct < p) s[nct+1] = x[nct+1][nct+1];
      if (n < m) s[m] = 0.0;
      if (nrt+1 < m) e[nrt+1] = x[nrt+1][m];

      e[m] = 0.0;

//   if required, generate u

      if (wantu) {

//         if (ncu >= nct+1) {        This test is not necessary under Java.
//                                    The loop will be skipped if ncu < nct+1.

            for (j = nct+1; j <= ncu; j++) {

               for (i = 1; i <= n; i++) {

                  u[i][j] = 0.0;

               }

               u[j][j] = 1.0;

            }

//         }           This test is not necessary under Java.  See above.

//         if (nct >= 1) {            This test is not necessary under Java.
//                                    The loop will be skipped if nct < 1.

            for (ll = 1; ll <= nct; ll++) {

               l = nct - ll + 1;

               if (s[l] != 0.0) {

                  lp1 = l + 1;

//                  if (ncu >= lp1) {   Not necessary under Java.
//                                      Loop will be skipped if ncu < lp1.

                     for (j = lp1; j <= ncu; j++) {

                        t = -Blas_f77.coldot_f77(n-l+1,u,l,l,j)/u[l][l];
                        Blas_f77.colaxpy_f77(n-l+1,t,u,l,l,j);

                     }

//                  }   Not necessary under Java.  See above.

                  Blas_f77.colscal_f77(n-l+1,-1.0,u,l,l);
                  u[l][l]++;
                  lm1 = l - 1;

//                  if (lm1 >= 1) {   Not necessary under Java.
//                                    Loop will be skipped if lm1 < 1.

                     for (i = 1; i <= lm1; i++) {

                        u[i][l] = 0.0;

                     }

//                  }   Not necessary under Java.  See above.

               } else {

                  for (i = 1; i <= n; i++) {

                     u[i][l] = 0.0;

                  }

                  u[l][l] = 1.0;

               }

            }

//         }      This test is not necessary under Java.  See above.

      }

//   if it is required, generate v

      if (wantv) {

         for (ll = 1; ll <= p; ll++) {

            l = p - ll + 1;
            lp1 = l + 1;

            if ((l <= nrt) && (e[l] != 0.0)) {

               for (j = lp1; j <= p; j++) {

                  t = -Blas_f77.coldot_f77(p-l,v,lp1,l,j)/v[lp1][l];
                  Blas_f77.colaxpy_f77(p-l,t,v,lp1,l,j);

               }

            }

            for (i = 1; i <= p; i++) {

               v[i][l] = 0.0;

            }

            v[l][l] = 1.0;

         }

      }

//   main iteration loop for the singular values

      mm = m;
      iter = 0;

      while (true) {

//   quit if all of the singular values have been found

         if (m == 0) return;

//   if too many iterations have been performed, 
//   set flag and return     

         if (iter >= maxit) {

            throw new SVDCException(m);

         }

/*

   This section of the program inspects for
   negligible elements in the s and e arrays.
   On completion the variables kase and l are 
   set as follows:

      kase = 1     If s[m] and e[l-1] are negligible and l < m
      kase = 2     If s[l] is negligible and l < m
      kase = 3     If e[l-1] is negligible, l < m, and
                   s[l], ..., s[m] are not negligible (QR step)
      kase = 4     If e[m-1] is negligible (convergence)

*/


         for (ll = 1; ll <= m; ll++) {

            l = m - ll;

            if (l == 0) break;

            test = Math.abs(s[l]) + Math.abs(s[l+1]);
            ztest = test + Math.abs(e[l]);

            if (ztest == test) {

               e[l] = 0.0;
               break;

            }

         }

         
         if (l == m - 1) {

            kase = 4;

         } else {

            lp1 = l + 1;
            mp1 = m + 1;

            for (lls = lp1; lls <= mp1; lls++) {

               ls = m - lls + lp1;
               if (ls == l) break;

               test = 0.0;
               if (ls != m)  test += Math.abs(e[ls]);
               if (ls != l+1) test += Math.abs(e[ls-1]);

               ztest = test + Math.abs(s[ls]);

               if (ztest == test) {

                  s[ls] = 0.0;
                  break;

               }

            }

            if (ls == l) {

               kase = 3;

            } else if (ls == m) {

               kase = 1;

            } else {

               kase = 2;
               l = ls;

            }

         }

         l++;

//   perform the task indicated by kase

         switch (kase) {

            case 1:

//   deflate negligible s[m]

//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  This is the cause of the following changes:

//               mm1 = m - 1;
               f = e[m-1];
               e[m-1] = 0.0;

               for (kk = l; kk <= m-1; kk++) {

                  k = (m-1) - kk + l;
                  t1 = s[k];

//   Although objects are passed by reference, primitive types
//   (e.g., doubles, ints, ...) are passed by value.  Thus
//   rotvec is needed.

                  rotvec[1] = t1;
                  rotvec[2] = f;
                  Blas_f77.drotg_f77(rotvec);
                  t1 = rotvec[1];
                  f  = rotvec[2];
                  cs = rotvec[3];
                  sn = rotvec[4];

                  s[k] = t1;

                  if (k != l) {

                     f = -sn*e[k-1];
                     e[k-1] *= cs;

                  }

                  if (wantv) Blas_f77.colrot_f77(p,v,k,m,cs,sn);

               }

               break;

            case 2:

//   split at negligible s[l]               

               f = e[l-1];
               e[l-1] = 0.0;

               for (k = l; k <= m; k++) {

                  t1 = s[k];

//   Although objects are passed by reference, primitive types
//   (e.g., doubles, ints, ...) are passed by value.  Thus
//   rotvec is needed.

                  rotvec[1] = t1;
                  rotvec[2] = f;
                  Blas_f77.drotg_f77(rotvec);
                  t1 = rotvec[1];
                  f  = rotvec[2];
                  cs = rotvec[3];
                  sn = rotvec[4];

                  s[k] = t1;

                  f = -sn*e[k];

                  e[k] *= cs;

                  if (wantu) Blas_f77.colrot_f77(n,u,k,l-1,cs,sn);

               }

               break;

            case 3:

//   perform one QR step

//   calculate the shift

//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  Otherwise the following would have
//   been shorter:

               scale = Math.max(Math.abs(s[m]),Math.abs(s[m-1]));
               scale = Math.max(Math.abs(e[m-1]),scale);
               scale = Math.max(Math.abs(s[l]),scale);
               scale = Math.max(Math.abs(e[l]),scale);


//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  This is the cause of the following changes:

//               sm = s[m]/scale;

//               smm1 = s[m-1]/scale;
//               emm1 = e[m-1]/scale;
//               sl = s[l]/scale;
//               el = e[l]/scale;

//               b = ((smm1 + sm)*(smm1 - sm) + emm1*emm1)/2.0;
//               c = (sm*emm1)*(sm*emm1);


               b = ((s[m-1] + s[m])*(s[m-1] - s[m]) + 
                     e[m-1]*e[m-1])/(2.0*scale*scale);
               c = (s[m]*e[m-1])*(s[m]*e[m-1])/(scale*scale*scale*scale);

               shift = 0.0;

               if ((b != 0.0) || (c != 0.0)) {

                  shift = Math.sqrt(b*b + c);
                  if (b < 0.0) shift = -shift;
                  shift = c/(b + shift);

               }

//               f = (sl + sm)*(sl - sm) - shift;
//               g = sl*el;

               f = (s[l] + s[m])*(s[l] - s[m])/(scale*scale) - shift;
               g = s[l]*e[l]/(scale*scale);

//   chase zeros


//   There is a bug in the 1.0.2 Java compiler that
//   forces me to skimp on the number of local
//   variables.  This is the cause of the following changes:

//               mm1 = m - 1;

//               for (k = l; k <= mm1; k++) {

               for (k = l; k <= m - 1; k++) {

//   Although objects are passed by reference, primitive types
//   (e.g., doubles, ints, ...) are passed by value.  Thus
//   rotvec is needed.

                  rotvec[1] = f;
                  rotvec[2] = g;
                  Blas_f77.drotg_f77(rotvec);
                  f = rotvec[1];
                  g  = rotvec[2];
                  cs = rotvec[3];
                  sn = rotvec[4];

                  if (k != l) e[k-1] = f;
                  f = cs*s[k] + sn*e[k];
                  e[k] = cs*e[k] - sn*s[k];
                  g = sn*s[k+1];
                  s[k+1] *= cs;
                  
                  if (wantv) Blas_f77.colrot_f77(p,v,k,k+1,cs,sn);

//   Although objects are passed by reference, primitive types
//   (e.g., doubles, ints, ...) are passed by value.  Thus
//   rotvec is needed.

                  rotvec[1] = f;
                  rotvec[2] = g;
                  Blas_f77.drotg_f77(rotvec);
                  f = rotvec[1];
                  g  = rotvec[2];
                  cs = rotvec[3];
                  sn = rotvec[4];

                  s[k] = f;
                  f = cs*e[k] + sn*s[k+1];
                  s[k+1] = -sn*e[k] + cs*s[k+1];
                  g = sn*e[k+1];
                  e[k+1] *= cs;

                  if (wantu && (k < n)) Blas_f77.colrot_f77(n,u,k,k+1,cs,sn);

               }

               e[m-1] = f;
               iter++;

               break;

            case 4:

//   convergence

//   make the singular value positive

               if (s[l] < 0.0) {

                  s[l] = -s[l];
                  if (wantv) Blas_f77.colscal_f77(p,-1.0,v,1,l);

               }

//   order the singular value

               while (l != mm) {

                  if (s[l] >= s[l+1]) break;

                  t = s[l];
                  s[l] = s[l+1];
                  s[l+1] = t;

                  if (wantv && (l < p)) Blas_f77.colswap_f77(p,v,l,l+1);

                  if (wantu && (l < n)) Blas_f77.colswap_f77(n,u,l,l+1);

                  l++;

               }

               iter = 0;
               m--;

               break;

         }

      }

   }
                                    
}
