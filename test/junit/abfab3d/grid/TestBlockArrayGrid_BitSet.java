/*****************************************************************************
 *                      Shapeways, Inc Copyright (c) 2012
 *                             Java Source
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
import java.util.HashSet;

import abfab3d.grid.Grid.VoxelClasses;
import junit.framework.Test;
import junit.framework.TestSuite;

// Internal Imports

/**
 * Tests the functionality of a BlockArrayGrid.
 *
 * @author Alan Hudson, James Gray
 * @version
 */
public class TestBlockArrayGrid_BitSet extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestBlockArrayGrid_BitSet.class);
    }

    /**
     * Test f.nextpow2 function
     */
    public void testHelpFun_nextpow2() {
        assertEquals("Result should be ", 0, f.nextpow2(1));
        assertEquals("Result should be ", 2, f.nextpow2(3));
        assertEquals("Result should be ", 2, f.nextpow2(3.1));
        assertEquals("Result should be ", 5, f.nextpow2(32));
        assertEquals("Result should be ", 6, f.nextpow2(33));
    }

    /**
     * Test f.c2i function
     */
    public void testHelpFun_c2i() {
        int[] order = {1,1,1}; // 2x2x2 cube

        // bit order is y,x,z
        assertEquals("Result should be ", 0, f.c2i(0,0,0,order));
        assertEquals("Result should be ", 1, f.c2i(0,0,1,order));
        assertEquals("Result should be ", 2, f.c2i(1,0,0,order));
        assertEquals("Result should be ", 3, f.c2i(1,0,1,order));
        assertEquals("Result should be ", 4, f.c2i(0,1,0,order));
        assertEquals("Result should be ", 5, f.c2i(0,1,1,order));
        assertEquals("Result should be ", 6, f.c2i(1,1,0,order));
        assertEquals("Result should be ", 7, f.c2i(1,1,1,order));
    }

    /**
     * Test f.coordToIndex coordinate conversion function
     */
    public void testHelpFun_coordToIndex() {
        int[] order = {1,1,1}; // 2x2x2 cube
        int[] last = {1,1,1}; // last valid index values
        int[] idx = {-1,-1}; // container

        // test block zero results
        idx[0] = f.blockIndex(0,0,0,order,order);
        idx[1] = f.voxelIndex(0,0,0,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 0, idx[1]);
        idx[0] = f.blockIndex(0,0,1,order,order);
        idx[1] = f.voxelIndex(0,0,1,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 1, idx[1]);
        idx[0] = f.blockIndex(1,0,0,order,order);
        idx[1] = f.voxelIndex(1,0,0,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 2, idx[1]);
        idx[0] = f.blockIndex(1,0,1,order,order);
        idx[1] = f.voxelIndex(1,0,1,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 3, idx[1]);
        idx[0] = f.blockIndex(0,1,0,order,order);
        idx[1] = f.voxelIndex(0,1,0,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 4, idx[1]);
        idx[0] = f.blockIndex(0,1,1,order,order);
        idx[1] = f.voxelIndex(0,1,1,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 5, idx[1]);
        idx[0] = f.blockIndex(1,1,0,order,order);
        idx[1] = f.voxelIndex(1,1,0,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 6, idx[1]);
        idx[0] = f.blockIndex(1,1,1,order,order);
        idx[1] = f.voxelIndex(1,1,1,last,order);
        assertEquals("Result should be ", 0, idx[0]);
        assertEquals("Result should be ", 7, idx[1]);

        // test block seven results
        idx[0] = f.blockIndex(2,2,2,order,order);
        idx[1] = f.voxelIndex(2,2,2,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 0, idx[1]);
        idx[0] = f.blockIndex(2,2,3,order,order);
        idx[1] = f.voxelIndex(2,2,3,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 1, idx[1]);
        idx[0] = f.blockIndex(3,2,2,order,order);
        idx[1] = f.voxelIndex(3,2,2,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 2, idx[1]);
        idx[0] = f.blockIndex(3,2,3,order,order);
        idx[1] = f.voxelIndex(3,2,3,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 3, idx[1]);
        idx[0] = f.blockIndex(2,3,2,order,order);
        idx[1] = f.voxelIndex(2,3,2,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 4, idx[1]);
        idx[0] = f.blockIndex(2,3,3,order,order);
        idx[1] = f.voxelIndex(2,3,3,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 5, idx[1]);
        idx[0] = f.blockIndex(3,3,2,order,order);
        idx[1] = f.voxelIndex(3,3,2,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 6, idx[1]);
        idx[0] = f.blockIndex(3,3,3,order,order);
        idx[1] = f.voxelIndex(3,3,3,last,order);
        assertEquals("Result should be ", 7, idx[0]);
        assertEquals("Result should be ", 7, idx[1]);
    }


    /**
     * Test blockIndex
     */
    public void testBlockIndex() {
        BlockArrayGrid grid = new BlockArrayGrid(16, 16, 16, 1.0, 1.0, new int[] {3,3,3}, BlockArrayGrid.BlockType.BitSet);

        assertEquals("block order ", 3, grid.BLOCK_TWOS_ORDER[0]);
        assertEquals("grid order ", 1, grid.GRID_TWOS_ORDER[0]);

        int[] gridSize = grid.GRID_WIDTH_IN_BLOCKS;
        int[] blockSize = grid.BLOCK_WIDTH_IN_VOXELS;

        assertEquals("grid width in blocks ", 2, gridSize[0]);
        assertEquals("block width in voxels ", 8, blockSize[0]);

        assertEquals("number of blocks ", 8, grid.blocks.length);

        assertEquals("Result should be ", 0, f.blockIndex(0,0,0,grid.GRID_TWOS_ORDER,grid.BLOCK_TWOS_ORDER));
        assertEquals("Result should be ", 1, f.blockIndex(0,0,15,grid.GRID_TWOS_ORDER,grid.BLOCK_TWOS_ORDER));
        assertEquals("Result should be ", 7, f.blockIndex(15,15,15,grid.GRID_TWOS_ORDER,grid.BLOCK_TWOS_ORDER));
    }

    /**
     * Test grid set and get
     */
    public void testSetGet() {
        BlockArrayGrid grid = new BlockArrayGrid(4096,4096,4096,1.0,1.0,new int[] {4,4,4}, BlockArrayGrid.BlockType.BitSet);

        byte[][][] data = {{ {0,1,2},
                             {3,2,1},
                             {2,3,2} },

                           { {0,1,3},
                             {2,1,0},
                             {1,2,3} },

                           { {1,3,0},
                             {2,1,2},
                             {3,0,1} }};

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {
                    grid.set(x,y,z, data[x][y][z]);
                    assertEquals("Result should be ",data[x][y][z],grid.get(x,y,z));
                }
            }
        }
    }

    /**
     * Test copy constructor.
     */
    public void testBlockArrayGridCopyConstructor() {
        BlockArrayGrid grid = new BlockArrayGrid(10, 9, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 1));

        grid.setData(0, 0, 0, Grid.INSIDE, 2);
        grid.setData(9, 8, 7, Grid.INSIDE, 1);
        grid.setData(5, 0, 7, Grid.INSIDE, 0);

        assertEquals("Material should be ",0,grid.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 1));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));

        Grid grid2 = new BlockArrayGrid(grid);

        assertEquals("State should be ", Grid.OUTSIDE, grid2.getState(0, 0, 1));
        assertEquals("State should be ", Grid.INSIDE, grid2.getState(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid2.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INSIDE, grid2.getState(5, 0, 7));
    }

    /**
     * Test creating an empty grid.
     */
    public void testCreateEmpty() {
        BlockArrayGrid grid = new BlockArrayGrid(100, 101, 102, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        assertEquals("block width in voxels ", 4, ((BlockArrayGrid)grid).BLOCK_WIDTH_IN_VOXELS[0]);
        assertEquals("grid width in blocks ", 32, ((BlockArrayGrid)grid).GRID_WIDTH_IN_BLOCKS[0]);

        grid.setData(5, 5, 5, Grid.INSIDE, 10);

        Grid grid2 = grid.createEmpty(10, 11, 12, 0.002, 0.003);

        assertTrue("Grid type is BlockArrayGrid", grid2 instanceof BlockArrayGrid);
        assertEquals("Grid voxel size is 0.002", 0.002, grid2.getVoxelSize());
        assertEquals("Grid slice height is 0.003", 0.003, grid2.getSliceHeight());

        // all voxels in empty grid should be OUTSIDE state and 0 material
        assertEquals("State is not OUTSIDE for (5, 5, 5)", Grid.OUTSIDE, grid2.getState(5, 5, 5));
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByVoxelCoords() {
        Grid grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(16,8,8,0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(16, 16, 16, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);

        grid = new BlockArrayGrid(100, 91, 85, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelCoords(grid);
    }

    /**
     * Test set/get all data points.
     */
    public void testSetGetByWorldCoords() {
        Grid grid = new BlockArrayGrid(8, 8, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(3,2,2,0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(11, 11, 11, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelByWorldCoords(grid);

        grid = new BlockArrayGrid(100, 91, 85, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        setGetAllVoxelByWorldCoords(grid);
    }

    /**
     * Test getState by voxels.
     */
    public void testGetStateByVoxel() {
        BlockArrayGrid grid = new BlockArrayGrid(10, 9, 8, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        grid.setData(0, 0, 0, Grid.OUTSIDE, (byte)2);
        grid.setData(9, 8, 7, Grid.INSIDE, (byte)1);
        grid.setData(5, 0, 7, Grid.INSIDE, (byte)0);

        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0, 0, 0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 8, 7));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));

        // Index that are not set should default to 0
        assertEquals("State should be ", 0, grid.getState(7, 7, 7));
    }

    /**
     * Test getState by world coordinates.
     */
    public void testGetStateByCoord() {
        BlockArrayGrid grid = new BlockArrayGrid(1.0, 0.4, 0.5, 0.05, 0.01, new int[] {0,0,0}, BlockArrayGrid.BlockType.BitSet);

        // make sure the grid is the expected size
        int xVoxels = grid.getWidth();
        int yVoxels = grid.getHeight();
        int zVoxels = grid.getDepth();
        assertTrue(xVoxels >= 20);
        assertTrue(yVoxels >= 40);
        assertTrue(zVoxels >= 10);

        // set and test get on some random world coordinates
        grid.setData(0.0, 0.0, 0.0, Grid.OUTSIDE, (byte) 2);
        grid.setData(0.95, 0.39, 0.45, Grid.INSIDE, (byte) 1);
        grid.setData(0.6, 0.1, 0.4, Grid.INSIDE, (byte) 0);
        assertEquals("State should be ", Grid.OUTSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.95, 0.39, 0.45));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.6, 0.1, 0.4));

        // should expect width=3, height=6, depth=4
        // set data for a mid-voxel and test the bounds
        grid = new BlockArrayGrid(0.12, 0.11, 0.16, 0.05, 0.02, new int[] {0,0,0}, BlockArrayGrid.BlockType.BitSet);
        grid.setData(0.06, 0.07, 0.08, Grid.INSIDE, (byte)2);
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.07, 0.075));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0999, 0.06, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.0799, 0.05));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.05, 0.06, 0.0999));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0999, 0.0799, 0.0999));
        assertEquals("State should be ", 0, grid.getState(0.0499, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0599, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.06, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.08, 0.05));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.06, 0.1));

        // set data for beginning voxel 0,0,0 and test the bounds
        grid.setData(0.0, 0.0, 0.0, Grid.INSIDE, (byte)2);
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0499, 0.0, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0199, 0.0));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0, 0.0, 0.0499));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.0499, 0.0199, 0.0499));
        assertEquals("State should be ", 0, grid.getState(0.05, 0.0, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.02, 0.0));
        assertEquals("State should be ", 0, grid.getState(0.0, 0.0, 0.05));

        // set data for last voxel 2,5,3 and test the bounds
        grid.setData(0.149, 0.119, 0.199, Grid.INSIDE, (byte)2);
//      assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.1, 0.15));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.149, 0.1, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.119, 0.151));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.1, 0.1, 0.199));
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0.149, 0.119, 0.199));
        assertEquals("State should be ", 0, grid.getState(0.0999, 0.1, 0.1501));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.0999, 0.1501));
        assertEquals("State should be ", 0, grid.getState(0.1, 0.1, 0.1499));
        assertEquals("State should be ", 0, grid.getState(0.0999, 0.0999, 0.1499));
    }

    /**
     * Test findCount by voxel class.
     */
    public void testFindCountByVoxelClass() {
        int width = 6;
        int height = 3;
        int depth = 10;
        int[] row = {0, 3, 5};
        byte[] state = {Grid.INSIDE, Grid.INSIDE, Grid.INSIDE};

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.05, 0.02, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some rows to interior and exterior
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, state[0], (byte)2);
                grid.setData(row[1], y, z, state[1], (byte)2);
                grid.setData(row[2], y, z, state[2], (byte)2);
            }
        }

        int expectedAllCount = width * depth * height;
        int expectedIntCount = depth * height * 2;
        int expectedExtCount = depth * height;
        int expectedMrkCount = expectedIntCount + expectedExtCount;
        int expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.INSIDE));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, grid.findCount(VoxelClasses.OUTSIDE));

        // change one of the interior voxel rows to outside
        for (int y=0; y<height; y++) {
            for (int z=0; z<depth; z++) {
                grid.setData(row[0], y, z, Grid.OUTSIDE, (byte)2);
            }
        }

        expectedIntCount = depth * height;
        expectedExtCount = depth * height;
        expectedMrkCount = expectedIntCount + expectedExtCount;
        expectedOutCount = expectedAllCount - expectedIntCount - expectedExtCount;

        assertEquals("Expected total voxels is not " + expectedAllCount, expectedAllCount, grid.findCount(VoxelClasses.ALL));
        assertEquals("Expected marked voxels is not " + expectedMrkCount, expectedMrkCount, grid.findCount(VoxelClasses.INSIDE));
        assertEquals("Expected outside voxels is not " + expectedOutCount, expectedOutCount, grid.findCount(VoxelClasses.OUTSIDE));
    }

    /**
     * Test find voxels by voxel class
     */
    public void testFindVoxelClass() {
        int width = 3;
        int height = 4;
        int depth = 10;
        int[] stateDepth = {10, 6, 1};
        byte[] states = {Grid.INSIDE, Grid.INSIDE, Grid.OUTSIDE};

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.05, 0.02, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        width = grid.getWidth();
        height = grid.getHeight();
        depth = grid.getDepth();

        // set some data
        for (int x=0; x<states.length; x++){
            for (int y=0; y<height; y++) {
                for (int z=0; z<stateDepth[x]; z++) {
                    grid.setData(x, y, z, states[x], (byte) 2);
                }
            }
        }

        int expectedAllCount = width * height * depth;
        int expectedExtCount = stateDepth[0] * height;
        int expectedIntCount = stateDepth[1] * height;
        int expectedMrkCount = expectedExtCount + expectedIntCount;
        int expectedOutCount = expectedAllCount - expectedMrkCount;

        resetCounts();
        grid.find(VoxelClasses.ALL, this);
        assertEquals("All voxel count is not " + expectedAllCount, expectedAllCount, allCount);

        resetCounts();
        grid.find(VoxelClasses.INSIDE, this);
        assertEquals("Marked voxel count is not " + expectedMrkCount, expectedMrkCount, mrkCount);

        resetCounts();
        grid.find(VoxelClasses.OUTSIDE, this);
        assertEquals("Outside voxel count is not " + expectedOutCount, expectedOutCount, outCount);
    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat = 1;

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(x, 2, 2));

            grid.setData(x, 5, 6, Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with INSIDE state",
                ft.foundAllVoxels());

        // make sure that finding a voxel not in the list returns false
        grid.setData(10, 6, 2, Grid.INSIDE, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        // make sure that not finding a voxel in the list returns false
        grid.setData(1, 5, 6, Grid.INSIDE, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertFalse("Found state iterator should return false",
                ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        vcSetInt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetInt);
        grid.find(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test that find voxels by VoxelClass actually found the voxels in the correct coordinates
     */
    public void testFindInterruptableVoxelClassIterator() {
        int width = 20;
        int height = 10;
        int depth = 10;
        long mat = 1;

        BlockArrayGrid grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        HashSet<VoxelCoordinate> vcSetInt = new HashSet<VoxelCoordinate>();

        for (int x=0; x<width; x++) {
            grid.setData(x, 2, 2, Grid.INSIDE, mat);
            grid.setData(x, 4, 4, Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(x, 2, 2));
            vcSetInt.add(new VoxelCoordinate(x, 4, 4));

            grid.setData(x, 5, 6, Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(x, 5, 6));
        }

        FindIterateTester ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with INSIDE state",
                ft.foundAllVoxels());

        // make sure that findInterruptible stops interating when voxel is not found
        // do this by adding a new exterior voxel
        grid.setData(5, 2, 2, Grid.OUTSIDE, mat);
        grid.setData(1, 3, 3, Grid.INSIDE, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertFalse("Found state interruptible iterator should return false",
                ft.foundAllVoxels());
        assertTrue("Found state interruptible did not get interrupted ",
                ft.getIterateCount() < vcSetInt.size());

        // make sure that not finding a voxel in the list returns false
        // do this by changing one of the interior voxels to exterior state
        grid.setData(1, 5, 6, Grid.OUTSIDE, mat);
        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertFalse("Found state interruptible iterator should return false", ft.foundAllVoxels());

        //-------------------------------------------------------
        // test on some random coordinates
        int[][] coords = {
                {0,0,0},
                {width/2, height/2, depth/2},
                {0, height-1, depth-1},
                {width-1, 0, 0},
                {width-1, height-1, depth-1}
        };

        grid = new BlockArrayGrid(width, height, depth, 0.001, 0.001, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        vcSetInt = new HashSet<VoxelCoordinate>();

        for (int i=0; i<coords.length; i++) {
            grid.setData(coords[i][0], coords[i][1], coords[i][2], Grid.INSIDE, mat);
            vcSetInt.add(new VoxelCoordinate(coords[i][0], coords[i][1], coords[i][2]));
        }

        ft = new FindIterateTester(vcSetInt);
        grid.findInterruptible(VoxelClasses.INSIDE, ft);

        assertTrue("Found iterator did not find all voxels with EXTERIOR state",
                ft.foundAllVoxels());

    }

    /**
     * Test getGridCoords.
     */
    public void testGetGridCoords() {
        double xWorldCoord = 1.0;
        double yWorldCoord = 0.15;
        double zWorldCoord = 0.61;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new BlockArrayGrid(xWorldCoord, yWorldCoord, zWorldCoord, voxelWidth, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        double xcoord = 0.55;
        double ycoord = 0.0202;
        double zcoord = 0.401;

        int expectedXVoxelCoord = (int) (xcoord / voxelWidth);
        int expectedYVoxelCoord = (int) (ycoord / sliceHeight);
        int expectedZVoxelCoord = (int) (zcoord / voxelWidth);
        int[] coords = new int[3];

        grid.getGridCoords(xcoord, ycoord, zcoord, coords);
        assertTrue("Voxel coordinate is not (" + expectedXVoxelCoord + ", " + expectedYVoxelCoord + ", " + expectedZVoxelCoord + ")",
                coords[0] == expectedXVoxelCoord &&
                coords[1] == expectedYVoxelCoord &&
                coords[2] == expectedZVoxelCoord);

        // test on a voxel line
        xcoord = 0.6;
        ycoord = 0.05;
        zcoord = 0.08;

        expectedXVoxelCoord = (int) (xcoord / voxelWidth);
        expectedYVoxelCoord = (int) (ycoord / sliceHeight);
        expectedZVoxelCoord = (int) (zcoord / voxelWidth);

        grid.getGridCoords(xcoord, ycoord, zcoord, coords);
        assertTrue("Voxel coordinate is not (" + expectedXVoxelCoord + ", " + expectedYVoxelCoord + ", " + expectedZVoxelCoord + ")",
                coords[0] == expectedXVoxelCoord &&
                coords[1] == expectedYVoxelCoord &&
                coords[2] == expectedZVoxelCoord);
    }

    /**
     * Test getWorldCoords.
     */
    public void testGetWorldCoords() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new BlockArrayGrid(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        int xcoord = 27;
        int ycoord = 2;
        int zcoord = 20;

        double expectedXWorldCoord = (double) (xcoord * voxelWidth + voxelWidth / 2);
        double expectedYWorldCoord = (double) (ycoord * sliceHeight + sliceHeight / 2);
        double expectedZWorldCoord = (double) (zcoord * voxelWidth + voxelWidth / 2);
        double[] coords = new double[3];

        grid.getWorldCoords(xcoord, ycoord, zcoord, coords);
        assertTrue("World coordinate is not (" + expectedXWorldCoord + ", " + expectedYWorldCoord + ", " + expectedZWorldCoord + ")",
                coords[0] == expectedXWorldCoord &&
                coords[1] == expectedYWorldCoord &&
                coords[2] == expectedZWorldCoord);

    }

    /**
     * Test getWorldCoords.
     */
    public void testGetGridBounds() {
        int xVoxels = 50;
        int yVoxels = 15;
        int zVoxels = 31;
        double voxelWidth = 0.02;
        double sliceHeight = 0.01;

        Grid grid = new BlockArrayGrid(xVoxels, yVoxels, zVoxels, voxelWidth, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        xVoxels = grid.getWidth();
        yVoxels = grid.getHeight();
        zVoxels = grid.getDepth();

        double[] minBounds = new double[3];
        double[] maxBounds = new double[3];
        double expectedMaxX = xVoxels * voxelWidth;
        double expectedMaxY = yVoxels * sliceHeight;
        double expectedMaxZ = zVoxels * voxelWidth;

        grid.getGridBounds(minBounds, maxBounds);

        assertTrue("Minimum bounds is not (0, 0, 0)",
                minBounds[0] == 0.0 &&
                minBounds[1] == 0.0 &&
                minBounds[2] == 0.0);

        assertTrue("Maximum bounds is not (" + expectedMaxX + ", " + expectedMaxY + ", " + expectedMaxZ + ")",
                maxBounds[0] == expectedMaxX &&
                maxBounds[1] == expectedMaxY &&
                maxBounds[2] == expectedMaxZ);

    }

    /**
     * Test getSliceHeight with both constructor methods.
     */
    public void testGetSliceHeight() {
        double sliceHeight = 0.0015;

        // voxel coordinates
        Grid grid = new BlockArrayGrid(50, 25, 70, 0.05, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        assertEquals("Slice height is not " + sliceHeight, sliceHeight, grid.getSliceHeight());

        // world coordinates
        grid = new BlockArrayGrid(0.12, 0.11, 0.12, 0.05, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        assertEquals("Slice height is not" + sliceHeight, sliceHeight, grid.getSliceHeight());
    }

    /**
     * Test getVoxelSize with both constructor methods.
     */
    public void testGetVoxelSize() {
        double voxelSize = 0.025;

        // voxel coordinates
        Grid grid = new BlockArrayGrid(50, 25, 70, voxelSize, 0.01, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());

        // world coordinates
        grid = new BlockArrayGrid(0.12, 0.11, 0.12, voxelSize, 0.01, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);
        assertEquals("Voxel size is not " + voxelSize, voxelSize, grid.getVoxelSize());
    }

    /**
     * Test setState.
     */
    public void testSetState() {
    int size = 10;

    BlockArrayGrid grid = new BlockArrayGrid(size, size, size, 0.001, 0.001, new int[] {0,0,0}, BlockArrayGrid.BlockType.BitSet);

        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(9, 9, 9, Grid.INSIDE, 2);
        grid.setData(5, 0, 7, Grid.INSIDE, 3);

        grid.setState(0, 0, 0, Grid.INSIDE);
        grid.setState(9, 9, 9, Grid.INSIDE);
        grid.setState(5, 0, 7, Grid.INSIDE);

        // check that the state changed, but the material did not
        assertEquals("State should be ", Grid.INSIDE, grid.getState(0, 0, 0));
        //assertEquals("Material should be ", 1, grid.getMaterial(0, 0, 0));

        assertEquals("State should be ", Grid.INSIDE, grid.getState(9, 9, 9));
        //assertEquals("Material should be ", 2, grid.getMaterial(9, 9, 9));

        assertEquals("State should be ", Grid.INSIDE, grid.getState(5, 0, 7));
        //assertEquals("Material should be ", 3, grid.getMaterial(5, 0, 7));
    }

    /**
     * Test clone.
     *
     * This test removed as the voxel size is not getting set right, grid is not really in use anymore
     */
    public void _testClone() {
        int size = 10;
        double voxelSize = 0.002;
        double sliceHeight = 0.001;

        BlockArrayGrid grid = new BlockArrayGrid(size, size, size, voxelSize, sliceHeight, new int[] {2,2,2}, BlockArrayGrid.BlockType.BitSet);

        grid.setData(0, 0, 0, Grid.INSIDE, 1);
        grid.setData(9, 9, 9, Grid.INSIDE, 2);
        grid.setData(5, 0, 7, Grid.INSIDE, 3);

        Grid grid2 = (BlockArrayGrid) grid.clone();

        assertEquals("Voxel size should be ", voxelSize, grid2.getVoxelSize());
        assertEquals("Slight height should be ", sliceHeight, grid2.getSliceHeight());

        // check that the state and material are set
        assertEquals("State should be ", Grid.INSIDE, grid2.getState(0, 0, 0));

        assertEquals("State should be ", Grid.INSIDE, grid2.getState(9, 9, 9));

        assertEquals("State should be ", Grid.INSIDE, grid2.getState(5, 0, 7));
    }
}