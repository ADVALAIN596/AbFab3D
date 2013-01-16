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

package abfab3d.mesh;

// External Imports

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.output.SAVExporter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.j3d.geom.GeometryData;
import org.web3d.util.ErrorReporter;
import org.web3d.util.LongHashMap;
import org.web3d.vrml.export.*;
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import static abfab3d.util.Output.printf;

// Internal Imports

/**
 * Tests the functionality of WingedEdgeMesh
 *
 * @author Alan Hudson
 */
public class TestWingedEdgeTriangleMesh extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestWingedEdgeTriangleMesh.class);
    }

    /**
     * Test that we can create a simple object without crashing.
     *
     * @throws Exception
     */
    public void testBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);

        //we.writeOBJ(System.out);

        Vertex[][] findex = we.getFaceIndexes();
        Vertex v = we.getVertices();
/*
        while (v != null) {
            System.out.println(v);
            v = v.getNext();
        }

        for (int i = 0; i < findex.length; i++) {

            Vertex face[] = findex[i];

            System.out.print("[");
            for (int j = 0; j < face.length; j++) {
                System.out.print(" " + face[j]);
            }
            System.out.println(" ]");

        }
  */

        writeMesh(we, "c:/tmp/pyramid.x3dv");
    }

    public void testCollapse() throws Exception {
        Point3d[] verts = new Point3d[]{
                new Point3d(-0.5, 0, -0.5),
                new Point3d(0.5, 0, -0.5),
                new Point3d(-1, 0, 0),
                new Point3d(0, 0, -0.25),
                new Point3d(1, 0, 0),
                new Point3d(0, 0, 0.5),
                new Point3d(-0.5, 0, 1),
                new Point3d(0.5, 0, 1)
        };
        int faces[][] = new int[][]{{0, 2, 3}, {0, 3, 1}, {1, 3, 4}, {3, 2, 5}, {4, 3, 5}, {2, 6, 5}, {5, 6, 7}, {4, 5, 7}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        //we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/collapse1.x3dv");

        int expected_verts = 8;
        int expected_faces = 8;
        int expected_edges = 15;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());


        double EPS = 1e-8;
        // Find edge from vertex 3 to 5
        Vertex v1 = we.findVertex(verts[3],EPS);
        Vertex v2 = we.findVertex(verts[5],EPS);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        //System.out.println("edge: " + edges);

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[3].x + verts[5].x) / 2.0;
        pos.y = (verts[3].y + verts[5].y) / 2.0;
        pos.z = (verts[3].z + verts[5].z) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();

        boolean result = we.collapseEdge(edges, pos, ecp, ecr);
        assertEquals("Did collapse", result, true);

        writeMesh(we, "c:/tmp/collapse2.x3dv");

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts - 1, we.getVertexCount());
        assertEquals("Face Count", expected_faces - 2, we.getFaceCount());
        assertEquals("Edge Count", expected_edges - 3, we.getEdgeCount());
    }

    public void testDegenerateFace() throws Exception {
        Point3d[] verts = new Point3d[]{
                new Point3d(-1, 0, -1),       // 0
                new Point3d(0, 0, -1),        // 1
                new Point3d(1, 0, -1),        // 2
                new Point3d(0, 0, -0.5),      // 3
                new Point3d(-1, 0, 0),        // 4
                new Point3d(1, 0, 0),         // 5
                new Point3d(0, 0, 0.5),       // 6
                new Point3d(-1, 0, 1),        // 7
                new Point3d(0, 0, 1),         // 8
                new Point3d(1, 0, 1),         // 9
                new Point3d(0.5, 0, 0),       // 10
                new Point3d(0,-1,0)           // 11
        };
        int faces[][] = new int[][]{{1, 0, 3}, {2, 1, 3}, {0, 4, 3}, {2, 3, 5}, {3, 4, 6}, {3, 6, 10}, {3, 10, 5},
                {10, 6, 5}, {4, 7, 6}, {6, 7, 8}, {6, 8, 9}, {5, 6, 9},
                // base
                {7,4,11},
                {4,0,11},
                {2,5,11},
                {5,9,11},
                {0,1,11},
                {1,2,11},
                {9,8,11},
                {8,7,11},
        };

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        //we.writeOBJ(System.out);

        writeMesh(we, "c:/tmp/degenface1.x3dv");

        int expected_verts = 12;
        int expected_faces = 20;
        int expected_edges = 30;
        assertEquals("Initial Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Initial Face Count", expected_faces, we.getFaceCount());
        assertEquals("Initial Edge Count", expected_edges, we.getEdgeCount());
        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Structural Check", verifyStructure(we, true));


        double EPS = 1e-8;
        // Find edge from vertex 3 to 5
        Vertex v1 = we.findVertex(verts[3],EPS);
        Vertex v2 = we.findVertex(verts[6],EPS);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        //System.out.println("edge: " + edges);

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[3].x + verts[6].x) / 2.0;
        pos.y = (verts[3].y + verts[6].y) / 2.0;
        pos.z = (verts[3].z + verts[6].z) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();
        boolean result = we.collapseEdge(edges, pos, ecp, ecr);

        assertEquals("Did not collapse", result, false);

        writeMesh(we, "c:/tmp/degenface2.x3dv");
        //we.writeOBJ(System.out);
        assertTrue("Structural Check", verifyStructure(we, false));

        //we.removeDegenerateFaces();
        writeMesh(we, "c:/tmp/degenface3.x3dv");
        //we.writeOBJ(System.out);
        assertTrue("Structural Check", verifyStructure(we, false));

        // verify number of vertices remaining
        assertEquals("Vertex Count", expected_verts, we.getVertexCount());
        assertEquals("Face Count", expected_faces, we.getFaceCount());
        assertEquals("Edge Count", expected_edges, we.getEdgeCount());
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testBox() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                1, 2, 3,
                0, 1, 3,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);
        int edge_cnt = we.getEdgeCount();
        int face_cnt = we.getFaceCount();

        writeMesh(we, "c:/tmp/box.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Structural Check", verifyStructure(we, true));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 1 to 2
        double EPS = 1e-8;

        Vertex v1 = we.findVertex(verts[1], EPS);
        Vertex v2 = we.findVertex(verts[2], EPS);

        assertNotNull("Vertex1", v1);
        assertNotNull("Vertex2", v2);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[1].x + verts[2].x) / 2.0;
        pos.y = (verts[1].y + verts[2].y) / 2.0;
        pos.z = (verts[1].z + verts[2].z) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();
        we.collapseEdge(edges, pos, ecp, ecr);

/*
        System.out.println("Removed Edges: " + ecr.removedEdges);
        for(Edge e : ecr.removedEdges) {
            System.out.println("e: " + e.hashCode());
        }
*/
        writeMesh(we, "c:/tmp/box2.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));
        assertTrue("Structural Check", verifyStructure(we, true));

        int removed_edges = 3;
        int removed_faces = 2;
        assertEquals("Edge count", edge_cnt - removed_edges, we.getEdgeCount());
        assertEquals("Removed Edges", removed_edges, ecr.removedEdges.size());
        assertEquals("Removed Faces",face_cnt - removed_faces,we.getFaceCount());
    }

    /**
     * Test a box is manifold on construction and edge collapse where its mixed oriented to cause manifold failure
     */
    public void testBoxMixedOrientation() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                2, 3, 0,
                1, 2, 0,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);
        int edge_cnt = we.getEdgeCount();

        writeMesh(we, "c:/tmp/box.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        // Find edge from vertex 0 to 2
        double EPS = 1e-8;
        Vertex v1 = we.findVertex(verts[0], EPS);
        Vertex v2 = we.findVertex(verts[2], EPS);

        Edge edges = we.getEdges();
        HalfEdge he = null;

        boolean found = false;

        while (edges != null) {
            he = edges.getHe();

            if ((he.getStart() == v1 && he.getEnd() == v2) ||
                    (he.getStart() == v2 && he.getEnd() == v1)) {
                found = true;
                break;
            }

            edges = edges.getNext();
        }

        if (!found) {
            fail("Edge not found");
        }

        // use center point of vertices as new pos
        Point3d pos = new Point3d();
        pos.x = (verts[1].x + verts[2].x) / 2.0;
        pos.y = (verts[1].y + verts[2].y) / 2.0;
        pos.z = (verts[1].z + verts[2].z) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();
        we.collapseEdge(edges, pos, ecp, ecr);

        //we.writeOBJ(System.out);
        writeMesh(we, "c:/tmp/box2.x3dv");

        assertTrue("Manifold2", isManifold(we));

        assertTrue("Triangle Check2", verifyTriangles(we));

        // No change expected
        assertEquals("Edge count", edge_cnt, we.getEdgeCount());
        assertEquals("Removed Edges", 0, ecr.removedEdges.size());

    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldSpeedKnot() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/speed-knot.x3db"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/speed-knot1.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 1000;
        int valid = 0;

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(we.getEdgeCount());

            Edge e = we.getEdges();

            for (int j = 0; j < idx; j++) {
                e = e.getNext();
            }


            //System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            Point3d p1 = e.getHe().getStart().getPoint();
            Point3d p2 = e.getHe().getEnd().getPoint();
            pos.x = (p1.x + p2.x) / 2.0;
            pos.y = (p1.y + p2.y) / 2.0;
            pos.z = (p1.z + p2.z) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            if (we.collapseEdge(e, pos, ecp, ecr)) {
                valid++;
                //System.out.println("Collapsed Edge: " + valid);
                //writeMesh(we, "c:/tmp/speed-knot_loop" + i + ".x3dv");
                assertTrue("Manifold", isManifold(we));
                assertTrue("Triangle Check", verifyTriangles(we));
                //assertTrue("Structural Check", verifyStructure(we,true));
            }
        }

        assertTrue("Structural Check", verifyStructure(we, true));
        writeMesh(we, "c:/tmp/speed-knot2.x3dv");
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldSphere() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/sphere_10cm_rough_manifold.x3dv"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/sphere_10cm_rough_manifold1.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 158;
        int valid = 0;

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(we.getEdgeCount());

            Edge e = we.getEdges();

            for (int j = 0; j < idx; j++) {
                e = e.getNext();
            }

            if (e == null) {
                System.out.println("Cannot find egde?");
                continue;
            }
            //System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            Point3d p1 = e.getHe().getStart().getPoint();
            Point3d p2 = e.getHe().getEnd().getPoint();
            pos.x = (p1.x + p2.x) / 2.0;
            pos.y = (p1.y + p2.y) / 2.0;
            pos.z = (p1.z + p2.z) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            if (we.collapseEdge(e, pos, ecp, ecr)) {
                valid++;
                //writeMesh(we, "c:/tmp/sphere_10cm_rough_manifold_loop" + i + ".x3dv");
            }


            assertTrue("Manifold", isManifold(we));
            assertTrue("Structural Check", verifyStructure(we,true));
        }

        writeMesh(we, "c:/tmp/sphere_10cm_rough_manifold2.x3dv");
        assertTrue("Structural Check", verifyStructure(we, true));
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldE() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("test/models/wTest1_ITS.x3d"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/etest1.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifold(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        Random rand = new Random(42);
        int collapses = 10;

        for (int i = 0; i < collapses; i++) {
            idx = rand.nextInt(faces.length / 3);

            Edge e = we.getEdges();

            for (int j = 0; j < idx; j++) {
                e = e.getNext();
            }


            //System.out.println("Collapse: " + idx + " e: " + e);
            Point3d pos = new Point3d();
            Point3d p1 = e.getHe().getStart().getPoint();
            Point3d p2 = e.getHe().getEnd().getPoint();
            pos.x = (p1.x + p2.x) / 2.0;
            pos.y = (p1.y + p2.y) / 2.0;
            pos.z = (p1.z + p2.z) / 2.0;

            EdgeCollapseResult ecr = new EdgeCollapseResult();
            EdgeCollapseParams ecp = new EdgeCollapseParams();
            we.collapseEdge(e, pos, ecp, ecr);

            writeMesh(we, "c:/tmp/etest_loop" + i + ".x3dv");
            //we.writeOBJ(System.out);

            assertTrue("Manifold", isManifold(we));
            assertTrue("Triangle Check", verifyTriangles(we));
        }

        assertTrue("Manifold2", isManifold(we));
        assertTrue("Triangle Check2", verifyTriangles(we));

        writeMesh(we, "c:/tmp/etest2.x3dv");
    }

    /**
     * Test a box is manifold on construction and edge collapse
     */
    public void testManifoldError() throws Exception {
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        loader.processFile(new File("c:/tmp/debug.x3dv"));

        GeometryData data = new GeometryData();
        data.geometryType = GeometryData.INDEXED_TRIANGLES;
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        writeMesh(we, "c:/tmp/me1.x3dv");

        //we.writeOBJ(System.out);

        assertTrue("Initial Manifold", isManifoldOver(we));
        assertTrue("Initial Triangle Check", verifyTriangles(we));

        idx = 1;

        Edge e = we.getEdges();

        for (int j = 0; j < idx; j++) {
            e = e.getNext();
        }


        //System.out.println("Collapse: " + idx + " e: " + e);
        Point3d pos = new Point3d();
        Point3d p1 = e.getHe().getStart().getPoint();
        Point3d p2 = e.getHe().getEnd().getPoint();
        pos.x = (p1.x + p2.x) / 2.0;
        pos.y = (p1.y + p2.y) / 2.0;
        pos.z = (p1.z + p2.z) / 2.0;

        EdgeCollapseResult ecr = new EdgeCollapseResult();
        EdgeCollapseParams ecp = new EdgeCollapseParams();
        we.collapseEdge(e, pos, ecp, ecr);

        //we.writeOBJ(System.out);
        writeMesh(we, "c:/tmp/me2.x3dv");

        assertTrue("Manifold2", isManifoldOver(we));
        assertTrue("Triangle Check2", verifyTriangles(we));
    }

    private void writeMesh(WingedEdgeTriangleMesh we, String filename) throws IOException {
        SAVExporter se = new SAVExporter();
        HashMap<String, Object> params = new HashMap<String, Object>();

        FileOutputStream fos = null;

        try {
            BinaryContentHandler writer = null;
            fos = new FileOutputStream(filename);
            String encoding = filename.substring(filename.lastIndexOf(".") + 1);

            ErrorReporter console = new PlainTextErrorReporter();

            if (encoding.equals("x3db")) {
                writer = new X3DBinaryRetainedDirectExporter(fos,
                        3, 0, console,
                        X3DBinarySerializer.METHOD_FASTEST_PARSING,
                        0.001f, true);
            } else if (encoding.equals("x3dv")) {
                writer = new X3DClassicRetainedExporter(fos, 3, 0, console);
            } else if (encoding.equals("x3d")) {
                writer = new X3DXMLRetainedExporter(fos, 3, 0, console);
            } else {
                throw new IllegalArgumentException("Unhandled X3D encoding: " + encoding);
            }

            writer.startDocument("", "", "utf8", "#X3D", "V3.0", "");
            writer.profileDecl("Immersive");
            writer.startNode("NavigationInfo", null);
            writer.startField("avatarSize");
            writer.fieldValue(new float[]{0.01f, 1.6f, 0.75f}, 3);
            writer.endNode(); // NavigationInfo

            se.outputX3D(we, params, writer, null);
            writer.endDocument();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Test whether a mesh is manifold
     *
     * @param mesh
     * @return
     */
    public static boolean isManifold(WingedEdgeTriangleMesh mesh) {
        // Check via twins structure
        boolean manifold = true;
        Edge edges = mesh.getEdges();

        while (edges != null) {
            HalfEdge he = edges.getHe();
            HalfEdge twin = he.getTwin();

            if (twin == null) {
                manifold = false;

                System.out.println("NonManifold edge: " + edges + " he: " + he);
                return false;
            }

            edges = edges.getNext();
        }


        // check via counts

        Vertex[][] faces = mesh.getFaceIndexes();

        int len = faces.length;

        LongHashMap edgeCount = new LongHashMap();

        for (int i = 0; i < len; i++) {
            //System.out.println("Count face: " + faces[i][0].getID() + " " + faces[i][1].getID() + " " + faces[i][2].getID());
            processEdge(faces[i][0].getID(), faces[i][1].getID(), edgeCount);
            processEdge(faces[i][1].getID(), faces[i][2].getID(), edgeCount);
            processEdge(faces[i][2].getID(), faces[i][0].getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count != 2) {
                manifold = false;
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

            }
        }

        return manifold;
    }

    /**
     * Test whether a mesh is manifold
     *
     * @param mesh
     * @return
     */
    private boolean isManifoldOver(WingedEdgeTriangleMesh mesh) {
        boolean manifold = true;
        // check via counts

        Vertex[][] faces = mesh.getFaceIndexes();

        int len = faces.length;

        LongHashMap edgeCount = new LongHashMap();

        for (int i = 0; i < len; i++) {
            //System.out.println("Count face: " + faces[i][0].getID() + " " + faces[i][1].getID() + " " + faces[i][2].getID());
            processEdge(faces[i][0].getID(), faces[i][1].getID(), edgeCount);
            processEdge(faces[i][1].getID(), faces[i][2].getID(), edgeCount);
            processEdge(faces[i][2].getID(), faces[i][0].getID(), edgeCount);
        }

        long[] keys = edgeCount.keySet();

        for (int i = 0; i < keys.length; i++) {

            Integer count = (Integer) edgeCount.get(keys[i]);

            if (count > 2) {
                manifold = false;
                int index1 = (int) (keys[i] >> 32);
                int index2 = (int) (keys[i]);

                System.out.println("Invalid edge: " + index1 + "->" + index2 + " cnt: " + count);

            }
        }

        return manifold;
    }

    private static void processEdge(int index1, int index2, LongHashMap edgeCount) {

//System.out.println("Edges being processed: " + index1 + "," + index2);

        long edge;
        int count = 1;

        // place the smallest index first for
        // consistent lookup
        if (index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        // put the larger of the 2 points into the long
        edge = index2;

        // shift the point to the left to make room for
        // the smaller point
        edge <<= 32;

        // bit OR the smaller point into the long
        edge |= index1;

        // add the edge to the count
        if (edgeCount.containsKey(edge)) {
            Integer val = (Integer) edgeCount.get(edge);
            count = val.intValue();
            count++;
        }

        edgeCount.put(edge, new Integer(count));
    }

    /**
     * Test vertex iterator
     *
     * @throws Exception
     */
    public void testVertexIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Vertex> itr = we.vertexIterator(); itr.hasNext(); ) {
            Vertex v = itr.next();
            cnt++;
        }

        int expected_verts = pyr_vert.length;
        assertEquals("Vertex Count", expected_verts, cnt);
    }

    /**
     * Test face iterator
     *
     * @throws Exception
     */
    public void testFaceIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Face> itr = we.faceIterator(); itr.hasNext(); ) {
            Face f = itr.next();
            cnt++;
        }

        int expected_faces = pyr_faces.length;

        assertEquals("Face Count", expected_faces, cnt);
    }

    /**
     * Test edge iterator
     *
     * @throws Exception
     */
    public void testEdgeIteratorBasic() throws Exception {

        Point3d[] pyr_vert = new Point3d[]{new Point3d(-1, -1, -1), new Point3d(1, -1, -1),
                new Point3d(1, 1, -1), new Point3d(-1, 1, -1),
                new Point3d(0, 0, 1)};
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2, 1, 0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);


        int cnt = 0;

        for (Iterator<Edge> itr = we.edgeIterator(); itr.hasNext(); ) {
            Edge e = itr.next();
            cnt++;
        }

        int expected_edges = 9;

        assertEquals("Edge Count", expected_edges, cnt);
    }

    public void testVertexVertexIterator() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                1, 2, 3,
                0, 1, 3,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        double EPS = 1e-8;
        Vertex vert = we.findVertex(verts[2], EPS);
        VertexVertexIterator vii = new VertexVertexIterator(we, vert);

        int cnt = 0;
        while(vii.hasNext()) {
            Vertex v = vii.next();
            //System.out.println("Vertex: " + v.getID());
            cnt++;
        }

        HashSet<Integer> expected_verts = new HashSet<Integer>();
        expected_verts.add(3);
        expected_verts.add(7);
        expected_verts.add(5);
        expected_verts.add(6);
        expected_verts.add(1);

        assertEquals("Vertex Count", expected_verts.size(), cnt);


    }
    public void testVertexEdgeIterator() throws Exception {
        GeometryData data = new GeometryData();

        data.vertexCount = 8;
        data.coordinates = new float[]{
                1, -1, 1,
                1, 1, 1,
                -1, 1, 1,
                -1, -1, 1,
                1, -1, -1,
                1, 1, -1,
                -1, 1, -1,
                -1, -1, -1
        };

        data.indexesCount = 36;
        data.indexes = new int[]{
                1, 2, 3,
                0, 1, 3,

                5, 1, 0,
                4, 5, 0,

                5, 6, 2,
                5, 2, 1,

                2, 6, 7,
                2, 7, 3,

                6, 5, 4,
                7, 6, 4,

                3, 7, 4,
                3, 4, 0
        };

        Point3d[] verts = new Point3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;

        for (int i = 0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Point3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        int faces[][] = new int[len][3];
        idx = 0;

        for (int i = 0; i < len; i++) {
            faces[i][0] = data.indexes[idx++];
            faces[i][1] = data.indexes[idx++];
            faces[i][2] = data.indexes[idx++];
        }

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(verts, faces);

        double EPS = 1e-8;
        Vertex vert = we.findVertex(verts[2],EPS);
        VertexEdgeIterator itr = new VertexEdgeIterator(we, vert);

        int cnt = 0;
        int expected_edges = 5;
        int[][] expected_results = new int[][] {
                {1,2}, {2,3}, {2,7}, {2,6}, {2,5}
        };

        int[][] results = new int[expected_edges][2];

        while(itr.hasNext()) {
            Edge e = itr.next();

            results[cnt][0] = Math.min(e.getHe().getStart().getID(),e.getHe().getEnd().getID());
            results[cnt][1] = Math.max(e.getHe().getStart().getID(),e.getHe().getEnd().getID());
            cnt++;
        }

        assertEquals("Edge Count", expected_edges, cnt);

        int found_cnt = 0;
        for(int i=0; i < expected_results.length; i++) {
            boolean found = false;

            for(int j=0; j < results.length; j++) {
                if (results[j][0] == expected_results[i][0] && results[j][1] == expected_results[i][1]) {
                    found_cnt++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("Didn't find: " + expected_results[i][0] + "->" + expected_results[i][1]);
            }
        }

        assertEquals("Edges found", expected_results.length, found_cnt);
    }

    /**
     * Verify all triangles contain 3 distinct points and area > 0
     *
     * @param mesh
     * @return
     */
    private boolean verifyTriangles(WingedEdgeTriangleMesh mesh) {
        Vector3d svec1 = new Vector3d();
        Vector3d svec2 = new Vector3d();

        for (Face faces = mesh.getFaces(); faces != null; faces = faces.getNext()) {
            Vertex p1;
            Vertex p2;
            Vertex p3;

            p1 = faces.getHe().getStart();
            p2 = faces.getHe().getEnd();

            HalfEdge he = faces.getHe().getNext();

            if (he.getStart() != p1 && he.getStart() != p2) {
                p3 = he.getStart();
            } else if (he.getEnd() != p1 && he.getEnd() != p2) {
                p3 = he.getEnd();
            } else {
                System.out.println("Cannot find third unique point?");
                he = faces.getHe();
                HalfEdge start = he;
                while (he != null) {
                    //System.out.println(he);
                    he = he.getNext();

                    if (he == start) {
                        break;
                    }
                }
                return false;
            }

            double EPS = 1e-10;

            if (p1.getPoint().epsilonEquals(p2.getPoint(), EPS)) {
                System.out.println("Points equal(1,2): " + p1 + " p2: " + p2 + " face: " + faces);
                return false;
            }
            if (p1.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
                System.out.println("Points equal(1,3): " + p1 + " p2: " + p3 + " face: " + faces);
                return false;
            }
            if (p2.getPoint().epsilonEquals(p3.getPoint(), EPS)) {
                System.out.println("Points equal(2,3): " + p2 + " p2: " + p3 + " face: " + faces);
                return false;
            }

            svec1.x = p2.getPoint().x - p1.getPoint().x;
            svec1.y = p2.getPoint().y - p1.getPoint().y;
            svec1.z = p2.getPoint().z - p1.getPoint().z;

            svec2.x = p3.getPoint().x - p1.getPoint().x;
            svec2.y = p3.getPoint().y - p1.getPoint().y;
            svec2.z = p3.getPoint().z - p1.getPoint().z;

            svec1.cross(svec1, svec2);
            double area = svec1.length();

            if (area < EPS) {
                System.out.println("Triangle area 0: " + faces);
                return false;
            }
        }

        return true;
    }

    /**
     * Verify that the mesh structure is correct.  Chase as many pointers and references as we can to confirm that
     * nothing is messed up.
     *
     * @param mesh
     * @return
     */
    public static boolean verifyStructure(WingedEdgeTriangleMesh mesh, boolean manifold) {
        // Walk edges and make sure no referenced head or twin values are null
        // Make sure twin references same vertices

        Iterator<Edge> eitr = mesh.edgeIterator();
        while(eitr.hasNext()) {
            Edge e = eitr.next();

            if (e.getHe() == null) {
                System.out.println("Edge found with null Head: " + e);
                return false;
            }

            if (e.getHe().isRemoved()) {
                System.out.println("Edge removed but used: " + e.getHe());
            }
            HalfEdge twin = e.getHe().getTwin();

            if (manifold && twin == null) {
                System.out.println("Edge found with null Twin: " + e);
                return false;
            }

            if (twin != null) {
                if (twin.isRemoved()) {
                    System.out.println("Twin Edge removed but used: " + e.getHe());
                }
                if (e.getHe().getStart() != twin.getEnd() ||
                    e.getHe().getEnd() != twin.getStart()) {
                    System.out.println("Invalid twins: " + e.getHe() + " twin: " + twin);
                    System.out.println("Invalid twins: " + e.getHe().hashCode() + " twin: " + twin.hashCode());
                    return false;
                }
            }
        }

        // Make sure all faces have three half edges
        // Make sure all edge and face references in halfedge are valid
        // Make sure forward traversal(next) around face is same as backwards(prev)

        Iterator<Face> fitr = mesh.faceIterator();
        while(fitr.hasNext()) {
            Face f = fitr.next();

            HalfEdge he = f.getHe();
            HalfEdge start = he;

            if (he == null) {
                System.out.println("Half edge null: " + f);
                return false;
            }
            int cnt = 0;
            while(he != null) {
                if (!findEdge(mesh, he.getEdge())) {
                    System.out.println("Cannot find edge: " + he.getEdge());
                    return false;
                }
                if (!findFace(mesh, he.getLeft())) {
                    System.out.println("Cannot find face: " + he.getLeft());
                    return false;
                }

                cnt++;
                he = he.getNext();
                if (he == start) {
                    break;
                }
            }

            if (cnt != 3) {
                System.out.println("Face without 3 half edges(next): " + f);
                return false;
            }

            he = f.getHe();
            start = he;
            cnt = 0;
            while(he != null) {
                cnt++;
                he = he.getPrev();
                if (he == start) {
                    break;
                }
            }

            if (cnt != 3) {
                System.out.println("Face without 3 half edges(prev): " + f);
                return false;
            }

        }

        // verify vertex link is bidirectional, ie edge thinks its connected to vertex
        Iterator<Vertex> vitr = mesh.vertexIterator();
        while(vitr.hasNext()) {
            Vertex v = vitr.next();

            if (v.isRemoved()) {
                System.out.println("Vertex removed but linked: " + v);
                return false;
            }
            HalfEdge he = v.getLink();

            if (he == null) {
                System.out.println("Vertex not linked: " + v);
                return false;
            }

            if (he.getStart() != v && he.getEnd() != v) {
                System.out.println("Vertex linkage not bidirectional: " + v + " he: " + he);
            }
        }

        // Check for any edges that are duplicated
        Iterator<Edge> eitr1 = mesh.edgeIterator();
        Iterator<Edge> eitr2 = mesh.edgeIterator();

        while(eitr1.hasNext()) {
            Edge e1 = eitr1.next();

            while(eitr2.hasNext()) {
                Edge e2 = eitr2.next();

                if (e1 == e2) {
                    continue;
                }

                HalfEdge he1 = e1.getHe();
                HalfEdge he2 = e2.getHe();

                if (he1 != null) {
                    if (he1.getStart() == he1.getEnd()) {
                        System.out.println("Collapsed edge detected: " + he1);
                        return false;
                    }
                }
                if (he2 != null) {
                    if (he2.getStart() == he2.getEnd()) {
                        System.out.println("Collapsed edge detected: " + he2);
                        return false;
                    }
                }
                if (he1 != null && he2 != null) {
                    Vertex start1 = he1.getStart();
                    Vertex end1 = he1.getEnd();
                    Vertex start2 = he2.getStart();
                    Vertex end2 = he2.getEnd();

                    if ((start1 == start2 && end1 == end2) ||
                            (start1 == end2 && end1 == start2)) {
                        System.out.println("Duplicate detected: " + e1 + " is: " + e2);
                        return false;
                    }
                }
            }
        }

        vitr = mesh.vertexIterator();

        while(vitr.hasNext()){
            Vertex v = vitr.next();

            //printf("vertex: %s\n", v);

            HalfEdge start = v.getLink();
            HalfEdge he = start;
            int tricount = 0;

            if (start.isRemoved()) {
                System.out.println("Using dead half edge: " + he + " hc: " + he.hashCode());
                return false;
            }

            do{

                //printf("he: %s\n", he + " hc: " + he.hashCode());

                HalfEdge twin = he.getTwin();
                he = twin.getNext();

            } while(he != start && tricount++ < 20);

            if (tricount >= 20) {
                System.out.println("***Strange linking error?");
            }
        }

        return true;
    }

    /**
     * Traverse edges list and make sure an edge is traversible from there.
     *
     * @param mesh
     * @param e
     * @return
     */
    private static boolean findEdge(WingedEdgeTriangleMesh mesh, Edge e) {
        Edge edges = mesh.getEdges();

        while(edges != null) {
            if (edges == e) {
                return true;
            }

            edges = edges.getNext();
        }

        return false;
    }

    /**
     * Traverse edges list and make sure an edge is traversible from there.
     *
     * @param mesh
     * @param f
     * @return
     */
    private static boolean findFace(WingedEdgeTriangleMesh mesh, Face f) {
        Face faces = mesh.getFaces();

        while(faces != null) {
            if (faces == f) {
                return true;
            }

            faces = faces.getNext();
        }

        return false;
    }
}
