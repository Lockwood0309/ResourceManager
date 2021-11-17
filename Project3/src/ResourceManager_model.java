import java.io.File;
import java.lang.reflect.Array;
import java.security.KeyStore.TrustedCertificateEntry;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.swing.JFrame;

public class ResourceManager_model{

    private Dictionary<Integer,ArrayList<Integer>> wait = new Hashtable<Integer,ArrayList<Integer>>();
    private Dictionary<Integer,ArrayList<Integer>> hold = new Hashtable<Integer,ArrayList<Integer>>();

    private Dictionary<Integer,ArrayList<Integer>> detect_wait = new Hashtable<Integer,ArrayList<Integer>>();
    private Dictionary<Integer,ArrayList<Integer>> detect_hold = new Hashtable<Integer,ArrayList<Integer>>();


    // Integer for resource & Queue of Integers for process waiting line
    private Dictionary<Integer,Queue<Integer>> waiting_list = new Hashtable<Integer,Queue<Integer>>();

    int process_count;
    int resource_count;

    ArrayList<Integer> max_available;

    ArrayList<Boolean> finish;
    ArrayList<Integer> open;


    public ResourceManager_model(){
        
    }

    public void display_test_stats(){
        System.out.println("#P: " + process_count);
        System.out.println("#R: " + resource_count);
        for(int i = 0; i<resource_count ;i++){
            System.out.println("MAX #R"+i+": " + max_available.get(i) + " ");
        }
        System.out.println("OPEN____________________________________");
        for(int i = 0; i<resource_count ;i++){
            System.out.println("OPEN #R"+i+": " + open.get(i) + " ");
        }
        System.out.println("END STAT PRINT");
    }

    public void display_tables(){
        System.out.println("HOLD____________________________________");
        for(int i = 0; i<process_count; i++){
            for(int j = 0; j<resource_count; j++){
                System.out.println("P[" + i + "][" + j + "]: " + hold.get(i).get(j));
            }
        }
        System.out.println("WAIT____________________________________");
        for(int i = 0; i<process_count; i++){
            for(int j = 0; j<resource_count; j++){
                System.out.println("P[" + i + "][" + j + "]: " + wait.get(i).get(j));
            }
        }
        System.out.println("END TABLE PRINT");
    }

    public void set_processes_count(int p){
        process_count = p;
    }

    public void set_resource_count(int r, ArrayList<Integer> max_r){
        resource_count = r;
        max_available = new ArrayList<Integer>(max_r);
        open = new ArrayList<Integer>(max_r);

        for(int i = 0; i<resource_count; i++){
            waiting_list.put(i, new LinkedList<Integer>());
        }
        init_tables();
    }

    public void request(int p, int r, int n){
        if(open.get(r) >= n){                                           // Check if the requested r amount is less than or equal to the open r amount
            hold.get(p).set(r,hold.get(p).get(r) + n);                  // Give the process the requested n resources
            open.set(r,open.get(r)-n);                                  // set open r list with updated amount
        }else if(open.get(r) > 0){                                      // check if some resources can be allocated
            hold.get(p).set(r,hold.get(p).get(r) + open.get(r));        // allocate all the availble resources
            wait.get(p).set(r,wait.get(p).get(r) + n - open.get(r));    // update wait list with the amount still needed
            open.set(r,0);                                              // set the open list to empty
            waiting_list.get(r).add(p);                                 // place p into a waiting queue for r
        }else{                                                          // No resources available
            wait.get(p).set(r,wait.get(p).get(r) + n);                  // update wait table for all requested resources
            waiting_list.get(r).add(p);                                 // add p to a waiting queue for r
        }
    }

    public void release(int p, int r, int n){
        hold.get(p).set(r,hold.get(p).get(r)-n); // Release the resources from the process hold table
        open.set(r,open.get(r)+n);               // Return resources to the open vector
        check_to_allocate();                     // Check if other processes can be allocated resources
    }

    private void check_to_allocate(){
        for(int i = 0; i<resource_count; i++){
            while(true){
                if(open.get(i) == 0){
                    break;
                }else if(!waiting_list.get(i).isEmpty() && open.get(i) < wait.get(waiting_list.get(i).peek()).get(i)){                                        // Check if the amount of resource is less that what the processes is asking for
                    wait.get(waiting_list.get(i).peek()).set(i, wait.get(waiting_list.get(i).peek()).get(i) - open.get(i)); // Allocate what resource are available 
                    open.set(i,0);
                }else{  
                    if(!waiting_list.get(i).isEmpty()){                                                                                                    // Allocate the requested resources and remove the waiting process from the waitng_list
                        open.set(i,open.get(i) + wait.get(waiting_list.get(i).peek()).get(i));
                        wait.get(waiting_list.get(i).remove()).set(i, 0);
                    }else{
                        break;
                    }
                }
            }
        }
    }

    // initialize wait and hold tables to 0
    private void init_tables(){
        for(int i =0; i<process_count; i++){
            wait.put(i,new ArrayList<Integer>(){{
              for(int j=0;j<resource_count;j++){
                  add(0);
              }  
            }});
            hold.put(i,new ArrayList<Integer>(){{
                for(int j=0;j<resource_count;j++){
                    add(0);
                }  
            }});
        }
    }

    public boolean is_deadlock(){
        detect_hold = hold;
        detect_wait = wait;
        finish = new ArrayList<Boolean>(){{
            for(int i = 0; i<process_count; i++){
                add(false);
            }
        }};
        while(true){                    // While able to step throught step function 
            if(!step()){                // step returns true if a finish was set true and false if no process finished
                System.out.println("deadlock couldn't make a step");
                break;
            }
            System.out.println("deadlock made a step");
        }
        return check_deadlock();        // check if system is in deadlock 
    }

  
    // This function shouldn't actually release it's resource but instead should have another 
    // List to manipulate.
    private boolean step(){
        for(int i = 0; i<process_count; i++){                      // Iterate over each process
            if(!finish.get(i)){                                    // if a process isn't finished
                Boolean can_release = true;                 
                for(int j=0; j<resource_count; j++){               // Iterate over each resource
                    if(detect_wait.get(i).get(j) > open.get(j)){   // If a processes has requested more resources than available 
                        can_release = false;                       // it can't release any of it's resources
                    }
                }
                if(can_release){                                   // If a processes is able to release its resources
                    for(int x = 0; x<resource_count; x++){         // Iterate over each resource
                        release(i, x, detect_hold.get(i).get(x));  // release all the resources this process was holding
                        finish.set(i, true);                       // set finish = true becasue this processes can finish
                    }
                    return true;                                   // return true if resources were released
                }
            }
        }
        return false;                                              // return false if resources couldn't be released 
    }

    private boolean check_deadlock(){
        int count = 0;
        for (Boolean p_val : finish) {   
            System.out.println("p" + count++ + ": " + p_val);                   // Iterate over each process
            if(!p_val){                                     // If the process hasn't finished the system is in deadlock
                return true;
            }
        }
        return false;                                        // return true if all processes finished.
    }
}
