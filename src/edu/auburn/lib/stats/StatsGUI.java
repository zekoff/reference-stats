package edu.auburn.lib.stats;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;

import com.toedter.calendar.JDateChooser;

public class StatsGUI {

	private JFrame frmLibraryStatsTool;
	private JDateChooser beginDateWidget;
	private JDateChooser endDateWidget;
	private JComboBox comboContactPoint;
	private JComboBox comboReadScale;
	private JComboBox comboTransactionType;
	private JButton btnExportToCsv;
	private JEditorPane textPane;

	private StatsParser parser;
	private JLabel totalTransactionsLabel;
	private JButton btnChooseDifferentFile;

	/**
	 * Create the application.
	 * 
	 * @param parser
	 *            The main StatsParser object running the program.
	 */
	public StatsGUI(StatsParser parser) {
		this.parser = parser;
		initialize();
		addListeners();
		frmLibraryStatsTool.setVisible(true);
	}

	/**
	 * Add listeners as appropriate for the GUI elements.
	 */
	private void addListeners() {
		beginDateWidget.addPropertyChangeListener(parser);
		endDateWidget.addPropertyChangeListener(parser);
		comboContactPoint.addActionListener(parser);
		comboReadScale.addActionListener(parser);
		comboTransactionType.addActionListener(parser);
		btnExportToCsv.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parser.exportCsv();
			}
		});
		btnChooseDifferentFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parser.pickFile();
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmLibraryStatsTool = new JFrame();
		frmLibraryStatsTool.setResizable(false);
		frmLibraryStatsTool.setTitle("Library Stats Tool");
		frmLibraryStatsTool.setBounds(100, 100, 654, 427);
		frmLibraryStatsTool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLibraryStatsTool.getContentPane().setLayout(null);
		ImageIcon image = new ImageIcon("res/au_icon.jpg");
		frmLibraryStatsTool.setIconImage(image.getImage());
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 198, 378);
		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		frmLibraryStatsTool.getContentPane().add(panel);
		panel.setLayout(null);

		JLabel lblBeginDate = new JLabel("Begin date:");
		lblBeginDate.setBounds(14, 116, 55, 14);
		panel.add(lblBeginDate);

		beginDateWidget = new JDateChooser();
		beginDateWidget.setBounds(0, 129, 185, 20);
		beginDateWidget.setDateFormatString("MM/dd/yy");
		panel.add(beginDateWidget);

		JLabel lblEndDate = new JLabel("End Date:");
		lblEndDate.setBounds(14, 161, 48, 14);
		panel.add(lblEndDate);

		endDateWidget = new JDateChooser();
		endDateWidget.setBounds(0, 174, 185, 20);
		endDateWidget.setDateFormatString("MM/dd/yy");
		panel.add(endDateWidget);

		JLabel lblContactPoint = new JLabel("Contact Point:");
		lblContactPoint.setBounds(10, 243, 69, 14);
		panel.add(lblContactPoint);

		comboContactPoint = new JComboBox();
		comboContactPoint.setBounds(0, 258, 185, 20);
		comboContactPoint.setModel(new DefaultComboBoxModel(
				StatsParser.CONTACT_POINTS));
		panel.add(comboContactPoint);

		JLabel lblNewLabel = new JLabel("READ Scale:");
		lblNewLabel.setBounds(10, 289, 59, 14);
		panel.add(lblNewLabel);

		comboReadScale = new JComboBox();
		comboReadScale.setBounds(0, 302, 185, 20);
		comboReadScale.setModel(new DefaultComboBoxModel(
				new String[] { "All", "1 (Directional)", "Not 1 (Reference)",
						"2", "3", "4", "5", "6" }));
		panel.add(comboReadScale);

		JLabel lblTransactionType = new JLabel("Transaction type:");
		lblTransactionType.setBounds(10, 333, 85, 14);
		panel.add(lblTransactionType);

		comboTransactionType = new JComboBox();
		comboTransactionType.setBounds(0, 347, 185, 20);
		comboTransactionType.setModel(new DefaultComboBoxModel(new String[] {
				"All", "Traditional (Face to face, phone)",
				"Virtual (Email, chat, text)", StatsParser.TRANS_FACE_TO_FACE,
				StatsParser.TRANS_PHONE, StatsParser.TRANS_EMAIL,
				StatsParser.TRANS_CHAT, StatsParser.TRANS_TEXT }));
		panel.add(comboTransactionType);

		JLabel lblLibraryStatsTool = new JLabel("Library Stats Tool");
		lblLibraryStatsTool.setFont(new Font("Arial Black", Font.PLAIN, 16));
		lblLibraryStatsTool.setBounds(14, 0, 171, 20);
		panel.add(lblLibraryStatsTool);

		JLabel lblFilename = new JLabel("Using file:");
		lblFilename.setBounds(14, 33, 126, 14);
		panel.add(lblFilename);

		btnChooseDifferentFile = new JButton("Choose Different File");
		btnChooseDifferentFile.setBounds(0, 45, 185, 23);
		panel.add(btnChooseDifferentFile);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(218, 11, 420, 378);
		panel_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null,
				null));
		frmLibraryStatsTool.getContentPane().add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_1.add(panel_2, BorderLayout.NORTH);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

		totalTransactionsLabel = new JLabel(
				"Total transactions matching filters:");
		panel_2.add(totalTransactionsLabel);

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3, BorderLayout.SOUTH);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

		btnExportToCsv = new JButton("Export to CSV (spreadsheet)");
		btnExportToCsv.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(btnExportToCsv);

		textPane = new JTextPane();
		textPane.setContentType("text/html");
		textPane.setEditable(false);
		JScrollPane scrollArea = new JScrollPane(textPane);
		scrollArea
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel_1.add(scrollArea, BorderLayout.CENTER);
	}

	public Calendar getBeginDateCalendar() {
		return beginDateWidget.getCalendar();
	}

	public Calendar getEndDateCalendar() {
		return endDateWidget.getCalendar();
	}

	public void setTransactions(int transactions) {
		totalTransactionsLabel.setText("Total transactions matching filters: "
				+ transactions);
	}

	public void setDisplay(String display) {
		textPane.setText("<html>" + display + "</html>");
		textPane.setCaretPosition(0);
	}

	public String getContactPoint() {
		return comboContactPoint.getSelectedItem().toString();
	}

	public String getReadScale() {
		return comboReadScale.getSelectedItem().toString();
	}

	public String getTransactionType() {
		return comboTransactionType.getSelectedItem().toString();
	}

	protected JFrame getFrame() {
		return frmLibraryStatsTool;
	}

	public void setFilename(String name) {
		btnChooseDifferentFile.setText(name);
	}
}
