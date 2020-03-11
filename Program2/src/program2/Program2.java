/***************************************************************
* file: Program1.java
* author: Luke Doukakis
* class: CS 4450 – Computer Graphics
*
* assignment: program 2
* date last modified: 2/9/2020
*
* NOTE: takes coordinates.txt from src folder ("src/coordinates.txt")
*
****************************************************************/ 

package program2;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import org.lwjgl.input.Keyboard;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

public class Program2 {
    
    int debugLoopCount;
    
    public static void main(String[] args) {
       
        Program2 main = new Program2();
        main.start();
    }
    
    public void start(){
        
        try {
            
            createWindow();
            initGL();
            
            debugLoopCount = 0;
            
            // render until user presses ESC
            while(!Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
                render();
            }
            
            Display.destroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void createWindow() throws Exception {
        Display.setFullscreen(false);
        Display.setDisplayMode(new DisplayMode(640, 480));
        Display.setTitle("Program 1");
        Display.create();
    }
    
    void initGL(){
        glClearColor(.0f, .0f, .0f, .0f);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        
        // origin centered on window
        glOrtho(-320, 320, -240, 240, 1, -1);
        
        glMatrixMode(GL_MODELVIEW);
        glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
    }
    
    void render(){
        
        try{
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glPointSize(2);
            
            // create list of Polygons to draw from coordinates.txt
            File coords = new File("src/coordinates.txt");
            ArrayList<Polygon> polygons = initPolygons(coords);
            
            // for each polygon: draw outline, fill, and transform
            for(int i = 0; i < polygons.size(); i++){
                 
                Polygon p = polygons.get(i);
                
                glPushMatrix();
                
                // transform polygon
                applyTransformations(p);
                
                // draw polygon outline
                glColor3f(p.color_R, p.color_G, p.color_B);
                glBegin(GL_LINE_LOOP);  
                for(int j = 0; j < p.rawVertices.size(); j++){
                    float[] vertex = (float[])p.rawVertices.get(j);
                    glVertex2f(vertex[0], vertex[1]);
                }
                glEnd();
                   
                // fill polygon
                glBegin(GL_POINTS);
                fillPolygon(p);
                glEnd();
                
                glPopMatrix();    
            }
        
            Display.update();
            Display.sync(60);
        } catch(Exception e){
            e.printStackTrace();
        }
        
        debugLoopCount++;
    }
    
    
    void fillPolygon(Polygon p){
        
        // initialize parity to false, scanline to global_edges first y-min value
        float parity = 0;
        float y_scanLine = ((float[])p.global_edges.get(0))[0];
        
        // do while active_edges is not empty
        do{
            
            if(debugLoopCount < 1){
                System.out.println("----------- scan-line y: " + y_scanLine + " -----------\n");
            }
            
            // move all edges where y-min = y_scanline from global_edges to active_edges
            for(int i = 0; i < p.global_edges.size(); i++){
                float[] cell = new float[3];
                if(((float[])p.global_edges.get(i))[0] == y_scanLine){
                    cell[0] = ((float[])p.global_edges.get(i))[1];
                    cell[1] = ((float[])p.global_edges.get(i))[2];
                    cell[2] = ((float[])p.global_edges.get(i))[3];
                    p.active_edges.add(cell);
                    p.global_edges.remove(i);
                    i--;
                }else{
                    break;
                }
            }
            
            // print active_edges to console (debug)
            if(debugLoopCount < 1){
                System.out.println("active_edges:\n");
                for(int i = 0; i < p.active_edges.size(); i++){
                    System.out.println("EDGE " + i + ": "); 
                    System.out.println("y-max: " +((float[])p.active_edges.get(i))[0]);
                    System.out.println("x: " + ((float[])p.active_edges.get(i))[1]);
                    System.out.println("1/m: " + ((float[])p.active_edges.get(i))[2]);
                    System.out.println();
                }
            }
    
            // increment parity every time scanline hits an edge
            parity = 0;
            for(int x = -320; x <= 320; x++){    
                for(int i = 0; i < p.active_edges.size(); i++){
                    if(x == (int)((float[])p.active_edges.get(i))[1]){
                        parity++;
                    }
                }
                // draw point if parity is odd
                if(parity % 2 != 0){
                    glVertex2f(x, y_scanLine);
                } 
            }
            
            // increment scan-line
            y_scanLine++;
                
            // remove edges in active_edges where y-max <= scanline
            for(int i = 0; i < p.active_edges.size(); i++){
                if(((float[])p.active_edges.get(i))[0] <= y_scanLine){
                    p.active_edges.remove(i);
                }
            }
                
            // update x-values in active_edges to x = x + 1/m
            for(int i = 0; i < p.active_edges.size(); i++){
                float[] cell = (float[])p.active_edges.get(i);
                cell[1] += cell[2];
                p.active_edges.set(i, cell);
            }
            
        } while(!p.active_edges.isEmpty());
    }
    
    // transform polygon in order of rotate, scale, translate
    void applyTransformations(Polygon p){
        
        // translate
        if(p.hasTranslation){
            for(int i = 0; i < p.translations.size(); i++){
            
                float[] translation = (float[])p.translations.get(i);
                float factorX = translation[0];
                float factorY = translation[1];
                
                glTranslatef(factorX, factorY, 0);
            }
        }

        // scale
        if(p.hasScaling){
            for(int i = 0; i < p.scalings.size(); i++){
            
                float[] scaling = (float[])p.scalings.get(i);
                float factorX = scaling[0];
                float factorY = scaling[1];
                
                glScalef(factorX, factorY, 0);
            }
        }
            
        // rotate
        if(p.hasRotation){
            for(int i = 0; i < p.rotations.size(); i++){
            
                float[] rotation = (float[])p.rotations.get(i);
                float angle = rotation[0];
                float pivotX = rotation[1];
                float pivotY = rotation[2];
            
                glRotatef(angle, pivotX, pivotY, 1);
            }
        }
    }
    
    
    // return ArrayList of Polygon objects with attributes specified in coordinates.txt
    // Polygon's all_edges and global_edges initialized here
    ArrayList initPolygons(File file){

        Scanner s;
        Scanner vertexScanner;
        
        ArrayList<Polygon> polygons = new ArrayList<>();
        try{
            
            // initialize vars
            s = new Scanner(file);
            int scans = 0;
            String curLine;
            String[] curLine_components;
            Polygon curPolygon = null;
            String firstVertexLine = "";
            
            
            // initialize all_edges table based on coordinates.txt
            while(s.hasNextLine()){
                
                curLine = s.nextLine();
                curLine_components = curLine.split(" ");
                scans++;
                
                // when reaching a new polygon line, add last Polygon to list and set up a new one
                if(curLine_components[0].equals("P")){
                    
                    vertexScanner = new Scanner(file);
                    for(int i = 0; i < scans; i++){
                        vertexScanner.nextLine();
                    }
                    firstVertexLine = vertexScanner.nextLine();
                    
                    // new Polygon
                    if(curPolygon != null){
                        polygons.add(curPolygon);
                    }
                    curPolygon = new Polygon();
                    
                    // set colors
                    curPolygon.color_R = Float.parseFloat(curLine_components[1]);
                    curPolygon.color_G = Float.parseFloat(curLine_components[2]);
                    curPolygon.color_B = Float.parseFloat(curLine_components[3]);      
                }
                else{
                    
                    // when reaching "translation" line
                    if(curLine_components[0].equals("t")){
                        curPolygon.hasTranslation = true;
                        float[] translation = new float[2];
                        if(curLine_components[1].startsWith("-")){
                            translation[0] = Float.parseFloat(curLine_components[1].substring(1)) * -1;
                            translation[1] = Float.parseFloat(curLine_components[2].substring(1)) * -1;      
                        }else{
                            translation[0] =  Float.parseFloat(curLine_components[1]);    // factor x
                            translation[1] = Float.parseFloat(curLine_components[2]);
                    }
                        curPolygon.translations.add(translation);             
                    }
                    else{
                        
                        // when reaching "scale" line
                        if(curLine_components[0].equals("s")){
                            curPolygon.hasScaling = true;
                            float[] scaling = new float[4]; 
                            if(curLine_components[1].startsWith("-")){
                                scaling[0] = Float.parseFloat(curLine_components[1].substring(1)) * -1;
                                scaling[1] = Float.parseFloat(curLine_components[2].substring(1)) * -1;
                                scaling[2] = Float.parseFloat(curLine_components[3].substring(1)) * -1;
                                scaling[3] = Float.parseFloat(curLine_components[4].substring(1)) * -1;
                            }else{
                                scaling[0] = Float.parseFloat(curLine_components[1]);    // factor x
                                scaling[1] = Float.parseFloat(curLine_components[2]);    // factor y        
                                scaling[2] = Float.parseFloat(curLine_components[3]);   // pivot pt x      
                                scaling[3] = Float.parseFloat(curLine_components[4]);     // pivot pt y   
                            }
                            curPolygon.scalings.add(scaling);
                        }
                        else{
                            // when reaching "rotation" line
                            if(curLine_components[0].equals("r")){
                                curPolygon.hasRotation = true;
                                float[] rotation = 
                                { 
                                    Float.parseFloat(curLine_components[1]),    // rotation angle
                                    Float.parseFloat(curLine_components[2]),    // pivot pt x 
                                    Float.parseFloat(curLine_components[3]),    // pivot pt y
                                };
                                curPolygon.rotations.add(rotation);
                            }else{
                                
                                // when reaching a vertex
                                if(!curLine_components[0].equals("T")){
                                    
                                    // add vertex to rawVertices list
                                    float[] vertex =
                                    { Float.parseFloat(curLine_components[0]),
                                      Float.parseFloat(curLine_components[1])
                                    };
                                    curPolygon.rawVertices.add(vertex);
                                    
                                    float v1_x;
                                    float v1_y;
                                    float v2_x;
                                    float v2_y;
                                    
                                    // set first vertex according to current line
                                    v1_x = Float.parseFloat(curLine_components[0]);
                                    v1_y = Float.parseFloat(curLine_components[1]);
                                    
                                    // set second vertex according to next line (if it's a vertex), or the first vertex of the polygon
                                    String vertexLine2;
                                    vertexScanner = new Scanner(file);
                                    for(int i = 0; i < scans; i++){
                                        vertexScanner.nextLine();
                                    }
                                    if(vertexScanner.nextLine().equals("T")){
                                        vertexLine2 = firstVertexLine;
                                    }else{
                                        vertexScanner = new Scanner(file);
                                        for(int i = 0; i < scans; i++){
                                            vertexScanner.nextLine();
                                        }
                                        vertexLine2 = vertexScanner.nextLine();
                                    }
                                    v2_x = Float.parseFloat(vertexLine2.split(" ")[0]);
                                    v2_y = Float.parseFloat(vertexLine2.split(" ")[1]);
                                    
                                    // calculate y-min, y-max, x of y-min, slope of current pair of vertices  
                                    float x_max;
                                    float x_min;
                                    float y_max;
                                    float y_min;
                                    float x_of_y_min;
                                    if(v1_y < v2_y){
                                        y_min = v1_y;
                                        y_max = v2_y;
                                        x_of_y_min = v1_x;
                                    }else{
                                        y_min = v2_y;
                                        y_max = v1_y;
                                        x_of_y_min = v2_x;
                                    }  
                                    if(v1_x < v2_x){
                                        x_max = v2_x;
                                        x_min = v1_x;
                                    } else{
                                        x_max = v1_x;
                                        x_min = v2_x;
                                    } 
                                    float slope;
                                    float invSlope;
                                    slope = ((y_max - y_min) / (x_max - x_min));
                                    if(Float.isInfinite(slope)){
                                        invSlope = 0;
                                    }else{
                                        invSlope = 1 / slope;
                                    }
                                    
                                    // add edge to all_edges table
                                    float[] cell = { y_min, y_max, x_of_y_min, invSlope };
                                    curPolygon.all_edges.add(cell);
                                    
                                    // if m != 0, add edge to global_edges in order of y-min
                                    if(slope != 0){
                                        
                                        if(curPolygon.global_edges.isEmpty()){
                                            curPolygon.global_edges.add(cell);
                                        }else{     
                                            // find correct index for edge based on y-min, then x-value, then y-max
                                            int index = 0;
                                            boolean set = false;
                                            while(!set){       
                                                if(cell[0] < ((float[])curPolygon.global_edges.get(index))[0]){
                                                    curPolygon.global_edges.add(index, cell);
                                                    set = true;
                                                }
                                                else{
                                                    if(cell[0] > ((float[])curPolygon.global_edges.get(index))[0]){
                                                        index++;
                                                    }
                                                    else{   
                                                        if(cell[2] < ((float[])curPolygon.global_edges.get(index))[2]){
                                                            curPolygon.global_edges.add(index, cell);
                                                            set = true;
                                                        }
                                                        else{
                                                            if(cell[2] > ((float[])curPolygon.global_edges.get(index))[2]){
                                                                index++;
                                                            }
                                                            else{
                                                                if(cell[1] < ((float[])curPolygon.global_edges.get(index))[1]){
                                                                    curPolygon.global_edges.add(index, cell);
                                                                    set = true;
                                                                }
                                                                else{
                                                                    index++;
                                                                }
                                                            }
                                                        }   
                                                    }
                                                }
                                                if(index >= curPolygon.global_edges.size()){
                                                    curPolygon.global_edges.add(cell);
                                                    set = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }    
            }
            
            // add the last Polygon to list of Polygons
            polygons.add(curPolygon);
                    
        }
        catch(FileNotFoundException e){
            e.printStackTrace();
        }
        
        return polygons;
    }
    
}
