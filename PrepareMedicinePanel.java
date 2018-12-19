//*********************************************************************************
//
//SYSTEM        : MESExpress System
//
//PROGRAM NAME  : ManageProcessPanel.java
//
//Outline       : Group Numbering
//
//(c) Copyright 2005, International Business Machines Corp
//
//Modification history:
//
//DATE        LEVEL  NAME             COMMENT
//----------  -----  ---------------  --------------------------------------------
//2007/05/29  A0.00  IBM              Initial release
//*********************************************************************************
//Greatek Modification history :
//Date           Level                     Changed By            REQ/PTR No                  Change Description
//-------------  -----------------------   --------------------  -------------------------   --------------------------------
//2017/02/21     K0.00					   Hsiwei											 Kenda Release New Vision
//2017/03/07     K0.01					   Hsiwei											 modify line_id and eqpt_id combobox
//2017/04/06     K0.02					   Hsiwei											 Add Color When ope_no_flag='N' and Add Key
//2017/05/19     K0.03					   Allen											 show data after chosen date
//2017/07/05     K0.04					   Hsiwei											 Add Multithread and 3 TF
//2017/07/17     K0.05					   Hsiwei											 Add sub material
//2017/07/18     K0.06					   Hsiwei											 Fix Bug
//2017/08/16     K0.07					   Hsiwei											 If didn't find sub material m_finish=NO 
//2017/08/24     K0.08					   Hsiwei											 Add new button: Update_Stat
//2017/08/28     K0.09					   Hsiwei											 Change Medicine Combox to TextFiled 
//2017/09/22     K0.10					   Hsiwei											 Fix Cut Rubber Sht Cnt Bug 
//2017/11/08     K0.11					   Hsiwei											 Show Out Sourcing Lot ID
//2017/11/20     K0.12					   Hsiwei											 Fix Replace Material Bug  
//2017/11/27     K0.13					   Hsiwei											 Add outside flag
//2018/02/26  	 K0.14  				   Hsiwei 		  									 Add Moron Control for Kenda Super Prodigy
//2018/08/09  	 K0.15					   Hsiwei 		  									 Expired Date Check logic 
//2018/12/03  	 K0.16					   Hsiwei 		  									 Returning to the primitive era from the age of civilization
package com.ibm.mesexpress.opimes.panels.stb;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import com.ibm.mesexpress.comm.TrxXML;
import com.ibm.mesexpress.opi.Global;
import com.ibm.mesexpress.opi.panels.OPIPanel;
import com.ibm.mesexpress.opi.widget.LVMultiDataTable;
import com.ibm.mesexpress.opi.widget.LVOPISubPanel;
import com.ibm.mesexpress.opi.widget.Utility;
//K0.16 import com.ibm.mesexpress.opimes.dialog.input.CutRubberDialog;
import com.ibm.mesexpress.opimes.dialog.crr.CutRubberDialog;	//K0.16
import com.ibm.mesexpress.opimes.dialog.crr.MedicineRegisterDialog;
import com.ibm.mesexpress.opimes.widget.LVSelectBayComboBox;
import com.ibm.mesexpress.opimes.widget.LVSelectCodeCateComboBox;
import com.ibm.mesexpress.opimes.widget.LVSelectEqpByTypeComboBox;
import com.ibm.mesexpress.tx.APCCHSTEi;
import com.ibm.mesexpress.tx.APCCHSTEo;
import com.ibm.mesexpress.tx.APLRSVPRi;
import com.ibm.mesexpress.tx.APLRSVPRo;
import com.ibm.mesexpress.tx.APLRSVPRo_a;
import com.ibm.mesexpress.tx.Xlgdef;

public class PrepareMedicinePanel extends OPIPanel{

	private static final long serialVersionUID = -7131558673306775616L;

	// GUI
	private LVSelectBayComboBox m_bayIDCB;
	private LVSelectEqpByTypeComboBox m_eqptIDCB;
	private LVSelectCodeCateComboBox m_prepTypeCB;
	private LVMultiDataTable m_MedicineMT;


	private APLRSVPRi 	m_aplrsvpri;
	private APLRSVPRo 	m_aplrsvpro;
	private APCCHSTEi   m_apcchstei;			//K0.08
	private APCCHSTEo	m_apcchsteo;			//K0.08
	
	private JCheckBox m_outside_chk;			//K0.13
	
	public PrepareMedicinePanel() {
		super(Global.getText("Prepare_Medicine_Panel"), "PM001");
	}

	// 畫panel的地方
	protected void initWorkArea() {
		try {
			addComponent(Up_Panel(), new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			// m_MedicineMT
			m_MedicineMT = new LVMultiDataTable(Global.getText(""), "MedicinePanel.MedicineColumns",
					JTable.AUTO_RESIZE_ALL_COLUMNS);
			m_MedicineMT.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
					Global.getText(""), TitledBorder.LEADING, TitledBorder.TOP));
			addComponent(m_MedicineMT, new GridBagConstraints(0, 1, 5, 1, 1.0, 1.0, GridBagConstraints.WEST,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			m_MedicineMT.setSortByColumnEnabled(false); // 欄位全鎖住
			
			//K0.08 Add Start
			m_MedicineMT.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent me) {
					try {
						String type = m_MedicineMT.getValueByColName(m_MedicineMT.getSelectedRow(),"prep_type").toString();
						String stat = m_MedicineMT.getValueByColName(m_MedicineMT.getSelectedRow(),"prep_stat").toString();
						
						if(stat.equals(Xlgdef.PROC_COMPLETE)){
							setButtonEnabledByName("Update_Stat", false);
						}else{
							setButtonEnabledByName("Update_Stat", true); 
							if(type.equals(Xlgdef.PREP_TYPE_DRUG) && stat.equals(Xlgdef.INITIAL_STATUS)){
								setButtonEnabledByName("Update_Stat", false); 
							}
						}
						
					} catch (Exception $$exception$$) {
						Utility.exceptionHandler($$exception$$);
					}
				}
			});	
			//K0.08 Add End	
			
			setButtonEnabledByName("Preparation", false); // initial set disable
			
		} catch (Exception $$exception$$) {
			Utility.exceptionHandler($$exception$$);
		}

	}
	
	
	
	private JPanel Up_Panel() {
		GridBagLayout gbl = new GridBagLayout();
		JPanel rtnComp = new JPanel(gbl);
		rtnComp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Global.getText(""),
				TitledBorder.LEADING, TitledBorder.TOP));

		// eqpt(要寫在Bay之前,這樣bay做addPropertyChangeListener才有東西)
		LVOPISubPanel rtnRqpt = new LVOPISubPanel(2);
		m_eqptIDCB = new LVSelectEqpByTypeComboBox();
		rtnRqpt.addPairComponent(new JLabel(Global.getText("EQPT_ID")), m_eqptIDCB);
		gbl.setConstraints(rtnRqpt, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
		rtnComp.add(rtnRqpt);
		m_eqptIDCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});

		// Bay
		LVOPISubPanel rtnBay = new LVOPISubPanel(2);
		m_bayIDCB = new LVSelectBayComboBox(LVSelectBayComboBox.SELECT_BAY_FOR_OPI);
		rtnBay.addPairComponent(new JLabel(Global.getText("Bay_ID")), m_bayIDCB);
		gbl.setConstraints(rtnBay, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		rtnComp.add(rtnBay);
		m_bayIDCB.addPropertyChangeListener(m_eqptIDCB);
		m_bayIDCB.setPreferredSize(DEFAULT_PRE_CB_D);
		
		//Type
     	LVOPISubPanel rtnType = new LVOPISubPanel(2);
     	m_prepTypeCB = new LVSelectCodeCateComboBox(Xlgdef.CODE_CATE_MPAR);
     	rtnType.addPairComponent(new JLabel(Global.getText("Prepare_Type")), m_prepTypeCB);
     	gbl.setConstraints(rtnType, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
     			GridBagConstraints.NONE, new Insets(0, 5, 0, 5), 0, 0));
//K0.13     	rtnComp.add(rtnType);
     	m_prepTypeCB.setInitiData(null);
     	
     	m_prepTypeCB.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
								
				if(Utility.isEmpty(m_prepTypeCB.getSelectedCode())){
					setButtonEnabledByName("Preparation", false);
				} else{
					setButtonEnabledByName("Preparation", true);
				}
				
				refresh();
			}
		});
     	
     	//K0.13 Add Start
     	m_outside_chk = new JCheckBox(Global.getText("OUTSIDE"));
     	m_outside_chk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
     	rtnType.add(m_outside_chk);
     	rtnComp.add(rtnType);
     	//K0.13 Add End
    
		return rtnComp;
	}



	//K0.08 Add Start
	public boolean preUpdateStat(){
		if(m_MedicineMT.getSelectedSubAry()== null ){
			showWarningDialog(Global.getText("Please_Select_One_Record"));
			return false;
		}

		String selectType = m_MedicineMT.getSelectedItemByName("prep_type");
		String selectStat = m_MedicineMT.getSelectedItemByName("prep_stat");
		
		//don't cut rubber case.
		if(selectType.equals(Xlgdef.PREP_TYPE_RCUT) && selectStat.equals(Xlgdef.INITIAL_STATUS)){
			int rc = JOptionPane.showConfirmDialog(this,Global.getText("Are_you_sure_don't_need_to_Cut_Rubber?"),
														Global.PRODUCT_TITLE,JOptionPane.YES_NO_OPTION);
			if(rc == JOptionPane.YES_OPTION)
				return true;
			else
				return false;
		}
		return true;
	}
	public boolean doUpdateStat(){
		if(sendAPCCHSTE()){
			if(m_apcchsteo.stat.equals(Xlgdef.PROC_COMPLETE)){
				showWarningDialog(Global.getText("Prepare_Material_State_Complete"));
				refresh();
			} else {
				int mtrl_cnt = Utility.parseInt(m_apcchsteo.mtrl_cnt);
				if( mtrl_cnt>0 ){ //DRUG
					String msg="\n";
					for(int i=0; i<Utility.parseInt(m_apcchsteo.mtrl_cnt); i++){
						msg += Utility.toString(m_apcchsteo.oary[i].mtrl_product_id);
						
						if(i<Utility.parseInt(m_apcchsteo.mtrl_cnt)-1)
							msg += "\n";
					}
					showWarningDialog(Global.getText("Prepare_Material_State_Not_Complete_,_Need_To_Prepare_Material_")+ msg);	 
				}else{ //RCUT
					showWarningDialog(Global.getText("Prepare_Material_State_Not_Complete"));
				}
			}
		}
		return true;
	}
	//K0.08 Add End
	
	public boolean prePreparation() {
		if (m_MedicineMT.getSelectedSubAry() == null) {
			showWarningDialog(Global.getText("Please_Select_One_Record"));
			return false;
		}

		if (m_MedicineMT.getSelectedItemByName("prep_stat").equals(Xlgdef.PROC_COMPLETE)) {
			showWarningDialog(Global.getText("The_preparation_already_done"));
			return false;
		}

		return true;
	}

	public boolean doPreparation() {
		boolean rc;

		switch (m_MedicineMT.getSelectedItemByName("prep_type")) {
		case Xlgdef.PREP_TYPE_DRUG:
			rc = doPrepareMedicineRegister();
			break;

		case Xlgdef.PREP_TYPE_RCUT:
			rc = doCutRubber();
			break;

		default:
			showWarningDialog(Global.getText("Wrong_Preparation_Type"));
			rc = false;
			break;
		}

		if (rc)
			refresh();

		return rc;
	}
	
	private boolean doPrepareMedicineRegister() {
		try {

			APLRSVPRo_a oary = (APLRSVPRo_a) m_MedicineMT.getSelectedSubAry();
			MedicineRegisterDialog dialog = new MedicineRegisterDialog(oary);
//K0.09		if (dialog.isNotMedicine()) {
//K0.09			showWarningDialog(Global.getText("This_Lot_ID_Don't_Need_To_Prepare_Medicine"));
//K0.09			return false;
//K0.09		}

			dialog.setDefaultOkBtn(false);
			dialog.setVisible(true);

			if (dialog.isCancelPresssed()) {
				return false;
			}

		} catch (Exception $$exception$$) {
			Utility.exceptionHandler($$exception$$);
		}

		return true;
	}

	
	private boolean doCutRubber() {
		try {
			APLRSVPRo_a oary = (APLRSVPRo_a) m_MedicineMT.getSelectedSubAry();
			
			
			CutRubberDialog dialog = new CutRubberDialog(oary);
			
			
			if (!dialog.CheckRubber()){
				showWarningDialog("Don't Need To Cut Rubber");
				return false;
			}
			
			if (dialog.isCancelPresssed()) {				
				return false;
			}
			
			dialog.setDefaultOkBtn(false);
			dialog.setVisible(true);
			
		} catch (Exception $$exception$$) {
			Utility.exceptionHandler($$exception$$);
		}
		return true;
	}
	
	@Override
	public void refresh() {
		try {
			if (sendAplrsvpr()){
				m_MedicineMT.setTxObject(m_aplrsvpro.oary);
				m_MedicineMT.setRowForegroundColor(Color.RED, "lot_stat", "OSUR");	//K0.11
			}
			else
				m_MedicineMT.clearAllSetValue();
			
			

		} catch (Exception $$exception$$) {
			Utility.exceptionHandler($$exception$$);
		}
	}

	
	// Aplrsvpr
	private boolean sendAplrsvpr() {
		m_aplrsvpri = new APLRSVPRi();
		m_aplrsvpri.bay_id = m_bayIDCB.getSelectedBayID();
		m_aplrsvpri.resv_eqpt_id = m_eqptIDCB.getSelectedEqpID();
		m_aplrsvpri.resv_date = "1911-01-01";
//K0.04		m_aplrsvpri.resv_date = Utility.convertDateToString(m_claimStartDateCB.getSelectedDate());
		m_aplrsvpri.prep_type = m_prepTypeCB.getSelectedCode();
		
		if(m_outside_chk.isSelected())						//K0.13
			m_aplrsvpri.only_outside_flg = Xlgdef._YES;		//K0.13

		m_aplrsvpro = (APLRSVPRo) TrxXML.sendRecv(m_aplrsvpri);
		m_aplrsvpri = null;

		if (!Utility.checkReturnCode(m_aplrsvpro)) {
			m_aplrsvpro = null;
			return false;
		}

		return true;
	}

//	private void setEnable(boolean flg, String type){
//		if(Utility.isEmpty(type)){
//			setButtonEnabledByName("Preparation", flg);
//		}else{
//			setButtonEnabledByName("Preparation", !flg); //設定畫面上底下的button
//		}
//	}
	//K0.04 Add End
	
	//K0.08 Add Start
	private boolean sendAPCCHSTE(){
		
		m_apcchstei = new APCCHSTEi();
		m_apcchstei.type 			= m_MedicineMT.getSelectedItemByName("prep_type");
		m_apcchstei.stat 			= m_MedicineMT.getSelectedItemByName("prep_stat");
		m_apcchstei.bom_id 			= m_MedicineMT.getSelectedItemByName("nx_ope_id");
		m_apcchstei.lot_id 			= m_MedicineMT.getSelectedItemByName("lot_id");
		m_apcchstei.sht_id 			= m_MedicineMT.getSelectedItemByName("lot_id")+"000";
		m_apcchstei.nx_ope_no 		= m_MedicineMT.getSelectedItemByName("nx_ope_no$rsv_ope_no");
		m_apcchstei.mtrl_product_id = m_MedicineMT.getSelectedItemByName("mtrl_product_id");
		m_apcchstei.user_id 		= Global.MainFrame.getLoginUser();
//K0.10		m_apcchstei.sht_cnt 		= m_MedicineMT.getSelectedItemByName("sht_cnt");
		
		m_apcchsteo = (APCCHSTEo) TrxXML.sendRecv(m_apcchstei);
		boolean rtncode = Utility.checkReturnCode(m_apcchsteo);
		if(!rtncode){
			m_apcchstei = null;
			m_apcchsteo = null;
			return false;
		}
		else{
			m_apcchstei = null;
			return true;
		}
	}
	//K0.08 Add End

}
