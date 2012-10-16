/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;


import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;

import java.io.File;

import javax.imageio.ImageIO;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;


import static abfab3d.util.ImageUtil.getRed;
import static abfab3d.util.ImageUtil.getGreen;
import static abfab3d.util.ImageUtil.getBlue;
import static abfab3d.util.ImageUtil.getAlpha;
import static abfab3d.util.MathUtil.clamp;

/**
   
   a collection of various DataSource 

   @author Vladimir Bulatov

 */
public class DataSources {
   
    /**

       makes solid block of given size
       
     */
    public static class Block implements DataSource, Initializable {

        public double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.1, m_centerX=0, m_centerY=0, m_centerZ=0;             
        
        private double xmin, xmax, ymin, ymax, zmin, zmax;
        
        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;

            return RESULT_OK;
        }

        /**
         * returns 1 if pnt is inside of block of given size and location
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            if(x < xmin || x > xmax ||
               y < ymin || y > ymax ||
               z < zmin || z > zmax)
                res = 0;

            data.v[0] = res;
            
            return RESULT_OK;
        }                
    }  // class Block 



    /**

       makes embossed image fron given file of given size 
       
     */
    public static class ImageBitmap implements DataSource, Initializable {

        public double m_sizeX=0.1, m_sizeY=0.1, m_sizeZ=0.001, m_centerX=0, m_centerY=0, m_centerZ=0; 
        
        public double m_baseThickness = 0.5; // relative thickness of solid base 
        public String m_imagePath; 
        
        public int m_xTilesCount = 1; // number of image tiles in x-direction 
        public int m_yTilesCount = 1; // number of image tiles in y-direction 


        private double xmin, xmax, ymin, ymax, zmin, zmax, zbase;
        
        private int imageWidth, imageHeight;
        private int imageData[]; 


        public int initialize(){
            
            xmin = m_centerX - m_sizeX/2;
            xmax = m_centerX + m_sizeX/2;

            ymin = m_centerY - m_sizeY/2;
            ymax = m_centerY + m_sizeY/2;

            zmin = m_centerZ - m_sizeZ/2;
            zmax = m_centerZ + m_sizeZ/2;
            zbase = zmin + (zmax - zmin)*m_baseThickness;

            BufferedImage image = null;

            try {
                image = ImageIO.read(new File(m_imagePath));                
            } catch(Exception e){
                e.printStackTrace();
                return RESULT_ERROR;
            }
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();
            
            DataBuffer dataBuffer = image.getRaster().getDataBuffer();          
            imageData = new int[imageWidth * imageHeight];
            image.getRGB(0,0,imageWidth, imageHeight, imageData, 0, imageWidth);
            
            return RESULT_OK;
        }

        /**
         * returns 1 if pnt is inside of image
         * returns 0 otherwise
         */
        public int getDataValue(Vec pnt, Vec data) {

            double res = 1.;
            double 
                x = pnt.v[0],
                y = pnt.v[1],
                z = pnt.v[2];

            x = (x-xmin)/(xmax-xmin);
            y = (y-ymin)/(ymax-ymin);
            
            if(x < 0 || x > 1 ||
               y < 0 || y > 1 ||
               z < zmin || z > zmax){
                data.v[0] = 0;
                return RESULT_OK;
            }

            if(m_xTilesCount > 1){
                x *= m_xTilesCount;
                x -= Math.floor(x);
            }
            if(m_yTilesCount > 1){
                y *= m_yTilesCount;
                y -= Math.floor(y);
            }
                
            double imageX = imageWidth*x;
            double imageY = imageHeight*(1-y);// reverse Y-direction 

            double pixelValue = getPixelBox(imageX,imageY);
            // pixel value for black is 0 for white is 255;
            // we need to reverse it

            pixelValue = 1 - pixelValue/255.;

            double d = (zbase  + (zmax - zbase)*pixelValue - z);

            if(d > 0)
                data.v[0] = 1;
            else 
                data.v[0] = 0;
            
            return RESULT_OK;
        }                
        
        double getPixelBox(double x, double y){

            int ix = clamp((int)Math.floor(x), 0, imageWidth-1);
            int iy = clamp((int)Math.floor(y), 0, imageHeight-1);
            
            int rgb00 = imageData[ix + iy * imageWidth];
            
            int red   = getRed(rgb00);
            int green = getGreen(rgb00);
            int blue  = getBlue(rgb00);

            double alpha = getAlpha(rgb00);
            
            return (red + green + blue)/3.;

        }

    }  // class ImageBitmap
    

}
