/* Mesquite Chromaseq source code.  Copyright 2005-2010 David Maddison and Wayne Maddison.Version 0.980   July 2010Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html) */package mesquite.chromaseq.lib; import mesquite.lib.*;import java.awt.*;public class ChromaseqInfoPanel extends MesquitePanel {	String info;		public ChromaseqInfoPanel(String info) {		super();		this.info = info;	}		/*..........................*/	public void paint(Graphics g) {		g.drawString(info, 5, 16);	}}