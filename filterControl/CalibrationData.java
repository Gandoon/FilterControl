package filterControl;

import java.util.Properties;

/**
 * A container holding most data needed to be carried between components of the control interface. <br>
 * It keeps track of current and requested filter wheel position, as well as a list of the corresponding available ND filters.<br>
 * It further keeps track of the display state and timeout. <br>
 * Finally, it also contains a communicator object, for interaction with an Arduino
 * @author Erik Hedlund
 *
 */

public class CalibrationData {
	// Position data
	private int currPos;
	private int gotoPos;
	private int selectedPos;
	private final double[] NDval = {0.04, 0.5, 1.0, 1.5, 2.0, 2.5};
	
	// A container for the communicator object
	private Communications comms;
	
	// Settings for display
	private int loBr;
	private int hiBr;
	private long timeout;
	private long oldTimeout;
	private boolean dispOn;
	
	// Locally stored preferences object
	private Properties prefs;
	
	/**
	 * Create the CalibrationData container without a Communications object set
	 */
	public CalibrationData() {
		currPos = 0;
		setGotoPos(0);
		setComms(null);
		//NDval[6] = {0.04, 0.5, 1.0, 1.5, 2.0, 2.5}; //new double[6]; 
		// Set standard values for brightness
		setLoBr(31);
		setHiBr(127);
		setDispOn(true);
		setTimeout(0);
		setOldTo(7500);
		}

	/**
	 * @param Create the CalibrationData container with a Communications object set
	 */
	public CalibrationData(Communications comms) {
		currPos = 0;
		setGotoPos(0);
		this.setComms(comms);
		// Set standard values for brightness
		setLoBr(31);
		setHiBr(127);
		setDispOn(true);
		setTimeout(0);
		setOldTo(7500);
	}

	/**
	 * @return the Communications object
	 */
	public Communications getComms() {
		return comms;
	}

	/**
	 * @param comms the Communications object to set
	 */
	public void setComms(Communications comms) {
		this.comms = comms;
	}

	/**
	 * @return the currPos
	 */
	public int getCurrPos() {
		return currPos;
	}

	/**
	 * @param currPos the currPos to set
	 */
	public void setCurrPos(int currPos) {
		this.currPos = currPos;
	}
	
	/**
	 * @return the gotoPos
	 */
	public int getGotoPos() {
		return gotoPos;
	}

	/**
	 * @param gotoPos the gotoPos to set
	 */
	public void setGotoPos(int gotoPos) {
		this.gotoPos = gotoPos;
	}

	/**
	 * @return the NDVal
	 */
	public double getNDVal() {
		return NDval[currPos];
	}

	/**
	 * @return the selectedPos
	 */
	public int getSelectedPos() {
		return selectedPos;
	}

	/**
	 * @param selectedPos the selectedPos to set
	 */
	public void setSelectedPos(int selectedPos) {
		this.selectedPos = selectedPos;
	}
	
	public void commitPos() {
		currPos = selectedPos;
	}

	/**
	 * @return the loBr
	 */
	public int getLoBr() {
		return loBr;
	}

	/**
	 * @param loBr the loBr to set
	 */
	public void setLoBr(int loBr) {
		this.loBr = loBr;
	}

	/**
	 * @return the hiBr
	 */
	public int getHiBr() {
		return hiBr;
	}

	/**
	 * @param hiBr the hiBr to set
	 */
	public void setHiBr(int hiBr) {
		this.hiBr = hiBr;
	}

	/**
	 * @return the dispOn
	 */
	public boolean isDispOn() {
		return dispOn;
	}

	/**
	 * @param dispOn the dispOn to set
	 */
	public void setDispOn(boolean dispOn) {
		this.dispOn = dispOn;
	}

	/**
	 * @return the timeout
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	public boolean useTimeout() {
		return timeout > 0;
	}
	
	public void setOldTo(long oto) {
		oldTimeout = oto;
	}
	
	public long getOldTo() {
		return oldTimeout;
	}
	
	public void loadPrefs(Properties loadedPrefs, Properties defaults) {
		prefs = loadedPrefs;
		loBr    = Integer.parseInt(prefs.getProperty("loBr", defaults.getProperty("loBr", "31"))); // prefs.getProperty("loBr", "31")); // 
		hiBr    = Integer.parseInt(prefs.getProperty("hiBr", defaults.getProperty("loBr", "127")));
		currPos = Integer.parseInt(prefs.getProperty("currPos", defaults.getProperty("currPos", "0")));
		gotoPos = currPos;
		timeout = Long.parseLong(prefs.getProperty("timeout", defaults.getProperty("timeout", "0")));
		dispOn  = Boolean.parseBoolean(prefs.getProperty("dispOn", defaults.getProperty("dispOn", "true")));
	}
	
	public void commitPrefs() {
		// Update information on the Arduino
		System.out.print("Comms configured (in calData): ");
		System.out.println(comms.isConfigured());
		//System.out.print("First command to send: "+"l"+prefs.getProperty("loBr", "31")+"\n");
//		comms.sendCommand("l"+prefs.getProperty("loBr", "31")+"\n");
//		comms.sendCommand("h"+prefs.getProperty("hiBr", "127")+"\n");
//		comms.sendCommand("s"+prefs.getProperty("currpos", "0")+"\n");
//		comms.sendCommand("t"+prefs.getProperty("timeout", "0")+"\n");
//		comms.sendCommand("g"+prefs.getProperty("currPos", "0")+"\n");
		String dispCmd;
		if (dispOn) {
			dispCmd = "d1\n";
		} else {
			dispCmd = "d0\n";
		}
		System.out.print("l"+prefs.getProperty("loBr", "31")+"\n" +
				"h"+prefs.getProperty("hiBr",  "127")+"\n" +
				"s"+prefs.getProperty("currPos", "0")+"\n" +
				"t"+prefs.getProperty("timeout", "0")+"\n" + dispCmd);
		comms.sendCommand("l"+prefs.getProperty("loBr", "31")+"\n" +
				"h"+prefs.getProperty("hiBr",  "127")+"\n" +
				"s"+prefs.getProperty("currPos", "0")+"\n" +
				dispCmd + "t"+prefs.getProperty("timeout", "0")+"\n");
		
		
	}
	
	public void storePrefs(Properties prefs) {
		// Note the prefs may not be correctly stored
		this.prefs = prefs; //??
		prefs.setProperty("currPos", Integer.toString(currPos));
		prefs.setProperty("loBr", Integer.toString(loBr));
		prefs.setProperty("hiBr", Integer.toString(hiBr));
		prefs.setProperty("timeout", Long.toString(timeout));
		prefs.setProperty("dispOn", Boolean.toString(dispOn));
	}
}
