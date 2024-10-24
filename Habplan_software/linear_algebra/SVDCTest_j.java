
package linear_algebra;

import linear_algebra.*;
import corejava.Console;

import java.util.*;
import java.lang.*;
import java.io.*;

/**
*
*This class tests the (LINPACK) SVDC method.
*
*It
*<ol>
*<li> Randomly fills an n by n matrix, X.
*<li> Randomly generates a vector b0.
*<li> Calculates the vector y = X(b0).
*<li> Performs a UDV&#180 decomposition of X.
*<li> Solves the system Xb = y in an effort to recover b0. 
*<li> Handles the regression equation y = a0 + a1*x + a2*x^2 + e
*     where a0 = a1 = a2 = 1.  Its estimates of the parameters
*     should be close to 1.  Its estimate of the standard deviation
*     should be close to the input value.
*</ol>
*
*@author Steve Verrill
*@version .5 --- June 9, 1997
*
*/


public class SVDCTest_j extends Object {


   public static void main (String args[]) {


/*

Some of the variables of main:


*/ 

      long randstart;

      int n,p;

      int i,j,k;

      double randlow,randhigh,randdiff;

      double x[][] = new double[100][100];
      double copyx[][] = new double[100][100];

      double y[] = new double[100];
      double b0[] = new double[100];
      double b[] = new double[100];

      double s[] = new double[100];
      double e[] = new double[100];
      double u[][] = new double[100][100];
      double ut[][] = new double[100][100];
      double v[][] = new double[100][100];
      double work[] = new double[100];

      double pred[] = new double[100];

      double uty[] = new double[100];

      double sd,rss,diff;
      
      int job;


/*

   Console is a public domain class described in Cornell
   and Horstmann's Core Java (SunSoft Press, Prentice-Hall).

*/

      n = Console.readInt("\nWhat is the n value? (100 or less) ");
   
      randstart = Console.readInt("\nWhat is the starting value (an " +
      "integer)\n" +
      "for the random number generator? ");

      Random rand01 = new Random(randstart);

      randlow = Console.readDouble("\nWhat is randlow? ");

      randhigh = Console.readDouble("\nWhat is randhigh? ");

      randdiff = randhigh - randlow;

      sd = Console.readDouble("\nWhat is the standard deviation? ");


/*

   Generate an n by n X

*/


      for (i = 0; i < n; i++) {

         for (j = 0; j < n; j++) {

            x[i][j] = randlow + rand01.nextDouble()*randdiff;

         }

      }



/*
      System.out.print("\n\n");
      System.out.print("*********************************************\n");
      System.out.print("\nThe X matrix is \n\n");

      for (i = 0; i < n; i++) {

         for (j = 0; j < n; j++) {

            System.out.print(x[i][j] + "  ");

         }

         System.out.print("\n");

      }
*/



/*

   Generate a vector b

*/


      for (i = 0; i < n; i++) {

         b0[i] = randlow + rand01.nextDouble()*randdiff;

      }


/*

   Calculate Xb = y

*/

      for (i = 0; i < n; i++) {

         y[i] = 0.0;

         for (j = 0; j < n; j++) {

            y[i] += x[i][j]*b0[j];

         }

      }


      job = 11;

      try {

         SVDC_j.dsvdc_j(x,n,n,s,e,u,v,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

         Blas_j.mattran_j(u,ut,n,n);

         Blas_j.matvec_j(ut,y,uty,n,n);

         for (i = 0; i < n; i++) {

            uty[i] = uty[i]/s[i];

         }

         Blas_j.matvec_j(v,uty,b,n,n);


         System.out.print("\nThe input b vector was:\n\n");

         for (i = 0; i < n; i++) {

            System.out.print(b0[i] + "\n");

         }

         System.out.print("\n\nThe recovered b vector was:\n\n");

         for (i = 0; i < n; i++) {

            System.out.print(b[i] + "\n");

         }

      } catch (SVDCException svdce) {

         System.out.print("\nThere was an SVDC_j exception on the" +
         " first test.\n\n" +
         "The info value from SVDC_j was " + svdce.info +
         ".\n\n");  

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");


/*

   Generate a new X matrix and the y vector

*/

      for (i = 0; i < n; i++) {

         x[i][0] = 1.0;
         copyx[i][0] = x[i][0];
         x[i][1] = -1.0 + rand01.nextDouble()*2.0;
         copyx[i][1] = x[i][1];
         x[i][2] = x[i][1]*x[i][1];
         copyx[i][2] = x[i][2];

         y[i] = x[i][0] + x[i][1] + x[i][2] + sd*normi(rand01.nextDouble());

      }

      job = 21;

      try {

         SVDC_j.dsvdc_j(x,n,3,s,e,u,v,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

         Blas_j.mattran_j(u,ut,n,3);

         Blas_j.matvec_j(ut,y,uty,3,n);

         for (i = 0; i < 3; i++) {

            uty[i] = uty[i]/s[i];

         }

         b[0] = 0.0;
         b[1] = 0.0;
         b[2] = 0.0;
         Blas_j.matvec_j(v,uty,b,3,3);

         System.out.print("\nThe input b vector was: \n\n1\n1\n1\n\n");

         System.out.print("\n\nThe recovered b vector was:\n\n");

         for (i = 0; i < 3; i++) {

            System.out.print(b[i] + "\n");

         }

         Blas_j.matvec_j(copyx,b,pred,n,3);

         rss = 0.0;

         for (i = 0; i < n; i++) {

            diff = y[i] - pred[i];
            rss += diff*diff;

         }

         sd = Math.sqrt(rss/(n - 1.0));

         System.out.print("\nThe estimate of sigma from the residuals");
         System.out.print(" was " + sd + "\n");

      } catch (SVDCException svdce) {

         System.out.print("\nThere was an SVDC_j exception on the" +
         " second test.\n\n" +
         "The info value from SVDC_j was " + svdce.info +
         ".\n\n");  

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");


   }



/**
*
*<p>
*This is a normal cdf inverse routine.
*
*Created by Steve Verrill, March 1997.
*
*@param  u   The value (between 0 and 1) to invert.
*
*/

   public static double normi (double u) {

//   This is a temporary and potentially ugly way to generate
//   normal random numbers from uniform random numbers

      double c[] = new double[4];
      double d[] = new double[4];

      double normi,arg,t,t2,t3,xnum,xden;

      c[1] = 2.515517;
      c[2] = .802853;
      c[3] = .010328;

      d[1] = 1.432788;
      d[2] = .189269;
      d[3] = .001308;

      if (u <= .5) {

         arg = -2.0*Math.log(u);
         t = Math.sqrt(arg);
         t2 = t*t;
         t3 = t2*t;

         xnum = c[1] + c[2]*t + c[3]*t2;
         xden = 1.0 + d[1]*t + d[2]*t2 + d[3]*t3;

         normi = -(t - xnum/xden);

         return normi;

      } else {

         arg = -2.0*Math.log(1.0 - u);
  
         t = Math.sqrt(arg);
         t2 = t*t;
         t3 = t2*t;

         xnum = c[1] + c[2]*t + c[3]*t2;
         xden = 1.0 + d[1]*t + d[2]*t2 + d[3]*t3;

         normi = t - xnum/xden;

         return normi;

      }

   }

}



