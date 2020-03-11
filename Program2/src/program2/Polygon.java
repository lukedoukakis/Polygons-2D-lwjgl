/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package program2;

import java.util.ArrayList;

/**
 *
 * @author Luke
 */
public class Polygon {
    
    ArrayList rawVertices;
    
    ArrayList translations;
    ArrayList scalings;
    ArrayList rotations;
    
    ArrayList all_edges;
    ArrayList global_edges;
    ArrayList active_edges;
    
    
    boolean hasTranslation;
    boolean hasScaling;
    boolean hasRotation;

    float color_R, color_G, color_B;
    
    
    public Polygon(){
        
        rawVertices = new ArrayList<float[]>();
        
        translations = new ArrayList<float[]>();
        scalings = new ArrayList<float[]>();
        rotations = new ArrayList<float[]>();
        
        all_edges = new ArrayList<float[]>();
        global_edges = new ArrayList<float[]>();
        active_edges = new ArrayList<float[]>();
    }
        
    
    
   
}
