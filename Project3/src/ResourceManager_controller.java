import java.util.*;
import java.util.function.Function;

public class ResourceManager_controller {
    
    private ResourceManager_model model;
    private ResourceManager_view view;

    public void set_resource(int r, ArrayList<Integer> max_r){
        model.set_resource_count(r, max_r);
    }

    public void set_process(int p){
        model.set_processes_count(p);
    }

    private void test_model(){
        Scanner scan = new Scanner(System.in);
        int p = Integer.parseInt(scan.nextLine());
        StringTokenizer tokens = new StringTokenizer(scan.nextLine());
        int r = Integer.parseInt(tokens.nextToken());
        ArrayList<Integer> max_r = new ArrayList<Integer>();
        for(int i=0; i<r; i++){
            max_r.add(Integer.parseInt(scan.nextLine()));
        }
        model.set_processes_count(p);
        model.set_resource_count(r, max_r);
        while(true){
            tokens = new StringTokenizer(scan.nextLine());
            String input = tokens.nextToken();

            if(input.contains("quit")){
                break;
            }else if(input.contains("release")){
                p = Integer.parseInt(tokens.nextToken());
                r = Integer.parseInt(tokens.nextToken());
                int n = Integer.parseInt(tokens.nextToken());
                model.release(p, r, n);
            }else if(input.contains("request")){
                p = Integer.parseInt(tokens.nextToken());
                r = Integer.parseInt(tokens.nextToken());
                int n = Integer.parseInt(tokens.nextToken());
                model.request(p, r, n);
            }
            model.display_test_stats();
            model.display_tables();
            System.out.println("\nIs Deadlock: " + model.is_deadlock());
        }
    }

    // Initalize components of MVC model
    private void init_conroller(){
        model = new ResourceManager_model();
        //view = new ResourceManager_view(this);
        test_model();
        //view.setVisible(true);
    }

    // Start Resource Manager
    public static void main(String args[]){
        ResourceManager_controller control = new ResourceManager_controller();
        control.init_conroller();
        
    }
}
