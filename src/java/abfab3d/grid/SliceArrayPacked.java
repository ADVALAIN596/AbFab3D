/*****************************************************************************
 *                        Alan Hudson Copyright (c) 2011
 *                               Java Source
 *
 * This source is private and not licensed for any use.
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
 * A slice backed by an array.  Uses 2 bits per voxel to save space.
 *
 * States are:
 *   0 = empty
 *   1 = exterior
 *   2 = interior
 *   3 = visited
 *
 * Colored States are:
 *   0 = empty
 *   1 = materialID1
 *   2 = materialID2
 *   3 = materialID3
 *
 * @author Alan Hudson
 */
public class SliceArrayPacked implements Slice {
    /** The number of pixels in the x/width direction */
    protected int width;

    /** The number of pixels in the z/depth direction */
    protected int depth;

    /** Data, 0 = nothing.  > 0 materialID */
    protected byte[] data;

    public SliceArrayPacked(int w, int d) {
        width = w;
        depth = d;

        data = new byte[width * depth / 4];
    }

    /**
     * Set the data directly
     *
     * @param data The data
     */
    public void setData(byte[] newData) {
        data = new byte[newData.length];

        System.arraycopy(data, 0, newData, 0, newData.length);
    }

    /**
     * Get the data.
     *
     * @param data A reference to the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Get a pixel data directly.
     *
     * @param x The x value
     * @param z The z value
     * @return The data value
     */
    public VoxelData getData(int x, int z) {
        return null;
    }

    /**
     * Get a pixel state directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getState(int x, int z) {
        return (byte) ((data[z * width + x] & 0xFF) >> 6);
    }

    /**
     * Get a pixel material directly.
     *
     * @param x The x value
     * @param z The z value
     */
    public byte getMaterial(int x, int z) {
        return (byte) (0x3F & data[z * width + x]);
    }

    /**
     * Set a pixel directly.
     *
     * @param x The x value
     * @param z The z value
     * @param state The voxel state
     * @param material The materialID
     */
    public void setData(int x, int z, byte state, byte material) {
System.out.println("Need to code");
        data[z * width + x] = (byte) (0xFF & (state << 6 | material));
    }

    public String toStringSlice() {
        StringBuilder sb = new StringBuilder();

        // TODO: need to code.

System.out.println("TODO: need to code");
        for(int i=0; i < depth; i++) {
            for(int j=0; j < width; j++) {
                sb.append(data[i * width + j]);
                sb.append(" ");
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public int getWidth() {
        return width;
    }

    public int getDepth() {
        return depth;
    }
}