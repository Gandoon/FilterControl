package filterControl;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DisplayControl extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8703498757450907348L;	
	private final JPanel contentPanel = new JPanel();
	private JTextPane hiPerc;
	private JTextPane loPerc;
	private JSlider hiSlider;
	private JSlider loSlider;
	private int origLoVal;
	private int origHiVal; 
	private JCheckBox chckbxUseDisplayTimeout;
	//private JCheckBoxMenuItem menuCheckBox;
	private JSpinner timeout;
	private boolean selOff;
	private JToggleButton dispOn;
	private JTextPane timedOut;
	private JCheckBoxMenuItem chckbxmntmUseDisplayTimeout;

	/**
	 * Create the dialog.
	 */
	
//	public DisplayControl(CalibrationData calData) {
//		super();
//		origLoVal = calData.getLoBr();
//		origHiVal = calData.getHiBr();
//		selOff = true;
//		runDisplayControl(calData);
//	}
	
	public DisplayControl(CalibrationData calData, JToggleButton dispOn, JTextPane timedOut, JCheckBoxMenuItem chckbxmntmUseDisplayTimeout) {
		super();
		origLoVal = calData.getLoBr();
		origHiVal = calData.getHiBr();
		selOff = dispOn.isSelected();
		this.dispOn = dispOn;
		this.timedOut = timedOut;
		this.chckbxmntmUseDisplayTimeout = chckbxmntmUseDisplayTimeout;
		runDisplayControl(calData);
	}
	
	public void runDisplayControl(CalibrationData calData) {
				
		//origLoVal = calData.getLoBr();
		//origHiVal = calData.getHiBr();
		//selOff = dispOn.isSelected();
		//menuCheckBox = chckbxmntmUseDisplayTimeout;
		
		setBounds(100, 100, 450, 202);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			hiSlider = new JSlider(0,255,origHiVal);
			hiSlider.setToolTipText("Set the brightness of the display during actions");
			hiSlider.setBounds(172, 18, 190, 29);
			hiSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					hiPerc.setText(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
					//System.out.println(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
					//System.out.println("l"+hiSlider.getValue()+"\n");
					calData.getComms().sendCommand("l"+hiSlider.getValue()+"\n");
				}
			});
			contentPanel.setLayout(null);
			contentPanel.add(hiSlider);
		}

		{
			hiPerc = new JTextPane();
			hiPerc.setEditable(false);
			hiPerc.setBounds(367, 24, 40, 16);
			hiPerc.setBackground(UIManager.getColor("InternalFrame.background"));
			hiPerc.setText(Math.round(Double.valueOf(hiSlider.getValue())/255*100)+"%");
			contentPanel.add(hiPerc);
		}
		
		{
			JTextPane txtpnHighBrightnessLevel = new JTextPane();
			txtpnHighBrightnessLevel.setEditable(false);
			txtpnHighBrightnessLevel.setBounds(25, 24, 135, 16);
			txtpnHighBrightnessLevel.setBackground(UIManager.getColor("InternalFrame.background"));
			txtpnHighBrightnessLevel.setText("High brightness level");
			contentPanel.add(txtpnHighBrightnessLevel);

			
		}
		
		{
			JTextPane txtpnLowBrightnessLevel = new JTextPane();
			txtpnLowBrightnessLevel.setEditable(false);
			txtpnLowBrightnessLevel.setBounds(27, 58, 130, 16);
			txtpnLowBrightnessLevel.setBackground(UIManager.getColor("InternalFrame.background"));
			txtpnLowBrightnessLevel.setText("Low brightness level");
			contentPanel.add(txtpnLowBrightnessLevel);
		}
		{
			loSlider = new JSlider(0,255,origLoVal);
			loSlider.setToolTipText("Set the display idle brightness");
			loSlider.setBounds(172, 52, 190, 29);
			loSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					loPerc.setText(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
					//System.out.println(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
					//System.out.println("l"+loSlider.getValue()+"\n");
					calData.getComms().sendCommand("l"+loSlider.getValue()+"\n");
				}
			});
			contentPanel.add(loSlider);
		}
		{
			loPerc = new JTextPane();
			loPerc.setEditable(false);
			loPerc.setBounds(367, 58, 40, 16);
			loPerc.setBackground(UIManager.getColor("InternalFrame.background"));
			loPerc.setText(Math.round(Double.valueOf(loSlider.getValue())/255*100)+"%");
			contentPanel.add(loPerc);
		}
		double to; // = 2.5;
		if (calData.useTimeout()) {
			to = Double.valueOf(calData.getTimeout())/1000;
		} else {
			to = Double.valueOf(calData.getOldTo())/1000;
		}
		timeout = new JSpinner(new SpinnerNumberModel(to,0.0,Double.MAX_VALUE,0.1));
		timeout.setToolTipText("Display will turn off after this time in seconds");
		timeout.setBounds(182, 100, 67, 26);
		contentPanel.add(timeout);
		
		JTextPane txtpnDisplayTimeouts = new JTextPane();
		txtpnDisplayTimeouts.setBackground(UIManager.getColor("InternalFrame.background"));
		txtpnDisplayTimeouts.setText("Display timeout (s)");
		txtpnDisplayTimeouts.setBounds(25, 103, 135, 16);
		contentPanel.add(txtpnDisplayTimeouts);
		
		chckbxUseDisplayTimeout = new JCheckBox("Use display timeout");
		chckbxUseDisplayTimeout.setToolTipText("Displays turns off after set display timeout");
		chckbxUseDisplayTimeout.setBounds(261, 101, 157, 23);
		chckbxUseDisplayTimeout.setSelected(calData.useTimeout());
		contentPanel.add(chckbxUseDisplayTimeout);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.out.println("OK selected, final commands sent:");
						System.out.print("h"+Integer.toString(hiSlider.getValue())+" ");
						calData.setHiBr(hiSlider.getValue());
						calData.getComms().sendCommand("h"+hiSlider.getValue()+"\n");
						System.out.print("l"+Integer.toString(loSlider.getValue())+"\n");
						calData.setLoBr(loSlider.getValue());
						calData.getComms().sendCommand("l"+loSlider.getValue()+"\n");
						System.out.println("Timeout selected: "+chckbxUseDisplayTimeout.isSelected());
						if (chckbxUseDisplayTimeout.isSelected()) {
							System.out.print("Command sent: ");
							System.out.println("t"+Math.round((double)timeout.getValue()*1000));
							calData.setTimeout(Math.round((double)timeout.getValue()*1000));
							calData.getComms().sendCommand("t"+Math.round((double)timeout.getValue()*1000)+"\n");
							timedOut.setVisible(true);
							calData.setOldTo(Math.round((double)timeout.getValue()*1000));
							chckbxmntmUseDisplayTimeout.setSelected(true);
							//dispOn.setSelected(true);
						} else {
							calData.setTimeout(0);
							calData.getComms().sendCommand("t0\n");
							timedOut.setVisible(false);
							dispOn.setSelected(selOff);
							chckbxmntmUseDisplayTimeout.setSelected(false);
						}
						if (selOff) {
							System.out.println("Entered selOff check on Display menu OK");
							System.out.println("selOff == "+selOff);
							calData.getComms().sendCommand("d0\n");
						}
						System.out.println("--------");
						System.out.println("Testing calData");
						System.out.println("Timeout set: "+calData.useTimeout());
						System.out.println("Set timeout: "+calData.getTimeout());
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						calData.getComms().sendCommand("h"+Integer.toString(origHiVal)+"\n");
						calData.getComms().sendCommand("l"+Integer.toString(origLoVal)+"\n");
						if (selOff) {
							System.out.println("Entered selOff check on Display menu OK");
							System.out.println("selOff == "+selOff);
							calData.getComms().sendCommand("d0\n");
						}
						System.out.println("Cancel selected, reverting to original values with commands:");
						System.out.print("h"+Integer.toString(origHiVal)+" ");
						System.out.print("l"+Integer.toString(origLoVal)+"\n");
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
