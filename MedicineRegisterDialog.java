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
//*********************************************************************************
//Greatek Modification history :
//Date           Level                     Changed By            REQ/PTR No                  Change Description
//-------------  -----------------------   --------------------  -------------------------   --------------------------------
//2018/12/14     K0.00					   Hsiwei											 Kenda Rewrite New Vision
package com.ibm.mesexpress.opimes.dialog.crr;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;

import com.ibm.mesexpress.comm.TrxXML;
import com.ibm.mesexpress.opi.Global;
import com.ibm.mesexpress.opi.dialog.LVDialog;
import com.ibm.mesexpress.opi.widget.LVMultiDataTable;
import com.ibm.mesexpress.opi.widget.LVOPISubPanel;
import com.ibm.mesexpress.opi.widget.LVTextArea;
import com.ibm.mesexpress.opi.widget.LVTextField;
import com.ibm.mesexpress.opi.widget.Utility;
import com.ibm.mesexpress.tx.APCMTLSTi;
import com.ibm.mesexpress.tx.APCMTLSTi_a;
import com.ibm.mesexpress.tx.APCMTLSTi_a2;
import com.ibm.mesexpress.tx.APCMTLSTo;
import com.ibm.mesexpress.tx.APCSHTBMi_a;
import com.ibm.mesexpress.tx.APIMTLSTi;
import com.ibm.mesexpress.tx.APIMTLSTo;
import com.ibm.mesexpress.tx.APLPRDBMi;
import com.ibm.mesexpress.tx.APLPRDBMo;
import com.ibm.mesexpress.tx.APLRSVPRo_a;
import com.ibm.mesexpress.tx.XlgLen;
import com.ibm.mesexpress.tx.Xlgdef;

public class MedicineRegisterDialog extends LVDialog {
	private static final long serialVersionUID = -865067927156438321L;

	private LVTextField m_lotIDTF, m_markedCardIDTF, m_rawMtrlLotIDTF;
	private LVMultiDataTable m_mtrlListTB;
	private JButton m_check;
	private LVTextField m_mtrlProdIDTF;
	private LVTextArea m_needMtrlProdIDTA; 

	private APLRSVPRo_a m_rsvprOary;

	private APLPRDBMi m_aplprdbmi;
	private APLPRDBMo m_aplprdbmo;

	private APIMTLSTi m_apimtlsti;
	private APIMTLSTo m_apimtlsto;

	private APCMTLSTi m_apcmtlsti;
	private APCMTLSTo m_apcmtlsto;

	private ArrayList<String> m_mtrlPrdList;
	private String m_needMtrlProdID;
	private int m_errMsg;
	private Hashtable<String, Double> m_bomHashtable = new Hashtable<String, Double>(); 
	private MatchData m_matchData;
	private class MatchData {
		// aplprdbm_a2
		String mtrl_product_id;
		Double plan_qty;
		int ext_5;
		// material
		String mtrl_lot_id;
		Double mtrl_qty;
		String seq_no;
		// temp value
		Double std_qty; // Standard Quantity
	}

	public MedicineRegisterDialog(APLRSVPRo_a rsvPrOary) {
		super(Global.getText("Medicine_Register"), "MEDRE01");
		this.m_rsvprOary = rsvPrOary;
		m_mtrlPrdList = getMtrlProductIDArr();

		m_needMtrlProdID = ""; 
		for (int i = 0; i < m_mtrlPrdList.size(); i++) { 
			m_needMtrlProdID += m_mtrlPrdList.get(i); 

			if (i != m_mtrlPrdList.size() - 1) { 
				if (i != 0 && i % 6 == 0) 
					m_needMtrlProdID += "\n"; 
				else
					m_needMtrlProdID += " , ";
			} 
		} 
	}

	@Override
	protected void init() {
		this.setSize(770, 500);
		getContentPane().add(createMainP(), BorderLayout.CENTER);
		m_mtrlProdIDTF.setFocusable(false);
		m_needMtrlProdIDTA.setFocusable(false);
		m_markedCardIDTF.requestFocus();
		getRootPane().setDefaultButton(m_check);
	}

	private JPanel createMainP() {
		GridBagLayout gbl_rtnMainP = new GridBagLayout();
		JPanel rtnMainP = new JPanel(gbl_rtnMainP);

		// lotID
		m_lotIDTF = new LVTextField(XlgLen.LOT_ID_LEN, false);
		m_lotIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_lotIDTF.setText(this.m_rsvprOary.lot_id);
		m_lotIDTF.setEnabled(false);

		// MtrlProductId
		m_mtrlProdIDTF = new LVTextField(XlgLen.MTRL_ID_LEN); 
		m_mtrlProdIDTF.setPreferredSize(Utility.DEFAULT_LEN_250); 
		m_mtrlProdIDTF.setText(this.m_rsvprOary.mtrl_product_id);
		m_mtrlProdIDTF.setEditable(false);

		LVOPISubPanel pnl = new LVOPISubPanel();
		pnl.addPairComponent(new JLabel(Global.getText("Lot_ID")), m_lotIDTF);
		pnl.addPairComponent(new JLabel(Global.getText("Ingredient_Category")), m_mtrlProdIDTF);
		gbl_rtnMainP.setConstraints(pnl, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		rtnMainP.add(pnl);

		// need scan material product id
		m_needMtrlProdIDTA = new LVTextArea(XlgLen.CRR_NOTE_LEN); 
		m_needMtrlProdIDTA.setBackground(java.awt.Color.WHITE); 
		m_needMtrlProdIDTA.setFont(Global.DEFALT_MESSAGE_FONT); 
		m_needMtrlProdIDTA.setBorder(BorderFactory.createEtchedBorder()); 
		m_needMtrlProdIDTA.setText(m_needMtrlProdID); 
		m_needMtrlProdIDTA.setEditable(false); 
		JScrollPane sp = new JScrollPane(m_needMtrlProdIDTA); 
		sp.setOpaque(false); 
		sp.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
				Global.getText("Need_Mtrl_Prod_ID"), 
				TitledBorder.CENTER, TitledBorder.TOP, Global.DEFALT_LABEL_FONT, java.awt.Color.BLACK)); 
		gbl_rtnMainP.setConstraints(sp, new GridBagConstraints( 
				1, 0, 
				1, 1, 
				10.0, 0.0, 
				GridBagConstraints.WEST, 
				GridBagConstraints.BOTH, 
				new Insets(0, 0, 0, 0), 0, 0)); 
		rtnMainP.add(sp); 

		// marked card id
		m_markedCardIDTF = new LVTextField(XlgLen.MTRL_LOT_ID_LEN);
		m_markedCardIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_markedCardIDTF.addKeyListener(new KeyListener() { 
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (!chkMardCardId()) 
					m_markedCardIDTF.setText(null);

				if (Utility.isEmpty(m_markedCardIDTF.getText())) {
					m_markedCardIDTF.requestFocus(true);
				} else {
					m_rawMtrlLotIDTF.requestFocus(true);
				}
			}
			@Override
			public void keyPressed(KeyEvent e) {
			}
		}); 
		
		// raw material lot id
		m_rawMtrlLotIDTF = new LVTextField(XlgLen.MTRL_LOT_ID_LEN);
		m_rawMtrlLotIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);

		// confirm btn
		m_check = new JButton(Global.getText("Confirm"));
		m_check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (Utility.isEmpty(m_rawMtrlLotIDTF.getText()))
						return;

					if (!checkInputMaterialLot()) {
						switch (m_errMsg) {
						case 1:
							showWarningDialog(Global.getText("Input_Material_Not_Match"));
							break;
						case 2:
							showWarningDialog(Global.getText("The_RawMaterial_Is_Not_This_Medicine_Nedded"));
							break;
						case 3:
							showWarningDialog(Global.getText("Duplicate_Mtrl_Lot_ID_&_Mtrl_Product_ID"));
							break;
						case 4:
							showWarningDialog(Global.getText("Material_Lot_ID_QTY_Not_Enough"));
							break;
						case 5:
							showWarningDialog(Global.getText("This_Material_is_Expired."));
							break;
						default:
							break;
						}
					}
					m_rawMtrlLotIDTF.setText("");
					m_rawMtrlLotIDTF.requestFocus(true);

				} catch (Exception $$exception$$) {
					Utility.exceptionHandler($$exception$$);
				}
			}
		});

		LVOPISubPanel mtrlpnl = new LVOPISubPanel(2);
		mtrlpnl.addPairComponent(new JLabel(Global.getText("Marked_Card_ID")), m_markedCardIDTF);
		mtrlpnl.addPairComponent(new JLabel(Global.getText("Raw_Material_Lot_ID")), m_rawMtrlLotIDTF);
		mtrlpnl.add(m_check);
		gbl_rtnMainP.setConstraints(mtrlpnl, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		rtnMainP.add(mtrlpnl);

		// MT
		m_mtrlListTB = new LVMultiDataTable(Global.getText(""), "MedicineRegisterDialog.RegisterColumns",
				JTable.AUTO_RESIZE_OFF);
		gbl_rtnMainP.setConstraints(m_mtrlListTB, new GridBagConstraints(0, 2, 2, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		rtnMainP.add(m_mtrlListTB);
		m_mtrlListTB.setAllowEditFieldName("used_qty");

		return rtnMainP;
	}

	// get needed scan material
	private ArrayList<String> getMtrlProductIDArr() {
		ArrayList<String> arr = new ArrayList<String>();
		Double mtrlQty = 0.0;
		Double shtCnt = Utility.parseDouble(m_rsvprOary.sht_cnt);
		
		if (sendAplprdbm()) {
			for (int i = 0; i < Utility.parseInt(m_aplprdbmo.bom_cnt); i++) {
				if (m_aplprdbmo.oary1[i].ope_id.equals("PreSTB"))
					continue;

				for (int j = 0; j < Utility.parseInt(m_aplprdbmo.oary1[i].mtrl_cnt); j++) {
					if (m_rsvprOary.mtrl_product_id.equals(m_aplprdbmo.oary1[i].oary2[j].parent_id)){
						arr.add(m_aplprdbmo.oary1[i].oary2[j].mtrl_product_id);
						
						// Record all materials and corresponding quantities
						mtrlQty = shtCnt * Utility.parseDouble(m_aplprdbmo.oary1[i].oary2[j].plan_qty);
						if(!m_bomHashtable.containsKey(m_aplprdbmo.oary1[i].oary2[j].mtrl_product_id))
							m_bomHashtable.put(m_aplprdbmo.oary1[i].oary2[j].mtrl_product_id, mtrlQty);
					}
						 
				}
			}
		}
		arr.trimToSize(); // remove excess array space
		return arr;
	}
	
	// check marked material lot id is correct or not
	private boolean chkMardCardId() {
		char[] tmpMarkCardId = (m_markedCardIDTF.getText().toString()).toCharArray();
		if (Utility.toStringAndTrim(tmpMarkCardId[0]).equals("M") && tmpMarkCardId.length == 10)
			return true;
		else
			showWarningDialog(Global.getText("Wrong_Marked_Card_ID"));

		return false;
	}
	
	private boolean checkInputMaterialLot() {
		if (sendAPIMTLST()) {
			if (Utility.parseInt(m_apimtlsto.mtrl_cnt) == 0) {
				m_errMsg = 1;
				return false;
			} else {
				if (!checkParentID()) { 
					m_errMsg = 2;
					return false;
				}
				if (!chekcExpireDate()) {
					m_errMsg = 5;
					return false;
				}

				APCSHTBMi_a bom = new APCSHTBMi_a();
				bom.bom_product_id = m_matchData.mtrl_product_id; //record real bom material
				bom.mtrl_product_id = m_apimtlsto.oary[0].mtrl_product_id;
				bom.mtrl_lot_id = m_matchData.mtrl_lot_id;
				bom.seq_no = m_matchData.seq_no;
				if(m_matchData.std_qty > m_matchData.mtrl_qty) 
					bom.used_qty = Utility.toStringAndTrim(m_matchData.mtrl_qty);
				else 
					bom.used_qty = Utility.toStringAndTrim(m_matchData.std_qty);
				
				if (checkDuplicate(bom)) {
					m_errMsg = 3;
					return false;
				} else {
					m_mtrlListTB.appendTX(bom);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean checkParentID() {
		boolean isFound = false;
		String mtrlProdId = "";
		m_matchData = new MatchData();

		// Check if it is the material needed for the Drug pack
		for (int i = 0; i < m_mtrlPrdList.size(); i++) {
			if (m_apimtlsto.oary[0].mtrl_product_id.equals(m_mtrlPrdList.get(i))) {
				mtrlProdId = m_mtrlPrdList.get(i).trim();
				isFound = true;
			}
		}
		if(isFound == false) { // check if is the alternative material 
			for (int i = 0; i < m_mtrlPrdList.size(); i++) {
				for (int j = 0; j < Utility.parseInt(m_apimtlsto.mas_cnt); j++) {
					if (m_apimtlsto.oary2[j].mtrl_prod_id.equals(m_mtrlPrdList.get(i))) {
						mtrlProdId = m_mtrlPrdList.get(i).trim();
						isFound = true;
					}
				}
			}
		}
		if (isFound) {
			for (int i = 0; i < Utility.parseInt(m_aplprdbmo.oary1[1].mtrl_cnt); i++) {
				if (m_aplprdbmo.oary1[1].oary2[i].mtrl_product_id.equals(mtrlProdId)) {
					// bom
					m_matchData.mtrl_product_id = m_aplprdbmo.oary1[1].oary2[i].mtrl_product_id;
					m_matchData.plan_qty = Utility.parseDouble(m_aplprdbmo.oary1[1].oary2[i].plan_qty);
					m_matchData.ext_5 = Utility.parseInt(m_aplprdbmo.oary1[1].oary2[i].ext_5);
					// material
					m_matchData.mtrl_lot_id = m_apimtlsto.oary[0].mtrl_lot_id;
					m_matchData.mtrl_qty = Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty);
					m_matchData.seq_no = m_apimtlsto.oary[0].seq_no;
					// other:standard quantity 
					m_matchData.std_qty = m_matchData.plan_qty * Utility.parseDouble(m_rsvprOary.sht_cnt);
					break;
				}
			}
		}
		return isFound;
	}
	
	private boolean chekcExpireDate() {
		Calendar cal = Calendar.getInstance();
		String nowDate = Utility.convertDateToString(cal.getTime());
		if (m_apimtlsto.oary[0].expire_date.compareTo(nowDate) < 0)
			return false;
		else
			return true;
	}
	

	/********************************************/
	/* 是否輸入相同 mtrl lot id 及計算使用量                 */
	/********************************************/
	private boolean checkDuplicate(APCSHTBMi_a bom) {
		Hashtable<Integer, APCSHTBMi_a> ht = new Hashtable<Integer, APCSHTBMi_a>();
		
     	//defend use alternative materials or not
		for (int i = 0; i < m_mtrlListTB.getRowCount(); i++) {
			if (m_mtrlListTB.getValueByColName(i, "bom_product_id").equals(bom.bom_product_id)) {
				if (m_mtrlListTB.getValueByColName(i, "mtrl_lot_id").equals(bom.mtrl_lot_id)) {
					return true; // same mtrl lot id & mtrl product id -> not allow
				} else { // same mtrl prod id , different mtrl lot id -> allow
					ht.put(i, (APCSHTBMi_a) m_mtrlListTB.getTxAry()[i]);
				}
			}
		}
		// sum same mtrlProdId qty from mt
		double standardQty = m_matchData.std_qty;
		double mtQty = 0;
		double needQty = 0;

		for (int i : ht.keySet()) {
			mtQty += Utility.parseDouble(ht.get(i).used_qty);
		}
		if ( standardQty > mtQty ) {
			needQty = standardQty - mtQty;			
			if (Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty) < needQty) {
				bom.used_qty = String.format("%.5f", Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty));
			} else {
				bom.used_qty = String.format("%.5f", needQty);
			}
		} else {
			bom.used_qty = "0";
		}
		
		return false;
	}
	
	public void doOK() {
		if (m_markedCardIDTF.getText().trim().length() == 0) {
			showWarningDialog(Global.getText("Please_Input_Marked_Card_ID"));
			m_markedCardIDTF.requestFocus();
			return;
		}

		if (m_mtrlListTB.getRowCount() == 0) {
			showWarningDialog(this, Utility.sprint(Global.getText("Please_Input_{0}"), 
					Global.getText("Material_Lot_ID")));
			m_rawMtrlLotIDTF.requestFocus();
			return;
		}

		if(!checkColAndEng()){
			m_rawMtrlLotIDTF.requestFocus();
			return;
		}
			
		if (sendApcmtlst()) {
			showWarningDialog(Global.getText("Register_Success"));
		}
		super.doOK();
		return;

	}
	
	//check collected and quantity enough 
	private boolean checkColAndEng(){		
		Hashtable<String, Double> tmpHashtable = new Hashtable<String, Double>();
		tmpHashtable.putAll(m_bomHashtable); //initial
				
		Double stdQty = 0.0;
		Double rowQty = 0.0;
		Enumeration<String> mtrlProdId = null;
		
		for(int i=0; i< m_mtrlListTB.getRowCount(); i++){
			mtrlProdId = tmpHashtable.keys();
			while(mtrlProdId.hasMoreElements()){
				Object key = mtrlProdId.nextElement();
				if( key.equals(m_mtrlListTB.getValueByColName(i, "bom_product_id"))){
					stdQty = Utility.parseDouble(String.format("%.5f", tmpHashtable.get(key)));
					rowQty = Utility.parseDouble(m_mtrlListTB.getValueByColName(i, "used_qty"));  
					stdQty -= rowQty;	
					if(stdQty == 0.0)
						tmpHashtable.remove(key);
					else
						tmpHashtable.put(key.toString(), stdQty); //overwrite value
					
					break;
				}
			}
		}
		
		//show not enough material and last value
		mtrlProdId = tmpHashtable.keys();
		while(mtrlProdId.hasMoreElements()){
			Object key = mtrlProdId.nextElement();
			showWarningDialog("【 " + key.toString() + " 】" 
						+ Global.getText("Not_enough")
						+ String.format("%.5f", tmpHashtable.get(key)));
			return false;
		}
		return true;
	}

	private boolean sendAPIMTLST() {
		m_apimtlsti = new APIMTLSTi();
		m_apimtlsti.mtrl_lot_id = m_rawMtrlLotIDTF.getText();
		m_apimtlsti.query_sub_mtrl = Xlgdef._YES;
		m_apimtlsto = (APIMTLSTo) TrxXML.sendRecv(m_apimtlsti);
		boolean rtncode = Utility.checkReturnCode(m_apimtlsto);
		if (!rtncode) {
			m_apimtlsti = null;
			m_apimtlsto = null;
			return false;
		} else {
			m_apimtlsti = null;
			return true;
		}
	}

	private boolean sendAplprdbm() {
		m_aplprdbmi = new APLPRDBMi();
		m_aplprdbmi.product_id = m_rsvprOary.product_id;
		m_aplprdbmi.ec_code = m_rsvprOary.ec_code;
		m_aplprdbmi.route_id = m_rsvprOary.route_id;
		m_aplprdbmi.route_ver = m_rsvprOary.route_ver;
		m_aplprdbmi.ope_id = m_rsvprOary.nx_ope_id;
		m_aplprdbmi.ope_ver = m_rsvprOary.nx_ope_ver;
		m_aplprdbmi.resv_eqpt_id = m_rsvprOary.resv_eqpt_id;

		m_aplprdbmo = (APLPRDBMo) TrxXML.sendRecv(m_aplprdbmi);
		boolean rtncode = Utility.checkReturnCode(m_aplprdbmo);
		if (!rtncode) {
			m_aplprdbmi = null;
			m_aplprdbmo = null;
			return false;
		} else {
			m_aplprdbmi = null;
			return true;
		}
	}

	private boolean sendApcmtlst() {
		m_apcmtlsti = new APCMTLSTi();
		m_apcmtlsti.clm_mtst_typ = Xlgdef.ACT_TYPE_ADD;
		m_apcmtlsti.user_id = Global.MainFrame.getLoginUser();
		m_apcmtlsti.mtrl_cnt = "1";
		m_apcmtlsti.bom_id = m_aplprdbmo.oary1[1].bom_id; 

		APCSHTBMi_a[] mtData = (APCSHTBMi_a[]) m_mtrlListTB.getTxAry();

		m_apcmtlsti.iary[0] = new APCMTLSTi_a();
		m_apcmtlsti.iary[0].mtrl_product_id = m_mtrlProdIDTF.getText(); 
		m_apcmtlsti.iary[0].mtrl_lot_id = m_markedCardIDTF.getText(); 
		m_apcmtlsti.iary[0].line_id = m_rsvprOary.line_id;
		m_apcmtlsti.iary[0].comment = "PREP:" + m_rsvprOary.prep_type + ":" 
										+ m_rsvprOary.lot_id + ":"
										+ m_rsvprOary.nx_ope_no + ":" 
										+ m_rsvprOary.mtrl_product_id + ":" 
										+ m_rsvprOary.sht_cnt;
		
		m_apcmtlsti.iary[0].mtrl_qty = calculate(mtData);
		m_apcmtlsti.iary[0].per_sht_qty = "1";
		m_apcmtlsti.iary[0].sht_cnt = m_rsvprOary.sht_cnt;
		m_apcmtlsti.iary[0].mtrl_stat = Xlgdef.MTRL_LOT_STAT_AVAL;
		m_apcmtlsti.iary[0].tar_eqpt_id = m_rsvprOary.resv_eqpt_id;
		m_apcmtlsti.iary[0].flag = "P"; 
		m_apcmtlsti.iary[0].expire_date = getExpireDate();
		
		m_apcmtlsti.iary[0].iary2_cnt = Utility.toStringAndTrim(mtData.length);
		for (int i = 0; i < mtData.length; i++) {
			m_apcmtlsti.iary[0].iary2[i] = new APCMTLSTi_a2();
			m_apcmtlsti.iary[0].iary2[i].use_mtrl_product_id = mtData[i].mtrl_product_id;
			m_apcmtlsti.iary[0].iary2[i].use_mtrl_lot_id = mtData[i].mtrl_lot_id;
			m_apcmtlsti.iary[0].iary2[i].use_qty = mtData[i].used_qty;
			m_apcmtlsti.iary[0].iary2[i].lot_id = m_lotIDTF.getText();
			m_apcmtlsti.iary[0].iary2[i].use_seq_no = mtData[i].seq_no;
		}

		m_apcmtlsto = (APCMTLSTo) TrxXML.sendRecv(m_apcmtlsti);
		boolean rtncode = Utility.checkReturnCode(m_apcmtlsto);
		if (!rtncode) {
			m_apcmtlsti = null;
			m_apcmtlsto = null;
			return false;
		} else {
			m_apcmtlsti = null;
			return true;
		}
	}
	
	// calculate total quantity
	private String calculate(APCSHTBMi_a[] multiDataTable) {
		double totalUsed = 0;
		for (int i = 0; i < multiDataTable.length; i++)
			totalUsed += Utility.parseDouble(multiDataTable[i].used_qty);
		
		return String.format("%.5f", totalUsed);
	}
	
	// calculate expire date
	private String getExpireDate(){
		int validPeriod = m_matchData.ext_5;
		if (validPeriod > 0) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, validPeriod);
			return Utility.convertDateToString(cal.getTime());
		}
		return "";
	}
}
