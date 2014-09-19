/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2014
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.io.output;

import abfab3d.grid.AttributeGrid;
import abfab3d.grid.Grid;
import abfab3d.grid.AttributeDesc;
import abfab3d.grid.AttributeChannel;


import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static abfab3d.util.Output.printf;

/**
 * Writes a grid out in the svx format.
 *
 * @author Alan Hudson
 */
public class SVXWriter {
    /**
     * Writes a grid out to an svx file
     * @param grid
     * @param file
     */
    public void write(AttributeGrid grid, String file) {
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ZipOutputStream zos = null;

        try {

            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            zos = new ZipOutputStream(bos);


            ZipEntry zentry = new ZipEntry("manifest.xml");
            zos.putNextEntry(zentry);
            writeManifest(grid,zos);
            zos.closeEntry();

            SlicesWriter sw = new SlicesWriter();
            int orientation = 1;
            AttributeDesc attDesc = grid.getAttributeDesc();

            for(int i = 0; i < attDesc.size(); i++){
                
                AttributeChannel channel = attDesc.getChannel(i);                
                String channelPattern = channel.getName() + "/" + "slice%04d.png";                
                sw.writeSlices(grid,zos,channelPattern,0,0,grid.getHeight(), orientation, channel.getBitCount(), channel);                
            }
        } catch(IOException ioe) {

            ioe.printStackTrace();
            
        } finally {
            try {
                if (zos != null) zos.close();
                if (bos != null) bos.close();
                if (fos != null) fos.close();
            } catch(Exception e) {
                // ignore
            }
        }

    }

    private void writeManifest(AttributeGrid grid, OutputStream os) {

        int subvoxelBits = 8; // where it should came from? 

        AttributeDesc attDesc = grid.getAttributeDesc();        

        PrintStream ps = new PrintStream(os);

        double[] bounds = new double[6];

        grid.getGridBounds(bounds);

        printf(ps,"<?xml version=\"1.0\"?>\n");
        printf(ps, "<grid gridSizeX=\"%d\" gridSizeY=\"%d\" gridSizeZ=\"%d\" voxelSize=\"%f\" subvoxelBits=\"%d\" originX=\"%f\" originY=\"%f\" originZ=\"%f\">\n", 
               grid.getWidth(), grid.getHeight(),grid.getDepth(),
               grid.getVoxelSize(), subvoxelBits,bounds[0],bounds[2],bounds[4]);

        String indent = "    ";
        String indent2 = indent+indent;

        printf(ps, indent+"<channels>\n");

        for(int i = 0; i < attDesc.size(); i++){

            AttributeChannel channel = attDesc.getChannel(i);

            String channelPattern = channel.getName() + "/" + "slice%04d.png";
            int bits = channel.getBitCount(); 
            printf("channel type: %s name: %s\n", channel.getType(), channel.getName());
            printf(ps, indent2 + "<channel type=\"%s\" slices=\"%s\" bits=\"%d\">\n",channel.getType(), channelPattern, bits);
        }        
        printf(ps, indent + "</channels>\n");
        printf(ps, "</grid>\n");
        ps.flush();
    }
}
