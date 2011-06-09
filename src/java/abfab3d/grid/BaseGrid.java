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

package abfab3d.grid;

// External Imports
import java.util.*;
import java.io.*;

/**
 * Base class implementation of Grids.  Includes common code that
 * may get overwritten by faster implementations.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public abstract class BaseGrid implements Grid {
    protected int width;
    protected int height;
    protected int depth;
    protected double pixelSize;
    protected double hpixelSize;
    protected double sheight;
    protected double hsheight;
    protected int sliceSize;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(double w, double h, double d, double pixel, double sheight) {
        this((int) (w / pixel) + 1, (int) (h / sheight) + 1,
           (int) (d / pixel) + 1, pixel, sheight);
    }

    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseGrid(int w, int h, int d, double pixel, double sheight) {
        width = w;
        height = h;
        depth = d;
        this.pixelSize = pixel;
        this.hpixelSize = pixelSize / 2.0;
        this.sheight = sheight;
        this.hsheight = sheight / 2.0;

        sliceSize = w * d;
    }

    /**
     * Get an iterator for voxel state.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param vc The voxel state
     * @return The voxels matching the state specified
     */
    public Iterator<Voxel> getStateIterator(VoxelClasses vc) {
        return new StateIterator(vc);
    }

    /**
     * Get an iterator for materialID.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param mat The materialID
     * @return The voxels matching the materialID
     */
    public Iterator<Voxel> getMaterialIterator(byte mat) {
        return new MaterialIterator(mat);
    }

    /**
     * Get an iterator for state and materialID.  The returned object
     * Voxel may be reused so clone if you to keep it.  For speed
     * this iterator does not check for comodification, don't do
     * that.
     *
     * @param vc The voxel class
     * @param mat The materialID
     * @return The voxels that are the same state and materialID
     */
    public Iterator<Voxel> getIterator(VoxelClasses vc, byte mat) {
        return new StateMaterialIterator(vc,mat);
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public int findCount(VoxelClasses vc) {
        int ret_val = 0;

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = getData(x,y,z);

                    byte state;

                    switch(vc) {
                        case ALL:
                            ret_val++;
                            break;
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                ret_val++;
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                ret_val++;
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                ret_val++;
                            }
                            break;
                        case OUTSIDE:
                            state = vd.getState();
                            if (state == Grid.OUTSIDE) {
                                ret_val++;
                            }
                            break;
                    }
                }
            }
        }

        return ret_val;
    }

    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(byte mat) {
        int ret_val = 0;

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    VoxelData vd = getData(x,y,z);

                    byte state;

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        ret_val++;
                    }
                }
            }
        }

        return ret_val;
    }

    /**
     * Get the grid coordinates for a world coordinate.
     *
     * @param x The x value in world coords
     * @param y The y value in world coords
     * @param z The z value in world coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getGridCoords(double x, double y, double z, int[] coords) {
        coords[0] = (int) (x / pixelSize);
        coords[1] = (int) (y / sheight);
        coords[2] = (int) (z / pixelSize);
    }

    /**
     * Get the world coordinates for a grid coordinate.
     *
     * @param x The x value in grid coords
     * @param y The y value in grid coords
     * @param z The z value in grid coords
     * @param coords The ans is placed into this preallocated array(3).
     */
    public void getWorldCoords(int x, int y, int z, double[] coords) {
        coords[0] = x * pixelSize + hpixelSize;
        coords[1] = y * sheight + hsheight;
        coords[2] = z * pixelSize + hpixelSize;
    }

    /**
     * Get the grid bounds in world coordinates.
     *
     * @param min The min coordinate
     * @param max The max coordinate
     */
    public void getGridBounds(double[] min, double[] max) {
        min[0] = 0;
        min[1] = 0;
        min[2] = 0;

        max[0] = width * pixelSize;
        max[1] = height * sheight;
        max[2] = depth * pixelSize;
    }

    /**
     * Get the number of height cells.
     *
     * @return the val
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the number of width cells.
     *
     * @return the val
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the number of depth cells.
     *
     * @return the val
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Get the slice height.
     *
     * @return The value
     */
    public double getSliceHeight() {
        return sheight;
    }

    /**
     * Get the number of dots per meter.
     *
     * @return The value
     */
    public double getVoxelSize() {
        return pixelSize;
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                sb.append(getState(i,y,j));
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public String toStringAll() {
        StringBuilder sb = new StringBuilder();

        sb.append("Grid:  height: ");
        sb.append(height);
        sb.append("\n");

        for(int i=0; i < height; i++) {
            sb.append(i);
            sb.append(":\n");
            sb.append(toStringSlice(i));
        }

        return sb.toString();
    }

    private class StateIterator implements Iterator<Voxel> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursorX = 0;
        int cursorY = 0;
        int cursorZ = 0;

        VoxelClasses vc;
        Voxel voxel;

        public StateIterator(VoxelClasses vc) {
            this.vc = vc;
        }

        public boolean hasNext() {
            byte state;

//System.out.println("hasNext: " + cursorX + " " + cursorY + " " + cursorZ);
            for(int y=cursorY; y < height; y++) {
                for(int x=cursorX; x < width; x++) {
                    for(int z=cursorZ; z < depth; z++) {
                        VoxelData vd = getData(x,y,z);

//System.out.println("x: " + x + " y: " + y + " z: " + z);
                        switch(vc) {
                            case ALL:
                                cursorX = x;
                                cursorY = y;
                                cursorZ = z + 1;

                                voxel = new Voxel(x,y,z,vd.getState(), vd.getMaterial());
                                return true;
                            case MARKED:
                                state = vd.getState();
                                if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                    cursorX = x;
                                    cursorY = y;
                                    cursorZ = z + 1;

                                    voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                    return true;
                                }
                                break;
                            case EXTERIOR:
                                state = vd.getState();
                                if (state == Grid.EXTERIOR) {
                                    cursorX = x;
                                    cursorY = y;
                                    cursorZ = z + 1;

                                    voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                    return true;
                                }
                                break;
                            case INTERIOR:
                                state = vd.getState();
                                if (state == Grid.INTERIOR) {
                                    cursorX = x;
                                    cursorY = y;
                                    cursorZ = z + 1;

                                    voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                    return true;
                                }
                                break;
                            case OUTSIDE:
                                state = vd.getState();
                                if (state == Grid.OUTSIDE) {
                                    cursorX = x;
                                    cursorY = y;
                                    cursorZ = z + 1;

                                    voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                    return true;
                                }
                                break;
                        }
                    }
                }
            }

            voxel = null;
            return false;
        }

        public Voxel next() {
            if (voxel == null) {
                throw new NoSuchElementException();
            }

            return voxel;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class MaterialIterator implements Iterator<Voxel> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursorX = 0;
        int cursorY = 0;
        int cursorZ = 0;

        byte mat;
        Voxel voxel;

        public MaterialIterator(byte mat) {
            this.mat = mat;
        }

        public boolean hasNext() {
            for(int y=cursorY; y < height; y++) {
                for(int x=cursorX; x < width; x++) {
                    for(int z=cursorZ; z < depth; z++) {
                        VoxelData vd = getData(x,y,z);

                        if (vd.getMaterial() == mat) {
                            cursorX = x;
                            cursorY = y;
                            cursorZ = z + 1;

                            voxel = new Voxel(x,y,z,vd.getState(), vd.getMaterial());
                            return true;
                        }
                    }
                }
            }

            voxel = null;
            return false;
        }

        public Voxel next() {
            if (voxel == null) {
                throw new NoSuchElementException();
            }

            return voxel;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class StateMaterialIterator implements Iterator<Voxel> {
        /**
         * Index of element to be returned by subsequent call to next.
         */
        int cursorX = 0;
        int cursorY = 0;
        int cursorZ = 0;

        VoxelClasses vc;
        byte mat;
        Voxel voxel;

        public StateMaterialIterator(VoxelClasses vc, byte mat) {
            this.vc = vc;
            this.mat = mat;
        }

        public boolean hasNext() {
            byte state;

            for(int y=cursorY; y < height; y++) {
                for(int x=cursorX; x < width; x++) {
                    for(int z=cursorZ; z < depth; z++) {
                        VoxelData vd = getData(x,y,z);

                        switch(vc) {
                            case ALL:
                                if (vd.getMaterial() == mat) {
                                    cursorX = x;
                                    cursorY = y;
                                    cursorZ = z + 1;

                                    voxel = new Voxel(x,y,z,vd.getState(), vd.getMaterial());
                                    return true;
                                }
                                break;
                            case MARKED:
                                state = vd.getState();
                                if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                    if (vd.getMaterial() == mat) {
                                        cursorX = x;
                                        cursorY = y;
                                        cursorZ = z + 1;

                                        voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                        return true;
                                    }
                                }
                                break;
                            case EXTERIOR:
                                state = vd.getState();
                                if (state == Grid.EXTERIOR) {
                                    if (vd.getMaterial() == mat) {
                                        cursorX = x;
                                        cursorY = y;
                                        cursorZ = z + 1;

                                        voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                        return true;
                                    }
                                }
                                break;
                            case INTERIOR:
                                state = vd.getState();
                                if (state == Grid.INTERIOR) {
                                    if (vd.getMaterial() == mat) {
                                        cursorX = x;
                                        cursorY = y;
                                        cursorZ = z + 1;

                                        voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                        return true;
                                    }
                                }
                                break;
                            case OUTSIDE:
                                state = vd.getState();
                                if (state == Grid.OUTSIDE) {
                                    if (vd.getMaterial() == mat) {
                                        cursorX = x;
                                        cursorY = y;
                                        cursorZ = z + 1;

                                        voxel = new Voxel(x,y,z,state, vd.getMaterial());
                                        return true;
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            voxel = null;
            return false;
        }

        public Voxel next() {
            if (voxel == null) {
                throw new NoSuchElementException();
            }

            return voxel;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

