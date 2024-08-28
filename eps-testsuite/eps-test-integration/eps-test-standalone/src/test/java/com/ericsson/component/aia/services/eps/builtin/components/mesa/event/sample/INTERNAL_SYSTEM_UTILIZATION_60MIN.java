package com.ericsson.component.aia.services.eps.builtin.components.mesa.event.sample;

import java.io.Serializable;

import com.ericsson.component.aia.services.eps.builtin.components.mesa.event.AbstractEvent;

public final class INTERNAL_SYSTEM_UTILIZATION_60MIN extends AbstractEvent implements Serializable {
	
	private static final long serialVersionUID = -3592341766242696564L;
	
	private long DATETIME_ID;
	private long EVENT_TIME;
	private long SCANNER_ID;
	private byte HOUR_ID;
	private short EVENT_ID;
	private int RNC_MODULE_ID;
	private long C_ID_1;
	private short RNC_ID_1;
	private long RBS_ID;
	
	private long CAPACITY_CREDITS_UL;
	private long CAPACITY_CREDITS_DL;
	private short MAX_DL_POWER;
	private long CONSUMED_CREDITS_UL;
	private long CONSUMED_CREDITS_DL;
	private short DL_TX_POWER;
	private short DL_NON_HS_TX_POWER;
	private short DL_HS_REQ_POWER;
	private short UL_INTERFERENCE;
	private long TOTAL_ASE_UL;
	private long TOTAL_ASE_DL;
	private short TOT_DL_CHAN_CODE_TREE_CONS;
	private byte NR_OF_CONNECTIONS_SF8;
	private byte NR_OF_CONNECTIONS_SF16;
	private byte NR_OF_CONNECTIONS_SF32;
	private byte NO_OF_CM_USERS;
	private short NO_OF_USERS_TO_PHYS_HS_CHAN;
	private byte NO_OF_EDCH_USRS_SERV_CELL;
	private short NO_EDCH_USRS_NON_SERV_CELL;
	private byte NR_OF_CONNECTIONS_SF4_UL;
	private byte NR_OF_CONNECTIONS_SF8_UL;
	private byte NR_OF_CONNECTIONS_SF16_UL;
	private byte NUMBER_OF_MCCH_MICH;
	private byte NUMBER_OF_MTCH_64;
	private byte NUMBER_OF_MTCH_128;
	private byte NUMBER_OF_MTCH_256;
	private byte NO_TTI2EDCH_USERS_SERV_CELL;
	private byte NO_TTI2EDCH_USERS_NON_SERV_CELL;
	private short UL_TN_IUB_USAGE;
	private short DL_TN_IUB_USAGE;
	private short NR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL;
	private short NR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL;
	private short EUL_SCHEDULED_RATE;
	private short NO_OF_CONNECTIONS_SF64_UL;
	private short NO_OF_CONNECTIONS_SF128_DL;
	
	
	public long getDATETIME_ID() {
		return DATETIME_ID;
	}
	public void setDATETIME_ID(long dATETIME_ID) {
		DATETIME_ID = dATETIME_ID;
	}
	public long getEVENT_TIME() {
		return EVENT_TIME;
	}
	public void setEVENT_TIME(long eVENT_TIME) {
		EVENT_TIME = eVENT_TIME;
	}
	public long getSCANNER_ID() {
		return SCANNER_ID;
	}
	public void setSCANNER_ID(long sCANNER_ID) {
		SCANNER_ID = sCANNER_ID;
	}
	public byte getHOUR_ID() {
		return HOUR_ID;
	}
	public void setHOUR_ID(byte hOUR_ID) {
		HOUR_ID = hOUR_ID;
	}
	public short getEVENT_ID() {
		return EVENT_ID;
	}
	public void setEVENT_ID(short eVENT_ID) {
		EVENT_ID = eVENT_ID;
	}
	public int getRNC_MODULE_ID() {
		return RNC_MODULE_ID;
	}
	public void setRNC_MODULE_ID(int rNC_MODULE_ID) {
		RNC_MODULE_ID = rNC_MODULE_ID;
	}
	public long getC_ID_1() {
		return C_ID_1;
	}
	public void setC_ID_1(long c_ID_1) {
		C_ID_1 = c_ID_1;
	}
	public short getRNC_ID_1() {
		return RNC_ID_1;
	}
	public void setRNC_ID_1(short rNC_ID_1) {
		RNC_ID_1 = rNC_ID_1;
	}
	public long getRBS_ID() {
		return RBS_ID;
	}
	public void setRBS_ID(long rBS_ID) {
		RBS_ID = rBS_ID;
	}
	public long getCAPACITY_CREDITS_UL() {
		return CAPACITY_CREDITS_UL;
	}
	public void setCAPACITY_CREDITS_UL(long cAPACITY_CREDITS_UL) {
		CAPACITY_CREDITS_UL = cAPACITY_CREDITS_UL;
	}
	public long getCAPACITY_CREDITS_DL() {
		return CAPACITY_CREDITS_DL;
	}
	public void setCAPACITY_CREDITS_DL(long cAPACITY_CREDITS_DL) {
		CAPACITY_CREDITS_DL = cAPACITY_CREDITS_DL;
	}
	public short getMAX_DL_POWER() {
		return MAX_DL_POWER;
	}
	public void setMAX_DL_POWER(short mAX_DL_POWER) {
		MAX_DL_POWER = mAX_DL_POWER;
	}
	public long getCONSUMED_CREDITS_UL() {
		return CONSUMED_CREDITS_UL;
	}
	public void setCONSUMED_CREDITS_UL(long cONSUMED_CREDITS_UL) {
		CONSUMED_CREDITS_UL = cONSUMED_CREDITS_UL;
	}
	public long getCONSUMED_CREDITS_DL() {
		return CONSUMED_CREDITS_DL;
	}
	public void setCONSUMED_CREDITS_DL(long cONSUMED_CREDITS_DL) {
		CONSUMED_CREDITS_DL = cONSUMED_CREDITS_DL;
	}
	public short getDL_TX_POWER() {
		return DL_TX_POWER;
	}
	public void setDL_TX_POWER(short dL_TX_POWER) {
		DL_TX_POWER = dL_TX_POWER;
	}
	public short getDL_NON_HS_TX_POWER() {
		return DL_NON_HS_TX_POWER;
	}
	public void setDL_NON_HS_TX_POWER(short dL_NON_HS_TX_POWER) {
		DL_NON_HS_TX_POWER = dL_NON_HS_TX_POWER;
	}
	public short getDL_HS_REQ_POWER() {
		return DL_HS_REQ_POWER;
	}
	public void setDL_HS_REQ_POWER(short dL_HS_REQ_POWER) {
		DL_HS_REQ_POWER = dL_HS_REQ_POWER;
	}
	public short getUL_INTERFERENCE() {
		return UL_INTERFERENCE;
	}
	public void setUL_INTERFERENCE(short uL_INTERFERENCE) {
		UL_INTERFERENCE = uL_INTERFERENCE;
	}
	public long getTOTAL_ASE_UL() {
		return TOTAL_ASE_UL;
	}
	public void setTOTAL_ASE_UL(long tOTAL_ASE_UL) {
		TOTAL_ASE_UL = tOTAL_ASE_UL;
	}
	public long getTOTAL_ASE_DL() {
		return TOTAL_ASE_DL;
	}
	public void setTOTAL_ASE_DL(long tOTAL_ASE_DL) {
		TOTAL_ASE_DL = tOTAL_ASE_DL;
	}
	public short getTOT_DL_CHAN_CODE_TREE_CONS() {
		return TOT_DL_CHAN_CODE_TREE_CONS;
	}
	public void setTOT_DL_CHAN_CODE_TREE_CONS(short tOT_DL_CHAN_CODE_TREE_CONS) {
		TOT_DL_CHAN_CODE_TREE_CONS = tOT_DL_CHAN_CODE_TREE_CONS;
	}
	public byte getNR_OF_CONNECTIONS_SF8() {
		return NR_OF_CONNECTIONS_SF8;
	}
	public void setNR_OF_CONNECTIONS_SF8(byte nR_OF_CONNECTIONS_SF8) {
		NR_OF_CONNECTIONS_SF8 = nR_OF_CONNECTIONS_SF8;
	}
	public byte getNR_OF_CONNECTIONS_SF16() {
		return NR_OF_CONNECTIONS_SF16;
	}
	public void setNR_OF_CONNECTIONS_SF16(byte nR_OF_CONNECTIONS_SF16) {
		NR_OF_CONNECTIONS_SF16 = nR_OF_CONNECTIONS_SF16;
	}
	public byte getNR_OF_CONNECTIONS_SF32() {
		return NR_OF_CONNECTIONS_SF32;
	}
	public void setNR_OF_CONNECTIONS_SF32(byte nR_OF_CONNECTIONS_SF32) {
		NR_OF_CONNECTIONS_SF32 = nR_OF_CONNECTIONS_SF32;
	}
	public byte getNO_OF_CM_USERS() {
		return NO_OF_CM_USERS;
	}
	public void setNO_OF_CM_USERS(byte nO_OF_CM_USERS) {
		NO_OF_CM_USERS = nO_OF_CM_USERS;
	}
	public short getNO_OF_USERS_TO_PHYS_HS_CHAN() {
		return NO_OF_USERS_TO_PHYS_HS_CHAN;
	}
	public void setNO_OF_USERS_TO_PHYS_HS_CHAN(short nO_OF_USERS_TO_PHYS_HS_CHAN) {
		NO_OF_USERS_TO_PHYS_HS_CHAN = nO_OF_USERS_TO_PHYS_HS_CHAN;
	}
	public byte getNO_OF_EDCH_USRS_SERV_CELL() {
		return NO_OF_EDCH_USRS_SERV_CELL;
	}
	public void setNO_OF_EDCH_USRS_SERV_CELL(byte nO_OF_EDCH_USRS_SERV_CELL) {
		NO_OF_EDCH_USRS_SERV_CELL = nO_OF_EDCH_USRS_SERV_CELL;
	}
	public short getNO_EDCH_USRS_NON_SERV_CELL() {
		return NO_EDCH_USRS_NON_SERV_CELL;
	}
	public void setNO_EDCH_USRS_NON_SERV_CELL(short nO_EDCH_USRS_NON_SERV_CELL) {
		NO_EDCH_USRS_NON_SERV_CELL = nO_EDCH_USRS_NON_SERV_CELL;
	}
	public byte getNR_OF_CONNECTIONS_SF4_UL() {
		return NR_OF_CONNECTIONS_SF4_UL;
	}
	public void setNR_OF_CONNECTIONS_SF4_UL(byte nR_OF_CONNECTIONS_SF4_UL) {
		NR_OF_CONNECTIONS_SF4_UL = nR_OF_CONNECTIONS_SF4_UL;
	}
	public byte getNR_OF_CONNECTIONS_SF8_UL() {
		return NR_OF_CONNECTIONS_SF8_UL;
	}
	public void setNR_OF_CONNECTIONS_SF8_UL(byte nR_OF_CONNECTIONS_SF8_UL) {
		NR_OF_CONNECTIONS_SF8_UL = nR_OF_CONNECTIONS_SF8_UL;
	}
	public byte getNR_OF_CONNECTIONS_SF16_UL() {
		return NR_OF_CONNECTIONS_SF16_UL;
	}
	public void setNR_OF_CONNECTIONS_SF16_UL(byte nR_OF_CONNECTIONS_SF16_UL) {
		NR_OF_CONNECTIONS_SF16_UL = nR_OF_CONNECTIONS_SF16_UL;
	}
	public byte getNUMBER_OF_MCCH_MICH() {
		return NUMBER_OF_MCCH_MICH;
	}
	public void setNUMBER_OF_MCCH_MICH(byte nUMBER_OF_MCCH_MICH) {
		NUMBER_OF_MCCH_MICH = nUMBER_OF_MCCH_MICH;
	}
	public byte getNUMBER_OF_MTCH_64() {
		return NUMBER_OF_MTCH_64;
	}
	public void setNUMBER_OF_MTCH_64(byte nUMBER_OF_MTCH_64) {
		NUMBER_OF_MTCH_64 = nUMBER_OF_MTCH_64;
	}
	public byte getNUMBER_OF_MTCH_128() {
		return NUMBER_OF_MTCH_128;
	}
	public void setNUMBER_OF_MTCH_128(byte nUMBER_OF_MTCH_128) {
		NUMBER_OF_MTCH_128 = nUMBER_OF_MTCH_128;
	}
	public byte getNUMBER_OF_MTCH_256() {
		return NUMBER_OF_MTCH_256;
	}
	public void setNUMBER_OF_MTCH_256(byte nUMBER_OF_MTCH_256) {
		NUMBER_OF_MTCH_256 = nUMBER_OF_MTCH_256;
	}
	public byte getNO_TTI2EDCH_USERS_SERV_CELL() {
		return NO_TTI2EDCH_USERS_SERV_CELL;
	}
	public void setNO_TTI2EDCH_USERS_SERV_CELL(byte nO_TTI2EDCH_USERS_SERV_CELL) {
		NO_TTI2EDCH_USERS_SERV_CELL = nO_TTI2EDCH_USERS_SERV_CELL;
	}
	public byte getNO_TTI2EDCH_USERS_NON_SERV_CELL() {
		return NO_TTI2EDCH_USERS_NON_SERV_CELL;
	}
	public void setNO_TTI2EDCH_USERS_NON_SERV_CELL(
			byte nO_TTI2EDCH_USERS_NON_SERV_CELL) {
		NO_TTI2EDCH_USERS_NON_SERV_CELL = nO_TTI2EDCH_USERS_NON_SERV_CELL;
	}
	public short getUL_TN_IUB_USAGE() {
		return UL_TN_IUB_USAGE;
	}
	public void setUL_TN_IUB_USAGE(short uL_TN_IUB_USAGE) {
		UL_TN_IUB_USAGE = uL_TN_IUB_USAGE;
	}
	public short getDL_TN_IUB_USAGE() {
		return DL_TN_IUB_USAGE;
	}
	public void setDL_TN_IUB_USAGE(short dL_TN_IUB_USAGE) {
		DL_TN_IUB_USAGE = dL_TN_IUB_USAGE;
	}
	public short getNR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL() {
		return NR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL;
	}
	public void setNR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL(
			short nR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL) {
		NR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL = nR_OF_OPENED_SF256_FOR_FDPCH_SYSTEM_UTIL;
	}
	public short getNR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL() {
		return NR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL;
	}
	public void setNR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL(
			short nR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL) {
		NR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL = nR_OF_ALLOC_TIMEPOSITIONS_FOR_FDPCH_SYSTEM_UTIL;
	}
	public short getEUL_SCHEDULED_RATE() {
		return EUL_SCHEDULED_RATE;
	}
	public void setEUL_SCHEDULED_RATE(short eUL_SCHEDULED_RATE) {
		EUL_SCHEDULED_RATE = eUL_SCHEDULED_RATE;
	}
	public short getNO_OF_CONNECTIONS_SF64_UL() {
		return NO_OF_CONNECTIONS_SF64_UL;
	}
	public void setNO_OF_CONNECTIONS_SF64_UL(short nO_OF_CONNECTIONS_SF64_UL) {
		NO_OF_CONNECTIONS_SF64_UL = nO_OF_CONNECTIONS_SF64_UL;
	}
	public short getNO_OF_CONNECTIONS_SF128_DL() {
		return NO_OF_CONNECTIONS_SF128_DL;
	}
	public void setNO_OF_CONNECTIONS_SF128_DL(short nO_OF_CONNECTIONS_SF128_DL) {
		NO_OF_CONNECTIONS_SF128_DL = nO_OF_CONNECTIONS_SF128_DL;
	}	
}
