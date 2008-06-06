/* Mesquite chromaseq source code.  Copyright 2005-2008 D. Maddison and W. Maddison.
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
import mesquite.lib.*;


//  There will be one RegistrationCoordinator for each contig

public class RegistryCoordinator {
	int numMasterBases;
	int furthestUpstreamFrameStart=0;
	
	
	Contig contig;

	//various constants for specifying the different registries. 
	
	static final int PHD_FILE = 0;
	static final int ORIGINAL_CONTIG=1;
	static final int TRIMMED_CONTIG = 2;
	static final int VIEWER = 5;
	static final int MATRIX_UNALIGNED = 6;
	static final int MATRIX_ALIGNED = 7;

	// here are the registries connecting the master registry to various contigs.
	
	public RegistryMapper originalContigRegistryMapper;
	public RegistryMapper trimmedContigRegistryMapper;
	public RegistryMapper viewerRegistryMapper;
	public RegistryMapper matrixUnalignedRegistryMapper;
	public RegistryMapper matrixAlignedRegistryMapper;

	// here are the registries connecting the master registry to each .phd file.
	Vector phdRegistries;


	public RegistryCoordinator(Contig contig, int numContigBases, String contigBasesFromAceFile){
		phdRegistries = new Vector(0);
		this.contig = contig;
		numMasterBases =numContigBases;  // this is the starting number
		
		
//preparing the linkage between the original contig and the master registry
		createOriginalContigRegistry(numContigBases, contigBasesFromAceFile);

	}

	
	private void createOriginalContigRegistry(int numContigBases, String contigBasesFromAceFile){
		int numPads = StringUtil.characterCount(contigBasesFromAceFile,'*');	
		createRegistry(ORIGINAL_CONTIG,numContigBases-numPads);
		int count = 0;
		for (int i = 0; i<contigBasesFromAceFile.length(); i++) {  
			if (i-count<originalContigRegistryMapper.getLinkedLength())
				originalContigRegistryMapper.setMasterBaseAtLinkedBaseRegistryPosition(i-count, i);
			if (contigBasesFromAceFile.charAt(i)=='*')  //it's a padded position in the contig
				count++;
		}
		count = 0;
		for (int i = 0; i<originalContigRegistryMapper.getMasterLength() && i<contigBasesFromAceFile.length(); i++) {  
			if (contigBasesFromAceFile.charAt(i)!='*'){  //it's not a padded position in the contig
				originalContigRegistryMapper.setLinkedBaseAtMasterBaseRegistryPosition(i, count);
				count++;
			}
			// note that if it is a padded position, we leave the master registry to be unassigned
		}
	}

	public void cloneRegistry(int sourceRegistry, int sinkRegistry){
		RegistryMapper oldRegistry = getRegistry(sourceRegistry);
		if (oldRegistry!=null) {
			RegistryMapper newRegistry = getRegistry(sinkRegistry);
			if (newRegistry==null) {
				newRegistry = createRegistry(sinkRegistry,oldRegistry.getLinkedLength());
			}
			newRegistry.copyRegistry(oldRegistry);
		}
	}

	public void trimLinkedInRegistry(int registryNumber, int numTrimmedFromStart, int numTrimmedFromEnd){
		RegistryMapper theRegistry = getRegistry(registryNumber);
		if (theRegistry!=null) {
			theRegistry.trimLinked(numTrimmedFromStart, numTrimmedFromEnd);
		}
	}

		
		
		public RegistryMapper createRegistry(int theRegistry, int lengthLinked){
		switch (theRegistry) {
		case ORIGINAL_CONTIG: 
			originalContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked, "Original Contig");
			return originalContigRegistryMapper;
		case TRIMMED_CONTIG:
			trimmedContigRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked, "Imported Contig");
			return trimmedContigRegistryMapper;
		case VIEWER:
			viewerRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			return viewerRegistryMapper;
		case MATRIX_UNALIGNED:
			matrixUnalignedRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			return matrixUnalignedRegistryMapper;
		case MATRIX_ALIGNED:
			matrixAlignedRegistryMapper = new RegistryMapper(numMasterBases, lengthLinked);
			return matrixAlignedRegistryMapper;
		}
		return null;
	}

	public RegistryMapper getRegistry(int theRegistry){
		switch (theRegistry) {
		case ORIGINAL_CONTIG: 
			return originalContigRegistryMapper ;
		case TRIMMED_CONTIG:
			return trimmedContigRegistryMapper;
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

	public void addBasesToMasterRegistry(int starting, int num){
		if (num<=0) return;
		if (originalContigRegistryMapper!=null) 
			originalContigRegistryMapper.addBasesToMasterRegistry( starting, num);
		if (trimmedContigRegistryMapper!=null) 
			trimmedContigRegistryMapper.addBasesToMasterRegistry( starting, num);
		if (viewerRegistryMapper!=null) 
			viewerRegistryMapper.addBasesToMasterRegistry( starting, num);
		if (matrixUnalignedRegistryMapper!=null) 
			matrixUnalignedRegistryMapper.addBasesToMasterRegistry( starting, num);
		if (matrixAlignedRegistryMapper!=null) 
			matrixAlignedRegistryMapper.addBasesToMasterRegistry( starting, num);
		for (int i = 0; i<phdRegistries.size(); i++)
			getPhdRegistry(i).addBasesToMasterRegistry(starting, num);
		numMasterBases+=num;
	}

	public void createPhdRegistry(int lengthLinked, String name, int frameStart, String bases){
		if (frameStart<furthestUpstreamFrameStart) {
			addBasesToMasterRegistry(-1,Math.abs(furthestUpstreamFrameStart-frameStart));
			furthestUpstreamFrameStart = frameStart;
		}
		int startBaseInMaster = frameStart-furthestUpstreamFrameStart;
		int numPads = StringUtil.characterCount(bases,'*');
		int unpaddedLengthLinked = lengthLinked-numPads;
		int extraBasesInLinked = (startBaseInMaster+lengthLinked) - numMasterBases;
		
		if (extraBasesInLinked>0) {  //this read goes beyond the end of the master; lengthen Master
			addBasesToMasterRegistry(numMasterBases,extraBasesInLinked);
		}
		RegistryMapper rm = new RegistryMapper(numMasterBases, unpaddedLengthLinked, name);
		int count = 0;
		for (int i = 0; i<bases.length(); i++) {  
			if (i-count<unpaddedLengthLinked)
				rm.setMasterBaseAtLinkedBaseRegistryPosition(i-count, startBaseInMaster+i);
			if (bases.charAt(i)=='*')  //it's a padded position in the contig
				count++;
		}
		count = 0;
		for (int i = 0; i<rm.getMasterLength() && i<bases.length(); i++) {  
			if (bases.charAt(i)!='*'){  //it's not a padded position in the contig
				rm.setLinkedBaseAtMasterBaseRegistryPosition(startBaseInMaster+i, count);
				count++;
			}
			// note that if it is a padded position, we leave the master registry to be unassigned
		}
		phdRegistries.add(rm);
	}

	public RegistryMapper getPhdRegistry(int i){
		return (RegistryMapper)phdRegistries.get(i);
	}

	public void report(){
		if (originalContigRegistryMapper!=null) 
			originalContigRegistryMapper.report();
		if (trimmedContigRegistryMapper!=null) 
			trimmedContigRegistryMapper.report();
		if (viewerRegistryMapper!=null) 
			viewerRegistryMapper.report();
		if (matrixUnalignedRegistryMapper!=null) 
			matrixUnalignedRegistryMapper.report();
		if (matrixAlignedRegistryMapper!=null) 
			matrixAlignedRegistryMapper.report();
		for (int i = 0; i<phdRegistries.size(); i++)
			getPhdRegistry(i).report();
	}


}
