/*
* Copyright (C) 2016 Apliki Solutions Nyman & Yli-Opas
*/
package function;

import java.awt.event.*;
import java.awt.*;
import java.util.ArrayList;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.border.*;
import vilgraph.FunctionOptions;
import vilgraph.Graph;
import vilgraph.Window;

public class FunctionField extends JPanel{
    
    public static final int NONE = 0, DERIVE = 1, INTEGRATE = 2;
    
    private static final Color[] colors = {Color.red, Color.green, Color.blue, Color.orange, Color.magenta};
    private static int colorIndex = 0;
    private static boolean initialized = false;
    private static final ButtonGroup activeButtonGroup;
    private static ScriptEngine engine;
    private static Graph graph;
    
    private final JTextField field, infoText;
    private final JRadioButton activeButton;
    private Color color;
    private int diff;
    private double[] values;
    private boolean updateDiff = false, updateValues = true;
    private String lastString = "var y = ";
    
    static{
        activeButtonGroup = new ButtonGroup();
    }
    
    public static void setGraph(Graph g){
        graph = g;
    }
    
    public FunctionField(String s){
        this.color = colors[colorIndex];
        colorIndex++;
        if(colorIndex >= colors.length)
            colorIndex = 0;
        
        diff = NONE;
        
        setBorder(null);
        
        JPanel panel = new JPanel();
        panel.setBorder(new LineBorder(Color.gray));
        panel.setBackground(Color.white);
        
        //active category chooser
        activeButton = new JRadioButton();
        activeButtonGroup.add(activeButton);
        activeButton.setFocusable(false);
        activeButton.setOpaque(false);
        activeButton.setBorder(null);
        activeButton.addActionListener((ActionEvent e) -> {
            if(activeButton.isSelected())
                FunctionOptions.setActive(this);
        });
        activeButton.setSelected(true);
        FunctionOptions.setActive(this);
        panel.add(activeButton);
        
        infoText = new JTextField(" y =", 3);
        infoText.setHorizontalAlignment(JTextField.RIGHT);
        infoText.setEditable(false);
        infoText.setBorder(null);
        infoText.setFocusable(false);
        infoText.setOpaque(false);
//        panel.add(infoText);
        
        //the field for the function
        field = new JTextField(s, 30);
        field.setBorder(null);
        field.addKeyListener(new KeyAdapter(){
            @Override
            public void keyTyped(KeyEvent e){
                updateValues = true;
                Window.repaint();
            }
        });
        field.addFocusListener(new FocusListener(){
        @Override
            public void focusGained(FocusEvent e){
                activeButton.setSelected(true);
                FunctionOptions.setActive(FunctionField.this);
            }
            @Override
            public void focusLost(FocusEvent e){}
        });
        
        panel.add(field);
        
        
        JButton colorChooser = new JButton("");
        colorChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
        colorChooser.setBackground(color);
        colorChooser.addActionListener((ActionEvent e) -> {
            Color c = JColorChooser.showDialog(null, "Choose color", color);
            
            if(c != null){
                color = c;
            
                colorChooser.setBackground(c);
                Window.repaint();
            }
        });
        panel.add(colorChooser);
        
        add(panel);
    }//FunctionField
    
    
    
    
    public void draw(Graphics g, boolean update){
        
        if(updateDiff){
            String[] s = {" y =", " y = D", " y = ∫"};
            infoText.setText(s[diff]);
        }
        
        if(updateValues || update || updateDiff || values.length != graph.getWidth()){
            updateValues = false;
            updateDiff = false;
            updateValues(0, graph.getWidth());
        }
        
        g.setColor(color);
        
        
        int lasty = (int)Math.round(graph.yToPanel(values[0]));
        for(int x = 1; x < values.length; x++){
            
            int y = (int)Math.round(graph.yToPanel(values[x]));
            
            //don't draw if outside of panel
            if(graph.contains(x - 2, lasty) || graph.contains(x - 1, y))
                if(Double.isFinite(y) && Double.isFinite(lasty))
                    g.drawLine(x - 1, lasty, x, y);
            
            lasty = y;
        }
        
        
    }//draw
        
    private void updateValues(int from, int to){
        
        if(from == 0 && to == graph.getWidth())
            values = new double[graph.getWidth()];
        
        if(values.length != graph.getWidth()){
            values = new double[graph.getWidth()];
            from = 0;
            to = graph.getWidth();
        }
        
        if(diff != INTEGRATE){

            for(int x = from; x < to; x++){
                
                if(diff == NONE)
                    values[x] =  getValue(graph.xToGraph(x));
                else//diff == DERIVE
                    values[x] =  (getValue(graph.xToGraph(x)) - getValue(graph.xToGraph(x-1))) / graph.scaleX;
            }
            
        }else{//diff == INTEGRATE
            
            double y = 0;//graph's coordinate system
            double increment;
            
            int i;
            
            //only calculate new values
            if(from == 0)
                i = (int)graph.xToPanel(0);
            else{
                y = values[from - 1] + getValue(graph.xToGraph(from-1)) * graph.scaleX / 2;
                i = from;
            }
            
//            System.out.println(i + " " + to + " " + (to - i));
            
            //positive direction
            for(; i < to; i++){
                
                increment = getValue(graph.xToGraph(i)) * graph.scaleX;
                
                if(!Double.isFinite(increment))
                    increment = 0;
                
                y += increment;
                
                if(i >= 0)
                    values[i] = y - increment / 2;
            }
            
            y = getValue(0) * graph.scaleY / 2;
            
            
            //only calculate new values
            if(to == graph.getWidth())
                i = (int)graph.xToPanel(0);
            else{
                y = values[to + 1] + getValue(graph.xToGraph(to+1)) * graph.scaleX / 2;
                i = to;
            }
            
//            System.out.println(from + " " + i + " " + (i - from) + "\n");
            
            //negative direction
            for(; i >= from; i--){
                
                increment = getValue(graph.xToGraph(i)) * graph.scaleX;
                
                if(!Double.isFinite(increment))
                    increment = 0;
                
                y -= increment;
                
                if(i < graph.getWidth())
                    values[i] = y + increment / 2;
            }
            
        }
        
        lastString = field.getText().trim();
    }
    
    public void moveGraph(int dx, int dy){
        if(dx > 0){
            
            for(int i = values.length - dx - 1; i >= 0; i--)
                values[i + dx] = values[i];
            
            updateValues(0, dx);
        
        }else if(dx < 0){
            
            for(int i = -dx; i < values.length; i++)
                values[i + dx] = values[i];
            
            updateValues(values.length+dx, values.length);
        }
        
    }
    
    public double getValue(double x){
        try{//var y = 0; for(var i = 0; i < 10; i++){y += Math.sin(i * x);} y /=x; if(x <= 0) y = 0;
            engine.put("x", x);
            engine.put("y", 0);
            engine.eval(field.getText());
            Number n = (Number)engine.getBindings(ScriptContext.ENGINE_SCOPE).get("y");
            return n.doubleValue();
        }catch(Exception e){
            return Double.NaN;
        }
    }//getValue
    
    public double getSavedValue(int x){
        return values[x];
    }//getSavedValue
    
    public String solve(){
        
        //check the sign of first value, to find when changed
        int sign = (int)Math.signum(values[0]);
        
        ArrayList<Double> answers = new ArrayList<>();
        boolean hasMore = false;
            
        for(int x = 1; x < values.length; x++){
            if(sign * values[x] < 0){
                
                if(answers.size() == 10){
                    hasMore = true;
                    break;
                }
                
                double d = graph.xToGraph(x - Math.abs(values[x] / (values[x-1] + values[x])));
                
                answers.add(d);
                
                sign *= -1;
            }
        }
        
        if(answers.isEmpty())
            return "NaN";
        if(answers.size() == 1)
            return Double.toString(answers.get(0));
        
        //has many answers
        String s = "";
        for(int i = 0; i < answers.size(); i++)
            s += "x" + (i+1) + " ≈ " + answers.get(i) + "\n";
        
        if(hasMore)
            s += "...";
        else
            s = s.substring(0, s.length()-2);
        
        return s;
    }//solve
    
    public Color getColor(){
        return color;
    }//getColor
    
    public void setDiff(int i){
        diff = i;
        updateDiff = true;
    }//setDiff
    public int getDiff(){
        return diff;
    }//getDiff
    
    public void setActive(){
        activeButton.setSelected(true);
    }//setActive
    
    public static boolean isInitialized(){
        return initialized;
    }//isInitialized
    
    public static void init(){
        ScriptEngineManager manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
        
        initialized = true;
        
        if(Window.graphOpen())
            Window.repaint();
    }//init
}
