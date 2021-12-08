package filterControl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class Calibration extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 675049756686555735L;
	private final JPanel contentPanel = new JPanel();
	private int selectedIndex;


	/**
	 * Create the dialog.
	 */
	public Calibration(CalibrationData calData) {
		super();

		setBounds(100, 100, 400, 163);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);

		//contentPanel.setLayout(null);
		String[] posStrings = { "ND: 0.04", "ND: 0.5", "ND: 1.0", "ND: 1.5", "ND: 2.0", "ND: 2.5"  };
		@SuppressWarnings({ "unchecked", "rawtypes" })
		JComboBox comboBox = new JComboBox(posStrings);
		//ItemListener listEner = new ConfListEner();
		comboBox.setSelectedIndex(calData.getCurrPos());
		calData.setSelectedPos(calData.getCurrPos());
		comboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
			          //Object item = e.getItem();
			          String selItem = (String) e.getItem();
			          String[] result = selItem.split("ND: ");
			          System.out.println(result[1]);
			          selectedIndex = comboBox.getSelectedIndex();
			          calData.setSelectedPos(selectedIndex);
			          System.out.println(selectedIndex);
				}
		//(listEner);
		//listEner.itemStateChanged(ItemEvent e);
			}
		});
		

	
		
		comboBox.setBounds(247, 21, 107, 24);
		contentPanel.add(comboBox);
		
		JButton advButton = new JButton("Advance\nwheel one step");
		advButton.setForeground(new Color(51, 51, 51));
		advButton.setFont(new Font("Dialog", Font.PLAIN, 14));
		advButton.setBounds(89, 58, 215, 25);
		//advButton.setEnabled(false);
		contentPanel.add(advButton);
		
		JTextPane txtpnSelectCurrentWheel = new JTextPane();
		txtpnSelectCurrentWheel.setFont(new Font("Dialog", Font.PLAIN, 14));
		txtpnSelectCurrentWheel.setText("Select current wheel position");
		txtpnSelectCurrentWheel.setEditable(false);
		txtpnSelectCurrentWheel.setBackground(SystemColor.window);
		txtpnSelectCurrentWheel.setBounds(31, 22, 215, 24);
		contentPanel.add(txtpnSelectCurrentWheel);
		advButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//calData.setCurrPos(1);
				calData.getComms().sendCommand("g7\n");
			}
		});
		comboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//int selection = e.g
			}
		});
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//window.setPos(0); // Set the current position in the main application
						calData.commitPos();
						if(calData.getGotoPos() != calData.getCurrPos()) {
							calData.setGotoPos(calData.getCurrPos());
						}
						calData.getComms().sendCommand("s"+Integer.toString(selectedIndex)+"\n");
						System.out.println(calData.getCurrPos());
						System.out.println(calData.getNDVal());
						dispose();
						//this.setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println(calData.getCurrPos());
						System.out.println(calData.getNDVal());
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
}
