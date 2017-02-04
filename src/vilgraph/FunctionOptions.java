/*
* Copyright (C) 2016 Apliki Solutions Nyman & Yli-Opas
*/

package vilgraph;

import function.FunctionField;
import java.awt.event.ActionEvent;
import javax.swing.*;

public class FunctionOptions extends JPanel{
    
    private static FunctionField active;
    private static JComboBox diffChooser;
    
    public FunctionOptions(){
        
        JButton solveButton = new JButton("solve f(x) ≈ 0");
        solveButton.addActionListener((ActionEvent e) -> {
            String ans = active.solve();
            if(ans.equals("NaN"))
                JOptionPane.showMessageDialog(null, "no answer found on screen");
            JOptionPane.showMessageDialog(null, active.solve());
        });
        add(solveButton);
        
        String[] diffStrings = {"f(x)", "Df(x)", "∫f(x)"};
        diffChooser = new JComboBox(diffStrings);
        diffChooser.setSelectedIndex(0);
        diffChooser.addActionListener((ActionEvent e) -> {
            if(active == null)
                return;
            active.setDiff(diffChooser.getSelectedIndex());
            Window.repaint();
        });
        add(diffChooser);
        
        
        JButton removeButton = new JButton("remove");
        //removeButton.setBorder(new LineBorder(Color.gray));
        removeButton.setFocusable(false);
        removeButton.addActionListener((ActionEvent e) -> {
            if(active == null)
                return;
            Window.remove(active);
        });
        add(removeButton);
    }//FunctionOptions
    
    public static void setActive(FunctionField f){
        active = f;
        
        diffChooser.setSelectedIndex(active.getDiff());
    }//setActive
    
    public static FunctionField getActive(){
        return active;
    }//getActive
}
