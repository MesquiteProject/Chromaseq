/* Mesquite chromaseq source code.  Copyright 2005-2009 D. Maddison and W. Maddison.Version 2.6, January 2009.Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.Perhaps with your help we can be more than a few, and make Mesquite better.Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Mesquite's web site is http://mesquiteproject.orgThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.chromaseq.lib; import mesquite.lib.*;import mesquite.categ.lib.*;import mesquite.cont.lib.*;import mesquite.lib.table.*;import java.util.*;/* ======================================================================== */public class  ChromViewContext  {	public Taxon taxon;	public Contig contig;	public Read[] reads;	public SequencePanel[] sequences;	public ContigDisplay contigPanel;		public int id;	public ChromViewContext(Taxon taxon, Contig contig, ContigDisplay contigPanel, Read[] reads, SequencePanel[] sequences, int id){		this.taxon = taxon;		this.contig= contig;		this.reads = reads;		this.sequences = sequences;		this.contigPanel = contigPanel;		this.id = id;	} 	}