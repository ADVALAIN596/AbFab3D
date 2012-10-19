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

package ringpopper;

// External Imports
import java.io.*;
import java.util.*;
import javax.imageio.*;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.awt.image.BufferedImage;

import abfab3d.grid.query.RegionFinder;
import abfab3d.io.output.*;
import abfab3d.mesh.IndexedTriangleSetBuilder;
import abfab3d.mesh.LaplasianSmooth;
import abfab3d.mesh.MeshDecimator;
import abfab3d.mesh.WingedEdgeTriangleMesh;
import org.j3d.geom.GeometryData;
import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.sav.BinaryContentHandler;

import abfab3d.geom.*;
import abfab3d.grid.*;
import abfab3d.grid.op.*;
import abfab3d.creator.*;
import abfab3d.creator.shapeways.*;

//import java.awt.*;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import static abfab3d.util.MathUtil.TORAD;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.printf;
import static java.lang.System.currentTimeMillis;

import app.common.GridSaver;

/**
 * Geometry Kernel for the RingPopper
 *
 * @author Alan Hudson
 */
public class RingPopperKernel extends HostedKernel {
    /** Debugging level.  0-5.  0 is none */
    private static final int DEBUG_LEVEL = 0;

    enum BandStyle {NONE, TOP, BOTTOM, BOTH};

    private String[] availableMaterials = new String[] {"White Strong & Flexible", "White Strong & Flexible Polished",
        "Silver", "Silver Glossy", "Stainless Steel","Gold Plated Matte", "Gold Plated Glossy","Antique Bronze Matte",
        "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    // Physical Sizing
    private double innerDiameter;
    private double ringThickness;
    private double ringWidth;

    /** Percentage of the ringThickness to use as a base */
    private double baseThickness;

    /** The horizontal and vertical resolution */
    private double resolution;

    /** The image filename */
    private String image;

    private int tilingX;
    private int tilingY;
    private BandStyle bandStyle;

    private String material;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String,Parameter> getParams() {
        HashMap<String,Parameter> params = new HashMap<String,Parameter>();

        int seq = 0;
        int step = 0;

        // Image based params
        params.put("image", new Parameter("image", "Image", "The image to use", "images/Tile_dilate8_unedged.png", 1,
            Parameter.DataType.STRING, Parameter.EditorType.FILE_DIALOG,
            step, seq++, false, 0, 0.1, null, null)
        );

        params.put("tilingX", new Parameter("tilingX", "Tiling X", "The tiling along left/right of the ring", "1", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("tilingY", new Parameter("tilingY", "Tiling Y", "The tiling along up/down of the ring", "1", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("bandStyle", new Parameter("bandStyle", "Band Style", "Whether to put lines on the band", bandStyle.NONE.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(bandStyle.values()))
        );

        step++;
        seq = 0;

        // Size based params
        params.put("innerDiameter", new Parameter("innerDiameter", "Inner Diameter", "The inner diameter", "0.02118", 1,
            Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
            step, seq++, false, 0, 1, null, null)
        );

        params.put("ringWidth", new Parameter("ringWidth", "Ring Width", "The ring width(Finger Length)", "0.005", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("ringThickness", new Parameter("ringThickness", "Ring Thickness", "The thickness of the ring", "0.001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        params.put("baseThickness", new Parameter("baseThickness", "Base Thickness", "The thickness percent of the ring base", "0.4", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 1, null, null)
        );

        step++;
        seq = 0;

        params.put("material", new Parameter("material", "Material", "What material to design for", "Silver Glossy", 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, availableMaterials)
        );


        step++;
        seq = 0;

        // Advanced Params
        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.00006", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        return params;
    }

    /**
     * @param params The parameters
     * @param acc The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String,Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {

        long start = System.currentTimeMillis();

        pullParams(params);

        //resolution = 5.e-5;
        //double voxelSize = 5.e-5;
        double EPS = 1.e-8; // to distort exact symmetry, which confuses meshlab
        double margin = 4*resolution;

        double gridWidth = innerDiameter + 2*Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridHeight  = Math.hypot(ringThickness,ringWidth) + 2*margin;
        double gridDepth = gridWidth;

        double bounds[] = new double[]{-gridWidth/2,gridWidth/2+EPS,-gridHeight/2,gridHeight/2+EPS,-gridDepth/2,gridDepth/2+EPS};

        int nx = (int)((bounds[1] - bounds[0])/resolution);
        int ny = (int)((bounds[3] - bounds[2])/resolution);
        int nz = (int)((bounds[5] - bounds[4])/resolution);
        printf("grid: [%d x %d x %d]\n", nx, ny, nz);

        DataSources.ImageBitmap image_src = new DataSources.ImageBitmap();

        image_src.m_sizeX = innerDiameter*Math.PI;
        image_src.m_sizeY = ringWidth;
        image_src.m_sizeZ = ringThickness;
        image_src.m_centerZ = ringThickness/2;
        image_src.m_baseThickness = baseThickness;
        image_src.m_xTilesCount = tilingX;
        image_src.m_yTilesCount = tilingY;
        image_src.m_imagePath = image;

        VecTransforms.CompositeTransform compTrans = new VecTransforms.CompositeTransform();

        VecTransforms.RingWrap rw = new VecTransforms.RingWrap();
        rw.m_radius = innerDiameter/2;

        VecTransforms.Rotation rot = new VecTransforms.Rotation();
        rot.m_axis = new Vector3d(0,0,1);
        rot.m_angle = 0*TORAD;

        compTrans.add(rot);
        compTrans.add(rw);

        GridMaker gm = new GridMaker();

        gm.setBounds(bounds);
        gm.setTransform(compTrans);
        gm.setDataSource(image_src);

        Grid grid = new ArrayAttributeGridByte(nx, ny, nz, resolution, resolution);

        printf("gm.makeGrid()\n");
        gm.makeGrid(grid);
        printf("gm.makeGrid() done\n");


/*
        System.out.println("Finding Regions: ");
        // Remove all but the largest region
        RegionFinder finder = new RegionFinder();
        List<Region> regions = finder.execute(grid);
        Region largest = regions.get(0);

        System.out.println("Regions: " + regions.size());
        for(Region r : regions) {
            if (r.getVolume() > largest.getVolume()) {
                largest = r;
            }
            //System.out.println("Region: " + r.getVolume());
        }

        System.out.println("Largest Region: " + largest);
        RegionClearer clearer = new RegionClearer(grid);
        System.out.println("Clearing regions: ");
        for(Region r : regions) {
            if (r != largest) {
                //System.out.println("   Region: " + r.getVolume());
                r.traverse(clearer);
            }
        }
*/
/*
if (1==1) {
        BufferedImage image = createImage(bodyWidthPixels, bodyHeightPixels, "AbFab3D", "Pump Demi Bold LET", Font.BOLD, -1);

        Operation op = new ApplyImage(image,0,0,0,bodyImageWidthPixels, bodyImageHeightPixels, threshold, invert, bodyImageDepth, removeStray, mat);
        op.execute(grid);

        op = new Union(grid, 0, 0, 0, 1);

        op.execute(grid);
}
*/

        System.out.println("Writing grid");

        GridSaver.writeIsosurfaceMakerSTL("out.stl", grid,3, 1e-9);

        double[] min_bounds = new double[3];
        double[] max_bounds = new double[3];
        grid.getWorldCoords(0,0,0, min_bounds);
        grid.getWorldCoords(grid.getWidth() - 1, grid.getHeight() - 1, grid.getDepth() - 1, max_bounds);


        System.out.println("Total Time: " + (System.currentTimeMillis() - start));
        System.out.println("-------------------------------------------------");
        return new KernelResults(true, min_bounds, max_bounds);
    }

    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String,Object> params) {
        String pname = null;

        try {
            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "innerDiameter";
            innerDiameter = ((Double) params.get(pname)).doubleValue();

            pname = "ringThickness";
            ringThickness = ((Double) params.get(pname)).doubleValue();

            pname = "ringWidth";
            ringWidth = ((Double) params.get(pname)).doubleValue();

            pname = "baseThickness";
            baseThickness = ((Double) params.get(pname)).doubleValue();

            pname = "image";
            image = (String) params.get(pname);

            pname = "tilingX";
            tilingX = ((Integer) params.get(pname)).intValue();

            pname = "tilingY";
            tilingY = ((Integer) params.get(pname)).intValue();

            pname = "bandStyle";
            bandStyle = BandStyle.valueOf((String) params.get(pname));

            pname = "material";
            material = ((String) params.get(pname));

        } catch(Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }

    /**
     return bounds extended by given margin
     */
    static double[] extendBounds(double bounds[], double margin){
        return new double[]{
                bounds[0] - margin,
                bounds[1] + margin,
                bounds[2] - margin,
                bounds[3] + margin,
                bounds[4] - margin,
                bounds[5] + margin,
        };
    }

    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console,true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INTERIOR), new float[] {1,0,0});
        colors.put(new Integer(Grid.EXTERIOR), new float[]{0, 1, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[] {0,0,1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INTERIOR), new Float(0));
        transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static <T extends Enum<T>> String[] enumToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value: values) {
            result[i++] = value.name();
        }
        return result;
    }
}

class RegionClearer implements RegionTraverser {
    private Grid grid;

    public RegionClearer(Grid grid) {
        this.grid = grid;
    }
    @Override
    public void found(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);
    }

    @Override
    public boolean foundInterruptible(int x, int y, int z) {
        grid.setState(x,y,z,Grid.OUTSIDE);

        return true;
    }
}
