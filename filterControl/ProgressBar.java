package filterControl;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

//import com.sun.javafx.tk.Toolkit.Task;

public class ProgressBar extends JDialog {
//Panel implements ActionListener, PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 347227895504503986L;
	private JPanel contentPane; // = new JPanel();
	
	private JProgressBar progressBar;
	private JLabel pLabel;
	private int steps;
	private int currFill;


	/**
	 * Create the frame.
	 */
	public ProgressBar(Frame owner, String title, boolean modal, int steps) {
		super(owner, title, modal);
		
		this.steps = steps;
		currFill = 0;
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 408, 100);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		
		
		progressBar = new JProgressBar(currFill,steps*200);

		progressBar.setSize(400, 25);
		//progressBar.setMinimum(0);
		progressBar.setMaximum(steps*200);
		
		contentPane.add(progressBar, BorderLayout.CENTER);
		pLabel = new JLabel("Wheel moving...");
		contentPane.add(pLabel, BorderLayout.NORTH);
		
		//progressBar.setValue(150);
		progressBar.setVisible(true);
		if (modal) {
			runWheel();
			setLocationRelativeTo(owner);
			setVisible(true);
		}

	}
	public void runWheel() {
		System.out.println("Entering the wheel...");
		Thread t = new Thread(new Runnable() {
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
			    System.setProperty("sun.awt.exception.handler",
			                       ExceptionHandler.class.getName());
//				try {
					//SwingUtilities.invokeLater(new Runnable() { 
					//	public void run() {
					for(int i = 0; i <= steps*200; i++) {
						final int percent = i;
						//.invokeLater(new Runnable()

						//System.out.println("Running... "+percent);
						progressBar.setValue(percent);
						progressBar.updateUI();
						try {
							java.lang.Thread.sleep(9,500);
						} catch (InterruptedException ex) {
							//ex.printStackTrace();	//Nothing to do here
						}

					}

					//this.notify();
//				} catch (Exception ex) {
//					ex.printStackTrace();	//Nothing to do here
//				}	

				pLabel.setText("Done!");
				try {
					java.lang.Thread.sleep(750);

				} catch (Exception ex) {
					//Nothing to do here
				}
				dispose();
			}
		});
		t.start();
		return;	
	}
	
	public void updateWheel() {
		System.out.println("Updating wheel...");
		currFill++;
		System.out.println(currFill);
		progressBar.setValue(currFill);
		if (currFill == 20*steps) {
			try {
				System.out.println("Killing grounds... ");
				java.lang.Thread.sleep(2000);
				this.dispose();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
		}
		
		
	}
	public static class ExceptionHandler implements Thread.UncaughtExceptionHandler {

		public void handle(Throwable thrown) {
			// for EDT exceptions
			handleException(Thread.currentThread().getName(), thrown);
		}

		public void uncaughtException(Thread thread, Throwable thrown) {
			// for other uncaught exceptions
			handleException(thread.getName(), thrown);
		}

		/**
		 * Silently ignore various null pointer exceptions... they don't matter too much here anyway.
		 * @param tname
		 * @param thrown
		 */
		protected void handleException(String tname, Throwable thrown) {
						
			//System.err.println("Exception on " + tname);
			//thrown.printStackTrace();
		}
	}
}
