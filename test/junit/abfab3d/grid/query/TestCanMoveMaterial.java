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

package abfab3d.grid.query;

// External Imports
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.path.StraightPath;

/**
 * Tests the functionality of CanMoveMaterial Query.
 *
 * @author Alan Hudson
 * @version
 */
public class TestCanMoveMaterial extends BaseTestGrid {

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestCanMoveMaterial.class);
    }

    /**
     * Test basic operation
     */
    public void testBasic() {
        int size = 12;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,1, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,2, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,3, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,4, Grid.EXTERIOR, (byte) 1);

        // Add Object 2
        int mat2_count = 6;

        grid.setData(0,2,0, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,1, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,2, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,3, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,4, Grid.EXTERIOR, (byte) 2);
        grid.setData(0,2,5, Grid.EXTERIOR, (byte) 2);

        StraightPath path = new StraightPath(new int[] {1,0,0});
        CanMoveMaterial query = new CanMoveMaterial((byte) 1, path);
        boolean escaped = query.execute(grid);

        assertTrue("X Axis move", escaped == true);
        System.out.println("Escaped: " + escaped);

        path = new StraightPath(new int[] {0,1,0});
        query = new CanMoveMaterial((byte) 1, path);
        escaped = query.execute(grid);

        assertTrue("Y Axis move", escaped == false);

    }
    
    /**
     * Test a complex move where movement is only allowed in one direction
     */
    public void testComplexTrue() {
    	byte matToMove = (byte) 2;
    	Grid grid = new SliceGrid(100,100,100,0.001, 0.001, true);
    	
    	// set the voxels of a square
    	setX(grid, 50, 40, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setX(grid, 50, 60, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setZ(grid, 40, 50, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setZ(grid, 60, 50, Grid.EXTERIOR, (byte) 1, 40, 60);
    	
    	// set the voxels of a T shape with the bottom of the T intersecting the opening of the square
    	setX(grid, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
    	setY(grid, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);
    	
    	//------------------------------------------------------
    	// test movement in all directions
    	// should only be able to move in {0,1,0} direction
    	//------------------------------------------------------
    	
    	// negative X axis direction
        boolean escaped = canMove(grid, new int[] {-1,0,0}, matToMove);
        assertEquals("Negative X axis move is not false", false, escaped);
        
    	// positive X axis direction
        escaped = canMove(grid, new int[] {1,0,0}, matToMove);
        assertEquals("Positive X axis move is not false", false, escaped);
        
    	// negative Y axis direction
        escaped = canMove(grid, new int[] {0,-1,0}, matToMove);
        assertEquals("Negative Y axis move is not false", false, escaped);
        
    	// negative Z axis direction
        escaped = canMove(grid, new int[] {0,0,-1}, matToMove);
        assertEquals("Negative Z axis move is not false", false, escaped);
        
    	// positive Z axis direction
        escaped = canMove(grid, new int[] {0,0,1}, matToMove);
        assertEquals("Positive Z axis move is not false", false, escaped);
        
    	// positive Y axis direction
        escaped = canMove(grid, new int[] {0,1,0}, matToMove);
        assertEquals("Positive Y axis move is not true", true, escaped);
    }
    
    /**
     * Test a complex move where movement is not allowed
     */
    public void testComplexFalse() {
    	byte matToMove = (byte) 2;
    	Grid grid = new SliceGrid(100,100,100,0.001, 0.001, true);
    	
    	// set the voxels of a square
    	setX(grid, 50, 40, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setX(grid, 50, 60, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setZ(grid, 40, 50, Grid.EXTERIOR, (byte) 1, 40, 60);
    	setZ(grid, 60, 50, Grid.EXTERIOR, (byte) 1, 40, 60);
    	
    	// set the voxels of an I shape with the vertical part intersecting the opening of the square
    	setX(grid, 60, 50, Grid.EXTERIOR, matToMove, 30, 70);
    	setX(grid, 40, 50, Grid.EXTERIOR, matToMove, 30, 70);
    	setY(grid, 50, 50, Grid.EXTERIOR, matToMove, 40, 60);
    	
    	//------------------------------------------------------
    	// test movement in all directions
    	// movement should fail in all drections
    	//------------------------------------------------------
    	
    	// negative X axis direction
        boolean escaped = canMove(grid, new int[] {-1,0,0}, matToMove);
        assertEquals("Negative X axis move is not false", false, escaped);
        
    	// positive X axis direction
        escaped = canMove(grid, new int[] {1,0,0}, matToMove);
        assertEquals("Positive X axis move is not false", false, escaped);
        
    	// negative Y axis direction
        escaped = canMove(grid, new int[] {0,-1,0}, matToMove);
        assertEquals("Negative Y axis move is not false", false, escaped);
        
    	// positive Y axis direction
        escaped = canMove(grid, new int[] {0,1,0}, matToMove);
        assertEquals("Positive Y axis move is not false", false, escaped);
        
    	// negative Z axis direction
        escaped = canMove(grid, new int[] {0,0,-1}, matToMove);
        assertEquals("Negative Z axis move is not false", false, escaped);
        
    	// positive Z axis direction
        escaped = canMove(grid, new int[] {0,0,1}, matToMove);
        assertEquals("Positive Z axis move is not false", false, escaped);
    }
    
    public void testIgnoredVoxels() {
        int size = 12;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        // Add Object 1
        int mat1_count = 5;

        grid.setData(0,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(0,0,1, Grid.INTERIOR, (byte) 1);
        grid.setData(0,0,2, Grid.INTERIOR, (byte) 1);
        grid.setData(0,0,3, Grid.INTERIOR, (byte) 1);
        grid.setData(0,0,4, Grid.EXTERIOR, (byte) 1);

        StraightPath path = new StraightPath(new int[] {0,0,-1});
        CanMoveMaterial query = new CanMoveMaterial((byte) 1, path);
        boolean escaped = query.execute(grid);

//        assertTrue("X Axis move", escaped == true);
//        System.out.println("Escaped: " + escaped);

    }
    
    public void testIgnoredVoxelsPerformance() {
        int size = 300;
        int startIndex = 50;
        int endIndex = 295;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

//        for (int i=startIndex; i<=endIndex; i++){
//        	grid.setData(i,0,0, Grid.INTERIOR, (byte) 1);
//        }
        
        setX(grid, 0, 0, Grid.INTERIOR, (byte) 1, startIndex+1, endIndex-1);
        setX(grid, 0, 0, Grid.OUTSIDE, (byte) 1, 101, 109);
        
        grid.setData(startIndex,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(endIndex,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(100,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(110,0,0, Grid.EXTERIOR, (byte) 1);

        StraightPath path = new StraightPath(new int[] {-1,0,0});
        CanMoveMaterial query = new CanMoveMaterial((byte) 1, path);
        
        long stime = System.nanoTime();
        query.execute(grid);
        long totalTime1 = System.nanoTime() - stime;
        
        System.out.println("With ignored voxels on        : " + totalTime1);

    }
    
    public void testPerformance() {
        int size = 300;
        int startIndex = 50;
        int endIndex = 250;

        Grid grid = new SliceGrid(size,size,size,0.001, 0.001, true);

        setX(grid, 0, 0, Grid.INTERIOR, (byte) 1, startIndex+1, endIndex-1);
        setX(grid, 0, 0, Grid.OUTSIDE, (byte) 1, 101, 109);
        
        grid.setData(startIndex,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(endIndex,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(100,0,0, Grid.EXTERIOR, (byte) 1);
        grid.setData(110,0,0, Grid.EXTERIOR, (byte) 1);

        // Set the paths
        int[][] directions = new int[][] {{-1,0,0}, {1,0,0}, {0,-1,0}, {0,1,0}};
        StraightPath[] paths = new StraightPath[directions.length];
        
        for (int i=0; i<directions.length; i++) {
        	paths[i] = new StraightPath(directions[i]);
        }
        
        //------------------------------------------------------------
        // Can move material one path at a time for all voxels
        //------------------------------------------------------------
        CanMoveMaterial queryOnePath;

        long stime = System.nanoTime();

        for (int j=0; j<paths.length; j++) {
            queryOnePath = new CanMoveMaterial((byte) 1, paths[j]);
            queryOnePath.execute(grid);
        }

        long totalTime1 = System.nanoTime() - stime;
        System.out.println("CanMoveMaterial        : " + totalTime1);
        
        //------------------------------------------------------------
        // Can move material all paths at a time for each voxels
        //------------------------------------------------------------
        stime = System.nanoTime();
        
        CanMoveMaterialAllPaths queryAllPaths = new CanMoveMaterialAllPaths((byte) 1, paths);
        queryAllPaths.execute(grid);
        
        totalTime1 = System.nanoTime() - stime;
        System.out.println("CanMoveMaterialAllPaths: " + totalTime1);
    }
    
    private boolean canMove(Grid grid, int[] dir, byte mat) {
        StraightPath path = new StraightPath(dir);
        CanMoveMaterial query = new CanMoveMaterial(mat, path);
        
        return query.execute(grid);
    }
}
