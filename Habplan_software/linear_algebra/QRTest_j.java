
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
*@version .5 --- June 6, 1997
*
*/


public class QRTest_j extends Object {


   public static void main (String args[]) {


/*

Some of the variables of main:


*/ 

      long randstart;

      int n;

      int i,j,k;

      int info = 0;

      double randlow,randhigh,randdiff;

      double x[][] = new double[100][100];

      double y[] = new double[100];
      double b0[] = new double[100];
      double b[] = new double[100];

      double qraux[] = new double[100];
      double work[] = new double[100];
      double qy[] = new double[100];
      double qty[] = new double[100];
      double rsd[] = new double[100];
      double xb[] = new double[100];

      double sd,rss,diff;
      
      int jpvt[] = new int[100];
      int jpvts[] = new int[100];
      int origord[] = new int[100];

      int jobjpvt,job;

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

      jobjpvt = Console.readInt("\nWhat is jobjpvt? (0 for no pivoting)");

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


      job = jobjpvt;

      QR_j.dqrdc_j(x,n,n,qraux,jpvt,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

      job = 100;
      info = QR_j.dqrsl_j(x,n,n,qraux,y,qy,qty,b,rsd,xb,job);

      System.out.print("\nThe info value returned by QR_j.dqrsl_j was: " +
      info + "\n\n");


      System.out.print("\nThe input b vector was:\n\n");

      for (i = 0; i < n; i++) {

         System.out.print(b0[i] + "\n");

      }

      System.out.print("\n\nThe recovered b vector was:\n\n");

      if (jobjpvt != 0) {

         isort(jpvt,jpvts,origord,n);

         for (i = 0; i < n; i++) {

            System.out.print(b[origord[i]] + "\n");

         }

      }

      else {

         for (i = 0; i < n; i++) {

            System.out.print(b[i] + "\n");

         }

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

         for (i = 0; i < n; i++) {

            x[i][0] = 1.0;
            x[i][1] = -1.0 + rand01.nextDouble()*2.0;
            x[i][2] = x[i][1]*x[i][1];

            y[i] = x[i][0] + x[i][1] + x[i][2] + sd*normi(rand01.nextDouble());

//            pryx.print(y[i] + "  " + x[i][1] + "  ");
//            pryx.print(x[i][2] + "  " + x[i][3] + "\n");

         }

//      }

//      catch (Exception e) {

//         System.out.println("\nError:  Problem with the yx.data file\n");
//         System.exit(0);

//      }

      job = jobjpvt;
      QR_j.dqrdc_j(x,n,3,qraux,jpvt,work,job);

/*   

   This should obtain the solution to Xb = y.

*/

      job = 111;
      info = QR_j.dqrsl_j(x,n,3,qraux,y,qy,qty,b,rsd,xb,job);

      System.out.print("\nThe info value returned by QR_j.dqrsl_j was: " +
      info + "\n\n");

      System.out.print("\nThe input b vector was: \n\n1\n1\n1\n\n");

      System.out.print("\n\nThe recovered b vector was:\n\n");

      
      if (jobjpvt != 0) {

         isort(jpvt,jpvts,origord,3);

         for (i = 0; i < 3; i++) {

            System.out.print(b[origord[i]] + "\n");

         }

      }

      else {

         for (i = 0; i < 3; i++) {

            System.out.print(b[i] + "\n");

         }

      }

      rss = 0.0;

      for (i = 0; i < n; i++) {

         rss += rsd[i]*rsd[i];

      }

      sd = Math.sqrt(rss/(n - 1.0));

      System.out.print("\nThe estimate of sigma from the rsd vector");
      System.out.print(" was " + sd + "\n");

      rss = 0.0;

      for (i = 0; i < n; i++) {

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


/*
   This routine attempts to perform an n*log(n) sort
   rather than an n*n sort.
   It sorts doubles.
   It also keeps track of the original order of the x's.
  

   Steve Verrill, 5/2/89
   Translated to java from fortran on 6/7/96

   There are other, faster sorts that are available in java.
   See, for example, 

   http://www.cs.ubc.ca/spider/harrison/Java/sorting-demo.html

*/


   public static void isort(int x[], int xsort[], int origord[], int n) {

      int num = 1;
      int i,j,il,iu,numb,numbd2,itest,k,km1,ilp1;

      for (i = 0; i < n; i++) {

         origord[i] = i;

      }

      if (x[0] < x[1]) {

         xsort[0] = x[0];
         origord[0] = 0;
         xsort[1] = x[1];
         origord[1] = 1;

      }

      else {

         xsort[0] = x[1];
         origord[0] = 1;
         xsort[1] = x[0];
         origord[1] = 0;

      }
      
      for (j = 2; j < n; j++) {

//   insert the next value

         if (x[j] >= xsort[num]) {

            num++;
            xsort[num] = x[j];
            origord[num] = j;

//  The continue statement is described on page 90 of TYJ

            continue;

         }

         il = -1;

         if (x[j] > xsort[0]) {

            il = 0;
            iu = num;

            numb = iu - il;

            while (numb > 1) {

               numbd2 = numb/2;

               itest = il + numbd2;

               if (x[j] > xsort[itest]) il = itest;
               else iu = itest;

               numb = iu - il;

            }

         }

         num++;

         for (k = num; k > il + 1; k--) {

            km1 = k - 1;
            xsort[k] = xsort[km1];
            origord[k] = origord[km1];

         }

         ilp1 = il + 1;
         xsort[ilp1] = x[j];
         origord[ilp1] = j;

      } 

   }

}




