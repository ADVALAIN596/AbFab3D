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
 * A cutting plane tool for grids.  All voxels above the plane will be
 * set to EMPTY.
 *
 * Any INTERIOR voxels on the plane will be turned into EXTERIOR voxels.
 *
 * Place planes on the middle of a voxel for best results.
 *
 * @author Alan Hudson
 */
public class CuttingPlane implements Operation, AttributeOperation {
    /** The axis of the cutting plane */
    private Axis axis;

    /** The planar location */
    private double loc;

    /** The direction to cut.  1 = UP, -1 = DOWN or -1 = LEFT, 1 = RIGHT */
    private int dir;

    /** The material for new exterior voxels */
    private int material;

    public CuttingPlane(Axis axis, double loc, int dir, int material) {
        this.axis = axis;
        this.loc = loc;
        this.dir = dir;
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use
     * @return The new grid
     */
    public Grid execute(Grid dest) {
        int width = dest.getWidth();
        int depth = dest.getDepth();
        int height = dest.getHeight();

        if (axis == Axis.X) {
            if (dir == 1) {
                // RIGHT = +X direction, loc is Z coordinate

                // Convert location to grid coordinates
                int[] coords = new int[3];
                dest.getGridCoords(0, 0, loc, coords);

                // Mark all voxels above plane as OUTSIDE
                for(int k=coords[2]+1; k < depth; k++) {
                    for(int i=0; i < width; i++) {
                        for(int j=0; j < height; j++) {
                            dest.setState(i, j, k, Grid.OUTSIDE);
                        }
                    }
                }

                // Mark any INTERIOR voxels on the plane as EXTERIOR
                for(int k=coords[2]+1; k < depth; k++) {
                    for(int i=0; i < width; i++) {
                        for(int j=0; j < height; j++) {
                            if (dest.getState(i,j,k) == Grid.INTERIOR) {
                                dest.setState(i,j,k,Grid.EXTERIOR);
                            }
                        }
                    }
                }
            }
        }

        return dest;
    }
    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param dest The grid to use
     * @return The new grid
     */
    public AttributeGrid execute(AttributeGrid dest) {
        int width = dest.getWidth();
        int depth = dest.getDepth();
        int height = dest.getHeight();

        if (axis == Axis.X) {
            if (dir == 1) {
                // RIGHT = +X direction, loc is Z coordinate

                // Convert location to grid coordinates
                int[] coords = new int[3];
                dest.getGridCoords(0, 0, loc, coords);

                // Mark all voxels above plane as OUTSIDE
                for(int k=coords[2]+1; k < depth; k++) {
                    for(int i=0; i < width; i++) {
                        for(int j=0; j < height; j++) {
                            dest.setData(i, j, k, Grid.OUTSIDE, 0);
                        }
                    }
                }

                // Mark any INTERIOR voxels on the plane as EXTERIOR
                for(int k=coords[2]+1; k < depth; k++) {
                    for(int i=0; i < width; i++) {
                        for(int j=0; j < height; j++) {
                            if (dest.getState(i,j,k) == Grid.INTERIOR) {
                                dest.setData(i,j,k,Grid.EXTERIOR,material);
                            }
                        }
                    }
                }
            }
        }

        return dest;
    }
}
