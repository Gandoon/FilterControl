package filterControl;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.fazecast.jSerialComm.SerialPort;

public class SerialPortSelect extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3602993713404745928L;
	private final JPanel contentPanel = new JPanel();
	private Communications comms;
	private SerialPort port;
	@SuppressWarnings("unused")
	private CalibrationData calData;


	/**
	 * Create the dialog.
	 */
	public SerialPortSelect(Communications preSetComms) {
		comms = preSetComms;
		initialise();
	}
	
	public SerialPortSelect(Communications preSetComms, CalibrationData calData) {
		comms = preSetComms;
		this.calData = calData;
		initialise();
	}

	
	
	public void initialise() {
		
		
		setBounds(100, 100, 498, 138);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JLabel lblSelectArduinoSerial = new JLabel("Select Arduino serial interface");
			contentPanel.add(lblSelectArduinoSerial);
		}
		
			
		JComboBox<String> portList = new JComboBox<String>();
		SerialPort[] portNames = this.comms.getPorts();
		for(int i = 0; i < portNames.length; i++) {
			portList.addItem(portNames[i].getSystemPortName());
		}
		contentPanel.add(portList);
		String state;
		if(comms.isConfigured()) {
			state = "Disconnect";
			portList.setEnabled(false);
		} else {
			state = "Connect";
		}
		JButton btnConnect = new JButton(state);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (btnConnect.getText().equals("Connect")) {
					// Connect to serial port
					port = SerialPort.getCommPort(portList.getSelectedItem().toString());
					System.out.println("Selected port: "+port.getSystemPortName());
					port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
					if( !port.isOpen() ) { // false ) {//
						if(port.openPort()) {
							System.out.println("Successfully opened port "+portList.getSelectedItem().toString());
							btnConnect.setText("Disconnect");
							comms.setOpenPort(port);
							comms.initialise();
							portList.setEnabled(false);
						} else {
							JOptionPane.showMessageDialog(btnConnect,"Could not open serial port "+portList.getSelectedItem().toString(),
									"Error",JOptionPane.ERROR_MESSAGE);
						}
						System.out.println("Connect selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+")");
						//dispose();
					} else {
						System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");						
					}
				} else { 
					if(comms.closeOpenPort()) {
						
						portList.setEnabled(true);
						btnConnect.setText("Connect");
					} else {
						JOptionPane.showMessageDialog(btnConnect,"Failed to close serial port "+comms.getOpenPort().getSystemPortName(),
								"Error",JOptionPane.ERROR_MESSAGE);
					}
					
				}
			}
		});
		contentPanel.add(btnConnect);
		
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (comms.isConfigured()) {
					port = comms.getOpenPort();
				} else {
					port = SerialPort.getCommPort(portList.getSelectedItem().toString());
				}
				//port = SerialPort.getCommPort(portList.getSelectedItem().toString());
				if( !port.isOpen() ) { // false ) {//
					System.out.println("Selected port: "+port.getSystemPortName());
					port.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);
					if(port.openPort()) {
						System.out.println("Successfully opened port "+portList.getSelectedItem().toString());
						btnConnect.setText("Disconnect");
						comms.setOpenPort(port);
						comms.initialise();
						portList.setEnabled(false);
					} else {
						JOptionPane.showMessageDialog(btnConnect,"Could not open serial port "+portList.getSelectedItem().toString(),
								"Error",JOptionPane.ERROR_MESSAGE);
					}
					System.out.println("OK selected, commiting selected port ("+comms.getOpenPort().getSystemPortName()+") and disposing...");
					dispose();
				} else {
					System.out.println("Port "+comms.getOpenPort().getSystemPortName()+" already open");
					dispose();
					
				}
				//dispose();
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		{
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					System.out.println("Cancel selected, disposing...");
					dispose();
				}
			});
			cancelButton.setActionCommand("Cancel");
			buttonPane.add(cancelButton);
		}
		
	}

}
