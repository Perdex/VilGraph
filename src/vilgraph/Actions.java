package vilgraph;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

public class Actions extends MouseAdapter implements KeyListener{

    private final Graph g;
    private int lastX = 0, lastY = 0;

    public Actions(Graph g){
        this.g = g;
    }

    @Override
    public void mouseDragged(MouseEvent e){
        
        g.mouseX = e.getX();
        g.mouseY = e.getY();
        
        if(e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK){

            int dx = e.getX() - lastX;
            int dy = e.getY() - lastY;
            
            g.offsetX += dx;
            g.offsetY += dy;
            
            g.moveFunctions(dx, dy);
            g.repaint();

        }else if(e.getModifiersEx() == MouseEvent.BUTTON3_DOWN_MASK){

            double dx = (1 + (e.getX() - lastX) * g.scaleX / g.xToGraph(e.getX()));
            double dy = (1 - (e.getY() - lastY) * g.scaleY / g.yToGraph(e.getY()));

            g.scaleX /= clamp(dx, 1d/1.2, 1.2);
            g.scaleY /= clamp(dy, 1d/1.2, 1.2);
            
            g.repaintAndUpdate();
        }

        lastX = e.getX();
        lastY = e.getY();
        

    }//mouseDragged
    
    private double clamp(double value, double min, double max){
        return Math.max(Math.min(value, max), min);
    }

    @Override
    public void mouseMoved(MouseEvent e){
        g.mouseX = e.getX();
        g.mouseY = e.getY();
        g.repaint();
    }//mouseMoved

    @Override
    public void mousePressed(MouseEvent e){
        lastX = e.getX();
        lastY = e.getY();
    }//mousePressed

    @Override
    public void mouseWheelMoved(MouseWheelEvent e){
        double k = Math.pow(1.2, e.getUnitsToScroll() / 3);

        //zoom in respect to middle point of mouse and origin
        g.offsetX += (1 - 1 / k) * (e.getX() - g.offsetX);
        g.offsetY += (1 - 1 / k) * (e.getY() - g.offsetY);

        g.scaleX *= k;
        g.scaleY *= k;

        g.repaintAndUpdate();
    }//mouseWheelMoved

    @Override
    public void keyPressed(KeyEvent e){
        
        if(e.getKeyCode() == KeyEvent.VK_R)
            g.resetView();
    }

    @Override
    public void keyReleased(KeyEvent e){}

    @Override
    public void keyTyped(KeyEvent e){}
}
