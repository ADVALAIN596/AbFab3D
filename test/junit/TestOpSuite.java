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

// External Imports
import junit.framework.TestSuite;
import junit.framework.Test;

// Internal Imports
import abfab3d.grid.op.*;

/**
 * Grid Operation Tests
 *
 * @author Alan Hudson
 * @version
 */
public class TestOpSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Grid Operations Test Suite");

        suite.addTest(TestRemoveMaterial.suite());
        suite.addTest(TestUnion.suite());
        suite.addTest(TestSubtract.suite());
        suite.addTest(TestSetDifference.suite());
        suite.addTest(TestDilationCube.suite());
        suite.addTest(TestErosionCube.suite());
        suite.addTest(TestInteriorFinderVoxelBased.suite());

        return suite;
    }
}
