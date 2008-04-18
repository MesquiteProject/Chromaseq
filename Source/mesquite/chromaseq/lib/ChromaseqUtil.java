package mesquite.chromaseq.lib;

import mesquite.lib.MesquiteInteger;
import mesquite.lib.NameReference;
import mesquite.lib.characters.CharacterData;

public class ChromaseqUtil{
	
	public static final int TRIMMABLE=1;
	public static final int BASECALLED=2;
	static NameReference trimmableNameRef = NameReference.getNameReference("trimmable");

	
   	public static boolean isTrimmable(int ic, int it, CharacterData data){
   		if (data == null)
   			return false;
		if (ic>=0 && it>=0){ 
			Object obj = data.getCellObject(trimmableNameRef, ic, it);
	   		if (obj != null && obj instanceof MesquiteInteger)
	   			return ((MesquiteInteger)obj).getValue()==ChromaseqUtil.TRIMMABLE;
		}
   		return false;
   	}
   	public static boolean baseCalled(int ic, int it, CharacterData data){
   		if (data == null)
   			return false;
		if (ic>=0 && it>=0){ 
			Object obj = data.getCellObject(trimmableNameRef, ic, it);
	   		if (obj != null && obj instanceof MesquiteInteger)
	   			return ((MesquiteInteger)obj).getValue()==ChromaseqUtil.BASECALLED;
		}
   		return false;
   	}


}
