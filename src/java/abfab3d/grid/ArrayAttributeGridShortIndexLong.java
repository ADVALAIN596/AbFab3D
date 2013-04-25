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

/**
 * A grid backed by arrays.
 *
 * Likely better performance for memory access that is not slice aligned.
 *
 * Uses the X3D coordinate system.  Y-up.  Grid is located
 * on positive right side octant.
 *
 * @author Alan Hudson
 */
public class ArrayAttributeGridShortIndexLong extends BaseAttributeGrid {
    protected short[][][] data;

    /**
     * Constructor.
     *
     * @param w The width in world coords
     * @param h The height in world coords
     * @param d The depth in world coords
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public ArrayAttributeGridShortIndexLong(double w, double h, double d, double pixel, double sheight) {
        this((int) (Math.ceil(w / pixel)) + 1,
        	 (int) (Math.ceil(h / sheight)) + 1,
             (int) (Math.ceil(d / pixel)) + 1, 
             pixel, 
             sheight);
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
    public ArrayAttributeGridShortIndexLong(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);

        // TODO: Align memory with slice access patterns.  Need to verify
        data = new short[height][width][depth];
    }

    /**
     * Copy Constructor.
     *
     * @param grid The grid
     */
    public ArrayAttributeGridShortIndexLong(ArrayAttributeGridShortIndexLong grid) {
        super(grid.getWidth(), grid.getHeight(), grid.getDepth(),
            grid.getVoxelSize(), grid.getSliceHeight());
        this.data = grid.data.clone();
    }

    /**
     * Create an empty grid of the specified size.  Reuses
     * the grid type and material type(byte, short, int).
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public Grid createEmpty(int w, int h, int d, double pixel, double sheight) {
        Grid ret_val = new ArrayAttributeGridShortIndexLong(w,h,d,pixel,sheight);

        return ret_val;
    }

    /**
     * Get a new instance of voxel data.  Returns this grids specific sized voxel data.
     *
     * @return The voxel data
     */
    public VoxelData getVoxelData() {
        return new VoxelDataShort();
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     */
    public void getData(int x, int y, int z, VoxelData vd) {
        short datum = data[y][x][z];
        byte state = (byte) ((datum & 0xFFFF) >> 14);
        short mat = (short) (0x3FFF & datum);

        vd.setData(state,mat);
    }

    /**
     * Get the data of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public void getData(double x, double y, double z, VoxelData vd) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        short datum = data[slice][s_x][s_z];

        byte state = (byte) ((datum & 0xFFFF) >> 14);
        short mat = (short) (0x3FFF & datum);

        vd.setData(state,mat);
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        short datum = data[slice][s_x][s_z];

        byte state = (byte) ((datum & 0xFFFF) >> 14);

        return state;
    }

    /**
     * Get the state of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public byte getState(int x, int y, int z) {
        short datum = data[y][x][z];

        byte state = (byte) ((datum & 0xFFFF) >> 14);

        return state;
    }

    /**
     * Get the state of the voxel
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public int getAttribute(double x, double y, double z) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        short datum = data[slice][s_x][s_z];

        short mat = (short) (0x3FFF & datum);

        return mat;
    }

    /**
     * Get the material of the voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     */
    public int getAttribute(int x, int y, int z) {
        short datum = data[y][x][z];

        short mat = (short) (0x3FFF & datum);

        return mat;
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(double x, double y, double z, byte state, int material) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);


        data[slice][s_x][s_z] = (short) (0xFFFF & (((short)state) << 14 | ((short)material)));
    }

    /**
     * Set the value of a voxel.
     *
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @param z The z grid coordinate
     * @param state The voxel state
     * @param material The material
     */
    public void setData(int x, int y, int z, byte state, int material) {
        data[y][x][z] = (short) (0xFFFF & (((short)state) << 14 | (short)material));
    }

    /**
     * Set the material value of a voxel.  Leaves the state unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param material The materialID
     */
    public void setAttribute(int x, int y, int z, int material) {
        byte state = (byte) ((data[y][x][z] & 0xFFFF) >> 14);

        data[y][x][z] = (short) (0xFFFF & (((short)state) << 14 | (short)material));
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(int x, int y, int z, byte state) {
        short mat = (short) (0x3FFF & data[y][x][z]);

        data[y][x][z] = (short) (0xFFFF & (((short)state) << 14 | (short)mat));
    }

    /**
     * Set the state value of a voxel.  Leaves the material unchanged.
     *
     * @param x The x world coordinate
     * @param y The y world coordinate
     * @param z The z world coordinate
     * @param state The value.  0 = nothing. > 0 materialID
     */
    public void setState(double x, double y, double z, byte state) {
        int slice = (int) (y / sheight);
        int s_x = (int) (x / pixelSize);
        int s_z = (int) (z / pixelSize);

        short mat = (short) (0x3FFF & data[slice][s_x][s_z]);

        data[slice][s_x][s_z] = (short) (0xFFFF & (((short)state) << 14 | (short)mat));
    }

    /**
     * Clone the object.
     */
    public Object clone() {
        ArrayAttributeGridShortIndexLong ret_val = new ArrayAttributeGridShortIndexLong(this);

        return ret_val;
    }
}

