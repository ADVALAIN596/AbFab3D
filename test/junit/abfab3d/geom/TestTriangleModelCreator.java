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

package abfab3d.geom;

// External Imports
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.GeometryData;
import org.j3d.geom.BoxGenerator;
import org.j3d.geom.CylinderGenerator;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DBinaryRetainedDirectExporter;
import org.web3d.vrml.export.X3DBinarySerializer;
import org.web3d.vrml.sav.BinaryContentHandler;

// Internal Imports
import abfab3d.grid.*;
import abfab3d.grid.Grid.VoxelClasses;
import abfab3d.io.output.BoxesX3DExporter;

/**
 * Tests the functionality of TriangleModelCreator.
 *
 * @author Tony Wong
 * @version
 */
public class TestTriangleModelCreator extends TestCase {

    /** Horizontal resolution of the printer in meters.  */
    public static final double HORIZ_RESOLUTION = 0.004;

    /** Vertical resolution of the printer in meters.  */
    public static final double VERT_RESOLUTION = 0.004;
    
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestTriangleModelCreator.class);
    }

    /**
     * Test the voxelization of a simple triangle cube.
     */
    public void testCube() {
    	
    	// Use 0.0999 instead of 0.1 voxelization is unpredictable when model
    	// lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;
        byte outerMaterial = 1;
        byte innerMaterial = 2;
        
        // set the grid size with a slight over allocation
        int gWidth = (int) (width / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;
        
        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
        
        // translate the cube so it does not occupy one more row, height, and depth than it should
        // by having the "left" side start on a grid line
        int indexOffset = 5;
		double translateX = Math.round((width / 2.0f) * 1000f) / 1000f + indexOffset * HORIZ_RESOLUTION;
        double translateY = Math.round((height / 2.0f) * 1000f) / 1000f + indexOffset * VERT_RESOLUTION;
        double translateZ = Math.round((depth / 2.0f) * 1000f) / 1000f + indexOffset * HORIZ_RESOLUTION;
        
//System.out.println("test translate: " + Math.abs(Math.round((width / 2.0f) * 1000f)) / 1000f);
//System.out.println("translateX: " + translateX);
        
		//------------------------------------------------------
		// Test a cube with interior voxels filled
		//------------------------------------------------------

        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial, innerMaterial, 
        		GeometryData.TRIANGLES, true);
        
//System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

		int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
		int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
		int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
		
		int expectedExtCount = getCubeExteriorVoxelCount(xVoxels, yVoxels, zVoxels);
		int expectedIntCount = getCubeInteriorVoxelCount(xVoxels, yVoxels, zVoxels);
		
		// check exterior and interior voxel counts
		assertEquals("Exterior count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count is not " + expectedIntCount, 
				expectedIntCount, 
				grid.findCount(VoxelClasses.INTERIOR));
		
		// check outer and inner material counts
		assertEquals("Outer material count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(outerMaterial));
		
		assertEquals("Inner material count is not " + expectedIntCount, 
				expectedIntCount, 
				grid.findCount(innerMaterial));

		// check that the state and material is correct for each filled grid coordinate
		int cubeStartIndex = indexOffset;
		checkCubeVoxelStates(grid, outerMaterial, innerMaterial, 
				cubeStartIndex, cubeStartIndex, cubeStartIndex, xVoxels, yVoxels, zVoxels);
		
		//------------------------------------------------------
		// Test an exterior only cube
		//------------------------------------------------------

		grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
		
        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial, innerMaterial, 
        		GeometryData.TRIANGLES, false);
		
		// check exterior and interior voxel counts
		assertEquals("Exterior count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count is not 0", 
				0, 
				grid.findCount(VoxelClasses.INTERIOR));
		
		// check outer and inner material counts
		assertEquals("Outer material count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(outerMaterial));
		
		assertEquals("Inner material count is not " + expectedIntCount, 
				0, 
				grid.findCount(innerMaterial));
    }

    /**
     * Test the voxelization of a simple indexed triangle cube.
     */
    public void testCubeIndexed() {

    	// Use 0.0999 instead of 0.1 voxelization is unpredictable when model
    	// lines up exactly with a grid
        float width = 0.0999f;
        float height = 0.0999f;
        float depth = 0.0999f;
        byte outerMaterial = 1;
        byte innerMaterial = 2;
        
        // set the grid size with a slight over allocation
        int gWidth = (int) (width / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;
        
        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
        
        // translate the cube so it does not occupy one more row, height, and depth than it should
        // by having the "left" side start on a grid line
        int indexOffset = 5;
		double translateX = Math.round((width / 2.0f) * 1000f) / 1000f + indexOffset * HORIZ_RESOLUTION;
        double translateY = Math.round((height / 2.0f) * 1000f) / 1000f + indexOffset * VERT_RESOLUTION;
        double translateZ = Math.round((depth / 2.0f) * 1000f) / 1000f + indexOffset * HORIZ_RESOLUTION;
        
//System.out.println("test translate: " + Math.abs(Math.round((width / 2.0f) * 1000f)) / 1000f);
//System.out.println("translateX: " + translateX);
        
		//------------------------------------------------------
		// Test a cube with interior voxels filled
		//------------------------------------------------------

        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial, innerMaterial, 
        		GeometryData.INDEXED_TRIANGLES, true);
        
//System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

		int xVoxels = (int) Math.round(width / HORIZ_RESOLUTION);
		int yVoxels = (int) Math.round(height / VERT_RESOLUTION);
		int zVoxels = (int) Math.round(depth / HORIZ_RESOLUTION);
		
		int expectedExtCount = getCubeExteriorVoxelCount(xVoxels, yVoxels, zVoxels);
		int expectedIntCount = getCubeInteriorVoxelCount(xVoxels, yVoxels, zVoxels);
		
		// check exterior and interior voxel counts
		assertEquals("Exterior count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count is not " + expectedIntCount, 
				expectedIntCount, 
				grid.findCount(VoxelClasses.INTERIOR));
		
		// check outer and inner material counts
		assertEquals("Outer material count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(outerMaterial));
		
		assertEquals("Inner material count is not " + expectedIntCount, 
				expectedIntCount, 
				grid.findCount(innerMaterial));

		// check that the state and material is correct for each filled grid coordinate
		int cubeStartIndex = indexOffset;
		checkCubeVoxelStates(grid, outerMaterial, innerMaterial, 
				cubeStartIndex, cubeStartIndex, cubeStartIndex, xVoxels, yVoxels, zVoxels);
		
		//------------------------------------------------------
		// Test an exterior only cube
		//------------------------------------------------------

		grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
		
        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial, innerMaterial, 
        		GeometryData.INDEXED_TRIANGLES, false);
		
		// check exterior and interior voxel counts
		assertEquals("Exterior count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count is not 0", 
				0, 
				grid.findCount(VoxelClasses.INTERIOR));
		
		// check outer and inner material counts
		assertEquals("Outer material count is not " + expectedExtCount, 
				expectedExtCount, 
				grid.findCount(outerMaterial));
		
		assertEquals("Inner material count is not " + expectedIntCount, 
				0, 
				grid.findCount(innerMaterial));
    }
    
    /**
     * Test that two cubes with different material.
     */
    public void testMaterialofTwoShapes() {
    	
        float width = 0.0799f;
        float height = 0.0799f;
        float depth = 0.0799f;
        byte outerMaterial1 = 1;
        byte innerMaterial1 = 2;
        byte outerMaterial2 = 5;
        byte innerMaterial2 = 6;

        // set the grid size for two cubes with a slight over allocation
        int gWidth = (int) Math.round((width / HORIZ_RESOLUTION)) * 3;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = (int) (depth / HORIZ_RESOLUTION) + 10;
        
        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
//System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

        //-------------------------------------------------------
        // set up and add the first cube
        //-------------------------------------------------------
        
        // translate the first cube so it does not occupy one more row, height, and depth than it should
        // by having the "left" side start on a grid line
        int indexOffset = 5;
		int xVoxels1 = (int) Math.round(width / HORIZ_RESOLUTION);
		int yVoxels1 = (int) Math.round(height / VERT_RESOLUTION);
		int zVoxels1 = (int) Math.round(depth / HORIZ_RESOLUTION);

		double translateX = ((double)xVoxels1 / 2.0 + indexOffset) * HORIZ_RESOLUTION;
        double translateY = ((double)yVoxels1 / 2.0 + indexOffset) * VERT_RESOLUTION;
        double translateZ = ((double)zVoxels1 / 2.0 + indexOffset) * HORIZ_RESOLUTION;
        
//		double translateX = Math.abs(Math.round((width / 2.0f) * 1000f) / 1000f) + indexOffset * HORIZ_RESOLUTION;
//        double translateY = Math.abs(Math.round((height / 2.0f) * 1000f) / 1000f) + indexOffset * VERT_RESOLUTION;
//        double translateZ = Math.abs(Math.round((depth / 2.0f) * 1000f) / 1000f) + indexOffset * HORIZ_RESOLUTION;
        
//System.out.println("translateX: " + translateX);
//System.out.println("translateY: " + translateY);
//System.out.println("translateZ: " + translateZ);

        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial1, innerMaterial1, 
        		GeometryData.TRIANGLES, true);

//		int expectedMat1Count = xVoxels1 * yVoxels1 * zVoxels1;

//System.out.println("mat 1 count: " + grid.findCount(innerMaterial1));
//        assertEquals("Material 1 count before adding second cube is not " + expectedMat1Count, 
//        		expectedMat1Count, grid.findCount(innerMaterial1));

        //-------------------------------------------------------
        // set up and add the second cube of different size
        //-------------------------------------------------------

        width = 0.0399f;
        height = 0.0399f;
        depth = 0.0399f;

		int xVoxels2 = (int) Math.round(width / HORIZ_RESOLUTION);
		int yVoxels2 = (int) Math.round(height / VERT_RESOLUTION);
		int zVoxels2 = (int) Math.round(depth / HORIZ_RESOLUTION);
        int xStartIndex2 = xVoxels1 + 2 * indexOffset;

		translateX = (xStartIndex2 + xVoxels2 / 2.0) * HORIZ_RESOLUTION;
        translateY = ((double)yVoxels2 / 2.0 + indexOffset) * VERT_RESOLUTION;
        translateZ = ((double)zVoxels2 / 2.0 + indexOffset) * HORIZ_RESOLUTION;
        
//System.out.println("translateX: " + translateX);
//System.out.println("translateY: " + translateY);
//System.out.println("translateZ: " + translateZ);

        createCubeInGrid(grid, width, height, depth, 
        		translateX, translateY, translateZ,
        		outerMaterial2, innerMaterial2, 
        		GeometryData.TRIANGLES, true);

		int expectedExtCount1 = getCubeExteriorVoxelCount(xVoxels1, yVoxels1, zVoxels1);
		int expectedIntCount1 = getCubeInteriorVoxelCount(xVoxels1, yVoxels1, zVoxels1);
		int expectedExtCount2 = getCubeExteriorVoxelCount(xVoxels2, yVoxels2, zVoxels2);
		int expectedIntCount2 = getCubeInteriorVoxelCount(xVoxels2, yVoxels2, zVoxels2);

		// check exterior and interior voxel counts
		assertEquals("Exterior count for both cubes is not " + expectedExtCount1 + expectedExtCount2, 
				expectedExtCount1 + expectedExtCount2, 
				grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count for both cubes is not " + expectedIntCount1 + expectedIntCount2, 
				expectedIntCount1 + expectedIntCount2, 
				grid.findCount(VoxelClasses.INTERIOR));
		
		// check outer and inner material counts for cube 1
		assertEquals("Outer material count for cube 1 is not " + expectedExtCount1, 
				expectedExtCount1, 
				grid.findCount(outerMaterial1));
		
		assertEquals("Inner material count for cube 1 is not " + expectedIntCount1, 
				expectedIntCount1, 
				grid.findCount(innerMaterial1));
		
		// check outer and inner material counts for cube 2
		assertEquals("Outer material count for cube 2 is not " + expectedExtCount2, 
				expectedExtCount2, 
				grid.findCount(outerMaterial2));
		
		assertEquals("Inner material count for cube 2 is not " + expectedIntCount2, 
				expectedIntCount2, 
				grid.findCount(innerMaterial2));

		// check that the state and material is correct for each filled grid coordinate of cubes
		checkCubeOnlyVoxelStates(grid, outerMaterial1, innerMaterial1, 
				indexOffset, indexOffset, indexOffset, xVoxels1, yVoxels1, zVoxels1);
		
//System.out.println("xStartIndex2: " + xStartIndex2);
		checkCubeOnlyVoxelStates(grid, outerMaterial2, innerMaterial2, 
				xStartIndex2, indexOffset, indexOffset, xVoxels2, yVoxels2, zVoxels2);

    }
    
    /**
     * Test the voxelization of a simple triangle cylinder.
     */
    public void testCylinder() {
    	
    	// Use 0.0999 instead of 0.1 voxelization is unpredictable when model
    	// lines up exactly with a grid
        float radius = 0.0499f;
        float height = 0.0999f;
        byte outerMaterial = 1;
        byte innerMaterial = 2;

		double translateX = Math.round(radius * 1000f) / 1000f;
		double translateY = Math.round((height / 2.0f) * 1000f) / 1000f;
		double translateZ = Math.round(radius * 1000f) / 1000f;
		
        Grid grid = createCylinderInGrid(height, radius, 
        		translateX, translateY, translateZ,
        		outerMaterial, innerMaterial, GeometryData.TRIANGLES);

//System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());
		
//		int radiusInVoxels = (int) Math.round(radius / HORIZ_RESOLUTION);
//		int areaInVoxels = (int) Math.round(Math.PI * (double) (radiusInVoxels * radiusInVoxels));
//		int heightInVoxels = (int) Math.round(height / VERT_RESOLUTION);
//		int expectedMatCount = areaInVoxels * heightInVoxels;
//System.out.println("radiusInVoxels: " + radiusInVoxels);
//System.out.println("areaInVoxels: " + areaInVoxels);
//System.out.println("expectedMatCount: " + expectedMatCount);


		checkCylinder(grid, height, radius, outerMaterial, innerMaterial);

    }
    
    /**
     * Creates a simple cube in a grid and returns the grid.
     * 
     * @param cWidth The width of the cube
     * @param cHeight The height of the cube
     * @param cDepth The depth of the cube
     * @param outerMaterial The outer material
     * @param innerMaterial The inner material
     * @return The grid containing the cube
     */
    private static void createCubeInGrid(Grid grid, float cWidth, float cHeight, float cDepth,
    		double translateX, double translateY, double translateZ,
    		byte outerMaterial, byte innerMaterial, int geomType, boolean fill) {
    	
        BoxGenerator bg = new BoxGenerator(cWidth, cHeight, cDepth);
        GeometryData geom = new GeometryData();
        geom.geometryType = geomType;
        bg.generate(geom);

//        double bounds = findMaxBounds(geom);
//System.out.println("geometry bounds: " + bounds);

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom,translateX,translateY,translateZ,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,fill);

        tmc.generate(grid);
    }
    
    /**
     * Creates a simple cube in a grid and returns the grid.
     * 
     * @param cWidth The width of the cube
     * @param cHeight The height of the cube
     * @param cDepth The depth of the cube
     * @param outerMaterial The outer material
     * @param innerMaterial The inner material
     * @return The grid containing the cube
     */
    private static Grid createCylinderInGrid(float height, float radius, 
    		double translateX, double translateY, double translatZ,
    		byte outerMaterial, byte innerMaterial, int geomType) {
    	
        CylinderGenerator cg = new CylinderGenerator(height, radius);
        GeometryData geom = new GeometryData();
        geom.geometryType = geomType;
        cg.generate(geom);

//        double bounds = findMaxBounds(geom);
//System.out.println("geometry bounds: " + bounds);
        
        // twice the bounds (since centered at origin) plus a slight over allocate
        int gWidth = (int) (2.0f * radius / HORIZ_RESOLUTION) + 10;
        int gHeight = (int) (height / VERT_RESOLUTION) + 10;
        int gDepth = gWidth;

//System.out.println("grid dimensions: " + gWidth + " " + gHeight + " " + gDepth);

        Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
        
//        double x = bounds;// + 5.0 * HORIZ_RESOLUTION;
//        double y = x;
//        double z = x;

        double rx = 0,ry = 1,rz = 0,rangle = 0;

        TriangleModelCreator tmc = null;
        tmc = new TriangleModelCreator(geom, translateX, translateY, translatZ,
            rx,ry,rz,rangle,outerMaterial,innerMaterial,true);

        tmc.generate(grid);
        
        return grid;
    }
    
    /**
     * Check all voxels for correctness of its state. This function assumes a voxelized
     * cube in the grid and the starting index is the same in all three axes. Works
     * only for cubes aligned with the axes.
     * 
     * @param grid The grid
     * @param startIndex The starting index of the cube
     * @param xVoxels The number of cube voxels in the x axis
     * @param yVoxels The number of cube voxels in the y axis
     * @param zVoxels The number of cube voxels in the z axis
     */
    private void checkCubeVoxelStates(Grid grid, int outerMaterial, int innerMaterial, 
    		int xStartIndex, int yStartIndex, int zStartIndex, 
    		int xVoxels, int yVoxels, int zVoxels) {
    	
//System.out.println("cube interior count: " + grid.findCount(VoxelClasses.INTERIOR));
//System.out.println("cube exterior count: " + grid.findCount(VoxelClasses.EXTERIOR));   

		int xEndIndex = xStartIndex + xVoxels - 1;
		int yEndIndex = yStartIndex + yVoxels - 1;
		int zEndIndex = zStartIndex + zVoxels - 1;

//System.out.println("xEndIndex: " + xEndIndex);
//System.out.println("yEndIndex: " + yEndIndex);
//System.out.println("zEndIndex: " + zEndIndex);

		// Check each voxel for state correctness
        for (int x=0; x<grid.getWidth(); x++) {
        	for (int y=0; y<grid.getHeight(); y++) {
        		for (int z=0; z<grid.getDepth(); z++) {
//System.out.println("[" + x + ", " + y + ", " + z + "]: " + grid.getState(x, y, z));
					
        			// If x or y or z is at the starting index of the cube voxels,
					// and the other coordinates are less than the ending index
					// of the cube voxels, it is an exterior voxel. If x or y or z
					// is at the ending index and the other coordinates are less
					// than the ending index, it is an exterior voxel.
					//
					// If the index is between the starting and ending cube voxels,
					// exclusive, it is an interior voxel.
        			
					if ( (x == xStartIndex && (y >= yStartIndex && y <= yEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (y == yStartIndex && (x >= xStartIndex && x <= xEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (z == zStartIndex && (x >= xStartIndex && x <= xEndIndex) && (y >= yStartIndex && y <= yEndIndex) ) ||
						 (x == (xEndIndex) && (y >= yStartIndex && y <= yEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (y == (yEndIndex) && (x >= xStartIndex && x <= xEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (z == (zEndIndex) && (x >= xStartIndex && x <= xEndIndex) && (y >= yStartIndex && y <= yEndIndex) ) ) {
						
						assertEquals("State is not exterior for grid: " + x + ", " + y + ", " + z,
								Grid.EXTERIOR, grid.getState(x, y, z));
						
						assertEquals("Outer material is not " + outerMaterial + " for grid: " + x + ", " + y + ", " + z,
								outerMaterial, grid.getMaterial(x, y, z));
						
					} else if (x > xStartIndex && x < (xEndIndex) &&
							   y > yStartIndex && y < (yEndIndex) &&
							   z > zStartIndex && z < (zEndIndex)) {
						
						assertEquals("State is not interior for grid: " + x + ", " + y + ", " + z,
								Grid.INTERIOR, grid.getState(x, y, z));
						
						assertEquals("Inner material is not " + innerMaterial + " for grid: " + x + ", " + y + ", " + z,
								innerMaterial, grid.getMaterial(x, y, z));
						
					} else {
						assertEquals("State is not outside for grid: " + x + ", " + y + ", " + z,
								Grid.OUTSIDE, grid.getState(x, y, z));
					}
        		}
        	}        	
        }
    }
    
    /**
     * Check all voxels for correctness of its state. This function assumes a voxelized
     * cube in the grid and the starting index is the same in all three axes. Works
     * only for cubes aligned with the axes.
     * 
     * @param grid The grid
     * @param startIndex The starting index of the cube
     * @param xVoxels The number of cube voxels in the x axis
     * @param yVoxels The number of cube voxels in the y axis
     * @param zVoxels The number of cube voxels in the z axis
     */
    private void checkCubeOnlyVoxelStates(Grid grid, int outerMaterial, int innerMaterial, 
    		int xStartIndex, int yStartIndex, int zStartIndex, 
    		int xVoxels, int yVoxels, int zVoxels) { 

		int xEndIndex = xStartIndex + xVoxels - 1;
		int yEndIndex = yStartIndex + yVoxels - 1;
		int zEndIndex = zStartIndex + zVoxels - 1;

//System.out.println("xEndIndex: " + xEndIndex);
//System.out.println("yEndIndex: " + yEndIndex);
//System.out.println("zEndIndex: " + zEndIndex);

		// Check each voxel for state correctness
        for (int x=0; x<grid.getWidth(); x++) {
        	for (int y=0; y<grid.getHeight(); y++) {
        		for (int z=0; z<grid.getDepth(); z++) {
//System.out.println("[" + x + ", " + y + ", " + z + "]: " + grid.getState(x, y, z));
					
        			// If x or y or z is at the starting index of the cube voxels,
					// and the other coordinates are less than the ending index
					// of the cube voxels, it is an exterior voxel. If x or y or z
					// is at the ending index and the other coordinates are less
					// than the ending index, it is an exterior voxel.
					//
					// If the index is between the starting and ending cube voxels,
					// exclusive, it is an interior voxel.
        			
					if ( (x == xStartIndex && (y >= yStartIndex && y <= yEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (y == yStartIndex && (x >= xStartIndex && x <= xEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (z == zStartIndex && (x >= xStartIndex && x <= xEndIndex) && (y >= yStartIndex && y <= yEndIndex) ) ||
						 (x == (xEndIndex) && (y >= yStartIndex && y <= yEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (y == (yEndIndex) && (x >= xStartIndex && x <= xEndIndex) && (z >= zStartIndex && z <= zEndIndex) ) || 
						 (z == (zEndIndex) && (x >= xStartIndex && x <= xEndIndex) && (y >= yStartIndex && y <= yEndIndex) ) ) {
						
						assertEquals("State is not exterior for grid: " + x + ", " + y + ", " + z,
								Grid.EXTERIOR, grid.getState(x, y, z));
						
						assertEquals("Outer material is not " + outerMaterial + " for grid: " + x + ", " + y + ", " + z,
								outerMaterial, grid.getMaterial(x, y, z));
						
					} else if (x > xStartIndex && x < (xEndIndex) &&
							   y > yStartIndex && y < (yEndIndex) &&
							   z > zStartIndex && z < (zEndIndex)) {
						
						assertEquals("State is not interior for grid: " + x + ", " + y + ", " + z,
								Grid.INTERIOR, grid.getState(x, y, z));
						
						assertEquals("Inner material is not " + innerMaterial + " for grid: " + x + ", " + y + ", " + z,
								innerMaterial, grid.getMaterial(x, y, z));
					}
        		}
        	}        	
        }
    }
    
    /**
     * Check all voxels for correctness of its state. This function assumes a voxelized
     * cube in the grid and the starting index is the same in all three axes. Works
     * only for cubes aligned with the axes.
     * 
     * @param grid The grid
     * @param startIndex The starting index of the cube
     * @param xVoxels The number of cube voxels in the x axis
     * @param yVoxels The number of cube voxels in the y axis
     * @param zVoxels The number of cube voxels in the z axis
     */
    private void checkCylinder(Grid grid, float height, float radius, 
    		byte outerMaterial, byte innerMaterial) {
    	
    	int outerMatPerSlice = 0;
    	int innerMatPerSlice = 0;
    	int exteriorPerSlice = 0;
    	int interiorPerSlice = 0;
    	
    	int heightInVoxels = (int) Math.round(height / VERT_RESOLUTION);
    	int midHeight = heightInVoxels / 2;
    	byte state;
    	int mat;
    	
		// Material count per slice of the cylinder (not counting the top and bottom slice)
        for (int x=0; x<grid.getWidth(); x++) {
    		for (int z=0; z<grid.getDepth(); z++) {
    			
    			mat = grid.getMaterial(x, midHeight, z);
    			if (mat == outerMaterial) {
    				outerMatPerSlice++;
    			} else if (mat == innerMaterial) {
    				innerMatPerSlice++;
    			}
    			
    			state = grid.getState(x, midHeight, z);
				if (state == Grid.INTERIOR) {
					interiorPerSlice++;
				} else if (state == Grid.EXTERIOR) {
					exteriorPerSlice++;
				}
    		}
        }
        
//System.out.println("outerMatPerSlice: " + outerMatPerSlice);
//System.out.println("innerMatPerSlice: " + innerMatPerSlice);
//System.out.println("heightInVoxels: " + heightInVoxels);
//System.out.println("interiorPerSlice: " + interiorPerSlice);
//System.out.println("exteriorPerSlice: " + exteriorPerSlice);

		int expectedInteriorCount = interiorPerSlice * (heightInVoxels - 2);
		int expectedExteriorCount = exteriorPerSlice * heightInVoxels + 2 * interiorPerSlice;
		int expectedOutMatCount = expectedExteriorCount;
		int expectedInMatCount = expectedInteriorCount;
		
		assertEquals("Exterior count is not " + expectedExteriorCount, 
				expectedExteriorCount, grid.findCount(VoxelClasses.EXTERIOR));
		
		assertEquals("Interior count is not " + expectedInteriorCount, 
				expectedInteriorCount, grid.findCount(VoxelClasses.INTERIOR));
		
		assertEquals("Outer material count is not " + expectedOutMatCount, 
				expectedOutMatCount, grid.findCount(outerMaterial));
		
		assertEquals("Outer material count is not " + expectedInMatCount, 
				expectedInMatCount, grid.findCount(innerMaterial));
    }
    
    /**
     * Find the absolute maximum bounds of a geometry.
     *
     * @return The max
     */
    private static double findMaxBounds(GeometryData geom) {
        double max = Double.NEGATIVE_INFINITY;

        int len = geom.coordinates.length;

        for(int i=0; i < len; i++) {
            if (geom.coordinates[i] > max) {
                max = geom.coordinates[i];
            }
        }

        return Math.abs(max);
    }
    
    /**
     * Get the total number of voxels of a cube.
     * 
     * @param xVoxels Number of voxels in the x direction
     * @param yVoxels Number of voxels in the y direction
     * @param zVoxels Number of voxels in the z direction
     * @return The number of voxels of the cube
     */
    private int getCubeTotalVoxelCount(int xVoxels, int yVoxels, int zVoxels) {
    	return xVoxels * yVoxels * zVoxels;
    }
    
    /**
     * Get the total number of exterior voxels of a cube.
     * 
     * @param xVoxels Number of voxels in the x direction
     * @param yVoxels Number of voxels in the y direction
     * @param zVoxels Number of voxels in the z direction
     * @return The number of exterior voxels of the cube
     */
    private int getCubeExteriorVoxelCount(int xVoxels, int yVoxels, int zVoxels) {
        // expected number of exterior voxels should be the following formula:
        //   (exteriorVoxels per face in XY plane * 2) + 
        //   (exteriorVoxels per face in XZ plane * 2) + 
        //   (exteriorVoxels per face in YZ plane * 2) -
        //   (numEdges in X dir * gridWidth) -
        //   (numEdges in Y dir * gridHeight) -
        //   (numEdges in Z dir * gridDepth) +
        //   (number of corners)
		return (xVoxels * yVoxels * 2) + (xVoxels * zVoxels * 2) + (yVoxels * zVoxels * 2) - 
			   (4 * xVoxels) - (4 * yVoxels) - (4 * zVoxels) + 8;
    }
    
    /**
     * Get the total number of interior voxels of a cube.
     * 
     * @param xVoxels Number of voxels in the x direction
     * @param yVoxels Number of voxels in the y direction
     * @param zVoxels Number of voxels in the z direction
     * @return The number of interior voxels of the cube
     */
    private int getCubeInteriorVoxelCount(int xVoxels, int yVoxels, int zVoxels) {
		return (xVoxels - 2) * (yVoxels - 2) * (xVoxels - 2);
    }
    
    /**
     * Generate an X3D file 
     * @param filename
     */
/*    public static void generate(String filename) {
        try {
        	ErrorReporter console = new PlainTextErrorReporter();

            long stime = System.currentTimeMillis();
/*            
        	// Use 0.0999 instead of 0.01 voxelization is unpredictable when model
        	// lines up exactly with a grid
            float width = 0.0999f;
            float height = 0.0999f;
            float depth = 0.0999f;
            byte outerMaterial = 1;
            byte innerMaterial = 1;
            
            // twice the bounds (since centered at origin) plus a slight over allocate
            int gWidth = (int) (width / HORIZ_RESOLUTION) * 2 + 10;
            int gHeight = (int) (height / VERT_RESOLUTION) * 2 + 10;
            int gDepth = (int) (depth / HORIZ_RESOLUTION) * 2 + 10;
            
            Grid grid = new ArrayGrid(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
            
            int translateXVoxels = 0;
            int translateYVoxels = 0;
            int translateZVoxels = 0;
            
            createCubeInGrid(grid, width, height, depth, 
            		translateXVoxels, translateYVoxels, translateZVoxels,
            		outerMaterial, innerMaterial, 
            		GeometryData.TRIANGLES);

            float width = 0.0399f;
            float height = 0.0399f;
            float depth = 0.0399f;
            byte outerMaterial1 = 1;
            byte innerMaterial1 = 1;
            byte outerMaterial2 = 2;
            byte innerMaterial2 = 2;
            
            // set the grid size for two cubes with a slight over allocation
            int gWidth = (int) (width / HORIZ_RESOLUTION) * 2 + 10;
            int gHeight = (int) (height / VERT_RESOLUTION) + 20;
            int gDepth = (int) (depth / HORIZ_RESOLUTION) + 20;
            
            Grid grid = new ArrayGridByte(gWidth, gHeight, gDepth, HORIZ_RESOLUTION, VERT_RESOLUTION);
System.out.println("grid dimensions: " + grid.getWidth() + " " + grid.getHeight() + " " + grid.getDepth());

			OccupiedWrapper wrapper = new OccupiedWrapper(grid);

    		double translateX = (width / 2.0f) + 2.0f * HORIZ_RESOLUTION;
            double translateY = (height / 2.0f) + 2.0f * VERT_RESOLUTION;
            double translateZ = (depth / 2.0f) + 2.0f * HORIZ_RESOLUTION;

System.out.println("=============> CUBE 1");
            createCubeInGrid(wrapper, width, height, depth, 
            		translateX, translateY, translateZ,
            		outerMaterial1, innerMaterial1, 
            		GeometryData.TRIANGLES, true);

            int material1Count = wrapper.findCount(innerMaterial1);

System.out.println("mat 1 count: " + material1Count);

            translateX = 0.08;//3 * translateX + (10.0f * HORIZ_RESOLUTION);
            System.out.println("translateX: " + translateX);

System.out.println("=============> CUBE 2");
            createCubeInGrid(wrapper, width, height, depth, 
            		translateX, translateY, translateZ,
            		outerMaterial2, innerMaterial2, 
            		GeometryData.TRIANGLES, true);

System.out.println("mat 1 count: " + wrapper.findCount(outerMaterial1));
System.out.println("mat 2 count: " + wrapper.findCount(outerMaterial2));

		    FileOutputStream fos = new FileOutputStream(filename);
		    String encoding = filename.substring(filename.lastIndexOf(".")+1);
		    BoxesX3DExporter exporter = new BoxesX3DExporter(encoding, fos, console);

            HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
            colors.put(new Integer(Grid.INTERIOR), new float[] {0,1,0});
            colors.put(new Integer(Grid.EXTERIOR), new float[] {1,0,0});
            colors.put(new Integer(Grid.OUTSIDE), new float[] {0,1,1});

            HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
            transparency.put(new Integer(Grid.INTERIOR), new Float(0));
            transparency.put(new Integer(Grid.EXTERIOR), new Float(0.5));
            transparency.put(new Integer(Grid.OUTSIDE), new Float(0.85));

//            exporter.write(grid, null);
            exporter.writeDebug(grid, colors, transparency);
            
		    System.out.println("Gen time: " + (System.currentTimeMillis() - stime));
		
		    System.out.println("Writing x3d");
		    stime = System.currentTimeMillis();
		
		    exporter.close();
		
		    System.out.println("GenX3D time: " + (System.currentTimeMillis() - stime));
		
		    fos.close();

        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        generate("out.x3db");
    }
*/
}
