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

// Internal Imports
import abfab3d.grid.*;

/**
 * Copy a grid to another grid.  Assumes the grid fits into the
 * destination.
 *
 * TODO: Untested
 *
 * @author Alan Hudson
 */
public class Copy implements Operation {
    /** The x location */
    private int x;

    /** The y location */
    private int y;

    /** The z location */
    private int z;

    /** The dest grid */
    private Grid dest;

    public Copy(Grid dest, int x, int y, int z) {
        this.dest = dest;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid src
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        int width = grid.getWidth();
        int depth = grid.getDepth();
        int height = grid.getHeight();

        int state;

        int origin_x = x;
        int origin_y = y;
        int origin_z = z;
        VoxelData vd;

        // TODO: Optimize using copy constructors for same types
        // TODO: Optimize using iterator for MARKED only copies
        // TODO: Add a param for wether to copy outside.  Really just a union then

        for(int x=0; x < width; x++) {
            for(int y=0; y < height; y++) {
                for(int z=0; z < depth; z++) {
                    vd = grid.getData(x,y,z);

                    if (vd.getState() != Grid.OUTSIDE) {
                        // TODO: really only works on empty
                        dest.setData(origin_x + x, origin_y + y, origin_z + z,
                            vd.getState(), vd.getMaterial());
                    }
                }
            }
        }

        return grid;
    }
}
