package filterControl;

import java.io.PrintWriter;
import java.util.Scanner;

import com.fazecast.jSerialComm.SerialPort;

/**
 * @author Erik Hedlund
 *
 */
public class Communications {
	private SerialPort[] ports;
	private SerialPort selectedPort;
	private SerialPort openPort;
	private boolean configured;
	private Thread t;
	private static PrintWriter output;
	private Scanner scanner;

/**
 * Configure Communications automatically
 */
	public Communications() {
		super();
		ports = SerialPort.getCommPorts();
		configured = false;
		//this.setPorts(ports);
	}
	
	/**
	 * Configure Communications with predefined serial ports
	 * @param ports
	 */
	public Communications(SerialPort[] ports) {
		super();
		this.setPorts(ports);
		configured = false;
	}
	
	public SerialPort getSelectedPort() {
		return selectedPort;
	}

	public void setSelectedPort(SerialPort selectedPort) {
		this.selectedPort = selectedPort;
	}
	
	/**
	 * @return the ports
	 */
	public SerialPort[] getPorts() {
		return ports;
	}

	/**
	 * @param ports the ports to set
	 */
	public void setPorts(SerialPort[] ports) {
		this.ports = ports;
	}

	public SerialPort getOpenPort() {
		if (configured) {
			return(openPort);
		} else {
			return(null);
		}
	}

	public void setOpenPort(SerialPort openPort) {
		this.openPort = openPort;
		configured = true;
	}
	
	public boolean isConfigured() {
		return(configured);
	}
	
	public void initialise() {
		// Call to start a communication thread
		t = new Thread() {
			@Override public void run() {
				// Wait after connection
				try {
					Thread.sleep(200);
				} catch (Exception e) {
					//Silently ignore
				}
				
				output = new PrintWriter(getOpenPort().getOutputStream());
				
				scanner = new Scanner(getOpenPort().getInputStream());
				System.out.println("Scanner started...");
				while(configured) {
					while(scanner.hasNextLine()) { // 
						//System.out.println("Entered the scanning section...");
						String line = scanner.nextLine();
						System.out.println(line);
						//try{Thread.sleep(10);}catch(InterruptedException ex) {};
					}
				}
				//scanner.close();
			}
		};
		t.start();
	}
	
	public boolean closeOpenPort() {
		System.out.println("Attempting to close open port: "+openPort.getSystemPortName());	
		configured = false;
		try {
			t.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		output.close();
		scanner.close();
		
		//Start a new thread to flush residual garbage from the serial interface
		//initialise();
		//immediately close things down again...
		//output.close();
		//scanner.close();
//		try {
//			t.join();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		//...and close the port afterwards.
		if(openPort.closePort()) {
			System.out.println("Successfully closed port");
			openPort = null;
			//configured = false;
			return true;
		} else {
			configured = true;
			return false;
		}
	}
	
	public void setUnconfigured() {
		openPort = null;
		configured = false;
	}
	
	public void sendCommand(String cmd) {
		output.print(cmd);
		output.flush();
	}

}
