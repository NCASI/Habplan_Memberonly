
package linear_algebra;

import java.util.*;
import java.lang.*;
import java.io.*;
import linear_algebra.*;
import corejava.Console;

/**
*
*This class tests the (LINPACK) QR classes.
*
*It
*<ol>
*<li> Randomly fills an n by n matrix, X.
*<li> Randomly generates a vector b0.
*<li> Calculates the vector y = X(b0).
*<li> Performs a QR decomposition of X.
*<li> Solves the system Xb = y in an effort to recover b0. 
*<li> Handles the regression equation y = a0 + a1*x + a2*x^2 + e
*     where a0 = a1 = a2 = 1.  Its estimates of the parameters
*     should be close to 1.  Its estimate of the standard deviation
*     should be close to the input value.
*</ol>
*
*@author Steve Verrill
*@version .5 --- March 6, 1997
*
*/


public class QRTest_f77 extends Object {


   public static void main (String args[]) {


/*

Some of the variables of main:


*/ 

      long randstart;

      int n;

      int i,j,k;

      int info = 0;

      double randlow,randhigh,randdiff;

      double x[][] = new double[101][101];

      double y[] = new double[101];
      double b0[] = new double[101];
      double b[] = new double[101];

      double qraux[] = new double[101];
      double work[] = new double[101];
      double qy[] = new double[101];
      double qty[] = new double[101];
      double rsd[] = new double[101];
      double xb[] = new double[101];

      double sd,rss,diff;
      
      int jpvt[] = new int[101];
      int job;

      PrintStream pryx;


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

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= n; j++) {

            x[i][j] = randlow + rand01.nextDouble()*randdiff;

         }

      }


/*
      System.out.print("\n\n");
      System.out.print("*********************************************\n");
      System.out.print("\nThe X matrix is \n\n");

      for (i = 1; i <= n; i++) {

         for (j = 1; j <= n; j++) {

            System.out.print(x[i][j] + "  ");

         }

         System.out.print("\n");

      }
*/



/*

   Generate a vector b

*/

      for (i = 1; i <= n; i++) {

         b0[i] = randlow + rand01.nextDouble()*randdiff;

      }


/*

   Calculate Xb = y

*/

      for (i = 1; i <= n; i++) {

         y[i] = 0.0;

         for (j = 1; j <= n; j++) {

            y[i] += x[i][j]*b0[j];

         }

      }


      job = 0;

      QR_f77.dqrdc_f77(x,n,n,qraux,jpvt,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

      job = 100;
      info = QR_f77.dqrsl_f77(x,n,n,qraux,y,qy,qty,b,rsd,xb,job);

      System.out.print("\nThe info value returned by QR_f77.dqrsl_f77 was: " +
      info + "\n\n");


      System.out.print("\nThe input b vector was:\n\n");

      for (i = 1; i <= n; i++) {

         System.out.print(b0[i] + "\n");

      }

      System.out.print("\n\nThe recovered b vector was:\n\n");

      for (i = 1; i <= n; i++) {

         System.out.print(b[i] + "\n");

      }

      System.out.print("\n");
      System.out.print("\n");
      System.out.print("*********************************************\n");
      System.out.print("\n");


/*

   Generate a new X matrix and the y vector.

   Also, generate a data file that can be used with a different
   regression program to check whether the correct
   regression results are being obtained.

*/

//      try {

//         FileOutputStream fout = new FileOutputStream("yx.data");
//         pryx = new PrintStream(fout);

         for (i = 1; i <= n; i++) {

            x[i][1] = 1.0;
            x[i][2] = -1.0 + rand01.nextDouble()*2.0;
            x[i][3] = x[i][2]*x[i][2];

            y[i] = x[i][1] + x[i][2] + x[i][3] + sd*normi(rand01.nextDouble());

//            pryx.print(y[i] + "  " + x[i][1] + "  ");
//            pryx.print(x[i][2] + "  " + x[i][3] + "\n");

         }

//      }

//      catch (Exception e) {

//         System.out.println("\nError:  Problem with the yx.data file\n");
//         System.exit(0);

//      }

      job = 0;
      QR_f77.dqrdc_f77(x,n,3,qraux,jpvt,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

      job = 111;
      info = QR_f77.dqrsl_f77(x,n,3,qraux,y,qy,qty,b,rsd,xb,job);

      System.out.print("\nThe info value returned by QR_f77.dqrsl_f77 was: " +
      info + "\n\n");

      System.out.print("\nThe input b vector was: \n\n1\n1\n1\n\n");

      System.out.print("\n\nThe recovered b vector was:\n\n");

      for (i = 1; i <= 3; i++) {

         System.out.print(b[i] + "\n");

      }

      rss = 0.0;

      for (i = 1; i <= n; i++) {

         rss += rsd[i]*rsd[i];

      }

      sd = Math.sqrt(rss/(n - 1.0));

      System.out.print("\nThe estimate of sigma from the rsd vector");
      System.out.print(" was " + sd + "\n");

      rss = 0.0;

      for (i = 1; i <= n; i++) {

         diff = y[i] - xb[i];
         rss += diff*diff;

      }

      sd = Math.sqrt(rss/(n - 1.0));

      System.out.print("\nThe estimate of sigma from the xb vector");
      System.out.print(" was " + sd + "\n");


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



