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

package imageeditor;

// External Imports
import java.util.*;

// Local Imports
import abfab3d.creator.ui.*;
import abfab3d.creator.*;

public class UICreator {
    public static final void main(String[] args) {
        ImageEditorKernal kernal = new ImageEditorKernal();

System.out.println("Creating User Interface for imageeditor");
        JavaStandaloneUICreator uic = new JavaStandaloneUICreator();

        ArrayList<Step> steps = new ArrayList<Step>();
        steps.add(new Step(0, "Image", "Select your Image"));
        steps.add(new Step(1, "Body", "Select your Main shape"));
        steps.add(new Step(2, "Bail", "Add a Bail/Connector"));

        uic.createInterface("AbFab3D Image Creator","src/java/imageeditor/ui", steps, new HashMap<String,String>(), kernal, new HashSet<String>());
    }
}