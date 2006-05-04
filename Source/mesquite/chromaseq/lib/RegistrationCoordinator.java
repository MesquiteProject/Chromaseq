package mesquite.chromaseq.lib;

import mesquite.chromaseq.lib.*;
import java.util.*;

public class RegistrationCoordinator {
	Vector phdRegistries;
	int numMasterBases;
	static final int PHD_FILE = 0;
	static final int ORIGINAL_CONTIG=1;
	static final int IMPORTED_CONTIG = 2;
	static final int AUTO_MODIFIED_CONTIG = 3;
	static final int USER_MODIFIED_CONTIG = 4;
	static final int VIEWER = 5;
	static final int MATRIX_UNALIGNED = 6;
	static final int MATRIX_ALIGNED = 7;

	RegistryMapper originalContigRegistryMapper;
	RegistryMapper importedContigRegistryMapper;
	RegistryMapper autoModContigRegistryMapper;
	RegistryMapper userModContigRegistryMapper;
	RegistryMapper viewerRegistryMapper;
	RegistryMapper matrixUnalignedRegistryMapper;
	RegistryMapper matrixAlignedRegistryMapper;


	public RegistrationCoordinator(){
		phdRegistries = new Vector(0);
	}

	public void createRegistry(int theRegistry, int numLinked){
		switch (theRegistry) {
		case ORIGINAL_CONTIG: 
			originalContigRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case IMPORTED_CONTIG:
			importedContigRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case AUTO_MODIFIED_CONTIG:
			autoModContigRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case USER_MODIFIED_CONTIG:
			userModContigRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case VIEWER:
			viewerRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case MATRIX_UNALIGNED:
			matrixUnalignedRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
			break;
		case MATRIX_ALIGNED:
			matrixAlignedRegistryMapper = new RegistryMapper(numMasterBases, numLinked);
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

	public void createPhdRegistry(int numLinked, String name){
		RegistryMapper rm = new RegistryMapper(numMasterBases, numLinked, name);
		phdRegistries.add(rm);
	}

	public RegistryMapper getPhdRegistry(int i){
		return (RegistryMapper)phdRegistries.get(i);
	}



}
