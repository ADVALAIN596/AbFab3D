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

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;


import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.awt.Font;
import java.awt.Insets;


import java.io.File;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;
import javax.vecmath.Matrix4d;

import org.j3d.geom.GeometryData;
import org.j3d.geom.TorusGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;

// external imports 
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


// Internal Imports
import abfab3d.grid.Grid;
import abfab3d.grid.ArrayAttributeGridByte;
import abfab3d.grid.BlockBasedGridByte;
import abfab3d.grid.GridShortIntervals;

import abfab3d.util.Vec;
import abfab3d.util.VecTransform;
import abfab3d.util.MathUtil;
import abfab3d.util.TextUtil;
import abfab3d.util.Symmetry;

import abfab3d.io.output.IsosurfaceMaker;
import abfab3d.io.output.STLWriter;

import abfab3d.util.ImageMipMap;


import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.MathUtil.TORAD;

import static java.lang.System.currentTimeMillis;
import static java.lang.Math.sin;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;
import static java.lang.Math.PI;

/**
 * Tests the functionality of GridMaker
 *
 * @version
 */
public class TestGridMaker extends TestCase {

    static final double CM = 0.01; // cm -> meters
    static final double MM = 0.001; // mm -> meters

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestGridMaker.class);
    }

    public void _testGridMakerMT() {

        printf("testGridMakerMT()\n");

        double voxelSize = 1.e-4;
        int smoothSteps = 0;
        double margin = 2*voxelSize;

        double ringDiameter =20*MM;
        double ringWidth =7*MM;
        double ringThickness = 3*MM;

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = ringWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.Block box = new DataSources.Block();
        box.setSize(ringDiameter*Math.PI,ringWidth, ringThickness);
        box.setLocation(0,0, ringThickness/2);
        
        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(ringDiameter*Math.PI, ringWidth, ringThickness);
        image.setLocation(0,0,0);

        image.setBaseThickness(0.1);
        image.setImagePath("docs/images/numbers_1.png");
        image.setUseGrayscale(true);
        image.setTiles(2, 1);
        image.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED);


        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(ringDiameter/2);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(rw);
        gm.setDataSource(image);


        for(int i = 0; i <= 4; i++){
            int tcount = i;
            gm.setThreadCount(tcount);                
            long t0 = time();
            //Grid grid = new BlockBasedGridByte(nx, ny, nz, voxelSize, voxelSize, 5);
            //Grid grid = new GridShortIntervals(nx, ny, nz, voxelSize, voxelSize);
            Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);            
            printf("gm.makeGrid() threads: %d\n", tcount);
            gm.makeGrid(grid); 
            printf("gm.makeGrid() done %d ms\n", (time() - t0));
            //printf("writeIsosurface()\n");
            //t0 = time();
            //writeIsosurface(grid, bounds, voxelSize, smoothSteps, fmt("/tmp/ring_plain_%d.stl", tcount));
            //printf("writeIsosurface() done %d ms\n", (time() - t0));

        }        
    }

    /**
       
     */
    public void _testSymmetricImage() {
        /*
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_S442, "docs/images/wp_fd.png", "/tmp/wp01_s442.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_442, "docs/images/wp_fd.png", "/tmp/wp02_442.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_4S2, "docs/images/wp_fd.png", "/tmp/wp03_4S2.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_S632, "docs/images/wp_fd.png", "/tmp/wp04_S632.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_632, "docs/images/wp_fd.png", "/tmp/wp05_632.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_3S3, "docs/images/wp_fd.png", "/tmp/wp06_3S3.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_S333, "docs/images/wp_fd.png", "/tmp/wp07_S333.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_333, "docs/images/wp_fd.png", "/tmp/wp08_333.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_S2222, "docs/images/wp_fd_01.png", "/tmp/wp09_S2222.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_2222, "docs/images/wp_fd_01.png", "/tmp/wp10_2222.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_2S22, "docs/images/wp_fd_01.png", "/tmp/wp11_2S22.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_22S, "docs/images/wp_fd_01.png", "/tmp/wp12_22S.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_SS, "docs/images/wp_fd_01.png", "/tmp/wp13_SS.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_SX, "docs/images/wp_fd_01.png", "/tmp/wp14_SX.stl");
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_22X, "docs/images/wp_fd_01.png", "/tmp/wp15_22X.stl"); 
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_XX, "docs/images/wp_fd_01.png", "/tmp/wp16_XX.stl");                
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_O, "docs/images/wp/wp17_O.png", "/tmp/wp17_O.stl");
        */
        makeSymmetricImage(VecTransforms.WallpaperSymmetry.WP_O, "/tmp/Anonymous_alarm_clock.png", "/tmp/alarm_clock.stl");
        
    }
    
    public void makeSymmetricImage(int symmetryType, String imagePath, String outFileName) {
        printf("_testSymmetricImage()\n");
        double voxelSize = 1.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double rectWidth = 3*CM;
        double rectHeight = 3*CM; 
        double rectDepth = 1*CM; 
        double imageDepth = 0.1*CM;

        double tileWidth = 1*CM;
        double tileHeight = 1*CM;
        double tileSkew = 0.2;//0.3333;
        double baseThichness = 0.1*CM; 

        //String imagePath = "docs/images/shape_01.png";

        double tileRotationAngle = 0*TORAD;
        Vector3d tileRotationAxis = new Vector3d(1,1,1);
        boolean useGrayscale = true;
        int imageType = DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED;

        double gridWidth = rectWidth + 2*margin;
        double gridHeight  = rectHeight + 2*margin;
        double gridDepth = rectDepth + 2*margin;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(tileWidth, tileHeight, imageDepth);
        image.setLocation(0,0,0);

        image.setBaseThickness(baseThichness);
        image.setImagePath(imagePath);
        image.setUseGrayscale(useGrayscale);
        image.setImageType(imageType);

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();

        rot.m_axis = tileRotationAxis;
        rot.m_angle = tileRotationAngle;

        VecTransforms.WallpaperSymmetry wps = new VecTransforms.WallpaperSymmetry();
        wps.setSymmetryType(symmetryType);
        wps.setDomainWidth(tileWidth);
        wps.setDomainHeight(tileHeight);
        wps.setDomainSkew(tileSkew);

        //compTrans.add(rot);
        compTrans.add(wps);

        DataSources.DataTransformer tiling = new DataSources.DataTransformer();
        tiling.setDataSource(image);
        tiling.setTransform(compTrans);
        
        DataSources.Block box = new DataSources.Block();
        box.setSize(rectWidth, rectHeight, rectDepth);
        DataSources.Intersection clip = new DataSources.Intersection();
        
        clip.addDataSource(box);
        clip.addDataSource(tiling);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setDataSource(clip);

        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "/tmp/symmetry_test.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void _testImageRing1() {
        
        printf("testImageRing1()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(ringDiameter*Math.PI, ringWidth, ringThickness);
        image.setLocation(0,0,ringThickness/2);
        image.setBaseThickness(0.4);
        image.setImagePath("docs/images/numbers_1.png");
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(ringDiameter/2);

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.setRotation(new Vector3d(1,0,0),0*TORAD);
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_numbers.stl");
        printf("writeIsosurface() done\n");
        
    }

    public void _testRoundedRing() {
        
        printf("testImageRing1()\n");
        double voxelSize = 0.1*MM;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 1*voxelSize;

        double ringDiameter = 20*MM; 
        double ringWidth = 5*MM;  
        double ringThickness = 1.4*MM;  
        double bandLength = ringDiameter*Math.PI;

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(bandLength, ringWidth, ringThickness);
        image.setLocation(0,0,ringThickness/2);
        image.setBaseThickness(0.);
        image.setTiles(12,1);
        image.setUseGrayscale(true);        
        image.setImagePath("apps/ringpopper/images/tile_02.png");
        image.setInterpolationType(DataSources.ImageBitmap.INTERPOLATION_MIPMAP);
        image.setPixelWeightNonlinearity(-1.);
        image.setProbeSize(0.5*MM);

        DataSources.ImageBitmap crossSect = new DataSources.ImageBitmap();
        crossSect.setSize(ringWidth, ringThickness,bandLength);
        crossSect.setLocation(0,ringThickness/2,0);
        crossSect.setBaseThickness(0.);
        crossSect.setUseGrayscale(false);        
        crossSect.setImagePath("apps/ringpopper/images/crosssection_01.png");

        VecTransforms.CompositeTransform crossTrans = new VecTransforms.CompositeTransform();
        VecTransforms.Rotation crot1 = new VecTransforms.Rotation();
        crot1.setRotation(new Vector3d(0,0,1),-90*TORAD);
        VecTransforms.Rotation crot2 = new VecTransforms.Rotation();
        crot2.setRotation(new Vector3d(0,1,0),-90*TORAD);
        crossTrans.add(crot1);
        crossTrans.add(crot2);

        DataSources.DataTransformer transCross = new DataSources.DataTransformer();
        transCross.setDataSource(crossSect);
        transCross.setTransform(crossTrans);
        
        DataSources.Intersection inter = new DataSources.Intersection();
        inter.addDataSource(transCross);
        inter.addDataSource(image);

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(ringDiameter/2);

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.setRotation(new Vector3d(1,0,0),0*TORAD);
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(inter);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "/tmp/ring_rounded.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void _testImageRingTiled() {
        
        printf("testImageTiled()\n");
        double voxelSize = 1.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = ringDiameter*Math.PI;
        image.m_sizeY = ringWidth;
        image.m_sizeZ = ringThickness;
        image.m_centerZ = ringThickness/2;
        image.m_baseThickness = 0.5;
        image.m_xTilesCount = 12;
        image.m_yTilesCount = 1;
        image.m_imagePath = "docs/images/Tile_DecorativeCeiling_2.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(1,0,0);
        rot.m_angle = 0*TORAD;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_tiled.stl");
        printf("writeIsosurface() done\n");
        
    }


    /**

     */
    public void _testTorusRing() {
        
        printf("testTorusRing()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 1*voxelSize;

        double ringR = 3*CM;
        double ringr = 0.5*CM;
        double ringThickness = 0.2*CM;
        int tilesR = 20;
        int tilesr = 3;

        double gridWidth =2*(ringR + ringr) + 2*margin;
        double gridHeight  = 2*(ringr) + 2*margin;
        double gridDepth = gridWidth;

        double tileWidth = 2*PI*ringr/tilesr;
        double tileHeight = 2*PI*(ringR)/tilesR;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);  

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(tileWidth, tileHeight, ringThickness);
        image.setLocation(0,0,-ringThickness);
        image.setBaseThickness(0.);
        image.setTiles(1,1);
        image.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED);
        image.setImagePath("docs/images/tile_01.png");
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.WallpaperSymmetry wps = new VecTransforms.WallpaperSymmetry();
        wps.setSymmetryType(VecTransforms.WallpaperSymmetry.WP_O);
        wps.setDomainWidth(tileWidth);
        wps.setDomainHeight(tileHeight);
        wps.setDomainSkew(0);

        VecTransforms.RingWrap rw1 = new VecTransforms.RingWrap();
        rw1.setRadius(ringr);

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.setRotation(new Vector3d(0,0,1), 90*TORAD);

        VecTransforms.Scale scale = new VecTransforms.Scale();
        scale.setScale(1, 1, 0.5);
        
        VecTransforms.RingWrap rw2 = new VecTransforms.RingWrap();
        rw2.setRadius(ringR);
        
        compTrans.add(wps);
        compTrans.add(rw1);
        compTrans.add(rot);
        compTrans.add(scale);
        compTrans.add(rw2);
               

        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_torus.stl");
        printf("writeIsosurface() done\n");
        
    }

    public void _testRingWithBand() {
        
        printf("testImageTiled()\n");
        double voxelSize = 1.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double bandWidth = 0.002;// 1mm 
        double bandThickness = 0.004;// 4mm 
        
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.002; // 2mm 

        double gridWidth = ringDiameter + 2*bandThickness + 2*margin;
        double gridHeight  = ringWidth + 2*bandWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(ringDiameter*Math.PI,ringWidth,ringThickness);
        image.setLocation(0,0,ringThickness/2);
        image.setBaseThickness(0.5);
        image.setTiles(12, 1);
        image.setImagePath("docs/images/Tile_DecorativeCeiling_1k_h.png");

        DataSources.Block topBand = new DataSources.Block();
        topBand.setSize(ringDiameter*Math.PI,bandWidth, bandThickness);
        topBand.setLocation(0, ringWidth/2, bandThickness/2);

        //DataSources.Block bottomBand = new DataSources.Block();
        DataSources.ImageBitmap bottomBand = new DataSources.ImageBitmap();
        bottomBand.setSize(ringDiameter*Math.PI,bandWidth, bandThickness);
        bottomBand.setLocation(0, -ringWidth/2, bandThickness/2);
        bottomBand.setTiles((int)(ringDiameter*Math.PI/bandWidth), 1);
        bottomBand.setImagePath("docs/images/circle.png");

        DataSources.Union union = new DataSources.Union();
        
        union.addDataSource(image);
        union.addDataSource(topBand);
        union.addDataSource(bottomBand);

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(1,0,0);
        rot.m_angle = 0*TORAD;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(union);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_with_band.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**
       ring with fancy profile
     */
    public void _testImageRing2() {
        
        printf("testImageRingTiled()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.01; // 1cm

        double gridWidth = ringDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = ringWidth;
        image.m_sizeY = ringWidth;
        image.m_sizeZ = ringDiameter*Math.PI;
        image.m_centerX = 0;
        image.m_centerY = 0;
        image.m_centerZ = 0;
        image.m_baseThickness = 0.;
        image.m_xTilesCount = 1;
        image.m_imagePath = "docs/images/star_4_arms_1.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,1,0);
        rot.m_angle = 90*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_star_profile.stl");
        printf("writeIsosurface() done\n");
        
    }


    /**
       cup with image on outside
     */
    public void _testCup() {
        
        printf("testCup()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 5*CM; 
        double ringWidth = 5*CM; 
        double ringThickness = 0.3*CM;

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = ringWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(ringDiameter*Math.PI, ringWidth, ringThickness);
        image.setLocation(0,0,0);
        image.setBaseThickness(0.7);
        image.setTiles(20,4);
        //image.setImagePath("docs/images/star_4_arms_1.png");
        image.setImagePath("docs/images/R.png");
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,1,0);
        rot.m_angle = 0*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(ringDiameter/2);
        
        double ringRadius = ringDiameter/2;

        double soffset = 10*ringRadius;
        double sradius = Math.hypot(soffset, ringRadius);

        VecTransforms.SphereInversion inversion = new VecTransforms.SphereInversion();
        inversion.setSphere(new Vector3d(0,-soffset-ringWidth/2,0), sradius);

        //VecTransforms.Scale scale = new VecTransforms.Scale();
        //scale.setScale((ringRadius*ringRadius)/(sradius*sradius));

        VecTransforms.Translation translation = new VecTransforms.Translation();
        translation.setTranslation(0, ringWidth, 0);

        compTrans.add(rot);
        compTrans.add(rw);
        compTrans.add(inversion);
        compTrans.add(translation);

        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/cup_image.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**
       cup with fancy profile
     */
    public void _testCup2() {
        
        printf("testCup()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double cupDiameter = 5*CM; 
        double cupHeight = 5*CM; 
        double ringThickness = 0.3*CM;

        double gridWidth = cupDiameter + 2*margin;
        double gridHeight  = cupHeight + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(cupDiameter/2, cupHeight, cupDiameter*Math.PI);
        image.setLocation(-cupDiameter/4,0,0);
        image.setBaseThickness(0.0);
        image.setTiles(1,1);
        image.setImagePath("docs/images/cup_profile.png");
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,1,0);
        rot.m_angle = -90*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(cupDiameter/2);
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/cup_profile.stl");
        printf("writeIsosurface() done\n");
        
    }

    /*
      
     */
    public void _testPlate() {
        
        printf("testCup()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double cupDiameter = 10*CM; 
        double cupHeight = 1*CM; 

        double gridWidth = cupDiameter + 2*margin;
        double gridHeight  = cupHeight + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.setSize(cupDiameter/2, cupHeight, cupDiameter*Math.PI);
        image.setLocation(-cupDiameter/4,0,0);
        image.setBaseThickness(0.0);
        image.setTiles(1,1);
        image.setImagePath("docs/images/plate_profile.png");
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,1,0);
        rot.m_angle = -90*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.setRadius(cupDiameter/2);
        
        compTrans.add(rot);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/plate_profile.stl");
        printf("writeIsosurface() done\n");
        
    }

    public void _testText() {
        
        double textWidth = 5*CM;
        double textHeight = 1*CM;
        double textDepth = 0.3*CM;
        double voxelSize = 0.01*CM;
        double margin = 1*voxelSize;
        int smoothSteps = 0;
        
        double gridWidth = textWidth + 2*margin;
        double gridHeight  = textHeight + 2*margin;
        double gridDepth = 2*textDepth + 2*margin;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};        
        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap textBand = new DataSources.ImageBitmap();        
        textBand.setSize(textWidth, textHeight, textDepth);
        textBand.setLocation(0,0,0); 
        textBand.setBaseThickness(0.0);
        textBand.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED);
        textBand.setTiles(1,1);
        textBand.setImage(TextUtil.createTextImage(1000, 200, "Test Image Text gg", 
                                                   new Font("Times New Roman", Font.BOLD, 20), new Insets(10,10,10,10)));

        
        VecTransform ripples = new Ripples(textHeight/3,textHeight/3,textHeight/3,0.,0.,textDepth);

        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(ripples);
        gm.setDataSource(textBand);        
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/text_test.stl");
        printf("writeIsosurface() done\n");        
        
    }

    public void _testHoledWedge() {
        
        double blockWidth = 50*MM;
        double blockHeight = 10*MM;
        double blockDepth = 2*MM;
        double voxelSize = 0.1*MM;
        double margin = 5*voxelSize;

        int smoothSteps = 0;
        
        double gridWidth = blockWidth + 2*margin;
        double gridHeight  = blockHeight + 2*margin;
        double gridDepth = 2*blockDepth + 2*margin;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};        
        int nx = (int)Math.round((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)Math.round((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)Math.round((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap block = new DataSources.ImageBitmap();        
        block.setSize(blockWidth, blockHeight, blockDepth);
        block.setImagePath("docs/images/circles.png");                
        block.setLocation(0,0,0); 
        block.setBaseThickness(0.);
        //block.setImageType(DataSources.ImageBitmap.IMAGE_POSITIVE);
        block.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_ENGRAVED);
        block.setUseGrayscale(false);        
        block.setTiles(1,1);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setDataSource(block);        
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        long t0 = currentTimeMillis();
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "/tmp/holed_block.stl");
        printf("TIME_ORIG: %d ms\n", (currentTimeMillis()-t0));

        for(int i = 1; i < 10; i++){
            t0 = currentTimeMillis();
            writeIsosurface2(grid, bounds, voxelSize, i, fmt("/tmp/holed_block_%d.stl", i));
            printf("TIME(%d): %d ms\n", i, (currentTimeMillis()-t0));
        }

        printf("done\n");        
        
    }


    static class Ripples implements VecTransform {
        
        double m_periodX, m_periodY, m_periodZ, m_ampX, m_ampY, m_ampZ;

        Ripples(double periodX, double periodY, double periodZ, double ampX, double ampY, double ampZ){
            m_periodX = periodX;
            m_periodY = periodY;
            m_periodZ = periodZ;
            
            m_ampX = ampX;
            m_ampY = ampY;
            m_ampZ = ampZ;
            
        }

        public int transform(Vec in, Vec out) {
            
            return RESULT_OK;
        }

        public int inverse_transform(Vec in, Vec out) {

            return RESULT_OK;
        }        
    }

    

    /**
       ring with text on inside
     */
    public void _testTextRing() {
        
        printf("testImageRingTiled()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.003; // 3mm
        double textDepth = 0.001; // 1mm

        double gridWidth = ringDiameter + 2*ringWidth + 2*margin;
        double gridHeight  = ringWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

                
        DataSources.ImageBitmap ringBand = new DataSources.ImageBitmap();        
        ringBand.setSize(Math.PI*ringDiameter, ringWidth, ringThickness);
        ringBand.setLocation(0,0,ringThickness/2); // make z-offset to have band in positive z halfspace 
        ringBand.setBaseThickness(0.5);
        ringBand.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED);
        ringBand.setTiles(20,1); 
        ringBand.setImagePath("docs/images/star_4_arms_1.png");

        
        DataSources.ImageBitmap textBand = new DataSources.ImageBitmap();        
        textBand.setSize(Math.PI*ringDiameter, ringWidth, textDepth);
        textBand.setLocation(0,0,-textDepth/2); // text is offset in opposite z-direction because we have to rotate 180 around Y
        textBand.setBaseThickness(0.);
        textBand.setImageType(DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED);
        textBand.setTiles(1,1);
        textBand.setImage(TextUtil.createTextImage(1000, 150, "Test Image Text gg", 
                                                   new Font("Times New Roman", Font.BOLD, 20), new Insets(10,10,10,10)));
        
        // we want text on the inside. So it should face in opposite direction 
        VecTransforms.Rotation textRotation = new VecTransforms.Rotation();
        textRotation.setRotation(new Vector3d(0,1,0), 180*TORAD);
        
        // rotated text 
        DataSources.DataTransformer rotatedText = new DataSources.DataTransformer();
        rotatedText.setDataSource(textBand);
        rotatedText.setTransform(textRotation);
        
        DataSources.Subtraction ringMinusText = new DataSources.Subtraction();
        ringMinusText.setSources(ringBand, rotatedText); // 

        VecTransforms.RingWrap ringWrap = new VecTransforms.RingWrap();
        ringWrap.setRadius(ringDiameter/2);

        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(ringWrap);
        gm.setDataSource(ringMinusText);        
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_text.stl");
        printf("writeIsosurface() done\n");
        
    }


    /**
       ring with Frieze pattern 
     */
    public void _testFriezeRing() {
        
        printf("testImageRingTiled()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.003; // 3mm
        int tileCount = 20;


        double tileWidth = ringDiameter*Math.PI/tileCount; 

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = ringWidth*Math.sqrt(2) + 2*margin;
        double gridDepth = gridWidth;
        

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        image.setSize(ringWidth, ringWidth, ringThickness);
        image.setLocation(0,0,0);
        image.setBaseThickness(0.5);
        image.setTiles(1,1);
        image.m_imagePath = "docs/images/R.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        //
        // this may independently rotate each image fragmnent 
        //
        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,0,1);
        rot.m_angle = 0*TORAD;

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;
        
        VecTransforms.FriezeSymmetry fs = new VecTransforms.FriezeSymmetry();
        fs.setDomainWidth(tileWidth);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_S22I);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_II);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_IS);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_SII);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_2SI);
        //fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_22I);
        fs.setFriezeType(VecTransforms.FriezeSymmetry.FRIEZE_IX);

        compTrans.add(rot);
        compTrans.add(fs);
        compTrans.add(rw);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_frieze.stl");
        printf("writeIsosurface() done\n");
        
    }


    /**
       spherical inversion
     */
    public void _testSphericalInversion() {
        
        printf("testSphericalInversion()\n");
        double voxelSize = 2.e-4;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab 
        int smoothSteps = 0;
        double margin = 4*voxelSize;

        double sphereDiameter = 0.05; // 5cm 
        double sphereThickness = 0.002; // 2cm

        double gridWidth = sphereDiameter + 2*sphereThickness + 2*margin;
        double gridHeight  = gridWidth;
        double gridDepth = sphereDiameter/2 + sphereThickness + 2*margin;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,0,gridDepth};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        

        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image = new DataSources.ImageBitmap();
        
        image.m_sizeX = sphereDiameter;
        image.m_sizeY = sphereDiameter;
        image.m_sizeZ = sphereThickness;

        image.m_centerX = 0;
        image.m_centerY = 0;
        image.m_centerZ = 0;
        image.m_baseThickness = 0.;
        //image.m_imagePath = "docs/images/star_4_arms_1.png";
        image.m_imagePath = "docs/images/spiral.png";
        
        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();
        
        VecTransforms.PlaneReflection pr = new VecTransforms.PlaneReflection();
        pr.m_pointOnPlane = new Vector3d(0,0,0);
        pr.m_planeNormal = new Vector3d(0,0,1);

        VecTransforms.SphereInversion si = new VecTransforms.SphereInversion();
        si.m_radius = (sphereDiameter/2)*(Math.sqrt(3));
        si.m_center = new Vector3d(0,0,-sphereDiameter/2*Math.sqrt(2));
                
        compTrans.add(pr);
        compTrans.add(si);
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/sphere_star.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void _testPlainRing() {
        
        printf("testPlainRing()\n");
        double voxelSize = 2.e-4;
        int smoothSteps = 0;
        double margin = 2*voxelSize;

        double ringDiameter = 0.05; // 5cm 
        double ringWidth = 0.01; // 1cm 
        double ringThickness = 0.001; // 1mm 

        double gridWidth = ringDiameter + 2*ringThickness + 2*margin;
        double gridHeight  = ringWidth + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.Block box = new DataSources.Block();
        box.setSize(ringDiameter*Math.PI,ringWidth,ringThickness);
        box.setLocation(0,0,ringThickness/2);
        
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = ringDiameter/2;
        
        GridMaker gm = new GridMaker();
                
        gm.setBounds(bounds);
        gm.setTransform(rw);
        gm.setDataSource(box);
        
        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);               
        printf("gm.makeGrid() done\n");
        
        printf("writeIsosurface()\n");
        writeIsosurface(grid, bounds, voxelSize, smoothSteps, "c:/tmp/ring_plain.stl");
        printf("writeIsosurface() done\n");
        
    }

    /**

     */
    public void testImageBitmapPlace() {
        
        printf("testImageBitmapPlace()\n");

        double voxelSize = 0.1 *MM;
        double margin = 2*voxelSize;

        double width = 2*CM; 
        double height = 2*CM; 
        double thickness = 1*CM;

        double gridWidth = width + 2*margin;
        double gridHeight  = height + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2,-gridHeight/2,gridHeight/2,-gridDepth/2,gridDepth/2};

        int nx = (int)((bounds[1] - bounds[0])/voxelSize);
        int ny = (int)((bounds[3] - bounds[2])/voxelSize);
        int nz = (int)((bounds[5] - bounds[4])/voxelSize);        
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        int type[] = new int[]{DataSources.ImageBitmap.IMAGE_TYPE_EMBOSSED,DataSources.ImageBitmap.IMAGE_TYPE_ENGRAVED};
        int place[] = new int[]{DataSources.ImageBitmap.IMAGE_PLACE_TOP,DataSources.ImageBitmap.IMAGE_PLACE_BOTTOM, DataSources.ImageBitmap.IMAGE_PLACE_BOTH};
        double baseThickness = 0.0;

        for(int i= 0; i < type.length; i++){
            for(int k = 0; k < place.length; k++){
                
                DataSources.ImageBitmap image = new DataSources.ImageBitmap();       
                image.setSize(width,height,thickness);
                image.setLocation(0,0,0);
                image.setBaseThickness(baseThickness);
                image.setImageType(type[i]);
                image.setImagePlace(place[k]);
                image.setImagePath("docs/images/circle.png");
                GridMaker gm = new GridMaker();                
                gm.setBounds(bounds);
                gm.setThreadCount(4);
                gm.setDataSource(image);

                Grid grid = new ArrayAttributeGridByte(nx, ny, nz, voxelSize, voxelSize);
                printf("gm.makeGrid()\n");
                gm.makeGrid(grid);               
                printf("gm.makeGrid() done\n");

                printf("writeIsosurface()\n");
                writeIsosurface(grid, bounds, voxelSize, 0, fmt("/tmp/ring_%d_%d.stl", i, k));
                printf("writeIsosurface() done\n");                
            }
        }                
    }

        
    public void _testTransform() {

        double r = 0.1;
        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = r;

        Vec ringPnt = new Vec(3);
        Vec bandPnt = new Vec(3);
        int N = 10;

        for(int i = 0; i < N; i++){
            double phi = 2*Math.PI*i/N - Math.PI;
            double z = (r + 0.01)* cos(phi);
            double x = (r + 0.01)* sin(phi);
            double y = 0.1;

            ringPnt.set(x, y, z);
            rw.inverse_transform(ringPnt, bandPnt);
            printf("xyz: (%7.5f,%7.5f,%7.5f) -> (%7.5f,%7.5f,%7.5f)\n", x, y, z, bandPnt.v[0],bandPnt.v[1],bandPnt.v[2]);
            
        }        
    }
    
    public void _testFriezeTransform() {
        
        Vector4d p1 = new Vector4d(1,1,1,0);
        VecTransforms.normalizePlane(p1);

        Vector4d p2 = new Vector4d(1,1,1,0.1);
        VecTransforms.normalizePlane(p2);

        Matrix4d r1 = Symmetry.getReflection(p1);
        Matrix4d r2 = Symmetry.getReflection(p2);        
        Matrix4d r1r2 = new Matrix4d();
        Matrix4d r2r1 = new Matrix4d();
        r1r2.mul(r1,r2);
        r2r1.mul(r2,r1);
        
        for(int i = 0; i <= 10; i++){
            double x = 0.1 * i;
            double y = x;
            double z = x;

            Vector4d v = new Vector4d(x,y,z,1);
            Vector4d u = new Vector4d();
            Vector4d t = new Vector4d();

            r1r2.transform(v,u);
            r2r1.transform(v,t);
            //u.x /= u.w;
            //u.y /= u.w;
            //u.z /= u.w;

            printf("(%5.2f,%5.2f,%5.2f,%5.2f) ->(%5.2f,%5.2f,%5.2f,%5.2f) ->(%5.2f,%5.2f,%5.2f,%5.2f) \n", 
                   v.x,v.y,v.z,v.w, u.x,u.y,u.z,u.w, t.x,t.y,t.z,t.w);
            
        }
        
    }

    public void _testFriezeTransform2() {

        VecTransforms.FriezeSymmetry fs = new VecTransforms.FriezeSymmetry();
        fs.setDomainWidth(0.07);
        fs.initialize();
        
        for(int i = 0; i <= 10; i++){
            
            double x = 0.1 * i + 0.05;
            double y = x;
            double z = x;
            Vec in = new Vec(3);
            in.v[0] = x;
            in.v[1] = y;
            in.v[2] = z;
            Vec out = new Vec(3);
            fs.inverse_transform(in,out);
            
            printf("(%5.2f,%5.2f,%5.2f) -> (%5.2f,%5.2f,%5.2f)\n", in.v[0],in.v[1],in.v[2], out.v[0],out.v[1],out.v[2]);
            
        }

    }


    public void _testTransform3() {

        /*
          |      
       1  +            +
          |         /  |
          |   p2  /    |
          |     /      |  p1 
          |   /        |
          | /          |
          +------------+ .....
          0     p0     1
                
         */

        Vector4d p0 = new Vector4d(0,-1,0,0);
        Vector4d p1 = new Vector4d(1,0,0,-1);
        Vector4d p2 = new Vector4d(-1/sqrt(2),1/sqrt(2),0,0);
                
        Matrix4d r0 = Symmetry.getReflection(p0);
        Matrix4d r1 = Symmetry.getReflection(p1);
        Matrix4d r2 = Symmetry.getReflection(p2);
                
        Matrix4d trans[] = new Matrix4d[]{r0, r1, r2};
        Vector4d planes[] = new Vector4d[]{p0, p1, p2};
        int maxCount = 10;
        for(int i = 0; i <= 100; i++){
            
            double x = i*0.2;
            double y = 0.5;
            double z = 0;
        

            Vector4d in = new Vector4d(x,y,z,1);

            int res = Symmetry.toFundamentalDomain(in, planes, trans, maxCount );
            if(res == VecTransform.RESULT_OK)
                printf("(%5.2f,%5.2f,%5.2f) u:(%5.2f,%5.2f,%5.2f)\n", x,y,z, in.x,in.y,in.z);
            else 
                printf("(%5.2f,%5.2f,%5.2f) u:(%5.2f,%5.2f,%5.2f) error\n", x,y,z, in.x,in.y,in.z);
        }

    }

    public void _testTransform4() {

        Symmetry s = Symmetry.getO(1., 1., 0.);
        int maxCount = 100;
        
        Matrix4d t = Symmetry.getTranslation(1,0,0);
        printf("t: (%s)\n", t);
        Vector4d v = new Vector4d(0.5,0,0,1);
        printf("v: (%5.2f,%5.2f,%5.2f,%5.2f)\n", v.x,  v.y,  v.z,  v.w);
        t.transform(v);
        printf("-> (%5.2f,%5.2f,%5.2f,%5.2f)\n", v.x,  v.y,  v.z,  v.w);
        //for(int i = 0; i <= 100; i++){
        for(int i = 0; i <= 0; i++){
            
            double x = 1.2;
            double y = 0.;
            double z = 0;        

            Vector4d in = new Vector4d(x,y,z,1);

            int res = Symmetry.toFundamentalDomain(in, s, maxCount);
            if(res == VecTransform.RESULT_OK)
                printf("(%5.2f,%5.2f,%5.2f) u:(%5.2f,%5.2f,%5.2f)\n", x,y,z, in.x,in.y,in.z);
            else 
                printf("(%5.2f,%5.2f,%5.2f) u:(%5.2f,%5.2f,%5.2f) error\n", x,y,z, in.x,in.y,in.z);
        }

    }


    public void _testImageMipMap(){
        
        try {
            
            String fileName = "apps/ringpopper/images/tile_02.png";
            BufferedImage image = ImageIO.read(new File(fileName));
            ImageMipMap mm = new ImageMipMap(image, 1);
            
            double color[] = new double[4];
            
            for(int i = 0; i < 50; i++){
                double size = 10;
                mm.getPixel(i*20, 300, size, color);
                //mm.printData(0, 0, 50);
                //mm.getPixel(1500+1*i, 1400, 0, color);
                printf("%5.1f: (%8.1f,%8.1f,%8.1f,%8.1f)\n",size, color[0],color[1],color[2],color[3]);
            }
        } catch (Exception e){
            
            e.printStackTrace();
        }    
    }
    

    /**
       
     */
    void writeIsosurface(Grid grid, double bounds[], double voxelSize, int smoothSteps, String fpath){

        printf("writeIsosurface(%s)\n",fpath);

        IsosurfaceMaker im = new IsosurfaceMaker();

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();

        im.setIsovalue(0.);
        im.setBounds(MathUtil.extendBounds(bounds, -voxelSize/2));
        im.setGridSize(nx, ny, nz);

        IsosurfaceMaker.SliceGrid fdata = new IsosurfaceMaker.SliceGrid(grid, bounds, smoothSteps);
        
        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
       generates isosurface at half resolution 
       neigboursCount can be 0, 6, 18, 26
     */
    void writeIsosurface2(Grid grid, double bounds[], double voxelSize, int resamplingFactor, String fpath){

        printf("writeIsosurface2(%s)\n",fpath);

        IsosurfaceMaker im = new IsosurfaceMaker();

        int nx = grid.getWidth();
        int ny = grid.getHeight();
        int nz = grid.getDepth();
        double dx2 = resamplingFactor*(bounds[1] - bounds[0])/nx;
        double dy2 = resamplingFactor*(bounds[3] - bounds[2])/ny;
        double dz2 = resamplingFactor*(bounds[5] - bounds[4])/nz;
        printf("dx2:[%7.3f,%7.3f,%7.3f]mm\n",dx2/MM,dy2/MM,dz2/MM);
        
        int nx2 = (nx+resamplingFactor-1)/resamplingFactor;
        int ny2 = (ny+resamplingFactor-1)/resamplingFactor;
        int nz2 = (nz+resamplingFactor-1)/resamplingFactor;
        printf("nx2:[%d,%d,%d]\n",nx2, ny2, nz2);

        double bounds2[] = new double[]{
            bounds[0], bounds[0] + dx2*nx2, 
            bounds[2], bounds[2] + dy2*ny2, 
            bounds[4], bounds[4] + dz2*nz2
        };
        
        printf("bounds:[%7.3f,%7.3f,%7.3f,%7.3f,%7.3f,%7.3f]mm\n",bounds[0]/MM,bounds[1]/MM,bounds[2]/MM,bounds[3]/MM,bounds[4]/MM,bounds[5]/MM);
        printf("bounds2:[%7.3f,%7.3f,%7.3f,%7.3f,%7.3f,%7.3f]mm\n",bounds2[0]/MM,bounds2[1]/MM,bounds2[2]/MM,bounds2[3]/MM,bounds2[4]/MM,bounds2[5]/MM);
        im.setIsovalue(0.);
        im.setBounds(MathUtil.extendBounds(bounds2, -voxelSize/2));
        im.setGridSize(nx2, ny2, nz2);

        IsosurfaceMaker.SliceGrid2 fdata = new IsosurfaceMaker.SliceGrid2(grid, bounds, resamplingFactor, 2.9);
        
        try {
            STLWriter stlwriter = new STLWriter(fpath);
            im.makeIsosurface(fdata, stlwriter);
            stlwriter.close();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
