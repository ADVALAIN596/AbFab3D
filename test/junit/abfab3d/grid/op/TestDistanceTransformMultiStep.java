package abfab3d.grid.op;

import abfab3d.grid.AttributeGrid;

import static java.lang.Math.min;
import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.fmt;
import static abfab3d.util.Output.time;
import static abfab3d.util.Units.MM;
import static java.lang.Math.round;

/**
 * Test the DistanceTransformMultiStep class.
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov

 */
public class TestDistanceTransformMultiStep extends BaseTestDistanceTransform {
    private static final boolean DEBUG = true;
    double surfaceThickness = Math.sqrt(3)/2;
    int maxAttribute = 100;
    double voxelSize = 0.1*MM;

    public void testBoxInside(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2. * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
//        double maxInDistance = 1.2*MM;
        double maxInDistance = boxWidth/2;
        double maxOutDistance = 0;//

        MyGridWriter gw = new MyGridWriter(8,8);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",nx/2-1, nx/2, null);

        long t0 = time();
        DistanceTransformExact dt_ms = new DistanceTransformExact(max_attribute, maxInDistance, maxOutDistance);
        //DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxInDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_ms, norm , "/tmp/slices/distance/mox_ms_in_%03d.png",nx/2, nx/2+1, new DistanceColorizer(norm,0,0,0));
            for(int i=0; i < 40; i++) {
                printRow(dg_ms, 50, 90, i, nx / 2, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int maxDist = 0;
        int minDist = -(int)(Math.floor(0.5*(boxWidth/grid.getVoxelSize())*max_attribute + 0.5));

        printf("Max distance: %d  Min distance: %d",maxDist,minDist);

        long defaultInside = dt_ms.getInsideDefault();
        long defaultOutside = dt_ms.getOutsideDefault();

        checkMinValue(minDist, defaultInside, defaultOutside, dg_ms);
        checkMaxValue(maxDist, defaultInside, defaultOutside, dg_ms);
    }

    public void testBoxOutside(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2.0 * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 0*MM;
        double maxOutDistance = 1.0 * MM;

        MyGridWriter gw = new MyGridWriter(8,8);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_ms_out_%03d.png",nx/2, nx/2+1,null);

        long t0 = time();
        DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_ms,norm , "/tmp/slices/distance/ms_%03d.png",nx/2,nx/2+1, new DistanceColorizer(norm,0,0,0));
            for(int i=0; i < 18; i++) {
                printRow(dg_ms, 50, 90, i, 15, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int min = 0;
        int max = (int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_ms.getInsideDefault();
        long not_calced_outside = dt_ms.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_ms);
        checkMaxValue(max, not_calced_inside, not_calced_outside, dg_ms);
    }

    public void testBoxBoth(){

        int max_attribute = 100;
        int nx = 128;
        double boxWidth = 2.0 * MM;
        AttributeGrid grid = makeBox(nx, boxWidth, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 1*MM;
        double maxOutDistance = 1*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_ms_both_%03d.png",nx/2, nx/2+1, colorizer);

        long t0 = time();
        DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMutiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_ms,norm , "/tmp/slices/distance/box_ms_%03d.png",nx/2, nx/2+1, colorizer);
            for(int i=70; i < nx; i++) {
                printRow(dg_ms, 40, 70, i, 20, false, 50);
                printf("\n");
            }
        }

        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));
        int max = (int)(Math.ceil(boxWidth/2.0*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_ms.getInsideDefault();
        long not_calced_outside = dt_ms.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_ms);
        checkMaxValue(max, not_calced_inside, not_calced_outside, dg_ms);
        checkHightToLowToHigh(nx/2, nx/2, nx/2, not_calced_inside, not_calced_outside, dg_ms);
    }

    public void testSphereBoth(){

        int max_attribute = 100;
        int nx = 128;
        double sphereRadius = 5.0 * MM;
        AttributeGrid grid = makeSphere(nx, sphereRadius, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 1.0*MM;
        double maxOutDistance = 1*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/sphere_%03d.png",nx/2, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);

        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_ms,norm , "/tmp/slices/distance/sphere_ms_%03d.png",nx/2, nx/2+1, colorizer);
            for(int i=nx / 2 - 20; i < nx / 2 + 20; i++) {
                printRow(dg_ms, 50, 90, i, 20, false, 50);
                printf("\n");
            }
        }
        // check that the distance never exceeds half the box size
        int max = (int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));
        int min = -(int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_ms.getInsideDefault();
        long not_calced_outside = dt_ms.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_ms);
        checkMaxValue(max, not_calced_inside, not_calced_outside, dg_ms);
        checkHightToLowToHigh(nx/2, nx/2, nx/2, not_calced_inside, not_calced_outside, dg_ms);
    }

    public void testTorusBoth(){

        int max_attribute = 100;
        int nx = 128;
        double outRadius =3.5*MM;
        double inRadius = 1.5*MM;

        AttributeGrid grid = makeTorus(nx, outRadius, inRadius, voxelSize, max_attribute, surfaceThickness);
        double maxInDistance = 1*MM;
        double maxOutDistance = 1*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(8,8);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/torus_%03d.png",nx/2, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*(maxInDistance + maxOutDistance)/voxelSize);

        if (DEBUG) {
            gw.writeSlices(dg_ms,norm , "/tmp/slices/distance/torus_ms_%03d.png",nx/2, nx/2+1, colorizer);
            for(int i=50; i < 78; i++) {
                printRow(dg_ms, 0, 40, i, 55, false, 50);
                printf("\n");
            }
        }
        int max = (int)(Math.ceil(maxOutDistance*max_attribute/grid.getVoxelSize() + 0.5));
        int min = -(int)(Math.ceil(maxInDistance*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_ms.getInsideDefault();
        long not_calced_outside = dt_ms.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_ms);
        checkMaxValue(max, not_calced_inside, not_calced_outside, dg_ms);
    }

    public void _testGyroidBoth(){

        int max_attribute = 100;
        int nx = 384;
        double sphereRadius = 5.0 * MM;
        AttributeGrid grid = makeGyroid(nx, sphereRadius, voxelSize, max_attribute, surfaceThickness, nx * voxelSize / 3.0, 0.1);
        double maxInDistance = 0.5*MM;
        double maxOutDistance = 0.5*MM;

        int max_grid_att = (int) (Math.ceil(max_attribute * (maxInDistance + maxOutDistance) / voxelSize / 2.0));
        MyGridWriter gw = new MyGridWriter(1,1,2);
        DistanceColorizer colorizer =new DistanceColorizer(max_grid_att,0,0,0);
        if (DEBUG) gw.writeSlices(grid, maxAttribute, "/tmp/slices/gyroid_exact_%03d.png",0, nx/2+1, colorizer);
//        gw.writeSlices(grid, maxAttribute, "/tmp/slices/box_%03d.png",0, nx, new DistanceColorizer(max_attribute));

        long t0 = time();
        DistanceTransformMultiStep dt_ms = new DistanceTransformMultiStep(max_attribute, maxInDistance, maxOutDistance);
//        DistanceTransformFM dt_ms = new DistanceTransformFM(max_attribute, maxInDistance, maxOutDistance);
        AttributeGrid dg_ms = dt_ms.execute(grid);
        printf("DistanceTransformMultiStep done: %d ms\n", time() - t0);

        int norm = (int)round(maxAttribute*maxOutDistance/voxelSize);

        if (DEBUG) gw.writeSlices(dg_ms,norm , "/tmp/slices/distance/exact_%03d.png",0, nx, colorizer);
        /*
        for(int i=nx / 2 - 20; i < nx / 2 + 20; i++) {
            printRow(dg_ms, 50, 90, i, 20, false, 50);
            printf("\n");
        }
        */
        // check that the distance never exceeds half the box size
        int min = -(int)(Math.ceil(sphereRadius*max_attribute/grid.getVoxelSize() + 0.5));

        long not_calced_inside = dt_ms.getInsideDefault();
        long not_calced_outside = dt_ms.getOutsideDefault();

        checkMinValue(min, not_calced_inside, not_calced_outside, dg_ms);
    }

    public void _testMakeAllNeighbors(){

        int subvoxelResolution = 100;
        double dist = 3.57;
        int radius = (int)(dist * subvoxelResolution + 0.5);
        printf("testMakeAllNeighbors()\n");
        printf("  radius: %d subvoxels\n",radius);
        long t0 = time();
        int[][] neig = DistanceTransformMultiStep.makeAllBallNeighborsWithOffset(radius, subvoxelResolution);
        printf("  neighbours calculation: %d ms\n", (time() - t0));
        printf("  neighbours count: %d\n", neig[0].length);
        if(true){
            for(int k = 0; k < neig.length; k++){
                int mm[] = getMinMaxDist(neig[k]);
                if(mm[0] != min(k, subvoxelResolution-k))
                    fail(fmt("wrong min distance. Expected: %d actual value: %d ", k, mm[0]));
                if(mm[1] > radius)
                    fail(fmt("max distance exceeded. radius: %d maxDistance: %d ", radius, mm[1]));
                    
                if(false){
                    printf("%4d ", neig[k].length/4, mm[0], mm[1]);
                    if((k+1)%10 == 0)
                        printf("\n");
                }
            }
            if(false){
                for(int k = 0; k < neig.length; k++){
                    int mm[] = getMinMaxDist(neig[k]);
                    printf("(%4d %4d) ", mm[0], mm[1]);
                    if((k+1)%10 == 0)
                        printf("\n");
                }
            }
            if(false){
                int nn[] = neig[20];
                for(int k = 0; k < nn.length; k += 4){                                
                    printf("(%2d %2d %2d): %3d \n", nn[k], nn[k+1], nn[k+2], nn[k+3]);
                }
            }

        }
    }

    // returns min max distances from array of neighbors 
    static int[] getMinMaxDist(int neig[]){
        int mm[] = new int[2];
        mm[0] = neig[3];
        mm[1] = mm[0];
        for(int k = 0; k < neig.length; k+=4){
            int d = neig[k+3];
            if(d < mm[0])
                mm[0] = d;
            if(d > mm[1])
                mm[1] = d;
        }
        return mm;
    }

    public static void main(String arg[]){
        
        //for(int k  = 0; k < 10; k++){
        //new TestDistanceTransformMultiStep().testBoxInside();
        //new TestDistanceTransformMultiStep().testBoxOutside();
        //new TestDistanceTransformMultiStep().testBoxInside();
        new TestDistanceTransformMultiStep().testSphereBoth();
        //new TestDistanceTransformMultiStep().testTorusBoth();
        //new TestDistanceTransformMultiStep().testMakeAllNeighbors();
        //new TestDistanceTransformMultiStep().testBoxBoth();
        //}
    }
    
}
