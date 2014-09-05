/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.io.output;

import java.io.IOException;
import java.io.File;

import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.RenderingHints;


import java.util.Arrays;

import javax.imageio.ImageIO;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.util.LongConverter;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;


/**
 * Export grid into set of slice files in PNG format 
 *  
 * @author Vladimir Bulatov
 */
public class SlicesWriter {

    static final boolean DEBUG = false;
    static int debugCount = 0;

    static final int COLOR_WHITE = makeColor(0xFF);
    static final int COLOR_BLACK = makeColor(0);
    static final int COLOR_GRAY = makeColor(127);
    

    String m_filePattern = "slice_%04d.png";
    String m_imageFileType = "png";
    
    int imgCellSize = 1;  // size of grid cell to write to 
    int imgVoxelSize = 1; // 
    int m_subvoxelResolution=0;
    int m_backgroundColor = COLOR_WHITE; // solid white 
    int m_foregroundColor = COLOR_BLACK; // solid black

    int xmin=-1, xmax=-1, ymin=-1, ymax=-1, zmin=-1, zmax=-1;
    double m_levels[] = new double[0];
    Grid m_grid;

    Color m_levelsColor = Color.RED;

    boolean m_writeVoxels = true;
    boolean m_writeLevels = false;

    double m_levelLineWidth = 3.;
    
    LongConverter m_dataConverter = new DefaultDataConverter();
    LongConverter m_colorMaker = new DefaultColorMaker();

    /** Skip if the slice % modSkip == 0 and modeSkip != 0 */
    int m_modSkip;

    public void setBounds(int xmin, int xmax, int ymin, int ymax, int zmin, int zmax){

        this.xmin = xmin;
        this.ymin = ymin;
        this.zmin = zmin;
        this.xmax = xmax;
        this.ymax = ymax;
        this.zmax = zmax;
    }

    public void setModSkip(int skip) {
        m_modSkip = skip;
    }

    /**
       set level for topographical map output 
     */
    public void setLevels(double levels[]){
        m_levels = new double[levels.length];
        System.arraycopy(levels, 0, m_levels, 0, levels.length);
    }

    public void setWriteVoxels(boolean value){
        m_writeVoxels = value;
    }

    public void setWriteLevels(boolean value){
        m_writeLevels = value;
    }

    public void setDataConverter(LongConverter dataConverter){
        m_dataConverter = dataConverter;
    }
    public void setColorMaker(LongConverter colorMaker){
        m_colorMaker = colorMaker;
    }

    public void setMaxAttributeValue(int value){

        m_subvoxelResolution = value;

    }

    public void setSubvoxelResolution(int value){

        m_subvoxelResolution = value;
        
    }

    public void setBackgroundColor(int color){
        m_backgroundColor = color;
    }

    public void setForegroundColor(int color){
        m_foregroundColor = color;
    }
    public void setCellSize(int size){
        imgCellSize = size;
    }

    public void setVoxelSize(int size){
        
        imgVoxelSize = size;
    }

    public void setFilePattern(String pattern){

        m_filePattern = pattern;        

    }

    /**
       writes single pixel slices into a bunch of files 
     */
    public void writeSlices(AttributeGrid grid, String fileTemplate, int firstSlice, int fistFile, int sliceCount) throws IOException {
        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        BufferedImage outImage = new BufferedImage(nx, ny, BufferedImage.TYPE_INT_ARGB);

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());
        int[] imageData = dbi.getData();

        
    }
    
    public void writeSlices(Grid grid) throws IOException {

        if(DEBUG) printf("%s.writeSlices()\n", this.getClass().getName());

        m_grid = grid;

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        if(xmax < 0)xmax = nx;
        if(ymax < 0)ymax = ny;
        if(zmax < 0)zmax = nz;
        if(xmin < 0 )xmin = 0;
        if(ymin < 0 )ymin = 0;
        if(zmin < 0 )zmin = 0;

        xmin = clamp(xmin, 0, nx);
        xmax = clamp(xmax, 0, nx);
        ymin = clamp(ymin, 0, ny);
        ymax = clamp(ymax, 0, ny);
        zmin = clamp(zmin, 0, nz);
        zmax = clamp(zmax, 0, nz);

        if(xmin >= xmax || ymin >= ymax || zmin >= zmax){
            throw new IllegalArgumentException(fmt("bad grid export bounds[xmin:%d xmax:%d, ymin:%d ymax:%d zimn:%d, zmax:%d]\n",
                                                   xmin, xmax, ymin, ymax, zmin, zmax));
        }


        int imgWidth = (xmax-xmin)*imgCellSize;
        int imgHeight = (ymax-ymin)*imgCellSize;

        if(DEBUG) printf("slice image size:[%d x %d]\n", imgWidth, imgHeight);
            
        BufferedImage outImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = null;
        if(m_writeLevels){
            graphics = outImage.createGraphics();
            graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setStroke(new BasicStroke((float)m_levelLineWidth, BasicStroke.CAP_ROUND,  BasicStroke.JOIN_ROUND));
        }

        DataBufferInt dbi = (DataBufferInt)(outImage.getRaster().getDataBuffer());
        int[] imageData = dbi.getData();

        int skip = 0;
        for(int z = zmin; z < zmax; z++){

            if (m_modSkip != 0 && (skip > 0)) {
                skip--;
                continue;
            }
            if (m_modSkip != 0 && (skip == 0)) {
                skip = m_modSkip;
            }


            Arrays.fill(imageData, m_backgroundColor);

            if(m_writeVoxels){
                for(int y = ymin; y < ymax; y++){
                    for(int x = xmin; x < xmax; x++){                        
                        int cc = getVoxelColor(x,y,z);
                        
                        int ix = x-xmin;
                        int iy = y-ymin;

                        int ix0 = ix*imgCellSize;
                        int ix1 = ix0 + imgVoxelSize;
                        int iy0 = iy*imgCellSize;
                        int iy1 = iy0 + imgVoxelSize;
                        
                        for(int yy = iy0; yy < iy1; yy++) {
                            int yy0 = yy*imgWidth;
                            for(int xx = ix0; xx < ix1; xx++) {
                                imageData[xx + yy0] = cc;
                            }
                        }
                    }
                } // y cycle
            } // if(m_writeVoxels)
            
            
            if(m_writeLevels){

                graphics.setPaint(m_levelsColor);
                double values[] = new double[4];
                int ymax1 = ymax-1; // no processing of the last raw and column 
                Point2D points[] = new Point2D[4];
                for(int i = 0; i < 4; i++){
                    points[i] = new Point2D.Double();
                }
                int xmax1 = xmax-1;
                
                for(int y = ymin; y < ymax1; y++){
                    for(int x = xmin; x < xmax1; x++){
                        
                        values[0] = getVoxelValue(x,y,z);
                        values[1] = getVoxelValue(x+1,y,z);
                        values[2] = getVoxelValue(x+1,y+1,z);
                        values[3] = getVoxelValue(x,y+1,z);
                        
                        
                        double ix0 = (x-xmin)*imgCellSize + imgCellSize/2;
                        double iy0 = (y-ymin)*imgCellSize + imgCellSize/2;
                        double ix1 = ix0 + imgCellSize;
                        double iy1 = iy0 + imgCellSize;
                        points[0].setLocation(ix0, iy0);
                        points[1].setLocation(ix1, iy0);
                        points[2].setLocation(ix1, iy1);
                        points[3].setLocation(ix0, iy1);
                        drawLevels(graphics, values, points, m_levels);
                        //graphics.drawLine(ix0, iy0, ix1, iy1);
                        
                    }
                }                
            }

            String fileName = fmt(m_filePattern, (z-zmin));
            if(DEBUG)printf("slice: %s\n", fileName);

            ImageIO.write(outImage, m_imageFileType, new File(fileName));
            
        } // zcycle 
                
    } //    writeSlices(AttributeGtrid grid){   

    /**
       draw contours in rectangle 
     */
    void drawLevels(Graphics2D graphics, double values[], Point2D points[], double levels[]){

        for(int i = 0; i < levels.length; i++){
            drawLevel(graphics, values, points, levels[i]);
        }                                        
    }

    
    void drawLevel(Graphics2D graphics, double v[], Point2D p[], double level){
                    
        int count = 0;
        Point2D pnts[] = null;

        for(int i = 0; i < 4; i++){
            
            int i1 = (i+1)%4;
            
            if((v[i] - level) * (level - v[i1])  >= 0.){
                
                if(pnts == null){
                    pnts = new Point2D[2];
                    count = 0;
                }
                pnts[count++] = lerp(p[i], p[i1], v[i], v[i1], level);
                
                if(DEBUG && v[0] > 0 &&  debugCount-- > 0){
                    printf("[%7.3f %7.3f %7.3f  ->(%7.3f %7.3f)\n", v[i], v[i1], level,pnts[count-1].getX(),pnts[count-1].getY());
                }
                
                if(count == 2){
                    Line2D line = new Line2D.Double(pnts[0].getX(), pnts[0].getY(), pnts[1].getX(), pnts[1].getY());
                    graphics.draw(line);
                    count = 0;
                }                
            }
        }                                           
    }
    
    /**
       
     */
    static Point2D lerp(Point2D p0, Point2D p1, double v0, double v1, double v){

        double t0 = (v1 - v)/(v1 - v0);
        double t1 = (v - v0)/(v1 - v0);

        return new Point2D.Double(t0*p0.getX() + t1*p1.getX(), t0*p0.getY() + t1*p1.getY());

    }

    /**
       returns color to be used for given vocxel
    */
    int getVoxelColor(int x, int y, int z){

        switch(m_subvoxelResolution){
        case 0: // use grid state 
            {
                switch(m_grid.getState(x,y,z)){
                default:
                case Grid.OUTSIDE:
                    return (int)m_colorMaker.get(0);
                case Grid.INSIDE:
                    return (int)m_colorMaker.get(1);
                }
            }
        default: // use grid attribute 

            long a = m_dataConverter.get(((AttributeGrid)m_grid).getAttribute(x,y,z));
            return (int)m_colorMaker.get(a);
        }
    }

    long getVoxelValue(int x, int y, int z){
        
        
        switch(m_subvoxelResolution){
        case 0: // use grid state 
            {
                switch(m_grid.getState(x,y,z)){
                default:
                case Grid.OUTSIDE:
                    return 0;
                case Grid.INSIDE:
                    return 1;
                }
            }
        default: // use grid attribute 
            return m_dataConverter.get(((AttributeGrid)m_grid).getAttribute(x,y,z));            
        }
    }
    
    static final int makeColor(int gray){

        return 0xFF000000 | (gray << 16) | (gray << 8) | gray;

    }

    static final int makeNegativeColor(int gray){

        return 0xFF000000 | gray;

    }

    //
    //  default data converter - does nothing 
    //
    static class DefaultDataConverter implements LongConverter {
        
        public final long get(long data){
            return data;
        }
    }


    class DefaultColorMaker  implements LongConverter {

        public final long get(long a){
            if (m_subvoxelResolution == 0) {
                if (a == Grid.INSIDE) {
                    return makeColor(0);
                } else {
                    return m_backgroundColor;
                }
            }

            int  level = (int)(((m_subvoxelResolution - a) * 255)/m_subvoxelResolution);                
            if(level == 255) // return background color for max value 
                return m_backgroundColor;
            else 
                return makeColor(level);
        }
    }
}
