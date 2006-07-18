/* Mesquite chromaseq source code.  Copyright 2005-2006 D. Maddison and W. Maddison.
Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
Perhaps with your help we can be more than a few, and make Mesquite better.

Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Mesquite's web site is http://mesquiteproject.org

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/
package mesquite.chromaseq.lib;

import mesquite.chromaseq.lib.*;
import java.util.*;


//  There will be one RegistrationCoordinator for each contig

public class RegistrationCoordinator {
	int numMasterBases;
	
	Contig contig;

	//various constants for specifying the different registries. 
	
	static final int PHD_FILE = 0;
	static final int ORIGINAL_CONTIG=1;
	static final int IMPORTED_CONTIG = 2;
	static final int AUTO_MODIFIED_CONTIG = 3;
	static final int USER_MODIFIED_CONTIG = 4;
	static final int VIEWER = 5;
	static final int MATRIX_UNALIGNED = 6;
	static final int MATRIX_ALIGNED = 7;

	// here are the registries connecting the master registry to various contigs.
	
	public RegistryMapper originalContigRegistryMapper;
	public RegistryMapper importedContigRegistryMapper;
	public RegistryMapper autoModContigRegistryMapper;
	public RegistryMapper userModContigRegistryMapper;
	public RegistryMapper viewerRegistryMapper;
	public RegistryMapper matrixUnalignedRegistryMapper;
	public RegistryMapper matrixAlignedRegistryMapper;

	// here are the registries connecting the master registry to each .phd file.
	Vector phdRegistries;


	public RegistrationCoordinator(Contig contig, int numContigBases){
		phdRegistries = new Vector(0);
		this.contig = contig;
		createRegistry(ORIGINAL_CONTIG,numContigBases);
	}

	public void createRegistry(int theRegistry, int lengthLinked){
		switch (theRegistry) {
		case ORIGINAL_CONTIG: 
			originalContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case IMPORTED_CONTIG:
			importedContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case AUTO_MODIFIED_CONTIG:
			autoModContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case USER_MODIFIED_CONTIG:
			userModContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case VIEWER:
			viewerRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case MATRIX_UNALIGNED:
			matrixUnalignedRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		case MATRIX_ALIGNED:
			matrixAlignedRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			break;
		}
	}

	public RegistryMapper getRegistry(int theRegistry){
		switch (theRegistry) {
		case ORIGINAL_CONTIG: 
			return originalContigRegistryMapper ;
		case AUTO_MODIFIED_CONTIG:
			return autoModContigRegistryMapper;
		case IMPORTED_CONTIG:
			return importedContigRegistryMapper;
		case USER_MODIFIED_CONTIG:
			return userModContigRegistryMapper;
		case VIEWER:
			return viewerRegistryMapper;
		case MATRIX_UNALIGNED:
			return matrixUnalignedRegistryMapper ;
		case MATRIX_ALIGNED:
			return matrixAlignedRegistryMapper;
		default:
				return null;
		}
	}

	public void createPhdRegistry(int lengthLinked, String name){
		RegistryMapper rm = new RegistryMapper(numMasterBases, lengthLinked, name);
		phdRegistries.add(rm);
	}

	public RegistryMapper getPhdRegistry(int i){
		return (RegistryMapper)phdRegistries.get(i);
	}



}
