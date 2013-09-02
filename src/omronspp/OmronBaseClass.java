package omronspp;

import android.util.Log;

public abstract class OmronBaseClass {
	/* generic class for omron device */

	public static final int UNKNOWN_DEVICE = 0;
	public static final int BLOOD_PRESSURE_MON = 1;
	public static final int WEIGHING_SCALE = 2;
	
	//Debugging
	private static final String TAG = "OmronBase";
	private static boolean D = true;

	protected int deviceType;
	private String deviceSerial;
	private String deviceModel;
	private int d1;
	private int d2;
	
	private boolean AT = false;

	/*
	 * public abstract class UserProfile{ public abstract String toString(); //
	 * pretty print public abstract Object pickle(); // maybe xml coded ver? for
	 * uploading
	 * 
	 * }
	 */

	
	public int getDeviceType() {
		return deviceType;
	}
	/*
	public void setDeviceType(int deviceType) {
		this.deviceType = deviceType;
	}
	 */
	public String getDeviceSerial() {
		return deviceSerial;
	}

	public void setDeviceSerial(String deviceSerial) {
		this.deviceSerial = deviceSerial;
	}

	public String getDeviceModel() {
		return deviceModel;
	}

	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}

	public int getD1() {
		return d1;
	}

	public void setD1(int d1) {
		this.d1 = d1;
	}

	public int getD2() {
		return d2;
	}

	public void setD2(int d2) {
		this.d2 = d2;
	}

	public String cmdVersion() {
		return "VER00";
	}

	public String cmdSerial() {
		return "GSI00";

	}

	public String cmdProfile() {
		return "GPD00";
	}

	public String cmdDataNum() {
		return "GDN00";
	}

	public String cmdError() {
		byte[] cmd = new byte[5];
		cmd[0] = 'E';
		cmd[1] = 'R';
		cmd[2] = 'R';
		cmd[3] = (byte) 0xff;
		cmd[4] = (byte) 0xff;
		return new String(cmd);
	}

	public String cmdClearMem() {
		return "CMD00";
	}

	public String cmdResumeMemData() {
		return "RMD00";
	}

	public String cmdGetClock() {
		return "GCL00";
	}
	
	public void handleGCL(byte[] frame, int len) {
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] response = new byte[len];
		for (int i=0;i<len; i++) {
			response[i] = frame[i];
		}
		if (len > -1) {
			if (len == 2) Log.e(TAG, "clock cannot be read "+(new String(response))+" - "+len);
			else {
				Log.i(TAG, "Year "+frame[3]);
			}
		}
	}

	public String cmdSetClock(int YY, int MM, int DD, int hh, int mm, int ss) {
		byte[] cmd = new byte[11];
		// TODO: SCL 00 YY MM DD hh mm ss BCC
		return "";

	}

	public String cmdMeasurementData(int dataIndex) {
		byte[] cmd = new byte[7];
		cmd[0] = 'G';
		cmd[1] = 'M';
		cmd[2] = 'D';
		cmd[3] = (byte) 0x00;
		cmd[4] = (byte) (dataIndex / 256); // int math
		cmd[5] = (byte) (dataIndex % 256);
		cmd[6] = (byte) (cmd[4] ^ cmd[5]);
		return new String(cmd);
	}

	public String cmdEndOk() {
		byte[] cmd = new byte[5];
		cmd[0] = 'T';
		cmd[1] = 'O';
		cmd[2] = 'K';
		cmd[3] = (byte) 0xFF;
		cmd[4] = (byte) 0xFF;

		return new String(cmd);
	}

	public void checkChecksum(byte[] frame, int len) throws ChecksumError {

	}

	public void processModelName(byte[] frame, int len) {
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String modelName = new String(frame).substring(3, 10);
		Log.d(TAG, "Model: " + modelName);

		if (modelName == "M7081-IT") {
			this.deviceType = BLOOD_PRESSURE_MON;
			Log.d(TAG, "bp");
		} else if (modelName == "B206IT  ") {
			this.deviceType = WEIGHING_SCALE;
			Log.d(TAG, "weighing scale");
		} else {
			Log.e(TAG, "no such model");
		}

	}
	
	public void handleDataNum(byte[] frame, int len){
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.d1 = frame[3] * 256 + frame[4];
		this.d2 = frame[5] * 256 + frame[6];
		
		Log.d(TAG, "dataNum:: d1:" + d1 + " d2:" + d2);
	}
	
	public void handleSerialNum(byte[] frame, int len){
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int serialLen = frame[3];
		this.setDeviceSerial(new String(frame).substring(4,len-1));
		Log.d(TAG, "Serial: " + this.getDeviceSerial());
		
		
	}
	
	public void handleCmdGAT(byte[] frame, int len) {
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] response = new byte[len];
		for (int i=0;i<len; i++) {
			response[i] = frame[i];
		}
		String resString = new String(response);
		if (len > -1) {
			if (len == 2) Log.e(TAG, "automatic transmission status could not be read "+ resString +" - "+len);
			else if (len == 5) {
				
				if (frame[4]==1||frame[4]==3) {
					this.AT = true;
				} else {
					this.AT = false;
				}
				Log.i(TAG, "automatic transmission "+this.AT+" - "+frame[4]+" - "+len);
			}
		}
	}
	
	public void handleCmdSAT(byte[] frame, int len) {
		try {
			checkChecksum(frame, len);
		} catch (ChecksumError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] response = new byte[len];
		for (int i=0;i<len; i++) {
			response[i] = frame[i];
		}
		String resString = new String(response);
		if (len > -1) {
			if (resString.equals("OK")) {
				Log.i(TAG, "status set "+ resString);
				
			} else if (resString.equals("NO")) {
				Log.e(TAG, "automatic transmission status could not be set "+resString);
			}
			
		}
	
	}
	
	public static String handleCmdTOK(byte[] frame, int len) {
		
		byte[] response = new byte[len];
		for (int i=0;i<len; i++) {
			response[i] = frame[i];
		}
		String resString = new String(response);
		
		if (len > -1) {
			if (resString.indexOf("OK")>=0) {
				Log.i(TAG, "ok switching back to normal mode "+ resString);
				
			} else if (resString.indexOf("NO")>=0) {
				Log.e(TAG, "BPM is still busy communicating "+resString);
			}
			
		}
		return resString;
	}
	
	public static boolean checkReady(byte[] frame, int len) {
		boolean flag = false;
		byte[] response = new byte[len];
		for (int i=0;i<len; i++) {
			response[i] = frame[i];
		}
		String resString = new String(response);
		if (resString.indexOf("READY")>=0) flag = true;
		
		Log.i(TAG, "ready "+ flag);
		return flag;
		
	}
	
	public abstract void handleProfileData(byte[] frame, int len);
	
	public abstract String profileString();
	
	public abstract OmronMeasurementData handleMeasurementData(byte[] frame, int len, int dataNum);
	
	//public abstract String measurementDataToString(int index);
	public String cmdGAT() {
		/*byte[] cmd = new byte[5];
		cmd[0] = 'G';
		cmd[1] = 'A';
		cmd[2] = 'T';
		cmd[3] = (byte) 0x00;
		cmd[4] = (byte) 0x00;
		return new String(cmd);*/
		return "GAT00";
	}
	
	public String cmdSAT() {
		byte[] cmd = new byte[6];
		cmd[0] = 'S';
		cmd[1] = 'A';
		cmd[2] = 'T';
		cmd[3] = (byte) 0x00;
		cmd[4] = (byte) 0x11;
		cmd[5] = (byte) 0x11;
		return new String(cmd);
		//return "SAT030";
	}
	
	public static String cmdTOK() {
		if (D) Log.d(TAG, "testing");
		byte[] cmd = new byte[5];
		cmd[0] = 'T';
		cmd[1] = 'O';
		cmd[2] = 'K';
		cmd[3] = (byte) 0xFF;
		cmd[4] = (byte) 0xFF;
		return new String(cmd);
	}
	
}
