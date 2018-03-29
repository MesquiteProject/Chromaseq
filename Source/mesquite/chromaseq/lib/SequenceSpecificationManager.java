/* Mesquite Chromaseq source code.  Copyright 2005-2011 David Maddison and Wayne Maddison.
Version 1.0   December 2011
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */

package mesquite.chromaseq.lib;


import mesquite.chromaseq.SequenceSpecificationForGenBank.SequenceSpecification;
import mesquite.lib.*;
import mesquite.lib.duties.*;
import java.awt.*;

/* Modules of this duty class are responsible for being able to supply the sample code and primer name given the chromatogram file name */
public abstract class SequenceSpecificationManager extends MesquiteInit {

	public Class getDutyClass(){
		return SequenceSpecificationManager.class;
	}

	public abstract String getSequenceModifiers(int sequenceType);

	public abstract String[] getListOfSpecifications();

	public abstract boolean manageSequenceSpecifications();

	public boolean queryOptions(){
		return true;
	}

	public boolean optionsSpecified(){
		return false;
	}

	public boolean hasOptions(){
		return false;
	}


}


