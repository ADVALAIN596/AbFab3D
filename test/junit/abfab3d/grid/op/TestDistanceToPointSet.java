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

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;


import abfab3d.grid.AttributeGrid;
import abfab3d.grid.ArrayAttributeGridShort;

import abfab3d.geom.PointCloud;

import static java.lang.Math.round;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static abfab3d.util.MathUtil.L2S;

/**
 * Test the DistanceToPointSet class.
 *
 * @author Vladimir Bulatov
 */
public class TestDistanceToPointSet extends TestCase {

    private static final boolean DEBUG = true;

    int subvoxelResolution = 100;


    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestDistanceToPointSet.class);
    }

    public void testPoints(){

        double vs = 0.005*MM;
        double x0 = -1*MM,y0 = -1*MM, z0 = -1*MM;
        double x1 = 1*MM,y1 = 1*MM, z1 = 1*MM;

        int nx = (int)((x1-x0)/vs), ny = (int)((y1-y0)/vs), nz = (int)((z1-z0)/vs); 
        // recalculate bounds to voxels boundary 
        x1 = x0 + nx*vs;
        y1 = y0 + ny*vs;
        z1 = z0 + nz*vs;
        printf("grid size: [%d x %d x %d]\n",nx, ny, nz);

        PointCloud pnts = new PointCloud(1);

        // center of a voxel near grid center 
        double xc = x0 + ((nx/2) + 0.5)*vs;
        double yc = y0 + ((ny/2) + 0.5)*vs;
        double zc = z0 + ((nz/2) + 0.5)*vs;

        int iterCount = 1000;
        for(int k = 0; k < iterCount; k++){
            pnts.addPoint(xc+2*vs, yc+vs, zc);
            pnts.addPoint(xc-3*vs, yc+vs, zc);
            pnts.addPoint(xc+2*vs, yc-2*vs, zc);
            pnts.addPoint(xc-3*vs, yc-2*vs, zc);
        } 
        if(DEBUG) printf("points count: %d\n", pnts.size());
        
        DistanceToPointSet dps = new DistanceToPointSet(pnts, 0, 30*vs, subvoxelResolution);
        //dps.setAlgorithm(DistanceToPointSet.ALG_EXACT);
        dps.setAlgorithm(DistanceToPointSet.ALG_LAYERED);
        AttributeGrid grid = new ArrayAttributeGridShort(nx, ny, nz, vs, vs);        
        grid.setGridBounds(new double[]{x0,x1, y0, y1, z0, z1});
        long t0 = time();
        dps.execute(grid);
        printf("DistanceToPointSet done %d ms\n", time() - t0);
        if(false){
            // print slices 
            printSlice(grid, nz/2-1);
            printSlice(grid, nz/2);
            printSlice(grid, nz/2+1);
        }
    }

    static void printSlice(AttributeGrid grid, int z){
        int 
            nx = grid.getWidth(), 
            ny = grid.getHeight(),
            nz = grid.getDepth();
        printf("grid:[ %d x %d x %d] slice %d\n",nx,ny,nz,z);

        for(int y = 0; y < ny; y++){
            for(int x = 0; x < nx; x++){
                int d = L2S(grid.getAttribute(x,y,z));
                switch(d){
                case Short.MAX_VALUE: printf("    +"); break;
                case -Short.MAX_VALUE: printf("    -"); break;
                default:printf("%5d", d); break;
                }
            }
            printf("\n");
        }
    }

    public static void main(String arg[]){

        new TestDistanceToPointSet().testPoints();
        
    }

}
