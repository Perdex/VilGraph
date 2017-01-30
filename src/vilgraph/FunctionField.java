/*
* Copyright (C) 2016 Apliki Solutions Nyman & Yli-Opas
*/
package vilgraph;

import java.awt.event.*;
import java.awt.*;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.*;
import javax.swing.border.*;

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
    private boolean updateDiff = false;
    
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
    
    
    
    
    public void draw(Graphics g){
        
        if(updateDiff){
            String[] s = {" y =", " y = D", " y = ∫"};
            infoText.setText(s[diff]);
        }
            
        
        values = new double[graph.getWidth()];
        
        g.setColor(color);
        
        
        if(diff != INTEGRATE){

            for(int x = 0; x < graph.getWidth(); x++){
                
                if(diff == NONE)
                    values[x] =  getValue(graph.xToGraph(x));
                else//diff == DERIVE
                    values[x] =  (getValue(graph.xToGraph(x)) - getValue(graph.xToGraph(x-1))) / (-graph.scaleY);
            }
            
        }else{//diff == INTEGRATE
            
            double y = -getValue(0) * (-graph.scaleY) / 2;//graph's coordinate system
            double increment;
            
            //positive direction
            for(int x = (int)graph.xToPanel(0); x < graph.getWidth(); x++){
                increment = getValue(graph.xToGraph(x)) * (-graph.scaleY);
                if(!Double.isFinite(increment))
                    increment = 0;
                
                y += increment;
                
                if(x >= 0)
                    values[x] = y - increment / 2;
            }
            
            y = getValue(0) * (-graph.scaleY) / 2;
            //negative direction
            for(int x = (int)graph.xToPanel(0); x >= 0; x--){
                increment = getValue(graph.xToGraph(x)) * (-graph.scaleY);
                if(!Double.isFinite(increment))
                    increment = 0;
                
                y -= increment;
                
                if(x < graph.getWidth())
                    values[x] = y + increment / 2;
            }
            
        }
        
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
        
        int sign = 1;
        
        if(values[0] < 0)
            sign = -1;
            
        for(int x = 1; x < values.length; x++){
            if(sign * values[x] < 0){
                double d = graph.xToGraph(x - Math.abs(values[x]  / (values[x-1] + values[x])));
                
                double m = Math.pow(10, (int)-Math.log10(graph.scaleX) + 2);
                
                return Double.toString(((int)d * m) / m);
            }
        }
        
        return "NaN";
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
