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
 * Removes all voxels with the specific Material.
 *
 * @author Alan Hudson
 */
public class RemoveMaterial implements Operation {
    /** The material to remove */
    private int material;

    public RemoveMaterial(int material) {
        this.material = material;
    }

    /**
     * Execute an operation on a grid.  If the operation changes the grid
     * dimensions then a new one will be returned from the call.
     *
     * @param grid The grid to use for grid src
     * @return The new grid
     */
    public Grid execute(Grid grid) {
        grid.removeMaterial(material);

        return grid;
    }
}
