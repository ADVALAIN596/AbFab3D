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

package booleanops;

// External Imports
import java.util.*;
import java.io.*;
import org.web3d.vrml.sav.ContentHandler;
import org.web3d.vrml.export.*;
import org.web3d.util.ErrorReporter;
import org.j3d.geom.*;

// Internal Imports
import abfab3d.geom.*;
import abfab3d.geom.CubeCreator.Style;
import abfab3d.grid.*;
import abfab3d.grid.op.Subtract;


/**
 * Linked Cubes.
 *
 * Would be nice to support this style as well:
 *   http://www.shapeways.com/model/130911/cubichain_22cm_6mm.html?mode=3d
 *
 *   I suspect its done via solid modeling.  Not sure about the inner edges
 *   not being straight though
 *
 * @author Alan Hudson
 */
public class BooleanOps {
    public static final double HORIZ_RESOLUTION = 0.001;

    /** Verticle resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.001;

    public void generate(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ErrorReporter console = new PlainTextErrorReporter();

            X3DBinaryRetainedDirectExporter writer = new X3DBinaryRetainedDirectExporter(fos,
                                                         3, 0, console,
                                                         X3DBinarySerializer.METHOD_SMALLEST_NONLOSSY,
                                                         0.001f);

            float bsize = 0.04f;
            BoxGenerator tg = new BoxGenerator(bsize,bsize,bsize);
            GeometryData geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            tg.generate(geom);

            double bounds = findMaxBounds(geom);
            double size = 2.4 * bounds;  // Slightly over allocate

System.out.println("bounds: " + bounds + " size: " + size);
            Grid grid = new SliceGrid(size,size,size,
                HORIZ_RESOLUTION, VERT_RESOLUTION, false);

            TriangleModelCreator tmc = null;
            double x = bounds;
            double y = x;
            double z = x;

            double rx = 0,ry = 1,rz = 0,rangle = 0;
            byte outerMaterial = 1;
            byte innerMaterial = 1;


            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            //tmc.generate(grid);

//            double height = bsize * 1.1;
//            double radius = bsize * 0.5;
            double height = 0.03;
            double radius = 0.03;
            int facets = 64;

            CylinderGenerator cg = new CylinderGenerator((float)height, (float)radius, facets);
            geom = new GeometryData();
            geom.geometryType = GeometryData.TRIANGLES;
            cg.generate(geom);

            double[] trans =  new double[3];
            double[] maxsize = new double[3];

            findGridParams(geom, HORIZ_RESOLUTION, VERT_RESOLUTION, trans, maxsize);
            x = trans[0];
            y = trans[1];
            z = trans[2];

System.out.println("trans: " + java.util.Arrays.toString(trans));
System.out.println("size: " + java.util.Arrays.toString(maxsize));
System.out.flush();
try { Thread.sleep(50); } catch(Exception e) {}

            grid = new SliceGrid(maxsize[0],maxsize[1],maxsize[2],
                HORIZ_RESOLUTION, VERT_RESOLUTION, false);


            tmc = new TriangleModelCreator(geom,x,y,z,
                rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

            tmc.generate(grid);

/*
            Grid grid2 = new SliceGrid(size,size,size,
                HORIZ_RESOLUTION, VERT_RESOLUTION, false);


            Subtract op = new Subtract(grid2, 0, 0, 0, (byte) 1);
            op.execute(grid);
*/

            writer.startDocument("","", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[] {0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo
            writer.startNode("Viewpoint", null);
            writer.startField("position");
            writer.fieldValue(new float[] {0.028791402f,0.005181627f,0.11549001f},3);
            writer.startField("orientation");
            writer.fieldValue(new float[] {-0.06263941f,0.78336f,0.61840385f,0.31619227f},4);
            writer.endNode(); // Viewpoint

/*
            grid.toX3D(writer, null);
*/
            HashMap<Byte, float[]> colors = new HashMap<Byte, float[]>();
            colors.put(Grid.INTERIOR, new float[] {0,1,0});
            colors.put(Grid.EXTERIOR, new float[] {1,0,0});
            colors.put(Grid.OUTSIDE, new float[] {0,0,1});

            HashMap<Byte, Float> transparency = new HashMap<Byte, Float>();
            transparency.put(Grid.INTERIOR, new Float(0));
            transparency.put(Grid.EXTERIOR, new Float(0.5));
            transparency.put(Grid.OUTSIDE, new Float(0.98));


            grid.toX3DDebug(writer, colors, transparency);

            writer.endDocument();

            fos.close();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Find the params needed to place a model in the grid.
     *
     * @param geom The geometry
     * @param horiz The horiz voxel size
     * @param vert The vertical voxel size
     * @param trans The translation to use.  Preallocate to 3.
     * @param size The minimum size the grid needs to be
     */
    private void findGridParams(GeometryData geom, double horiz, double vert, double[] trans, double[] size) {
        double[] min = new double[3];
        double[] max = new double[3];

        min[0] = Double.POSITIVE_INFINITY;
        min[1] = Double.POSITIVE_INFINITY;
        min[2] = Double.POSITIVE_INFINITY;
        max[0] = Double.NEGATIVE_INFINITY;
        max[1] = Double.NEGATIVE_INFINITY;
        max[2] = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length / 3;
        int idx = 0;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[idx] > max[0]) {
                max[0] = geom.coordinates[idx];
            }
            if (geom.coordinates[idx] < min[0]) {
                min[0] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[1]) {
                max[1] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[1]) {
                min[1] = geom.coordinates[idx];
            }

            idx++;

            if (geom.coordinates[idx] > max[2]) {
                max[2] = geom.coordinates[idx];
            }

            if (geom.coordinates[idx] < min[2]) {
                min[2] = geom.coordinates[idx];
            }

            idx++;
        }

        // Leave one ring of voxels around the item

        int numVoxels = 1;

        size[0] = (max[0] - min[0]) + (numVoxels * 2 * horiz);
        size[1] = (max[1] - min[1]) + (numVoxels * 2 * vert);
        size[2] = (max[2] - min[2]) + (numVoxels * 2 * horiz);

        trans[0] = -min[0] + numVoxels * horiz;
        trans[1] = -min[1] + numVoxels * vert;
        trans[2] = -min[2] + numVoxels * horiz;
    }

    /**
     * Find the absolute maximum bounds of a geometry.
     *
     * @return The max
     */
    private double findMaxBounds(GeometryData geom) {
        double max = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[i] > max) {
                max = geom.coordinates[i];
            }
        }

        return Math.abs(max);
    }

    public static void main(String[] args) {
        BooleanOps c = new BooleanOps();
        c.generate("out.x3db");
    }
}