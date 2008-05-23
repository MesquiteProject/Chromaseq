package mesquite.chromaseq.lib;

import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.cont.lib.*;
import mesquite.categ.lib.*;
import mesquite.meristic.lib.*;

public class ChromaseqBaseMapper {
	CharacterData data;
	ContinuousData qualityData;
	DNAData originalData;
	MeristicData registryData;
	MeristicData reverseRegistryData;
	boolean valid = false;

	public ChromaseqBaseMapper (CharacterData data){
		this.data = data;
		registryData = ChromaseqUtil.getRegistryData(data);
		reverseRegistryData = ChromaseqUtil.getReverseRegistryData(data);
		qualityData = ChromaseqUtil.getQualityData(data);
		originalData = ChromaseqUtil.getOriginalData(data);
		if (registryData!=null && qualityData!=null && originalData!=null)
			valid=true;
	}

	public boolean isValid() {
		return valid;
	}

	public double getQualityScore(int ic, int it){
		if (!isValid())
			return MesquiteDouble.unassigned;
		int mapping = registryData.getState(ic, it);
		return qualityData.getState(mapping, it, 0);
	}

	public long getOriginalState(int ic, int it){
		if (!isValid())
			return MesquiteLong.unassigned;
		int mapping = registryData.getState(ic, it);
		return originalData.getState(mapping, it);
	}

	public boolean originalIsInapplicable(int ic, int it){
		if (!isValid())
			return true;
		int mapping = registryData.getState(ic, it);
		return originalData.isInapplicable(mapping, it);
	}

	public int findNearestEditMatrixPosition(int originalPos, int it){
		if (!isValid())
			return -1;
		int pos = MesquiteInteger.impossible;
		if (reverseRegistryData.isInapplicable(originalPos, it)) {
			pos = reverseRegistryData.firstApplicable(it);
			if (pos>originalPos){ // we go up
				int newPos = reverseRegistryData.getState(pos, it) ;
				return (pos-originalPos-newPos);
			}
			else {
				pos = reverseRegistryData.lastApplicable(it);
				if (pos<originalPos) {// we go down
					int newPos = reverseRegistryData.getState(pos, it) ;
					return originalPos-pos+newPos;
				}

			}
			
		}
		return -1;
	}

	public int getEditedMatrixPositionFromOriginal(int originalPos, int it){
		if (!isValid())
			return -1;
		if (reverseRegistryData==null) {
			for(int ic=0; ic<registryData.getNumChars(); ic++) {
				int mapping = registryData.getState(ic, it);
				if (mapping==originalPos)
					return ic;
			}
		}
		else  {
			int pos = reverseRegistryData.getState(originalPos, it) ;
			if (pos<0 || MesquiteInteger.isInapplicable(pos) || !MesquiteInteger.isCombinable(pos))  // there is no reverse registry data for this one, so find the nearest one
				pos = findNearestEditMatrixPosition(originalPos, it);
			return pos;
		}
		return -1;

	}
	public int getOriginalPositionFromEditedMatrix(int ic, int it){
		if (!isValid())
			return -1;
		return registryData.getState(ic, it);
	}



}
