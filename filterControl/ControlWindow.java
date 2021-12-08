package filterControl;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;

//import com.sun.org.apache.xml.internal.serializer.utils.SystemIDResolver;

import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.fazecast.jSerialComm.SerialPort;

import filterControl.OsCheck.OSType;

public class ControlWindow {

	private JFrame frmFilterControl;
	private ImageIcon wIcon;
	private CalibrationData calData;
	private Communications comms;
	private JTextPane displayTimedOut;
	private boolean firstRun;
	
	private JToggleButton dispOn;
    private JButton button_1, button_2, button_3, button_4, button_5, button_6;
    
    private JCheckBoxMenuItem chckbxmntmUseDisplayTimeout;
    
    private Properties prefs, defaults;
    private File prefsPath;
    
    private final String version = "1.3.3";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ControlWindow window = new ControlWindow();
					window.frmFilterControl.setTitle("Attenuation filter control");
					window.frmFilterControl.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ControlWindow() {
		firstRun = true;
		initialize();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	@SuppressWarnings("deprecation")
	private void initialize() {
		comms = new Communications();
		calData = new CalibrationData(comms);
		prefs = new Properties();
		defaults = new Properties();
		OSType localOS = OsCheck.getOperatingSystemType(); 
		System.out.println(localOS);

		// Read the defaults and previous settings
		try (InputStream pin = ResourceLoader.load("/FilterDefaults.xml")) {
			defaults.loadFromXML(pin); //FromXML
			pin.close();
			prefsPath = new File(defaults.getProperty("prefsPath"));
			
		} catch (IOException e1) {
			System.out.println("No defaults found...");
			prefsPath = new File("FilterPrefs.xml");
			//e1.printStackTrace();
		} catch (NullPointerException e2) {
			System.out.println("Totally failed to open defaults file, this normally should not happen...");
			e2.printStackTrace();
			//return;
		}
		try (FileInputStream pin = new FileInputStream(prefsPath)) {
			prefs.loadFromXML(pin); //FromXML
			pin.close();
			// Read preferences from FilterPrefs and store in calData, if not found use defaults.
			// If defaults are not found, use hardcoded standard values
			File prePrefsPath = new File(prefs.getProperty("prefsPath"));
			// File(prefs.getProperty("prefsPath")+ File.separator +"FilterPrefs.xml");
			System.out.print("prefsPath: ");
			System.out.println(prefsPath);
			System.out.print("prePrefsPath: ");
			System.out.println(prePrefsPath);
//			System.out.print("prefsPath == prePrefsPath: ");
//			System.out.println(prePrefsPath.equals(prefsPath));
			if (!prePrefsPath.equals(prefsPath)) {
				//prefsPath = prePrefsPath;
				try (FileInputStream pin2 = new FileInputStream(prePrefsPath)) {
					prefs.loadFromXML(pin2); //FromXML
					pin.close();
					prefsPath = new File(prefs.getProperty("prefsPath")); // ,"FilterPrefs.xml"
					System.out.print("Read prefsPath: ");
					System.out.println(prefsPath);
				}
			}
			calData.loadPrefs(prefs, defaults);
		} catch (IOException e1) {
			System.out.println("No preferences found...\nLoading defaults.");
			prefs = defaults;
		} catch (NullPointerException e2) {
			System.out.println("Totally failed to open prefs file, this should not normally happen...");
			e2.printStackTrace();
			return;
		}
		

		
		frmFilterControl = new JFrame();
		frmFilterControl.getContentPane().setFocusCycleRoot(true);
		frmFilterControl.setFocusTraversalPolicyProvider(true);
		frmFilterControl.getContentPane().setFocusTraversalPolicyProvider(true);
		frmFilterControl.setTitle("Filter Control");
		frmFilterControl.setBounds(100, 100, 480, 320);
		frmFilterControl.setMinimumSize(new Dimension(450, 300));
		frmFilterControl.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Properties systemID = System.getProperties();
		
		//System.out.println(OsCheck.detectedOS);
		String portStringDefault, portString;
		if (localOS == OSType.MacOS) {
			portStringDefault = defaults.getProperty("MacSerialPort", "cu.usbmodem141521");
			portString = prefs.getProperty("SerialPort", portStringDefault);
			SerialPort port;
			if (portString.equals("")) {
				port = SerialPort.getCommPort(portStringDefault);
				System.out.println("Selecting default port: "+portStringDefault);
			} else {
				port = SerialPort.getCommPort(portString);
				System.out.println("Selecting port from previous session: "+portString);
			}
			//port = SerialPort.getCommPort(portList.getSelectedItem().toString());
			System.out.println("Selected port: "+port.getSystemPortName());
			port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
			if( !port.isOpen() ) { // false ) {//
				if(port.openPort()) {
					System.out.println("Successfully opened port " + portString);
					comms.setOpenPort(port);
					comms.initialise();
					System.out.println("Automatically selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+")");

				} else {
//					JOptionPane.showMessageDialog(frmFilterControl,"Could not open serial port " + portString,
//							"Error",JOptionPane.ERROR_MESSAGE);
					SerialPortSelect dialog = new SerialPortSelect(comms);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					dialog.setAlwaysOnTop(true);
				}
			} else {
				System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");						
			}
		} else if (localOS == OSType.Windows) {
			portStringDefault = defaults.getProperty("WinSerialPort", "COM5");
			portString = prefs.getProperty("SerialPort", portStringDefault);
			SerialPort port;
			if (portString.equals("")) {
				port = SerialPort.getCommPort(portStringDefault);
			} else {
				port = SerialPort.getCommPort(portString);
			}
			//port = SerialPort.getCommPort(portList.getSelectedItem().toString());
			System.out.println("Selected port: "+port.getSystemPortName());
			port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
			if( !port.isOpen() ) { // false ) {//
				if(port.openPort()) {
					System.out.println("Successfully opened port " + portString);
					comms.setOpenPort(port);
					comms.initialise();
					//calData.commitPrefs();
					System.out.println("Automatically selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+")");
				} else {
//					JOptionPane.showMessageDialog(frmFilterControl,"Could not open serial port " + portString,
//							"Error",JOptionPane.ERROR_MESSAGE);
					SerialPortSelect dialog = new SerialPortSelect(comms);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					dialog.setAlwaysOnTop(true);
				}				
			} else {
				System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");						
			}
		} else {
			SerialPortSelect dialog = new SerialPortSelect(comms);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			dialog.setAlwaysOnTop(true);
		}
		String prefsPathStr = prefs.getProperty("prefsPath");
		if (prefsPathStr != null) {
			prefsPath = new File(prefsPathStr);
			System.out.println("Saved path set: "+prefsPath.getAbsolutePath());
		} else {
			prefsPath = new File("").getAbsoluteFile();
			System.out.println("Default path set: "+prefsPath.getAbsolutePath());
		}

		
		String iconURL = "/Wheel_small.jpg";
		BufferedImage bIm;
		try {
			bIm = ImageIO.read(ResourceLoader.load(iconURL));
			wIcon = new ImageIcon(bIm);
		} catch (Exception e) {
			System.out.println("Could not load icon image.");
		}
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] {0, 0, 130, 0, 0, 120, 0, 0, 0, 120, 40, 0, 0};
		gridBagLayout.rowHeights = new int[] {0, 0, 30, 30, 30, 30, 30, 0, 45, 15};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		frmFilterControl.getContentPane().setLayout(gridBagLayout);
		
		Component verticalGlue_4 = Box.createVerticalGlue();
		verticalGlue_4.setFocusable(false);
		GridBagConstraints gbc_verticalGlue_4 = new GridBagConstraints();
		gbc_verticalGlue_4.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_4.gridx = 5;
		gbc_verticalGlue_4.gridy = 0;
		frmFilterControl.getContentPane().add(verticalGlue_4, gbc_verticalGlue_4);
		
		Component verticalGlue_3 = Box.createVerticalGlue();
		verticalGlue_3.setFocusable(false);
		GridBagConstraints gbc_verticalGlue_3 = new GridBagConstraints();
		gbc_verticalGlue_3.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_3.gridx = 5;
		gbc_verticalGlue_3.gridy = 1;
		frmFilterControl.getContentPane().add(verticalGlue_3, gbc_verticalGlue_3);
		
		JTextPane txtpnSelectNdFilter = new JTextPane();
		txtpnSelectNdFilter.setFocusable(false);
		txtpnSelectNdFilter.setBackground(UIManager.getColor("Panel.background"));
		txtpnSelectNdFilter.setEditable(false);
		txtpnSelectNdFilter.setText("Select ND filter");
		GridBagConstraints gbc_txtpnSelectNdFilter = new GridBagConstraints();
		gbc_txtpnSelectNdFilter.gridwidth = 6;
		gbc_txtpnSelectNdFilter.insets = new Insets(0, 0, 5, 5);
		gbc_txtpnSelectNdFilter.gridx = 3;
		gbc_txtpnSelectNdFilter.gridy = 2;
		frmFilterControl.getContentPane().add(txtpnSelectNdFilter, gbc_txtpnSelectNdFilter);
		
		button_6 = new JButton("2.5");
		
		button_6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(5);
				calData.setGotoPos(5);
				if (calData.getCurrPos() != 5) {
					calData.setSelectedPos(5);
					switch (calData.getCurrPos()) {
						case 0: button_1.setEnabled(true);
						break;
						case 1: button_2.setEnabled(true);
						break;
						case 2: button_3.setEnabled(true);
						break;
						case 3: button_4.setEnabled(true);
						break;
						case 4: button_5.setEnabled(true);
						break;
					}

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g5\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					
				    System.out.println("Current position: "+calData.getCurrPos());
				}
				button_6.setEnabled(false);
				button_1.requestFocus();
			}
		});
		//java.net.URL imageURL = getResource(iconURL);
		
		button_1 = new JButton("0.04");
		
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				calData.setGotoPos(0);
				if (calData.getCurrPos() != 0) {
					calData.setSelectedPos(0);
					switch (calData.getCurrPos()) {
					case 1: button_2.setEnabled(true);
					break;
					case 2: button_3.setEnabled(true);
					break;
					case 3: button_4.setEnabled(true);
					break;
					case 4: button_5.setEnabled(true);
					break;
					case 5: button_6.setEnabled(true);
					break;
					}

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g0\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					
				    System.out.println("Current position: "+calData.getCurrPos());
				} else if (firstRun && comms.isConfigured()) {
					comms.sendCommand("g0\n");
					firstRun = false;
				}
				button_1.setEnabled(false);
				button_2.requestFocus();
			}
		});
		
		Component glue = Box.createGlue();
		glue.setFocusable(false);
		GridBagConstraints gbc_glue = new GridBagConstraints();
		gbc_glue.insets = new Insets(0, 0, 5, 5);
		gbc_glue.gridx = 2;
		gbc_glue.gridy = 3;
		frmFilterControl.getContentPane().add(glue, gbc_glue);
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_1.anchor = GridBagConstraints.SOUTH;
		gbc_button_1.insets = new Insets(0, 0, 5, 5);
		gbc_button_1.gridx = 5;
		gbc_button_1.gridy = 3;
		frmFilterControl.getContentPane().add(button_1, gbc_button_1);
		
		Component glue_1 = Box.createGlue();
		glue_1.setFocusable(false);
		GridBagConstraints gbc_glue_1 = new GridBagConstraints();
		gbc_glue_1.insets = new Insets(0, 0, 5, 5);
		gbc_glue_1.gridx = 9;
		gbc_glue_1.gridy = 3;
		frmFilterControl.getContentPane().add(glue_1, gbc_glue_1);
		
		Component horizontalGlue_9 = Box.createHorizontalGlue();
		horizontalGlue_9.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_9 = new GridBagConstraints();
		gbc_horizontalGlue_9.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_9.gridx = 1;
		gbc_horizontalGlue_9.gridy = 4;
		frmFilterControl.getContentPane().add(horizontalGlue_9, gbc_horizontalGlue_9);
		GridBagConstraints gbc_button_6 = new GridBagConstraints();
		gbc_button_6.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_6.anchor = GridBagConstraints.NORTH;
		gbc_button_6.insets = new Insets(0, 0, 5, 5);
		gbc_button_6.gridx = 2;
		gbc_button_6.gridy = 4;
		frmFilterControl.getContentPane().add(button_6, gbc_button_6);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalGlue.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue = new GridBagConstraints();
		gbc_horizontalGlue.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue.gridx = 4;
		gbc_horizontalGlue.gridy = 4;
		frmFilterControl.getContentPane().add(horizontalGlue, gbc_horizontalGlue);
		
		JLabel lblWheelim = new JLabel(wIcon,JLabel.CENTER);
		lblWheelim.setFocusable(false);
		GridBagConstraints gbc_lblWheelim = new GridBagConstraints();
		gbc_lblWheelim.anchor = GridBagConstraints.NORTH;
		gbc_lblWheelim.insets = new Insets(0, 0, 5, 5);
		gbc_lblWheelim.gridheight = 3;
		gbc_lblWheelim.gridx = 5;
		gbc_lblWheelim.gridy = 4;
		frmFilterControl.getContentPane().add(lblWheelim, gbc_lblWheelim);
		
		button_5 = new JButton("2.0");
		button_5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(4);
				calData.setGotoPos(4);
				if (calData.getCurrPos() != 4) {
					calData.setSelectedPos(4);
					switch (calData.getCurrPos()) {
					case 0: button_1.setEnabled(true);
					break;
					case 1: button_2.setEnabled(true);
					break;
					case 2: button_3.setEnabled(true);
					break;
					case 3: button_4.setEnabled(true);
					break;
					case 5: button_6.setEnabled(true);
					break;
					}

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g4\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					
				    System.out.println("Current position: "+calData.getCurrPos());
				}
				button_5.setEnabled(false);
				button_6.requestFocus();
			}
		});
		
		button_2 = new JButton("0.5");
		button_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(1);
				calData.setGotoPos(1);
				switch (calData.getCurrPos()) {
				case 0: button_1.setEnabled(true);
				break;
				case 2: button_3.setEnabled(true);
				break;
				case 3: button_4.setEnabled(true);
				break;
				case 4: button_5.setEnabled(true);
				break;
				case 5: button_6.setEnabled(true);
				break;
				}
				if (calData.getCurrPos() != 1) {
					calData.setSelectedPos(1);

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g1\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					
				    System.out.println("Current position: "+calData.getCurrPos());
				}
				button_2.setEnabled(false);
				button_3.requestFocus();
			}
		});
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalGlue_2.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_2 = new GridBagConstraints();
		gbc_horizontalGlue_2.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_2.gridx = 8;
		gbc_horizontalGlue_2.gridy = 4;
		frmFilterControl.getContentPane().add(horizontalGlue_2, gbc_horizontalGlue_2);
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_2.anchor = GridBagConstraints.NORTH;
		gbc_button_2.insets = new Insets(0, 0, 5, 5);
		gbc_button_2.gridx = 9;
		gbc_button_2.gridy = 4;
		frmFilterControl.getContentPane().add(button_2, gbc_button_2);
		
		Component horizontalGlue_5 = Box.createHorizontalGlue();
		horizontalGlue_5.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_5 = new GridBagConstraints();
		gbc_horizontalGlue_5.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_5.gridx = 10;
		gbc_horizontalGlue_5.gridy = 4;
		frmFilterControl.getContentPane().add(horizontalGlue_5, gbc_horizontalGlue_5);
		
		Component verticalGlue = Box.createVerticalGlue();
		verticalGlue.setFocusable(false);
		GridBagConstraints gbc_verticalGlue = new GridBagConstraints();
		gbc_verticalGlue.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue.gridx = 2;
		gbc_verticalGlue.gridy = 5;
		frmFilterControl.getContentPane().add(verticalGlue, gbc_verticalGlue);
		
		Component verticalGlue_1 = Box.createVerticalGlue();
		verticalGlue_1.setFocusable(false);
		GridBagConstraints gbc_verticalGlue_1 = new GridBagConstraints();
		gbc_verticalGlue_1.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_1.gridx = 9;
		gbc_verticalGlue_1.gridy = 5;
		frmFilterControl.getContentPane().add(verticalGlue_1, gbc_verticalGlue_1);
		
		Component horizontalGlue_10 = Box.createHorizontalGlue();
		horizontalGlue_10.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_10 = new GridBagConstraints();
		gbc_horizontalGlue_10.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_10.gridx = 0;
		gbc_horizontalGlue_10.gridy = 6;
		frmFilterControl.getContentPane().add(horizontalGlue_10, gbc_horizontalGlue_10);
		GridBagConstraints gbc_button_5 = new GridBagConstraints();
		gbc_button_5.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_5.anchor = GridBagConstraints.NORTH;
		gbc_button_5.insets = new Insets(0, 0, 5, 5);
		gbc_button_5.gridx = 2;
		gbc_button_5.gridy = 6;
		frmFilterControl.getContentPane().add(button_5, gbc_button_5);
		
		button_3 = new JButton("1.0");
		button_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(2);
				calData.setGotoPos(2);
				switch (calData.getCurrPos()) {
				case 0: button_1.setEnabled(true);
				break;
				case 1: button_2.setEnabled(true);
				break;
				case 3: button_4.setEnabled(true);
				break;
				case 4: button_5.setEnabled(true);
				break;
				case 5: button_6.setEnabled(true);
				break;
				}
				if (calData.getCurrPos() != 2) {
					calData.setSelectedPos(2);

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g2\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					
				    System.out.println("Current position: "+calData.getCurrPos());
				}
				button_3.setEnabled(false);
				button_4.requestFocus();
			}
		});
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalGlue_1.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_1 = new GridBagConstraints();
		gbc_horizontalGlue_1.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_1.gridx = 3;
		gbc_horizontalGlue_1.gridy = 6;
		frmFilterControl.getContentPane().add(horizontalGlue_1, gbc_horizontalGlue_1);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalGlue_3.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_3 = new GridBagConstraints();
		gbc_horizontalGlue_3.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_3.gridx = 6;
		gbc_horizontalGlue_3.gridy = 6;
		frmFilterControl.getContentPane().add(horizontalGlue_3, gbc_horizontalGlue_3);
		GridBagConstraints gbc_button_3 = new GridBagConstraints();
		gbc_button_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_3.anchor = GridBagConstraints.NORTH;
		gbc_button_3.insets = new Insets(0, 0, 5, 5);
		gbc_button_3.gridx = 9;
		gbc_button_3.gridy = 6;
		frmFilterControl.getContentPane().add(button_3, gbc_button_3);
		
		button_4 = new JButton("1.5");
		button_4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(3);
				calData.setGotoPos(3);
				switch (calData.getCurrPos()) {
				case 0: button_1.setEnabled(true);
				break;
				case 1: button_2.setEnabled(true);
				break;
				case 2: button_3.setEnabled(true);
				break;
				case 4: button_5.setEnabled(true);
				break;
				case 5: button_6.setEnabled(true);
				break;
				}
				if (calData.getCurrPos() != 3) {
					calData.setSelectedPos(3);

					//int steps = 1;
					int steps = (calData.getGotoPos()-calData.getCurrPos())%6;
				    if(steps < 0) {
				      steps += 6;
				    }
				    comms.sendCommand("g3\n");
					calData.commitPos();
					try {
						new ProgressBar(frmFilterControl, "Progress", true, steps); //ProgressBar dlg = 
					} catch(Exception ex) {
						//Do nothing...
					}
					//dlg.setVisible(true);
					//dlg.setLocationRelativeTo(frame);
					//dlg.runWheel();
					//System.out.println("Test case done.");
					//button_4.setEnabled(false);
				    System.out.println("Current position: "+calData.getCurrPos());
				}
				button_4.setEnabled(false);
				button_5.requestFocus();
			}
		});
		
		Component horizontalGlue_6 = Box.createHorizontalGlue();
		horizontalGlue_6.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_6 = new GridBagConstraints();
		gbc_horizontalGlue_6.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_6.gridx = 10;
		gbc_horizontalGlue_6.gridy = 6;
		frmFilterControl.getContentPane().add(horizontalGlue_6, gbc_horizontalGlue_6);
		
		Component verticalGlue_2 = Box.createVerticalGlue();
		verticalGlue_2.setFocusable(false);
		GridBagConstraints gbc_verticalGlue_2 = new GridBagConstraints();
		gbc_verticalGlue_2.insets = new Insets(0, 0, 5, 5);
		gbc_verticalGlue_2.gridx = 9;
		gbc_verticalGlue_2.gridy = 7;
		frmFilterControl.getContentPane().add(verticalGlue_2, gbc_verticalGlue_2);
		
		Component glue_2 = Box.createGlue();
		glue_2.setFocusable(false);
		GridBagConstraints gbc_glue_2 = new GridBagConstraints();
		gbc_glue_2.insets = new Insets(0, 0, 5, 5);
		gbc_glue_2.gridx = 2;
		gbc_glue_2.gridy = 8;
		frmFilterControl.getContentPane().add(glue_2, gbc_glue_2);
		GridBagConstraints gbc_button_4 = new GridBagConstraints();
		gbc_button_4.fill = GridBagConstraints.HORIZONTAL;
		gbc_button_4.anchor = GridBagConstraints.NORTH;
		gbc_button_4.insets = new Insets(0, 0, 5, 5);
		gbc_button_4.gridx = 5;
		gbc_button_4.gridy = 8;
		frmFilterControl.getContentPane().add(button_4, gbc_button_4);		
		dispOn = new JToggleButton("Display ON");
		dispOn.setFocusPainted(false);
		dispOn.setRequestFocusEnabled(false);
		dispOn.setForeground(UIManager.getColor("Button.foreground"));
		dispOn.setFocusable(false);
		//dispOn.setBackground(UIManager.getColor("Button.background"));
		dispOn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Display toggled: "+dispOn.isSelected());
				System.out.print("Command sent: ");
				if (dispOn.isSelected()) {
					System.out.println("d0");
					dispOn.setText("Display OFF");
					dispOn.setBackground(Color.gray);
					calData.setDispOn(false);
					calData.getComms().sendCommand("d0\n");
				} else {
					//System.out.println("d1");
					dispOn.setText("Display ON");
					displayTimedOut.setVisible(calData.useTimeout());
					if (calData.useTimeout()) {
						calData.setDispOn(true);
						calData.getComms().sendCommand("t"+Math.round((double)calData.getTimeout())+"\n");
						System.out.print("t"+Math.round((double)calData.getTimeout())+"\n");
					} else {
						calData.setDispOn(true);
						calData.getComms().sendCommand("d1\n");
					}
					
				}
				
			}
		});
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		horizontalGlue_4.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_4 = new GridBagConstraints();
		gbc_horizontalGlue_4.insets = new Insets(0, 0, 5, 5);
		gbc_horizontalGlue_4.gridx = 7;
		gbc_horizontalGlue_4.gridy = 8;
		frmFilterControl.getContentPane().add(horizontalGlue_4, gbc_horizontalGlue_4);
		GridBagConstraints gbc_dispOn = new GridBagConstraints();
		gbc_dispOn.anchor = GridBagConstraints.SOUTH;
		gbc_dispOn.gridwidth = 2;
		gbc_dispOn.insets = new Insets(0, 0, 5, 5);
		gbc_dispOn.gridx = 9;
		gbc_dispOn.gridy = 8;
		frmFilterControl.getContentPane().add(dispOn, gbc_dispOn);
		
		displayTimedOut = new JTextPane();
		displayTimedOut.setFocusable(false);
		displayTimedOut.setEnabled(false);
		displayTimedOut.setEditable(false);
		displayTimedOut.setVisible(calData.useTimeout());
		
		Component horizontalGlue_7 = Box.createHorizontalGlue();
		horizontalGlue_7.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_7 = new GridBagConstraints();
		gbc_horizontalGlue_7.insets = new Insets(0, 0, 5, 0);
		gbc_horizontalGlue_7.gridx = 12;
		gbc_horizontalGlue_7.gridy = 8;
		frmFilterControl.getContentPane().add(horizontalGlue_7, gbc_horizontalGlue_7);
		displayTimedOut.setFont(new Font("Lucida Grande", Font.PLAIN, 10));
		displayTimedOut.setBackground(UIManager.getColor("InternalFrame.background"));
		displayTimedOut.setText("(Timeout set)");
		GridBagConstraints gbc_displayTimedOut = new GridBagConstraints();
		gbc_displayTimedOut.insets = new Insets(0, 0, 0, 5);
		gbc_displayTimedOut.gridwidth = 2;
		gbc_displayTimedOut.fill = GridBagConstraints.VERTICAL;
		gbc_displayTimedOut.gridx = 9;
		gbc_displayTimedOut.gridy = 9;
		frmFilterControl.getContentPane().add(displayTimedOut, gbc_displayTimedOut);
		
		Component horizontalGlue_8 = Box.createHorizontalGlue();
		horizontalGlue_8.setFocusable(false);
		GridBagConstraints gbc_horizontalGlue_8 = new GridBagConstraints();
		gbc_horizontalGlue_8.insets = new Insets(0, 0, 0, 5);
		gbc_horizontalGlue_8.gridx = 11;
		gbc_horizontalGlue_8.gridy = 9;
		frmFilterControl.getContentPane().add(horizontalGlue_8, gbc_horizontalGlue_8);
		frmFilterControl.getContentPane().setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{button_1, button_2, button_3, button_4, button_5, button_6, dispOn, displayTimedOut, horizontalGlue_8, verticalGlue_4, verticalGlue_3, txtpnSelectNdFilter, glue, glue_1, horizontalGlue_9, horizontalGlue, lblWheelim, horizontalGlue_2, horizontalGlue_5, verticalGlue, verticalGlue_1, horizontalGlue_10, horizontalGlue_1, horizontalGlue_3, horizontalGlue_6, verticalGlue_2, glue_2, horizontalGlue_4, horizontalGlue_7}));
		
		/**
		 * Toggle display on and off. <br>Note that the display is ON for a value of false. 
		 */
		
		
		JMenuBar menuBar = new JMenuBar();
		frmFilterControl.setJMenuBar(menuBar);
		
		JMenu mnFilterControl = new JMenu("Filter Control");
		menuBar.add(mnFilterControl);
		
		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				calData.getComms().sendCommand("a\n");
				JOptionPane.showMessageDialog(mntmAbout,"Attenuator filter wheel control\nCopyright \u00a9 2018 Erik G Hedlund\n"
						+ "GPL 2.0\nv "+version,"About",JOptionPane.PLAIN_MESSAGE, wIcon);
				calData.getComms().sendCommand("b\n");
				//System.out.println("This is executed after the about box is invoked...");
			}
		});
		mnFilterControl.add(mntmAbout);
		
		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		mntmQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				// This bit saves preferences
				if (comms.isConfigured()) {
					prefs.setProperty("SerialPort",comms.getOpenPort().getSystemPortName());
				} else {
					prefs.setProperty("SerialPort","");
				}
				File repPath = new File("FilterPrefs.xml");
				if (!prefsPath.equals(repPath)) {
					repPath = prefsPath;
				}
				prefs.setProperty("prefsPath", prefsPath.getAbsolutePath());
				System.out.print("repPath: ");
				System.out.println(repPath);
				System.out.print("prefsPath: ");
				System.out.println(prefsPath);
				//prefs.setProperty("MacSerialPort", comms.getOpenPort().getSystemPortName());
				//prefs.setProperty("WinSerialPort", "COM5");
				calData.storePrefs(prefs);
				try (FileOutputStream pOutx = new FileOutputStream(repPath)){  
					
					prefs.storeToXML(pOutx, "---Filter wheel state and settings---");
					//pOut.close();
					pOutx.close();
				} catch (Exception ex) {
					System.out.println("Exception while saving preferences to file...");
					ex.printStackTrace();
				}
				// Close the port if opened...
				if (comms.isConfigured()) {
					comms.closeOpenPort();
				}				
				// ... and gracefully die.
				System.exit(0);
			}
		});
		mnFilterControl.add(mntmQuit);
		
		JMenu mnSettings = new JMenu("Settings");
		menuBar.add(mnSettings);
		
		JMenuItem mntmCalibrate = new JMenuItem("Calibrate");
		mntmCalibrate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
		mntmCalibrate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					
					switch (calData.getCurrPos()) {
						case 0: button_1.setEnabled(true);
						break;
						case 1: button_2.setEnabled(true);
						break;
						case 2: button_3.setEnabled(true);
						break;
						case 3: button_4.setEnabled(true);
						break;
						case 4: button_5.setEnabled(true);
						break;
						case 5: button_6.setEnabled(true);
					}
					Calibration dialog = new Calibration(calData);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);					
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					err.printStackTrace();
				}
				// System.out.println(calData.getCurrPos());

			}
		});
		mnSettings.add(mntmCalibrate);
		
		JMenuItem mntmConfSerial = new JMenuItem("Configure serial port");
		mntmConfSerial.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
		mntmConfSerial.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Configure serial port selected...");
				try {
					SerialPortSelect dialog = new SerialPortSelect(comms);
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setVisible(true);
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					err.printStackTrace();
				}
				// System.out.println(calData.getCurrPos());
			}
		});
		mnSettings.add(mntmConfSerial);
		
		JMenuItem mntmDisplayBrightness = new JMenuItem("Display brightness");
		mntmDisplayBrightness.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
		mntmDisplayBrightness.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("Display configuration selected...");
				try {
					DisplayControl dDialog = new DisplayControl(calData, dispOn, displayTimedOut, chckbxmntmUseDisplayTimeout);
					dDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dDialog.setVisible(true);
					//System.out.println(calData.getCurrPos());
				} catch (Exception err) {
					System.out.println("Couldn't start the display configurator");
					err.printStackTrace();
				}
			}
		});
		
		JMenuItem mntmSelectSettingsLocation = new JMenuItem("Select settings location");
		mntmSelectSettingsLocation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//JFrame chooserFrame = new JFrame();
				JFileChooser chooser = new JFileChooser();
				//FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT files", "txt");
				//chooser.setFileFilter(filter);
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//chooser.setCurrentDirectory(new File("").getAbsoluteFile());
				chooser.setCurrentDirectory(prefsPath);
				chooser.setDialogTitle("Select folder to save session settings to");
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				int returnVal = chooser.showSaveDialog(frmFilterControl);

	            
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File altPrefsPath = new File(chooser.getSelectedFile(),"FilterPrefs.xml");
					System.out.print("Original prefsPath: ");
					System.out.println(prefsPath);
					prefs.setProperty("prefsPath", altPrefsPath.getAbsolutePath());
					try (FileOutputStream pOutx = new FileOutputStream(prefsPath)) { 
						
						prefs.storeToXML(pOutx, "---Filter wheel state and settings---");
						//pOut.close();
						pOutx.close();
					} catch (Exception ex) {
						System.out.println("Exception while saving preferences to standard file...");
						ex.printStackTrace();
					}
//					System.out.println("You chose to save settings to this folder: " +
//							chooser.getSelectedFile().getAbsolutePath());
					prefsPath = altPrefsPath; //new File (altPrefsPath,"FilterPrefs.xml");
					System.out.print("You chose to save settings to this file: ");
					System.out.println(prefsPath);
				} else if (returnVal == JFileChooser.CANCEL_OPTION) {
					System.out.println("Cancel selected, previous path still valid.");
				}
				//int returnVal = fc.showOpenDialog(aComponent);
			}
		});
		mnSettings.add(mntmSelectSettingsLocation);
		mnSettings.add(mntmDisplayBrightness);
		
		chckbxmntmUseDisplayTimeout = new JCheckBoxMenuItem("Use display timeout");
		chckbxmntmUseDisplayTimeout.setSelected(calData.useTimeout());
		chckbxmntmUseDisplayTimeout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxmntmUseDisplayTimeout.isSelected()) {
					System.out.print("Command sent: ");
					System.out.println("t"+calData.getOldTo());
					calData.setTimeout(calData.getOldTo());
					calData.getComms().sendCommand("t"+calData.getOldTo()+"\n");
					displayTimedOut.setVisible(true);
					//calData.setOldTo(Math.round((double)timeout.getValue()*1000));
					//dispOn.setSelected(true);
				} else {
					calData.setOldTo(calData.getTimeout());
					calData.setTimeout(0);
					calData.getComms().sendCommand("t0\n");
					displayTimedOut.setVisible(false);
					//dispOn.setSelected(selOff);
				}
			}
		});
		mnSettings.add(chckbxmntmUseDisplayTimeout);			

		//Finalise the reading of preferences
		System.out.print("dispOn: ");
		System.out.println(Boolean.parseBoolean(prefs.getProperty("dispOn","true")));
		if (Boolean.parseBoolean(prefs.getProperty("dispOn","true"))) {
			dispOn.setSelected(false);
			dispOn.setText("Display ON");
		} else {
			dispOn.setSelected(true);
			dispOn.setText("Display OFF");
		}
		// It seems that the port needs a few milliseconds to properly open, around now this command seems to be safe to issue
		Thread t = new Thread() {
			@Override public void run() {
				// Wait after connection

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					System.out.println("The delay thread was interrupted while waiting for communications initialisation to stabilise.");
					//Silently ignore
				}

				if (comms.isConfigured()) {
					calData.commitPrefs();
				}				
			}
		};
		t.start();	
		try { t.join(); } catch (Exception e) { System.out.println("The delay thread was interrupted while waiting for it to die."); }
		
		switch (calData.getCurrPos()) {
		case 0: button_1.doClick();
		break;
		case 1: button_2.doClick();//setEnabled(false);
		break;
		case 2: button_3.doClick();//setEnabled(false);
		break;
		case 3: button_4.doClick();//setEnabled(false);
		break;
		case 4: button_5.doClick();//setEnabled(false);
		break;
		case 5: button_6.doClick();//setEnabled(false);
	}
		
		
	}		
}
