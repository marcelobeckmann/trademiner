package webcrawler;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class MonitorForm {

	private int DEFAULT_W = 640;
	private int DEFAULT_H = 430;

	private static List<Monitor> monitors = new ArrayList<Monitor>();
	
	private JFrame[] frame;
	private MonPanel monPanel;

	/**
	 * Launch the application.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		monitors=Monitor.load();
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MonitorForm window = new MonitorForm();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/**
	 * Create the application.
	 */
	public MonitorForm() {
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		if (frame==null)
		{
		    frame = new JFrame[monitors.size()];
		}
		int i=0;
		for (Monitor monitor:monitors) {
			
			//TODO include resize events
			frame[i] = new JFrame();
			frame[i].setBounds(40+(i*30), 40+(i*30), DEFAULT_W, DEFAULT_H);
			frame[i].setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame[i].getContentPane().setLayout(new FlowLayout(FlowLayout.LEFT));
			
			int stepy=5;
			monPanel= new MonPanel(frame[i],monitor);
			monPanel.setBounds(5, stepy, DEFAULT_W, MonPanel.DEFAULT_H);
			frame[i].getContentPane().add(monPanel);
			frame[i].setTitle(Monitor.getMainTitle()+ " - "+ monitor.getTitle());
			frame[i].setVisible(true);
			i++;
		}
	
		

	}

	public class MonPanel extends JPanel implements Runnable {

		static final int RESULT_HEIGHT=80+40;
		static final int DEFAULT_H=170+(RESULT_HEIGHT*2);
		JLabel lblTitle;
		JLabel lblRowcount;
		JLabel lblLogFile;
		JTextArea  lblErrors;
		
		JTextArea  lblSuccess;
		
		JScrollPane  scrollErrors;
		
		JScrollPane  scrollSuccess;
		
		JFrame frame;
		Monitor monitor;
		Thread thread;

		public MonPanel(JFrame frame,Monitor monitor) {

			this.frame=frame;
			this.monitor = monitor;
			//frame.addComponentListener(new ResizeListener());
			initialize();
			

			thread = new Thread(this);
			thread.start();
		}

		public void initialize() {
			
			setPreferredSize(new Dimension(frame.getWidth()-2, DEFAULT_H));
			
			setBorder(BorderFactory.createLineBorder(Color.BLACK));

			setLayout(null);
			int stepy = 5;

			lblTitle = new JLabel("process: " + monitor.getTitle());
			lblTitle.setBounds(5, stepy, DEFAULT_W, 20);
			
			add(lblTitle);

			lblLogFile = new JLabel("log file: " + monitor.getLogFile());
			lblLogFile.setBounds(5, stepy += 30, DEFAULT_W, 20);
			add(lblLogFile);

			lblRowcount = new JLabel(monitor.getTable() +",rowcount: " + monitor.getTableCount());
			lblRowcount.setBounds(5, stepy += 30, DEFAULT_W, 20);
			
			add(lblRowcount);

			//------------------------------------------------------------------
			JLabel jlabel = new JLabel("errors:");
			jlabel.setBounds(5, stepy += 30, 50, 10);
			add(jlabel);
			
			lblErrors = new JTextArea ();
			lblErrors.setOpaque(true);
			lblErrors.setWrapStyleWord(true);
			lblErrors.setAutoscrolls(true);
			lblErrors.setEditable(false);
			lblErrors.setForeground(Color.BLACK);
			
			scrollErrors = new JScrollPane(lblErrors);
			scrollErrors.setBounds(5, stepy += 20, DEFAULT_W-30, RESULT_HEIGHT);
			add(scrollErrors);
			//------------------------------------------------------------------			
			jlabel = new JLabel("success:");
			jlabel.setBounds(5, stepy += RESULT_HEIGHT+5, 70, 10);
			add(jlabel);

			
			
			lblSuccess = new JTextArea ();
			lblSuccess.setOpaque(true);
			lblSuccess.setWrapStyleWord(true);
			lblSuccess.setAutoscrolls(true);
			lblSuccess.setEditable(false);
			lblSuccess.setForeground(Color.BLACK);
			scrollSuccess = new JScrollPane(lblSuccess);
			scrollSuccess.setBounds(5, stepy += 15, DEFAULT_W-30, RESULT_HEIGHT);
			add(scrollSuccess);

			
		}

		public void run() {

			monitor.start();
			while (true) {

				lblRowcount.setText(monitor.getTable()+", rowcount: " + monitor.getTableCount());
				
				StringBuilder sb= new StringBuilder();
				
				for (int i=0;i<monitor.getErrorResults().size();i++)
				{
					sb.append(monitor.getErrorResults().get(i));
					sb.append('\n');
				}
				
				lblErrors.setText( sb.toString());

				sb= new StringBuilder();
				
				
				sb= new StringBuilder();
				
				for (int i=0;i<monitor.getSuccessResults().size();i++)
				{
					sb.append(monitor.getSuccessResults().get(i));
					sb.append('\n');
				}
				
				lblSuccess.setText(sb.toString());

				if (monitor.getErrorResults().size()>0)
				{
					lblErrors.setBackground(Color.RED);
					
				}
				
				if (monitor.getSuccessResults().size()>0)
				{
					lblSuccess.setBackground(Color.GREEN);
				}
				
				try {
					Thread.currentThread().join(1000);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		}
		
	
		

	}




	
}
