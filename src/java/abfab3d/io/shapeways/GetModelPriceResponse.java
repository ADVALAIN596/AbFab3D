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

package abfab3d.io.shapeways;

//External Imports
//none

//Internal Imports
import abfab3d.io.soap.encoders.DefaultObject;

/**
 *
 * Encapsulates the setViewPointResponse data retrieved from the web service
 *
 * @author Russell Dodds
 * @version $Revision 1.1$
 */
public class GetModelPriceResponse extends DefaultObject {

    /**
     * Constructor
     */
    public GetModelPriceResponse() {
        soapElementName = "getModelPriceResponse";
        soapElementType = "getModelPriceResponse";
    }

    //---------------------------------------------------------------
    // Local Methods
    //---------------------------------------------------------------

    /**
     * Gets the price value for this UdesignModel.
     *
     * @return price
     */
    public float getPrice() {
        return (Float)getProperty("price");
    }

    /**
     * Sets the price value for this UdesignModel.
     *
     * @param price
     */
    public void setPrice(float price) {
        setProperty("price", price);
    }

}
