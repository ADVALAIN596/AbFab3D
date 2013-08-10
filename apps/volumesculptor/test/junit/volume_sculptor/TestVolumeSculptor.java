/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package volume_sculptor;

// External Imports

import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.TriangleMesh;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import volumesculptor.shell.ExecResult;
import volumesculptor.shell.Main;

import java.io.File;
import static abfab3d.util.Units.MM;

/**
 * Tests the functionality of a VolumeSculptor
 *
 * @author Alan Hudson
 */
public class TestVolumeSculptor extends TestCase {
    private static final String IMGS_DIR =  "apps/volumesculptor/images/";
    private static final String MODELS_DIR =  "apps/volumesculptor/models/";
    private static final String SCRIPTS_DIR =  "apps/volumesculptor/scripts/";

    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestVolumeSculptor.class);
    }

    public void testCoinExample() throws Exception {
        String[] script_args = new String[] {".001", IMGS_DIR + "r5-bird.png", IMGS_DIR + "r5-circle.png" , IMGS_DIR + "r4-unicorn.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/coin_01.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            //var width = 38*MM;
            //var height = 38*MM;
            //var layerHeight = 1*MM;

            // If ImageBitmap worked then the final volume should < 3 full layers of material
            double max_volume = 38 * MM * 38 * MM * 1 * MM * 3;

            assertTrue("Volume", ac.getVolume() < max_volume * 0.9);
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testLightswitchExample() throws Exception {
        String[] script_args = new String[] {MODELS_DIR + "Light_Switch_Plate1.stl",IMGS_DIR + "chinese_lightswitch.png"};

        try {
            File f = new File(SCRIPTS_DIR + "examples/lightswitch.vss");

            String[] args = new String[] {f.toString()};

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

            AreaCalculator ac = new AreaCalculator();
            mesh.getTriangles(ac);

            //var width = 38*MM;
            //var height = 38*MM;
            //var layerHeight = 1*MM;

            double expected_volume = 2.1195704306735023E-5;

            // I can't see the volume of this changing more then 20% without something being broken
            double diff = Math.abs(ac.getVolume() - expected_volume);

            assertTrue("Volume", diff < (expected_volume * 0.2));
        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testSphere() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"0.005"};


        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }
    public void testGyroid() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\n" +
                "\tvar baseFile = args[0];\n" +
                "\tvar grid = load(baseFile);      \n" +
                "\tvar intersect = new Intersection();\n" +
                "\tintersect.add(new DataSourceGrid(grid, 255));\n" +
                "\tintersect.add(new VolumePatterns.Gyroid(10*MM, 1*MM));\n" +
                "\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(intersect);\n" +
                "\n" +
                "\tvar dest = createGrid(grid);\n" +
                "\tmaker.makeGrid(dest);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\volumesculptor\\models\\sphere.stl"};


        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();

            assertNotNull("Mesh",mesh);
            assertTrue("Triangle Count", mesh.getFaceCount() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testCompileError() {

        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(baseFile) {\n" +
                "\n" +
                "\tvar baseFile = args[0];\n" +
                "\tvar grid = loadBad(baseFile);      \n" +
                "\tvar intersect = new Intersection();\n" +
                "\tintersect.add(new DataSourceGrid(grid, 255));\n" +
                "\tintersect.add(new VolumePatterns.Gyroid(10*MM, 1*MM));\n" +
                "\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(intersect);\n" +
                "\n" +
                "\tvar dest = createGrid(grid);\n" +
                "\tmaker.makeGrid(dest);\n" +
                "\t\n" +
                "\treturn dest;\n" +
                "}";

        String[] script_args = new String[] {"C:\\cygwin\\home\\giles\\projs\\abfab3d\\code\\trunk\\apps\\volumesculptor\\models\\sphere.stl"};

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();
            String error = result.getErrors();
            System.out.println("Error String: " + error);
            assertTrue("Error String not empty",error.length() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }

    public void testSecurity() {
        String script = "importPackage(Packages.abfab3d.grid.op);\n" +
                "importPackage(Packages.abfab3d.grid);\n" +
                "importPackage(Packages.abfab3d.datasources);\n" +
                "\n" +
                "function main(args) {\n" +
                "\tvar radius = args[0];" +
                "var f = new java.io.File(\"c:/tmp/foo.txt\");" +
                "\tvar grid = createGrid(-25*MM,25*MM,-25*MM,25*MM,-25*MM,25*MM,.1*MM);\n" +
                "\tvar sphere = new Sphere(radius);\n" +
                "\tvar maker = new GridMaker();\n" +
                "\tmaker.setSource(sphere);\n" +
                "\tmaker.makeGrid(grid);\n" +
                "\t\n" +
                "\treturn grid;\n" +
                "}";

        String[] script_args = new String[] {"0.005"};

        try {
            File f = File.createTempFile("script","vss");

            String[] args = new String[] {f.toString()};
            FileUtils.write(f,script);

            ExecResult result = Main.execMesh(args, script_args);
            TriangleMesh mesh = result.getMesh();
            String error = result.getErrors();
            System.out.println("Error String: " + error);
            assertTrue("Error String not empty",error.length() > 0);

        } catch(Exception e) {
            e.printStackTrace();
            fail("Exception");
        }
    }
}
