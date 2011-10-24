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
import abfab3d.path.*;

/**
 * Grid Query Tests
 *
 * @author Tony Wong
 * @version
 */
public class TestPathSuite extends TestSuite {
    public static Test suite() {
        TestSuite suite = new TestSuite("Path Test Suite");

        suite.addTest(TestStraightPath.suite());
        return suite;
    }
}
