import javax.swing.plaf.DimensionUIResource;
import javax.swing.plaf.FileChooserUI;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.border.CompoundBorder;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import java.io.*;
import java.util.*;

public class ResourceManager_view extends JFrame {
    
    ResourceManager_controller control;

    JPanel base_panel;
    JPanel display;
    JPanel user_controls;
    JPanel IO;
    JPanel stats;
    JPanel tables;

    GridLayout simulation_display;
    
    JTable wait_table = new JTable();
    JTable hold_table = new JTable();

    JTextArea input_file_text = new JTextArea(100,100){{
        //LineB
        //setBorder(new CompoundBorder(new LineBorder(Color.darkGray), new EmptyBorder(4,4,4,4)));
        //setBorder(new CompoundBorder(new LineBorder(Color.DARK_GRAY), new EmptyBorder(4, 4, 4, 4)));
        //setBorder(BorderFactory.createLineBorder(Color.black));
        //setLocation(15+326*2, 455+140);
    }};   

    JButton btn_open_file = new JButton("Open File");
    JButton btn_step = new JButton("Step");
    JButton btn_reset = new JButton("Reset");

    JLabel lb_results = new JLabel("Simulation: ");
    JLabel lb_hold = new JLabel("Hold Table"); 
    JLabel lb_wait = new JLabel("Wait Table");

    final JFileChooser fileChooser = new JFileChooser();

    ArrayList<String> cmds;

    int resource_count = 0;
    int process_count = 0;

    int p_width;

    public ResourceManager_view(ResourceManager_controller controller){
        this.control = controller;

        init_gui();
        init_handlers();
    }

    // Init Frame & Position Components 
    private void init_gui(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1000,750);
        this.setLayout(null);

        set_base_panel();
        this.add(base_panel);

        //this.add(new resource());
    }

    private void set_base_panel(){
        this.base_panel = new JPanel();

        base_panel.setLayout(null);
        base_panel.setLocation(0,0);
        base_panel.setSize(this.getSize());

        //set_resource_display(0, 0);
        set_resource_display(2, 3);
        set_tables(resource_count, process_count);
        set_stats(resource_count, process_count);
        set_IO();
        set_user_controls();
    }

    private void set_user_controls(){
        user_controls = new JPanel();
        user_controls.setLayout(new FlowLayout());

        user_controls.add(btn_open_file);
        user_controls.add(btn_step);
        user_controls.add(btn_reset);

        IO.add(user_controls);
    }

    private void set_IO(){
        IO = new JPanel();

        IO.setLayout(new GridBagLayout());
        IO.setSize(326, 255);
        IO.setLocation(15+326*2, 460);
        IO.add(new JScrollPane(input_file_text){{
            setBounds(0,0,100,100);
        }});
       // IO.setBackground(Color.pink);

        //set_user_controls();

        base_panel.add(IO);
    }


    private void set_stats(int r_count, int p_count){
        try{
            base_panel.remove(stats);
            stats = new JPanel();

            

            base_panel.add(stats);
        }catch(Exception e){
            // Null pointer exception when stats is nothing
            // This happens when set_stats is called for the first time
            stats = new JPanel();
            set_stats(r_count,p_count);
        }
    }

    // remove the odd component and update it with a new one
    private void set_tables(int r_count, int p_count){
        try{
            base_panel.remove(tables);
            tables = new JPanel();

            base_panel.add(tables);
        }catch(Exception e){
            // Null pointer exception when tables is nothing
            // This happens when set_tables is called for the first time
            tables = new JPanel();
            set_tables(r_count,p_count);
        }
    }

    private void set_resource_display(int r_count, int p_count){
        try{
            base_panel.remove(display);
        display = new JPanel();
        display.setLayout(null);

        display.setSize(this.getSize().width - 10 , this.getSize().height - 300);
        display.setBorder(BorderFactory.createLineBorder(Color.black));;
        display.setLocation(5, 5);
        display.setBackground(Color.WHITE);
        
        p_width = 10;

        place_processes(display, p_count);
        place_resources(display, r_count);

        place_arrow(0, 1, true);

        base_panel.add(display);
        }catch(Exception e){
            display = new JPanel();
            set_resource_display(r_count, p_count);
        }
    }

    private void place_resources(JPanel display, int r_count){
        for(int i =0; i<r_count; i++){
            System.out.println("rec " + i);
            display.add(new resource(resource_count){{
                setBounds(5 + (resource_count * 105), 175, 100, 100);
                setBackground(Color.YELLOW);
                resource_count++;
            }});
        }
    }

    private void place_arrow(int process, int resource, Boolean isEnd){
        if(process % 2 == 0){
            display.add(new arrow((55*(process+1) + 50 * process), 100+15, (55*(resource+1) + 50 * resource), 175-10, isEnd){{
                setBackground(new Color(0,0,0,0));
                setBorder(BorderFactory.createLineBorder(Color.black));
                setBounds(0, 0, 990, 450);
            }});
        }else{
            display.add(new arrow((55*(process+1) + 50 * process), 350+15, (55*(resource+1) + 50 * resource), 315-10, isEnd){{
                setBackground(new Color(0,0,0,0));
                setBorder(BorderFactory.createLineBorder(Color.black));
                setBounds(0, 0, 990, 450);
            }});
        }
    }

    private void place_processes(JPanel display, int p_count){
        for(int i = 0; i<p_count; i++){
            display.add(new proccess(process_count){{
                if(process_count % 2 == 0){
                    setBounds(5 + (process_count * 105), 5, 100, 100);
                    setBackground(Color.GREEN);
                }else{
                    setBounds(5 + ((process_count) * 105), 345, 100, 100);
                    setBackground(Color.GREEN);
                }
                process_count++;
            }});
        }
    }

    private void readFile(File file){
        try{
            int count = 0;
            String line;
            cmds = new ArrayList<String>();
            ArrayList<Integer> available_resources = new ArrayList<Integer>();

            BufferedReader reader = new BufferedReader(new FileReader(file));

            while(true){
                line = reader.readLine();
                if(line.equals(null)){
                    break;
                }else if(line.charAt(0) != '#' && line.charAt(0) != ';' && line.charAt(0) != ' ' ){
                    StringTokenizer tokens = new StringTokenizer(line);
                    if(count == 0){
                        control.set_process(Integer.parseInt(tokens.nextToken()));
                    }else if(count == 1){
                        int r = Integer.parseInt(tokens.nextToken());
                        for(int i = 0; i<r; i++){
                            available_resources.add(Integer.parseInt(tokens.nextToken()));
                        }
                        control.set_resource(r,available_resources);
                    }else{
                        cmds.add(line);
                        //System.out.println(line);
                    }
                    count++;
                }
            }
        }catch(Exception e){
            System.out.println("Error with file " + e.getMessage());
        }
        for (String cmd : cmds) {
            System.out.println("CMD: " + cmd);
        }
    }

    private void init_handlers(){
        // Open File
        btn_open_file.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                fileChooser.showOpenDialog(display);
                readFile(fileChooser.getSelectedFile());
            }
        });

        // Step through file if file is loaded
        btn_step.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

            }
        });

        // Reset to first step if file is loaded
        btn_reset.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){

            }
        });
    }


    private class resource extends JPanel{
        int my_val;
        public resource(int r_val){
            my_val = r_val;
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawString("R_" + String.valueOf(my_val), (25-(4+String.valueOf(my_val).length())), 25 );
        }
    }
    private class proccess extends JPanel{
        int my_val;
        public proccess(int p_val){
            my_val = p_val;
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            g.drawString("P_" + String.valueOf(my_val), (25-(4+String.valueOf(my_val).length())), 25 );   
        }
    }

    // Arrow class for fold & wait arrows. Takes the resource and the process index to determine location
    private class arrow extends JPanel{
        int sX, sY, eX, eY;
        Boolean isEnd;
        
        public arrow(int startX,int startY,int endX,int endY,Boolean isEnd){
            sX = startX;
            sY = startY;
            eX = endX;
            eY = endY;
            this.isEnd = isEnd;
        }
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            if(isEnd){
                g.setColor(Color.red);
                g.drawLine(sX, sY-10, eX, eY);
                g.fillPolygon(new int[] {eX, eX+10, eX+5}, new int[] {eY, eY, eY+10}, 3);
            }else{
                g.setColor(Color.black);
                g.drawLine(sX, sY, eX, eY+10);
                g.fillPolygon(new int[] {sX, sX-10, sX-5}, new int[] {sY, sY, sY-10}, 3);
            }
        }
    }
}