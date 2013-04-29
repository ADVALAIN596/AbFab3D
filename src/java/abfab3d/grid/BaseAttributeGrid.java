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

import java.io.Serializable;
// External Imports

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
public abstract class BaseAttributeGrid extends BaseGrid implements AttributeGrid, Cloneable, Serializable {
    /**
     * Constructor.
     *
     * @param w The number of voxels in width
     * @param h The number of voxels in height
     * @param d The number of voxels in depth
     * @param pixel The size of the pixels
     * @param sheight The slice height in meters
     */
    public BaseAttributeGrid(int w, int h, int d, double pixel, double sheight) {
        super(w,h,d,pixel,sheight);
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(int mat, ClassAttributeTraverser t) {

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    
                    getData(x,y,z,vd);
                    
                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        t.found(x,y,z,vd);
                    }
                }
            }
        }
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, int mat, ClassAttributeTraverser t) {

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() != mat) {
                        continue;
                    }

                    byte state;

                    switch(vc) {
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Traverse a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(int mat, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        if (!t.foundInterruptible(x,y,z,vd))
                            break loop;
                    }
                }
            }
        }
    }

    /**
     * Traverse a class of voxel and material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param mat The material to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, int mat, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() != mat) {
                        continue;
                    }

                    byte state;

                    switch(vc) {
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                    }
                }
            }
        }
    }


    /**
     * Count a class of material types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param mat The class of material to traverse
     * @return The number
     */
    public int findCount(int mat) {
        int ret_val = 0;

        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        ret_val++;
                    }
                }
            }
        }

        return ret_val;
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttribute(VoxelClasses vc, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    byte state;

                    switch(vc) {
                        case ALL:
                            t.found(x,y,z,vd);
                            break;
                        case MARKED:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case EXTERIOR:
                            state = vd.getState();
                            if (state == Grid.EXTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case INTERIOR:
                            state = vd.getState();
                            if (state == Grid.INTERIOR) {
                                t.found(x,y,z,vd);
                            }
                            break;
                        case OUTSIDE:
                            state = vd.getState();
                            if (state == Grid.OUTSIDE) {
                                t.found(x,y,z,vd);
                            }
                            break;
                    }
                }
            }
        }
    }

    /**
     * Traverse a class of voxels types.  May be much faster then
     * full grid traversal for some implementations.
     *
     * @param vc The class of voxels to traverse
     * @param t The traverer to call for each voxel
     */
    public void findAttributeInterruptible(VoxelClasses vc, ClassAttributeTraverser t) {
        VoxelData vd = getVoxelData();

        loop:
        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);
                    byte state = getState(x,y,z);

                    switch(vc) {
                        case ALL:
                            if (!t.foundInterruptible(x,y,z,vd))
                                break loop;
                            break;
                        case MARKED:
                            if (state == Grid.EXTERIOR || state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case EXTERIOR:
                            if (state == Grid.EXTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case INTERIOR:
                            if (state == Grid.INTERIOR) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                        case OUTSIDE:
                            if (state == Grid.OUTSIDE) {
                                if (!t.foundInterruptible(x,y,z,vd))
                                    break loop;
                            }
                            break;
                    }
                }
            }
        }
    }
    
    /**
     * Reassign a group of materials to a new materialID
     *
     * @param materials The new list of materials
     */
    public void reassignAttribute(final int[] materials, int matID) {
        // assume unindexed if we got here.  Best to traverse
        // whole structure

        final int len = materials.length;
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    int mat;
                    byte state = vd.getState();

                    if (state != Grid.OUTSIDE) {
                        mat = vd.getMaterial();

                        for(int i=0; i < len; i++) {
                            if (mat == materials[i]) {
                                setData(x,y,z, state, matID);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove all voxels associated with the Material.
     *
     * @param mat The aterialID
     */
    public void removeAttribute(int mat) {
        VoxelData vd = getVoxelData();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                for(int z=0; z < depth; z++) {
                    getData(x,y,z,vd);

                    if (vd.getMaterial() == mat && vd.getState() != Grid.OUTSIDE) {
                        setData(x,y,z, Grid.OUTSIDE, 0);
                    }
                }
            }
        }
    }

    /**
     * Print out a slice of data.
     */
    public String toStringSlice(int y) {
        StringBuilder sb = new StringBuilder();

        for(int z=depth-1; z >= 0; z--) {
            for(int x=0; x < width; x++) {
                sb.append(getState(x,y,z));
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

    /**
       print given crossection of attributes
     */
    public String toStringAttributesSectionZ(int z) {

        StringBuilder sb = new StringBuilder();

        for(int y=0; y < height; y++) {
            for(int x=0; x < width; x++) {
                int att = getAttribute(x, y, z);
                sb.append((char)('A' + att));
            }
            sb.append('\n');
        }

        return sb.toString();
    }


    public abstract Object clone();

}

