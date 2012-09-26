/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2012
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import abfab3d.io.input.IndexedTriangleSetLoader;
import abfab3d.io.input.STLReader;
import abfab3d.io.output.SAVExporter;
import abfab3d.io.output.MeshExporter;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.j3d.geom.*;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.*;

// Internal Imports
import org.web3d.vrml.sav.BinaryContentHandler;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import static abfab3d.util.Output.printf; 
import static abfab3d.util.Output.fmt; 

import static java.lang.System.currentTimeMillis;

/**
 * Tests the functionality of MeshDecimator
 *
 * @author Vladimir Bulatov
 * @version
 */
public class TestMeshDecimator extends TestCase {
    /**
     * Creates a test suite consisting of all the methods that start with "test".
     */
    public static Test suite() {
        return new TestSuite(TestMeshDecimator.class);
    }

    /**
     * Test that we can create a simple object without crashing.
     *
     * @throws Exception
     */
    public void _testPyramid() throws Exception {

        Point3d[] pyr_vert = new Point3d[] {
            new Point3d(-1., -1., -1.), 
            new Point3d( 1., -1., -1.),
            new Point3d( 1.,  1., -1.), 
            new Point3d(-1.,  1., -1.),
            new Point3d( 0.,  0.,  1.), 
        };
        int pyr_faces[][] = new int[][]{{3, 2, 0}, {2,1,0}, {0, 1, 4}, {1, 2, 4}, {2, 3, 4}, {3, 0, 4}};

        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(pyr_vert, pyr_faces);

        //we.writeOBJ(System.out);

        Vertex[][] findex = we.getFaceIndexes();
        Vertex v = we.getVertices();
        while (v != null) {
            System.out.println(v);
            v = v.getNext();
        }
        
        Edge e1 = we.getEdges();
        int ecount = 0;
        while(e1 != null){
            e1.setUserData(new Integer(ecount++));
            e1 = e1.getNext();
        }


        /*
        for (int i = 0; i < findex.length; i++) {

            Vertex face[] = findex[i];

            System.out.print("[");
            for (int j = 0; j < face.length; j++) {
                System.out.print(" " + face[j]);
            }
            System.out.println(" ]");
        }
        */
        //MeshExporter.writeMesh(we,"c:/tmp/pyramid.x3d");

        //MeshDecimator md = new MeshDecimator();

        //printf("startng decimations\n");

        //int count = md.processMesh(we, 100);
        

        verifyVertices(we);

        Edge edge = findEdge(we, 4, 1);

        printf("\nedge to collapse: %s\n" ,edge);
        HalfEdge he = edge.getHe();
        
        Point3d pnt = new Point3d();
        
        pnt.set(he.getStart().getPoint());
        pnt.add(he.getStart().getPoint());
        pnt.scale(0.5);
        EdgeCollapseResult ecr = new EdgeCollapseResult();
        
        we.collapseEdge(edge, pnt, ecr);
        
        printf("moved vertex: %s\n", ecr.insertedVertex);          
        printf("edge coount after collapse: %d\n", we.getEdgeCount());  
        ArrayList<Edge> redges = ecr.removedEdges;
        printf("removed edges:(count %d) ", redges.size());
        for(Edge re : redges) {
            printf(" %s", re);
            // remove edge from array         
        }
        printf("\n");

        verifyVertices(we);
        
        
    }

    
    public void _testQuadric(){
        
        Point3d p[] = new Point3d[]{
            new Point3d(0,0,0), // 0
            new Point3d(1,0,0), // 1
            new Point3d(1,1,0), // 2
            new Point3d(0,1,0), // 3
            new Point3d(0,0,1), // 4
            new Point3d(1,0,1), // 5
            new Point3d(1,1,1), // 6
            new Point3d(0.1,1.1,1)};// 7
        
        int faces[][] = new int[][]{{6,5,2},{5,1,2}, {7,6,2}, {7,3,2}, {4,5,6}, {4,6,7}, {0,4,7}, {0,7,3}, {5,4,1}, {4,0,1}, {3,2,1}, {3,1,0}};

        Vector4d p526 = Quadric.makePlane(p[5], p[2], p[6],null);
        Vector4d p627 = Quadric.makePlane(p[6], p[2], p[7],null);
        Vector4d p675 = Quadric.makePlane(p[6], p[7], p[5],null);
        
        Quadric q526 = new Quadric(p[5], p[2], p[6]);
        Quadric q627 = new Quadric(p[6], p[2], p[7]);
        Quadric q675 = new Quadric(p[6], p[7], p[5]);
        Quadric q734 = new Quadric(p[7], p[3], p[4]);
        double eps = 1.0e-5;
        Quadric q56s = new Quadric(p[5],p[6], eps);

        Quadric q6 = new Quadric(q526);
        q6.addSet(q627).addSet(q675);
        Quadric q7 = new Quadric(q734);
        q7.addSet(q627).addSet(q675);
        
        Quadric q67 = new Quadric(q6);
        q67.addSet(q7);
        
        /*
        for(int i = 0; i < p.length ; i++){
            
            //printf("p526t[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p526, p[i]));
            //printf("p627[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p627, p[i]));
            //printf("p675[%d]): %10.7f\n",  i, MeshDecimator.planePointDistance2(p675, p[i]));
            //printf("q1(p[%d]): %10.7f\n",  i, q1.evaluate(p[i]));
            //printf("q2(p[%d]): %10.7f\n",  i, q2.evaluate(p[i]));
            //printf("q3(p[%d]): %10.7f\n",  i, q3.evaluate(p[i]));
            printf(" q6(p[%d]): %10.7f\n", i,   q6.evaluate(p[i]));
            printf(" q7(p[%d]): %10.7f\n", i, q7.evaluate(p[i]));
            printf("q67(p[%d]): %10.7f\n\n", i, q67.evaluate(p[i]));

        }
        
        Point3d p67 = q67.getMinimum(new Point3d());
        printf("p67: [%10.7f %10.7f %10.7f] \n", p67.x,p67.y,p67.z);
        
        printf("q67(p67): %10.7f\n\n", q67.evaluate(p67));
        Point3d pp = new Point3d(p67);
        pp.add(new Point3d(0.,-0.1,0.1));
        printf("q67(p67): %10.7f\n", q67.evaluate(pp));
        

        Quadric qe65 = new Quadric(q526);
        qe65.addSet(q675);
        
        printf("qe65(p6): %10.7f\n", qe65.evaluate(p[6]));
        printf("qe65(p5): %10.7f\n", qe65.evaluate(p[5]));
        
        printf("qe65.det:  %10.7e\n", qe65.determinant());
        printf("q56s.det: %10.7e\n", q56s.determinant());
        
        Point3d p56s = q56s.getMinimum(new Point3d());
                
        printf("p56s: %s\n",p56s);
        
        qe65.addSet(q56s);
        printf("dets: [%10.7e]\n", qe65.determinant());

        Point3d p65 = qe65.getMinimum(new Point3d());

        printf("p65: [%10.7f,%10.7f,%10.7f]\n", p65.x,p65.y,p65.z);

        
        Quadric q57 = new Quadric(p[5], p[7], 1.e-05);
        q675.addSet(q57);
        Point3d p57 =  q675.getMinimum(new Point3d());
        
        printf("det: %10.7e\n", q675.determinant());
        printf("p57: [%18.15f,%18.15f,%18.15f]\n", p57.x,p57.y,p57.z);
        */

        Quadric q713 = new Quadric(p[7], p[1], p[3]);
        printf("det713: %10.7e\n", q713.determinant());
        Quadric q71 = new Quadric(p[7], p[1], 1.e-05);
        printf("det 71: %10.7e\n", q71.determinant());
        q713.addSet(q71);
        printf("det 713: %10.7e\n", q713.determinant());
        Point3d p713 = q713.getMinimum(new Point3d());
        printf("p 713: [ %18.15f, %18.15f, %18.15f] \n", p713.x,p713.y,p713.z);


    }

    public void  _testArray() throws Exception {
        
        
        int N = 10000000;

        Integer al[] = new Integer[N];
        
        printf("testArray()  N: %d\n", N);
        
        long t0 = System.currentTimeMillis();

        Integer obj = new Integer(5);

        for(int i = 0; i < N; i++){
            al[i] = new Integer(i);
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));

        int n1 = N/10;

        Random rnd = new Random(49);

        t0 = System.currentTimeMillis();
        
        int count = N;
        int countMissed = 0;
        int alength = count;
        while(count > n1){

            int k = rnd.nextInt(alength);
            
            if(al[k] != null){
                al[k] = null;
                count--;
                if(count < alength*3/5){
                    // removes nulls from array 
                    for(int i =0, j = 0; i < alength; i++){
                        if(al[i] != null)
                            al[j++] = al[i];
                    }
                    alength = count;
                    //al = a;
                }
            } else {
                countMissed++;
            }
        }
        
        printf("count: %d, countMissed: %d, time: %d ms\n", count, countMissed, (System.currentTimeMillis()-t0));

        t0 = System.currentTimeMillis();

        for(int i = 0; i < N; i++){
            al[i] = obj;
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));
        
    }

    public void  _testArrayList() throws Exception {
        ArrayList al = new ArrayList();
        
        int N = 10000000;

        printf("testArrayList()  N: %d\n", N);
        
        long t0 = System.currentTimeMillis();
        for(int i = 0; i < N; i++){
            al.add(new Integer(i));
        }
        printf("fill array: %d ms\n", (System.currentTimeMillis()-t0));

        int n1 = N/10;

        Random rnd = new Random(49);

        t0 = System.currentTimeMillis();
        
        int count = N;
        int countMissed = 0;

        while(count > n1){
            int k = rnd.nextInt(N);
            if(al.get(k) != null){
                al.set(k, null);
                count--;
            } else {
                countMissed++;
            }
        }
        
        printf("count: %d, countMissed: %d, time: %d ms\n", count, countMissed, (System.currentTimeMillis()-t0));
        
    }

    public void testFile() throws Exception {

        //String fpath = "test/models/speed-knot.x3db";
        //String fpath = "test/models/sphere_10cm_rough_manifold.x3dv";
        //String fpath = "test/models/sphere_10cm_smooth_manifold.x3dv";
        //String fpath = "c:/tmp/text_iso_2.stl";
        //String fpath = "c:/tmp/sf31.stl";
        String fpath = "c:/tmp/leaf_01.stl";
        //String fpath = "c:/tmp/leaf_01_0832206.stl";
        //String fpath = "c:/tmp/sf21.stl";
        //String fpath = "c:/tmp/rtc_v3_04.stl";
        
        long t0 = currentTimeMillis();
        WingedEdgeTriangleMesh mesh = loadMesh(fpath);
        printf("mesh loading: %d ms\n",(currentTimeMillis() - t0));
        t0 = currentTimeMillis();

        //setVerticesUserData(mesh);

        int fcount = mesh.getFaceCount();

        MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_orig_%07d.stl", fcount));


        printf("mesh faces: %d, vertices: %d, edges: %d\n", fcount,mesh.getVertexCount(), mesh.getEdgeCount());
        
        printf("initial counts: faces: %d, vertices: %d, edges: %d \n", mesh.getFaceCount(),mesh.getVertexCount(), mesh.getEdgeCount());

        assertTrue("Initial Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));


        MeshDecimator md = new MeshDecimator();

        md.DEBUG = false;
        mesh.DEBUG = false; 

        for(int i = 0; i < 2; i++){
            
            fcount = fcount/8;
            t0 = currentTimeMillis();
            printf("processMesh() start\n");
            md.processMesh(mesh, fcount);
            //md.DEBUG = true;
            printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));
            fcount = mesh.getFaceCount();

            MeshExporter.writeMeshSTL(mesh,fmt("c:/tmp/mesh_dec_%07d.stl", fcount));
            
            // these things hang on large file - TODO - check this 
            //assertTrue("verifyVertices", verifyVertices(mesh));        
            //assertTrue("Structural Check", TestWingedEdgeTriangleMesh.verifyStructure(mesh, true));
            //assertTrue("Final Manifold", TestWingedEdgeTriangleMesh.isManifold(mesh));
            //printf("processMesh() done %d ms\n",(currentTimeMillis()-t0));
        }
    }
    

    /**
       
     */
    public static WingedEdgeTriangleMesh loadMesh(String fpath){
        if(fpath.toLowerCase().lastIndexOf(".stl") > 0){
            return loadSTL(fpath);
        } else {
            return loadX3D(fpath);
        }
    }

    /**
       load STL file 
     */
    public static WingedEdgeTriangleMesh loadSTL(String fpath){

        STLReader reader = new STLReader();
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        try {
            reader.read(fpath, its);
            return new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    

    /**
       load X3D file
     */
    public static WingedEdgeTriangleMesh loadX3D(String fpath){
        
        IndexedTriangleSetLoader loader = new IndexedTriangleSetLoader(false);
        
        loader.processFile(new File(fpath));
        
        GeometryData data = new GeometryData();
        data.coordinates = loader.getCoords();
        data.vertexCount = data.coordinates.length / 3;
        data.indexes = loader.getVerts();
        data.indexesCount = data.indexes.length;

        Vector3d[] verts = new Vector3d[data.vertexCount];
        int len = data.vertexCount;
        int idx = 0;        
        
        for(int i=0; i < len; i++) {
            idx = i * 3;
            verts[i] = new Vector3d(data.coordinates[idx++], data.coordinates[idx++], data.coordinates[idx++]);
        }

        len = data.indexes.length / 3;
        idx = 0;
        IndexedTriangleSetBuilder its = new IndexedTriangleSetBuilder();
        for(int i=0; i < len; i++) {
            its.addTri(verts[data.indexes[idx++]],verts[data.indexes[idx++]],verts[data.indexes[idx++]]);
        }
        
        WingedEdgeTriangleMesh we = new WingedEdgeTriangleMesh(its.getVertices(), its.getFaces());
        return we;

    }

    
    /**
       inits all vertices user data to Integer
     */
    static void setVerticesUserData(WingedEdgeTriangleMesh mesh){
        
        int vcount = 0;
        Vertex v = mesh.getVertices();
        while( v != null){
            v.setUserData(new Integer(vcount));
            vcount++;
            v = v.getNext();
        }
    }

    /**
       check, that all the vertices have consistent ring of faces 
     */
    static boolean verifyVertices(WingedEdgeTriangleMesh mesh){

        //printf("verifyVertices()\n");
        Vertex v = mesh.getVertices();
        int vcount = 0;
        while( v != null){
            //printf("v:%s: ", v);
            vcount++;
            HalfEdge start = v.getLink();
            HalfEdge he = start;
            int tricount = 0;
            
            do{                 
                //printf("[%3s %3s %3s] ", he.getEnd().getUserData(), he.getNext().getEnd().getUserData(),  he.getNext().getNext().getEnd().getUserData()); 
                
                if(tricount++ > 100){

                    printf("verifyVertices() !!! tricount exceeded\n");
                    
                    return false;
                }

                HalfEdge twin = he.getTwin();
                he = twin.getNext(); 
                
            } while(he != start);
            //printf("\n");
            
            v = v.getNext();
        }
        printf("vcount: %3d\n:", vcount);

        return true;

    }

    static Edge findEdge(WingedEdgeTriangleMesh mesh, int v0, int v1){

        printf("findEdge()\n");

        Vertex v = mesh.getVertices();
        Vertex vert0 = null;
        
        while(v != null){
            int id = v.getID();
            if(id == v0){
                vert0 = v;
                break;
            }
            v = v.getNext();
        }
        
        printf("vert0: %s\n", vert0);
        
        
        HalfEdge start = vert0.getLink();
        HalfEdge he = start;
        int tricount = 0;

        do{                 
            //printf("he: %s; %s; %s;\n", he, he.getNext(),  he.getNext().getNext()); 
            if(he.getEnd().getID() == v1){ 
                
                printf("vert1: %s\n", he.getEnd());
                return he.getEdge();
                
            }
                
            if(tricount++ > 20){
                printf("error: tricount exceded\n");
                break;
            }
            
            HalfEdge twin = he.getTwin();
            he = twin.getNext(); 
            
        } while(he != start);

        return null;        
    }
}

