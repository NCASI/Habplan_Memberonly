import java.awt.*;
import javax.swing.*;
import java.util.Random;
/**
*Metropolis Algorithm Class
*/
public class Metropolis extends Algorithm{
 
    double w=2.0;  //Dynamic weight. Jasa 2001 No 254 p562 
    final double maxW=Double.MAX_VALUE; //keep w from reaching infinity
    double theta=1.0;  //see dynamic weighting paper
    double a=1.1;     //for Q Type dynamic weighting
    boolean isDynamic=false;  //use dynamic weighting 
    FrameMenu parent;
    Random random;
    
public Metropolis(FrameMenu parent){
    super(parent);   
    this.parent=parent; 
    random=new Random();
}/*end constructor*/ 

    /**
     *turn Dynamic weighting on or off.  See JASA 2001 96(454):561-573
     */ 
    public void setDynamic(boolean isDynamic){
	this.isDynamic=isDynamic;
        w=2.0; //reset weight
    }/*end method*/

/**
*The Metropolis algorithm
*/
public void run(){

 double Edelta; /*holds the change in the objective function*/
 float pstar=0;  /*replacement probability in Metropolis algorithm*/
int xtoz_cnt=0; /*count how many polys change class in this iteration*/ 
 
//set up a random initial schedule
//parent.console.println("nPolys, nOptions polySched "+ObjForm.nPolys+" "+ObjForm.nOptions+" "+ObjForm.polySched.length);

  ObjForm.validScheds=new int[ObjForm.nOptions+1];

 if (!parent.isImport){  //only do this if no imported starting schedule
   for (int i=1; i<=ObjForm.nPolys; i++){
       ObjForm.polySched[i]=0; //make sure there is no initial schedule
    ObjForm.polySched[i]=randomSchedule(i);
    //parent.console.println("poly schedule "+i+" "+ObjForm.polySched[i]);
    //for (int k=1;k<=ObjForm.nScheds;k++)parent.console.print(ObjForm.validScheds[k]+" ");
    //parent.console.println(" "); 
   }
  }/*end if*/


 
//run the initialization routine for each added component
for (int j=1;j<=parent.nComponents;j++)
    if(parent.isAdded[j])parent.form[j].init();
    
 for (iter=1;iter<=UserInput.iterate;iter++) {
	     //suspend only after at least 1 iteration so graphs are made
     while (parent.runSuspend && iter>1){parent.outLabel.setText("Suspended before Iteration " + iter);
     isWaiting=true;
     }  ; //stay here and churn if runSuspend=true 
     isWaiting=false;  
     if (parent.runStop){parent.outLabel.setText("Stopped before Iteration " + iter);parent.start.setEnabled(true);
     isWaiting=true; return;}    //user wants to stop;
 
     xtoz_cnt=0;  //keep a count of the n of polygons that change schedule
  int unitI=0; //unitI is the management unit index
  int unitRandom; // a random unit number;
  boolean isRandom=(iter%10==0);  //visit polys in random order when true

  int[] members=null,members2=null;
 while(unitI<ObjForm.unitIndex.length-1){
     unitI++;
     unitRandom=unitI;

     //pick units randomly every nth iteration
     if(isRandom){
	 unitRandom=random.nextInt(ObjForm.unitIndex.length-1)+1;
     }

     members=ObjForm.unitIndex[unitRandom];
      int mem1=members[0];//unit's first member;

     if(members.length==1){
	 if(mem1==0)continue; //member polygon has no data 
	 zProp=randomSchedule(mem1);
     }
     else{
	 zProp=groupRandomSchedule(members); //get a new proposal schedule thats valid for all members in the group
	 if(zProp==-1)continue; //no valid schedule for this unit
     }


   if (zProp!=ObjForm.polySched[mem1] && !parent.isImport){ //only enter if a change is possible
   
    Edelta=0.0;
    zInit=zProp; //used to test if a component wants to change zProp
    
    //compute the change to the objective function from switching poly i to option z
   for (int j=1;j<=parent.nComponents;j++)
    if(parent.isAdded[j])Edelta+=parent.form[j].deltaObj(members,zProp);
    
    if (zInit!=zProp){//a component decided to change zProp (probably block component), redo Edelta
      Edelta=0.0;
     for (int j=1;j<=parent.nComponents;j++)
      if(parent.isAdded[j])Edelta+=parent.form[j].deltaObj(members,zProp);
    }/*end if zInit*/ 
    
   Edelta=Math.exp(Edelta);
   
   if(!isDynamic) pstar=(float) Math.min(1.0,Edelta);
   else {//use dynamic weighting
       double ETemp=w*Edelta/theta;
       pstar=(float) Math.min(1.0,ETemp);//dynamic weight adjustment
   }/*end if else*/

   if (random.nextFloat()<pstar){  /*then use z instead of current option*/

       int i=0;
       mem1=0;
       for(int mem=0;mem<members.length;mem++){
	   i=members[mem];
	   if(i==0)continue; //no data for this member
	   mem1=i;//a member with data
	   ObjForm.polySched[i]=zProp;
       }/*end for mem*/ 

       //mem1 used to check if subFlow contains this unit
	   for (int j=1;j<=parent.nComponents;j++)
	       if(parent.isAdded[j] && mem1>=parent.form[j].polyFirst && mem1<=parent.form[j].polyLast)parent.form[j].duringIter(); //make needed status changes 
	   xtoz_cnt++;
 

      if(isDynamic & Edelta>.00001){ //proposal accepted
	   w=Math.max(theta,w*Edelta);
	   w=(w>maxW)?maxW:w;
      } /*fi isDynamic &*/ 
 
   }else if(isDynamic &Edelta>.00001){ //proposal not accepted
	    w=Math.min(maxW,a*w);
    }/*end else if*/
   }/*end if z!=polySched && !isImport*/ 

   //  if((isDynamic))System.out.println("iter,theta  w pstar= "+iter+" "+theta+" "+w+" "+pstar);
     

  }/*end while unitI*/
 
 if(parent.form[1].isWt2big) 
      rescaleWeights();//keep weights from exceeding machine limits

 //do whatever before the next iteration
 postIter(); //see parent class
 parent.outLabel.setText("Iteration " + iter + ", " + xtoz_cnt + " polygons changed");


 }/*end for iter*/
 
 parent.start.setEnabled(true); //put control buttons in correct enabled mode
 parent.suspend.setEnabled(false);
 parent.stop.setEnabled(false);
 parent.runRunning=false;  //let start know that met isn't running
 parent.runStop=true;
 isWaiting=true;
 return;
}/*end  run()*/
 
/**
*rescale the weights when then get too big
*/    
 public void rescaleWeights(){
     
  parent.form[1].isWt2big=false;
 double maxWt=0.0;
 for (int j=1;j<=parent.nComponents;j++){
      double[] wt={0.0,0.0};
    if(parent.isAdded[j]){
     wt=parent.form[j].getWeight();
     maxWt=Math.max(wt[0],maxWt); //based on the dominant wt per form
    }
 }

 if(maxWt>=Float.MAX_VALUE){
     parent.console.println("Possible infeasibility: Try reducing goals.");
     for (int j=1;j<=parent.nComponents;j++)
     if(parent.isAdded[j])parent.form[j].scaleWeight(maxWt/1000.);
 }
  
 }/*end rescaleWeights()*/ 
    
}/*end Metropolis class*/
