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

package abfab3d.datasources;


import java.awt.image.BufferedImage;

import java.util.Vector;
import java.awt.Font;
import java.awt.Insets;


import javax.vecmath.Vector3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.AxisAngle4d;


import abfab3d.util.Vec;
import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.VecTransform;
import abfab3d.util.Units;
import abfab3d.util.TextUtil;

import abfab3d.util.PointToTriangleDistance;

import static java.lang.Math.sqrt;
import static java.lang.Math.atan2;
import static java.lang.Math.abs;

import static abfab3d.util.Output.printf;


import static abfab3d.util.MathUtil.clamp;
import static abfab3d.util.MathUtil.intervalCap;
import static abfab3d.util.MathUtil.step10;

import static abfab3d.util.Units.MM;


/**

   makes 3D text string with given font and dimensions 
       
   @author Vladimir Bulatov

 */
public class Text extends TransformableDataSource {

    static final boolean DEBUG = false;
    static int debugCount = 1000;

    private double m_sizeX=30*MM, m_sizeY=10*MM, m_sizeZ=2*MM;
    private double m_voxelSize = 0.1*MM;
    private String m_text  = "Text";
    private String m_fontName = "Times New Roman";

    private ImageBitmap m_bitmap = null; 
    private int m_fontSize = 50; // arbitrary font size, text is scaled to fit box anyway
    private double m_textBlurWidth = 1.;
    private int m_fontStyle = Font.PLAIN;
    private int m_textScale = 5;

    public Text(String text, String fontName, double width, double height, double depth, double voxelSize){
        
        m_sizeX = width;
        m_sizeY = height;
        m_sizeZ = depth;
        m_voxelSize = voxelSize;
        m_text = text;
        m_fontName = fontName;
        
    }
        
    /**
       
     */
    public int initialize(){

        super.initialize();
        
        // size of bitmap to render text 
        int nx = m_textScale*(int)Math.round(m_sizeX/m_voxelSize);
        int ny = m_textScale*(int)Math.round(m_sizeY/m_voxelSize); 
        
        BufferedImage img = TextUtil.createTextImage(nx, ny, m_text, new Font(m_fontName, m_fontStyle, m_fontSize), new Insets(0,0,0,0), true);
        
        printf("text bitmap: %d x %d\n", nx, ny);
        Font font = new Font(m_fontName, m_fontStyle, m_fontSize);

       
        m_bitmap = new ImageBitmap();

        m_bitmap.setImage(img);
        m_bitmap.setSize(m_sizeX, m_sizeY, m_sizeZ);
        m_bitmap.setBaseThickness(0.);
        m_bitmap.setImageType(ImageBitmap.IMAGE_TYPE_EMBOSSED);
        m_bitmap.setTiles(1, 1);
        m_bitmap.setBlurWidth(m_voxelSize/4);
        m_bitmap.setUseGrayscale(false);
        m_bitmap.setInterpolationType(ImageBitmap.INTERPOLATION_LINEAR);


        m_bitmap.initialize();

        
        return RESULT_OK;
        
    }
    
    /**
     * returns 1 if pnt is inside of block of given size and location
     * returns 0 otherwise
     */
    public int getDataValue(Vec pnt, Vec data) {
        
        super.transform(pnt);

        return m_bitmap.getDataValue(pnt, data);

        
    }
    
}  // class Text
