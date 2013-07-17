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

package volumesculptor;

// External Imports

import abfab3d.creator.GeometryKernel;
import abfab3d.creator.KernelResults;
import abfab3d.creator.Parameter;
import abfab3d.creator.shapeways.HostedKernel;
import abfab3d.creator.util.ParameterUtil;
import abfab3d.grid.Grid;
import abfab3d.io.input.BoundsCalculator;
import abfab3d.io.output.BoxesX3DExporter;
import abfab3d.io.output.MeshExporter;
import abfab3d.mesh.AreaCalculator;
import abfab3d.mesh.TriangleMesh;
import app.common.RegionPrunner;
import org.apache.commons.io.FileUtils;
import org.web3d.util.ErrorReporter;
import org.web3d.vrml.export.PlainTextErrorReporter;
import org.web3d.vrml.export.X3DXMLRetainedExporter;
import org.web3d.vrml.sav.BinaryContentHandler;
import volumesculptor.shell.Main;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static abfab3d.util.Output.printf;
import static abfab3d.util.Output.time;

/**
 * Geometry Kernel for the VolumeSculptor
 *
 * @author Alan Hudson
 * @author Vladimir Bulatov
 */
public class VolumeSculptorKernel extends HostedKernel {
    private static final int NUM_FILES = 10;
    private static final int NUM_PARAMS = 10;

    private static final boolean USE_MIP_MAPPING = false;
    private static final boolean USE_FAST_MATH = true;

    static final int
            GRID_SHORT_INTERVALS = 1,
            GRID_BYTE_ARRAY = 2,
            GRID_AUTO = -1;

    /**
     * Debugging level.  0-5.  0 is none
     */
    private static final int DEBUG_LEVEL = 0;
    static final double MM = 0.001; // millmeters to meters


    // large enough font size to be used to render text 
    static final int DEFAULT_FONT_SIZE = 50;
    // point size unit 
    static final double POINT_SIZE = 25.4 * MM / 72;
    static final double TEXT_RENDERING_PIXEL_SIZE = 25.4 * MM / 600; // 600 dpi 
    int MINIMAL_TEXT_OFFSET = 10; // minimal border around engraved text 

    enum EdgeStyle {NONE, TOP, BOTTOM, BOTH}

    ;

    // High = print resolution.  Medium = print * 2, LOW = print * 4
    enum PreviewQuality {
        LOW(4.), MEDIUM(2.), HIGH(1.0);

        private double factor;

        PreviewQuality(double f) {
            factor = f;
        }

        public double getFactor() {
            return factor;
        }
    }

    ;

    private String[] availableMaterials = new String[]{"White Strong & Flexible", "White Strong & Flexible Polished",
            "Silver", "Silver Glossy", "Stainless Steel", "Gold Plated Matte", "Gold Plated Glossy", "Antique Bronze Matte",
            "Antique Bronze Glossy", "Alumide", "Polished Alumide"};

    private String script;
    private String[] files;
    private String[] params;

    /**
     * The horizontal and vertical resolution
     */
    private double resolution;
    private PreviewQuality previewQuality;
    private int smoothSteps;
    private double smoothingWidth;
    private double maxDecimationError;

    private int threadCount;

    private String material;

    /**
     * How many regions to keep
     */
    private RegionPrunner.Regions regions;
    private boolean visRemovedRegions;

    /**
     * Get the parameters for this editor.
     *
     * @return The parameters.
     */
    public Map<String, Parameter> getParams() {
        HashMap<String, Parameter> params = new HashMap<String, Parameter>();

        int seq = 0;
        int step = 0;

        // Script
        params.put("script", new Parameter("script", "Script", "The script to run", "", 1,
                Parameter.DataType.STRING, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        for(int i=0; i < NUM_FILES; i++) {
            // File based params
            params.put("file" + i, new Parameter("file" + i, "File" + i, "File" + i + " to use", "", 1,
                    Parameter.DataType.URI, Parameter.EditorType.FILE_DIALOG,
                    step, seq++, false, 0, 0.1, null, null)
            );
        }

        for(int i=0; i < NUM_PARAMS; i++) {
            // File based params
            params.put("param" + i, new Parameter("param" + i, "Param" + i, "Param" + i + " to use", "", 1,
                    Parameter.DataType.STRING, Parameter.EditorType.DEFAULT,
                    step, seq++, false, 0, 0.1, null, null)
            );
        }

        step++;
        seq = 0;


        params.put("material", new Parameter("material", "Material", "What material to design for", "Silver Glossy", 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, availableMaterials)
        );

        step++;
        seq = 0;

        // Advanced Params

        params.put("resolution", new Parameter("resolution", "Resolution", "How accurate to model the object", "0.0001", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 0.1, null, null)
        );

        params.put("threads", new Parameter("threads", "Threads", "Threads to use for operations", "0", 1,
                Parameter.DataType.INTEGER, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 50, null, null)
        );

        params.put("previewQuality", new Parameter("previewQuality", "PreviewQuality", "How rough is the preview", PreviewQuality.MEDIUM.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(PreviewQuality.values()))
        );

        params.put("maxDecimationError", new Parameter("maxDecimationError", "MaxDecimationError", "Maximum error during decimation", "1e-10", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 1, null, null)
        );

        params.put("regions", new Parameter("regions", "Regions", "How many regions to keep", RegionPrunner.Regions.ALL.toString(), 1,
                Parameter.DataType.ENUM, Parameter.EditorType.DEFAULT,
                step, seq++, false, -1, 1, null, enumToStringArray(RegionPrunner.Regions.values()))
        );

        params.put("smoothingWidth", new Parameter("smoothingWidth", "Smoothing Width", "Width of Gaussian Smoothing", "0.5", 1,
                Parameter.DataType.DOUBLE, Parameter.EditorType.DEFAULT,
                step, seq++, true, 0, 100, null, null)
        );

        params.put("visRemovedRegions", new Parameter("visRemovedRegions", "Vis Removed Regions", "Visualize removed regions", "false", 1,
                Parameter.DataType.BOOLEAN, Parameter.EditorType.DEFAULT,
                step, seq++, false, 0, 100, null, null)
        );

        return params;
    }

    /**
     * @param params  The parameters
     * @param acc     The accuracy to generate the model
     * @param handler The X3D content handler to use
     */
    public KernelResults generate(Map<String, Object> params, Accuracy acc, BinaryContentHandler handler) throws IOException {
//        int gridType =  GRID_BYTE_ARRAY;
        int gridType = GRID_SHORT_INTERVALS;

        if (USE_FAST_MATH) {
            System.setProperty("jodk.fastmath.usejdk", "false");
        } else {
            System.setProperty("jodk.fastmath.usejdk", "true");
        }

        long start = time();

        pullParams(params);

        if (acc == Accuracy.VISUAL) {
            resolution = resolution * previewQuality.getFactor();
        }

        if (threadCount == 0) {
            int cores = Runtime.getRuntime().availableProcessors();

            threadCount = cores;

            // scales well to 4 threads, stop there.
            if (threadCount > 4) {
                threadCount = 4;
            }

            System.out.println("Number of cores:" + threadCount);
        }


        File temp = File.createTempFile("script", ".vss");
        FileUtils.write(temp, script);

        System.out.println("Loading script: " + script);

        String[] args = new String[] {temp.toString() };

        System.out.println("Files: " + files + " params: " + this.params);

        TriangleMesh mesh = Main.execMesh(args, files,this.params);

        System.out.println("After creation");

        HashMap<String, Object> out_params = new HashMap<String, Object>();
        MeshExporter.writeMesh(mesh, handler, out_params, true);

        BoundsCalculator bc = new BoundsCalculator();
        mesh.getTriangles(bc);
        double[] bounds = new double[6];
        bc.getBounds(bounds);

        AreaCalculator ac = new AreaCalculator();
        mesh.getTriangles(ac);
        double volume = ac.getVolume();
        double surface_area = ac.getArea();

        // Do not shorten the accuracy of these prints they need to be high
        printf("final surface area: %12.8f cm^2\n", surface_area * 1.e4);
        printf("final volume: %12.8f cm^3\n", volume * 1.e6);

        printf("Total time: %d ms\n", (time() - start));
        printf("-------------------------------------------------\n");

        double min_bounds[] = new double[3];
        double max_bounds[] = new double[3];

        System.out.println("Bounds: " + java.util.Arrays.toString(bounds));
        min_bounds[0] = bounds[0];
        max_bounds[0] = bounds[1];
        min_bounds[1] = bounds[2];
        max_bounds[1] = bounds[3];
        min_bounds[2] = bounds[4];
        max_bounds[2] = bounds[5];
        System.out.println("MinBounds: " + java.util.Arrays.toString(min_bounds));
        System.out.println("MaxBounds: " + java.util.Arrays.toString(max_bounds));

        return new KernelResults(true, min_bounds, max_bounds, volume, surface_area, 0);
    }

    /**
     * Pull the params into local variables
     *
     * @param params The parameters
     */
    private void pullParams(Map<String, Object> params) {
        String pname = null;

        try {
            pname = "script";
            script = (String) params.get(pname);

            System.out.println("Script: \n" + script);
            ArrayList<String> list = new ArrayList<String>();
            String val = null;

            for(int i=0; i < NUM_FILES; i++) {
                pname = "file" + i;
                val = (String) params.get(pname);

                if (val != null && val.length() > 0) {
                    list.add(val);
                }
            }

            files = new String[list.size()];
            files = list.toArray(files);

            System.out.println("VSK Files: " + java.util.Arrays.toString(files));
            list.clear();

            for(int i=0; i < NUM_PARAMS; i++) {
                pname = "param" + i;
                val = (String) params.get(pname);

                if (val != null && val.length() > 0) {
                    list.add(val);
                }
            }
            this.params = new String[list.size()];
            this.params = list.toArray(this.params);
            System.out.println("VSK Params: " + java.util.Arrays.toString(this.params));

            pname = "resolution";
            resolution = ((Double) params.get(pname)).doubleValue();

            pname = "previewQuality";
            previewQuality = PreviewQuality.valueOf((String) params.get(pname));

            pname = "maxDecimationError";
            maxDecimationError = ((Double) params.get(pname)).doubleValue();

            pname = "material";
            material = ((String) params.get(pname));

            pname = "smoothingWidth";
            smoothingWidth = ((Number) params.get(pname)).doubleValue();

            pname = "threads";
            threadCount = ((Integer) params.get(pname)).intValue();

            pname = "regions";
            regions = RegionPrunner.Regions.valueOf((String) params.get(pname));

            pname = "visRemovedRegions";
            visRemovedRegions = (Boolean) params.get(pname);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Error parsing: " + pname + " val: " + params.get(pname));
        }
    }


    private void writeDebug(Grid grid, BinaryContentHandler handler, ErrorReporter console) {
        // Output File

        BoxesX3DExporter exporter = new BoxesX3DExporter(handler, console, true);

        HashMap<Integer, float[]> colors = new HashMap<Integer, float[]>();
        colors.put(new Integer(Grid.INSIDE), new float[]{1, 0, 0});
        colors.put(new Integer(Grid.OUTSIDE), new float[]{0, 0, 1});

        HashMap<Integer, Float> transparency = new HashMap<Integer, Float>();
        transparency.put(new Integer(Grid.INSIDE), new Float(0));
        transparency.put(new Integer(Grid.OUTSIDE), new Float(0.98));

        exporter.writeDebug(grid, colors, transparency);
        exporter.close();
    }

    public static <T extends Enum<T>> String[] enumToStringArray(T[] values) {
        int i = 0;
        String[] result = new String[values.length];
        for (T value : values) {
            result[i++] = value.name();
        }
        return result;
    }

    public static void main(String[] args) {
        HashMap<String, String> params = new HashMap<String, String>();

        int LOOPS = 1;

        for (int i = 0; i < LOOPS; i++) {
            HostedKernel kernel = new VolumeSculptorKernel();

            System.out.println("***High Resolution");
            params.put("resolution", "0.00002");
            params.put("text", "");
            params.put("previewQuality", "HIGH");
            params.put("threads", "4");

            Map<String, Object> parsed_params = ParameterUtil.parseParams(kernel.getParams(), params);

            try {
                FileOutputStream fos = new FileOutputStream("/tmp/thread" + Thread.currentThread().getName() + ".x3d");
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                PlainTextErrorReporter console = new PlainTextErrorReporter();

                BinaryContentHandler writer = new X3DXMLRetainedExporter(bos, 3, 2, console);
                writer.startDocument("", "", "utf8", "#X3D", "V3.2", "");
                writer.profileDecl("Immersive");


                kernel.generate(parsed_params, GeometryKernel.Accuracy.VISUAL, writer);

                writer.endDocument();
                bos.close();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
