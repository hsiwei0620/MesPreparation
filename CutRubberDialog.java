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
//2018/12/05     K0.00					   Hsiwei											 Kenda Release New Vision	
package com.ibm.mesexpress.opimes.dialog.crr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import com.ibm.mesexpress.comm.TrxXML;
import com.ibm.mesexpress.opi.Global;
import com.ibm.mesexpress.opi.dialog.LVDialog;
import com.ibm.mesexpress.opi.widget.LVMultiDataTable;
import com.ibm.mesexpress.opi.widget.LVOPISubPanel;
import com.ibm.mesexpress.opi.widget.LVTextField;
import com.ibm.mesexpress.opi.widget.Utility;
import com.ibm.mesexpress.tx.APCCHSTEi;
import com.ibm.mesexpress.tx.APCCHSTEo;
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

public class CutRubberDialog extends LVDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4350591334554612596L;

	private LVTextField m_lotIDTF, m_bomIDTF, m_mtrlProdIDTF, 
						m_markedCardIDTF, m_rawMtrlLotIDTF, 
						m_perShtWeightTF, m_prepShtCntTF, m_totalWeightTF;

	private LVMultiDataTable m_mtrlListTB;
	private JButton m_check;

	private APLRSVPRo_a m_rsvprOary;

	private APLPRDBMi m_aplprdbmi;
	private APLPRDBMo m_aplprdbmo;

	private APIMTLSTi m_apimtlsti;
	private APIMTLSTo m_apimtlsto;

	private APCMTLSTi m_apcmtlsti;
	private APCMTLSTo m_apcmtlsto;
	
	private APCCHSTEi m_apcchstei;
	private APCCHSTEo m_apcchsteo;

	private int m_errMsg;
	private int m_validPeriod; 
	private boolean m_firstLoad = true;
	private int m_prepShtCnt;
	private double m_perShtWeight;
	private String m_totalWeight;
	
	public CutRubberDialog(APLRSVPRo_a rsvPrOary) {
		super(Global.getText("Rubber_Register"), "RUBRE01");
		this.m_rsvprOary = rsvPrOary;
		this.m_prepShtCnt = getPrepShtCnt();
	}

	public boolean CheckRubber() {
		boolean result = false;
		if (sendAplprdbm()) {
			for (int i = 0; i < Utility.parseInt(m_aplprdbmo.bom_cnt); i++) {
				if (m_aplprdbmo.oary1[i].ope_id.equals("PreSTB"))
					continue;

				for (int j = 0; j < Utility.parseInt(m_aplprdbmo.oary1[i].mtrl_cnt); j++) {
					if (Xlgdef.PREP_TYPE_RCUT.equals(m_aplprdbmo.oary1[i].oary2[j].ext_3)) { 
						result = true;
						break;
					}
				}
			}
		}
		return result;
	}

	@Override
	protected void init() {
		this.setSize(770, 500);
		getContentPane().add(createMainP(), BorderLayout.CENTER);

		// set default focus
		m_lotIDTF.setFocusable(false);
		m_bomIDTF.setFocusable(false);
		m_mtrlProdIDTF.setFocusable(false);
		m_perShtWeightTF.setFocusable(false);
		m_prepShtCntTF.setFocusable(false);
		m_totalWeightTF.setFocusable(false);
		m_markedCardIDTF.requestFocus();

		getRootPane().setDefaultButton(m_check); 
	}

	private JPanel createMainP() {
		GridBagLayout gbl_rtnMainP = new GridBagLayout();
		JPanel rtnMainP = new JPanel(gbl_rtnMainP);

		LVOPISubPanel pnl = new LVOPISubPanel();
		// lotID
		m_lotIDTF = new LVTextField(XlgLen.LOT_ID_LEN, false);
		m_lotIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_lotIDTF.setText(this.m_rsvprOary.lot_id);
		m_lotIDTF.setEnabled(false);

		// Bom ID
		m_bomIDTF = new LVTextField(XlgLen.BOM_ID_LEN, false);
		m_bomIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_bomIDTF.setText(this.m_rsvprOary.nx_ope_id);
		m_bomIDTF.setEditable(false);

		// MtrlProduct ID
		m_mtrlProdIDTF = new LVTextField(XlgLen.MTRL_ID_LEN);
		m_mtrlProdIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_mtrlProdIDTF.setText(this.m_rsvprOary.mtrl_product_id);
		m_mtrlProdIDTF.setEditable(false);

		pnl.addPairComponent(new JLabel(Global.getText("Lot_ID")), m_lotIDTF);
		pnl.addPairComponent(new JLabel(Global.getText("Oper_ID")), m_bomIDTF);
		pnl.addPairComponent(new JLabel(Global.getText("MTRL_PROD_ID")), m_mtrlProdIDTF);
		gbl_rtnMainP.setConstraints(pnl, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		rtnMainP.add(pnl);

		// Rubber Detail Area
		LVOPISubPanel subP = new LVOPISubPanel();
		subP.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), Global
				.getText("Rubber_Detail"), TitledBorder.CENTER, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		m_perShtWeightTF = new LVTextField(XlgLen.MTRL_ID_LEN);
		m_perShtWeightTF.setPreferredSize(Utility.DEFAULT_LEN_250);
//		m_perShtWeightTF.setText(this.m_perShtWeight);
		m_perShtWeightTF.setEditable(false);

		m_prepShtCntTF = new LVTextField(XlgLen.MTRL_ID_LEN);
		m_prepShtCntTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_prepShtCntTF.setText(Utility.toStringAndTrim(m_prepShtCnt));
		m_prepShtCntTF.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if(Utility.parseInt(m_prepShtCntTF.getText()) > m_prepShtCnt){
					showWarningDialog(Global.getText("Can't_Over_Prepare"));
					m_prepShtCntTF.setText(Utility.toStringAndTrim(m_prepShtCnt));
					return;
				}
				
				m_mtrlListTB.setTxObject(null);
				m_totalWeightTF.setText(null);
				
				double totalWg = Utility.parseDouble(m_perShtWeightTF.getText())
						* Utility.parseDouble(m_prepShtCntTF.getText());
				m_totalWeightTF.setText(String.format("%.5f", totalWg));
				
				m_prepShtCnt = Utility.parseInt(m_prepShtCntTF.getText());
				m_totalWeight = String.format("%.5f", totalWg); 
			}
		});

		m_totalWeightTF = new LVTextField(XlgLen.MTRL_ID_LEN);
		m_totalWeightTF.setPreferredSize(Utility.DEFAULT_LEN_250);
//		m_totalWeightTF.setText(this.m_totalWeight);
		m_totalWeightTF.setEditable(false);

		subP.addPairComponent(new JLabel(Global.getText("Rubber_Sht_weight")), m_perShtWeightTF);
		subP.addPairComponent(new JLabel(Global.getText("Total_Sheet_Count")), m_prepShtCntTF);
		subP.addPairComponent(new JLabel(Global.getText("Total_Rubber_weight")), m_totalWeightTF);

		gbl_rtnMainP.setConstraints(subP, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		rtnMainP.add(subP);

		// MarkCard and Raw Material Lot ID
		LVOPISubPanel mtrlpnl = new LVOPISubPanel(2);
		m_markedCardIDTF = new LVTextField(XlgLen.MTRL_LOT_ID_LEN);
		m_markedCardIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);

		// Raw Material Lot ID
		m_rawMtrlLotIDTF = new LVTextField(XlgLen.MTRL_LOT_ID_LEN);
		m_rawMtrlLotIDTF.setPreferredSize(Utility.DEFAULT_LEN_250);
		m_rawMtrlLotIDTF.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if(keyCode == KeyEvent.VK_SPACE && m_firstLoad==false)
					m_prepShtCntTF.setFocusable(true);
			}
		});
		
		// Confirm Button
		m_check = new JButton(Global.getText("Confirm"));
		m_check.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {					
					// Basic format control
					if (!checkMtrlLotId() || Utility.isEmpty(m_rawMtrlLotIDTF.getText())){
						m_markedCardIDTF.setText(null);
						m_rawMtrlLotIDTF.setText(null);
						m_markedCardIDTF.requestFocus(true);
						return;
					}
					if (!checkInputMaterialLot()) {
						switch (m_errMsg) {
						case 1:
							showWarningDialog(Global.getText("Input_Material_Not_Match"));
							break;
						case 2:
							showWarningDialog(Global.getText("Duplicate_Mtrl_Lot_ID_&_Mtrl_Product_ID"));
							break;
						case 3:
							showWarningDialog(Global.getText("This_Material_is_Expired."));
							break;
						case 4:
							showWarningDialog(Global.getText("Using_alternative_materials_but_not_found"));
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
//		m_mtrlListTB.setAllowEditFieldName("used_qty");

		return rtnMainP;
	}

	// set rubber detail
	private void setRubberArea() {
		double perShtWeight = 0.0 , planQty = 0.0;
		double minUseQty = Utility.parseDouble(m_apimtlsto.oary[0].min_use_qty);
		
		for (int i = 0; i < Utility.parseInt(m_aplprdbmo.oary1[1].mtrl_cnt); i++) {
			if (m_rsvprOary.mtrl_product_id.equals(m_aplprdbmo.oary1[1].oary2[i].mtrl_product_id)) {
				planQty = Utility.parseDouble(m_aplprdbmo.oary1[1].oary2[i].plan_qty);
				m_validPeriod = Utility.parseInt(m_aplprdbmo.oary1[1].oary2[i].ext_5);
				break;
			}
		}
		
		if (minUseQty != 0.0) {
			perShtWeight = planQty % minUseQty;
		} else {
			perShtWeight = planQty;
		}
		
		int totalShtCnt = Utility.parseInt(m_prepShtCnt);
		double totalWeight = perShtWeight * totalShtCnt;
		
		this.m_perShtWeight = perShtWeight;
		this.m_totalWeight = String.format("%.5f", totalWeight);
		
		m_perShtWeightTF.setText(String.format("%.5f", m_perShtWeight));
		m_totalWeightTF.setText(this.m_totalWeight);
	}

	// Before put into MultidataTable nedd some check 
	private boolean checkInputMaterialLot() {
		if (sendAPIMTLST()) {
			
			if(m_firstLoad) { 
				setRubberArea(); //calculating some value
				m_firstLoad = false;
			}
			
			if (Utility.parseInt(m_apimtlsto.mtrl_cnt) == 0) { 
				m_errMsg = 1;
				return false;
			} else {
				if (!chekcExpireDate()) {
					m_errMsg = 3;
					return false;
				}
				if(m_rsvprOary.mtrl_product_id.equals(m_apimtlsto.oary[0].mtrl_product_id)){
					return getMtTableResult(1.0);
				} 
				//check alternative material
				if( Utility.parseInt(m_apimtlsto.mas_cnt) > 0 ) { 
					for(int i=0; i<Utility.parseInt(m_apimtlsto.mas_cnt); i++){
						if(m_rsvprOary.mtrl_product_id.equals(m_apimtlsto.oary2[i].mtrl_prod_id)){
							double proportion = Utility.parseDouble(m_apimtlsto.oary2[i].proportion);
							return getMtTableResult(proportion);
						}
					}
					m_errMsg = 4;
					return false;
				} 
			}
		}
		return false;
	}
	
	// Set MultiDataTable value and return 
	private boolean getMtTableResult(double proportion){
		double total_qty = m_perShtWeight * Utility.parseDouble(m_prepShtCnt);
		double mtrl_qty = Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty) * proportion;
		total_qty = (mtrl_qty < total_qty) ? mtrl_qty : total_qty;
		
		APCSHTBMi_a bom = new APCSHTBMi_a();
		bom.mtrl_product_id = m_apimtlsto.oary[0].mtrl_product_id;
		bom.mtrl_lot_id = m_apimtlsto.oary[0].mtrl_lot_id; 
		bom.seq_no = m_apimtlsto.oary[0].seq_no;
		bom.used_qty = Utility.toStringAndTrim(total_qty);
		
		if (checkDuplicate(bom)) {
			m_errMsg = 2;
			return false;
		} else {
			m_mtrlListTB.appendTX(bom);
			return true;
		}
	}
	
	/********************************************/
	/* 尋找輸入的 material product id是否已輸入過*/
	/********************************************/
	private boolean checkDuplicate(APCSHTBMi_a bom) {
		boolean isMtrlLotSame = false;
		Hashtable<Integer, APCSHTBMi_a> ht = new Hashtable<Integer, APCSHTBMi_a>();

		for (int i = 0; i < m_mtrlListTB.getRowCount(); i++) {
			if (m_mtrlListTB.getValueByColName(i, "mtrl_lot_id").equals(bom.mtrl_lot_id)) {
				return (!isMtrlLotSame); 
			} else { 
				ht.put(i, (APCSHTBMi_a) m_mtrlListTB.getTxAry()[i]);
			}	
		}
		
		// Calculate same mtrlProductId weight
		double mtQty = 0;
		double standardQty = Utility.parseDouble(m_totalWeight);
		double needQty = 0;

		for (int i : ht.keySet()) {
			mtQty += Utility.parseDouble(ht.get(i).used_qty);
		}

		if (mtQty < standardQty) {
			needQty = standardQty - mtQty;
			if (Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty) < needQty) {
				bom.used_qty = String.format("%.5f", Utility.parseDouble(m_apimtlsto.oary[0].mtrl_qty));
			} else {
				bom.used_qty = String.format("%.5f", needQty);
			}
		} else {
			bom.used_qty = "0";
		}
		return isMtrlLotSame;
	}

	private boolean sendAPCCHSTE(){
		m_apcchstei = new APCCHSTEi();
		m_apcchstei.lot_id = m_rsvprOary.lot_id;
		m_apcchstei.nx_ope_no = m_rsvprOary.nx_ope_no;
		m_apcchstei.mtrl_product_id = m_rsvprOary.mtrl_product_id;
		m_apcchstei.prep_sht_flg = Xlgdef._YES;
		m_apcchsteo = (APCCHSTEo) TrxXML.sendRecv(m_apcchstei);
		boolean rtncode = Utility.checkReturnCode(m_apcchsteo);
		if (!rtncode) {
			m_apcchstei = null;
			m_apcchsteo = null;
			return false;
		} else {
			m_apcchstei = null;
			return true;
		}
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
		m_apcmtlsti.clm_mtst_typ = "A";
		m_apcmtlsti.user_id = Global.MainFrame.getLoginUser();
		m_apcmtlsti.mtrl_cnt = "1";
		m_apcmtlsti.bom_id = m_aplprdbmo.oary1[1].bom_id; 

		m_apcmtlsti.iary[0] = new APCMTLSTi_a();
		m_apcmtlsti.iary[0].mtrl_product_id = m_mtrlProdIDTF.getText(); 
		m_apcmtlsti.iary[0].mtrl_lot_id = m_markedCardIDTF.getText(); 
		m_apcmtlsti.iary[0].line_id = m_rsvprOary.line_id;
		m_apcmtlsti.iary[0].comment = "PREP:" + m_rsvprOary.prep_type + ":" + m_rsvprOary.lot_id + ":"
				+ m_rsvprOary.nx_ope_no + ":" + m_rsvprOary.mtrl_product_id + ":" + m_prepShtCnt;

		m_apcmtlsti.iary[0].mtrl_qty = m_totalWeightTF.getText();
		m_apcmtlsti.iary[0].per_sht_qty = m_perShtWeightTF.getText();
		m_apcmtlsti.iary[0].sht_cnt = Utility.toStringAndTrim(m_prepShtCnt);
		m_apcmtlsti.iary[0].mtrl_stat = Xlgdef.MTRL_LOT_STAT_AVAL;
		m_apcmtlsti.iary[0].tar_eqpt_id = m_rsvprOary.resv_eqpt_id;
		m_apcmtlsti.iary[0].flag = "C";

		if(m_validPeriod > 0){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, m_validPeriod);
			m_apcmtlsti.iary[0].expire_date = Utility.convertDateToString(cal.getTime());
		}
		
		APCSHTBMi_a[] MTData = (APCSHTBMi_a[]) m_mtrlListTB.getTxAry();
		m_apcmtlsti.iary[0].iary2_cnt = Utility.toStringAndTrim(MTData.length);
		for (int i = 0; i < MTData.length; i++) {
			m_apcmtlsti.iary[0].iary2[i] = new APCMTLSTi_a2();
			m_apcmtlsti.iary[0].iary2[i].use_mtrl_product_id = MTData[i].mtrl_product_id;
			m_apcmtlsti.iary[0].iary2[i].use_mtrl_lot_id = MTData[i].mtrl_lot_id;
			m_apcmtlsti.iary[0].iary2[i].use_qty = MTData[i].used_qty;			
			m_apcmtlsti.iary[0].iary2[i].use_seq_no = MTData[i].seq_no;
			m_apcmtlsti.iary[0].iary2[i].lot_id = m_lotIDTF.getText();
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

	public void doOK() {
		if (m_markedCardIDTF.getText().trim().length() == 0) {
			showWarningDialog(Global.getText("Please_Input_Marked_Card_ID"));
			m_markedCardIDTF.requestFocus();
			return;
		}

		if (m_mtrlListTB.getRowCount() == 0) {
			showWarningDialog(Global.getText("Please_Input_Material_Data"));
			m_rawMtrlLotIDTF.requestFocus();
			return;
		}

		if (checkTotalQty()){
			showWarningDialog(Global.getText("Material_Lot_ID_QTY_Not_Enough"));
			m_rawMtrlLotIDTF.requestFocus();
			return;
		}
		
		if (sendApcmtlst()) {
			showWarningDialog(Global.getText("Register_Success"));
		}
		super.doOK();
		return;
	}

	// Return Already Prepared Sheet Count
	private int getPrepShtCnt(){
		if(sendAPCCHSTE())
			return Utility.parseInt(m_rsvprOary.sht_cnt)- Utility.parseInt(m_apcchsteo.prep_cnt);
		else 
			return 0;
	}
	
	private boolean chekcExpireDate() {
		Calendar cal = Calendar.getInstance();
		String nowDate = Utility.convertDateToString(cal.getTime());

		if (m_apimtlsto.oary[0].expire_date.compareTo(nowDate) < 0)
			return false;
		else
			return true;
	}
	
	// Check Material Lot ID Format
	private boolean checkMtrlLotId() {
		boolean lRc = false;
		if(Utility.isEmpty(m_markedCardIDTF.getText()))
			return lRc;
		
		char[] tmpMarkCardId = (m_markedCardIDTF.getText().toString()).toCharArray();
		if (Utility.toStringAndTrim(tmpMarkCardId[0]).equals("M") && tmpMarkCardId.length == 10)
			lRc = true;
		else
			showWarningDialog(Global.getText("Wrong_Marked_Card_ID"));

		return lRc;
	}

	// Check MultiDataTable quantity enough or not, if enough then return false
	private boolean checkTotalQty(){
		double totalQty = Utility.parseDouble(m_totalWeightTF.getText());
		double mtQty = 0.0;
		APCSHTBMi_a[] MTData = (APCSHTBMi_a[]) m_mtrlListTB.getTxAry();
		 
		for(int i=0; i<MTData.length; i++)
			mtQty += Utility.parseDouble(MTData[i].used_qty);
		
		return (mtQty < totalQty) ? true : false;
	}
}
