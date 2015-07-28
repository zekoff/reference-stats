package edu.auburn.lib.stats;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Counts transactions from library stats collection.
 * <p>
 * Assumptions:
 * <ul>
 * <li>The stats are saved in a comma-separated value (CSV) file.</li>
 * <li>Timestamps are held in column A and appear in the following format:
 * "MM/dd/yy hh:mm:ss".</li>
 * <li>The transaction type appears in column C. The string values for the five
 * valid transaction types are class fields.</li>
 * <li>The READ scale value appears in column E. Only READ scales 1-6 are valid.
 * </li>
 * <li>The contact point appears in column I and is the same for all instances
 * of the target contact point. Valid contact points are enumerated in the
 * CONTACT_POINTS field.</li>
 * </ul>
 * <p>
 * Note that if errors are present in the fields, individual filters may not
 * register some transactions. This utility depends on "good behavior" from the
 * web interface for stat logging. To get transaction records of all well-formed
 * rows (regardless of whether individual fields are formatted correctly) apply
 * no filters.
 * 
 * @author Zekoff
 * 
 */
public class StatsParser implements PropertyChangeListener, ActionListener {
	static final String[] CONTACT_POINTS = new String[] { "All",
			"Reference Desk", "MDRL", "Government Docs", "Vet Med",
			"Circulation", "Information Desk", "Individual Office",
			"Document Delivery", "LADC" };
	static final String TRANS_FACE_TO_FACE = "Face to face";
	static final String TRANS_PHONE = "Phone";
	static final String TRANS_EMAIL = "Email";
	static final String TRANS_CHAT = "Chat";
	static final String TRANS_TEXT = "Text";

	private StatsGUI gui;
	private List<String[]> rows;
	private HashMap<String, HashMap<String, Integer>> timeslots;
	private SimpleDateFormat dateConverter;
	private SimpleDateFormat hourConverter;
	private JFileChooser fileChooser;
	private List<String> sortedDayKeys;
	private List<String> sortedHourKeys;

	public static void main(String[] args) {
		StatsParser parser = new StatsParser();
		parser.gui = new StatsGUI(parser);
		parser.fileChooser = new JFileChooser();
		parser.pickFile();
	}

	public StatsParser() {
		dateConverter = new SimpleDateFormat("EEE, MMM dd, yyyy");
		hourConverter = new SimpleDateFormat("h a");
	}

	/**
	 * Reads in all rows of the selected CSV file and stores them as a List of
	 * String[]s in the rows field.
	 * 
	 * @param file
	 *            The CSV file to parse.
	 */
	private void loadFile(File file) {

		CSVReader reader = null;
		try {
			reader = new CSVReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui.getFrame(),
					"There was an error loading the file.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
		try {
			rows = reader.readAll();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(gui.getFrame(),
					"There was an error reading the file.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

	}

	/**
	 * The primary logic of the application. Takes filters from the GUI and
	 * matches each transaction row against those filters. Transactions that do
	 * not match all filters are discarded. The resulting list of transactions
	 * is formatted and displayed in the GUI. Some fields are stored as parser
	 * attributes for use when exporting to CSV format.
	 */
	private void updateDisplay() {
		// Instantiate iteration variables
		Date testDate = null;
		int totalTransactions = 0;
		timeslots = new HashMap<String, HashMap<String, Integer>>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy hh:mm:ss");

		// get begin date filter
		Calendar beginDate = null;
		beginDate = gui.getBeginDateCalendar();
		if (beginDate != null) {
			beginDate.clear(Calendar.HOUR_OF_DAY);
			beginDate.clear(Calendar.HOUR);
			beginDate.clear(Calendar.AM_PM);
			beginDate.clear(Calendar.MINUTE);
			beginDate.clear(Calendar.SECOND);
		}
		// get end date filter
		Calendar endDate = null;
		endDate = gui.getEndDateCalendar();
		if (endDate != null) {
			endDate.add(Calendar.DATE, 1);
			endDate.clear(Calendar.HOUR_OF_DAY);
			endDate.clear(Calendar.HOUR);
			endDate.clear(Calendar.AM_PM);
			endDate.clear(Calendar.MINUTE);
			endDate.clear(Calendar.SECOND);
		}

		// Iterate through rows
		for (String[] row : rows) {
			try {
				testDate = dateFormat.parse(row[0]);
			} catch (ParseException e) {
				// Skip any rows with malformed timestamps
				continue;
			}
			// If datetime is not between beginning and ending dates, skip
			if (beginDate != null)
				if (testDate.before(beginDate.getTime()))
					continue;
			if (endDate != null)
				if (testDate.after(endDate.getTime()))
					continue;
			// Contact point filter
			if (gui.getContactPoint().compareTo("All") != 0)
				if (row[8].compareTo(gui.getContactPoint()) != 0)
					continue;
			// READ scale filter
			if (gui.getReadScale().compareTo("All") != 0) {
				if (gui.getReadScale().compareTo("1 (Directional)") == 0) {
					// Handle "1 (Directional)" case
					if (row[4].compareTo("1") != 0)
						continue;
				} else if (gui.getReadScale().compareTo("Not 1 (Reference)") == 0) {
					// Handle "Not 1 (Reference)" case
					int value = 0;
					try {
						value = Integer.parseInt(row[4]);
					} catch (NumberFormatException e) {
						continue;
					}
					switch (value) {
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
						break;
					default:
						continue;
					}
				} else if (row[4].compareTo(gui.getReadScale()) != 0)
					continue;
			}
			// Transaction type filter
			if (gui.getTransactionType().compareTo("All") != 0) {
				// Handle "Traditional (Face-to-face, phone)" case
				if (gui.getTransactionType().compareTo(
						"Traditional (Face to face, phone)") == 0) {
					if (row[2].compareTo("Face to face") != 0
							&& row[2].compareTo("Phone") != 0)
						continue;
					// Handle "Virtual (Email, chat, text)" case
				} else if (gui.getTransactionType().compareTo(
						"Virtual (Email, chat, text)") == 0) {
					if (row[2].compareTo("Email") != 0
							&& row[2].compareTo("Chat") != 0
							&& row[2].compareTo("Text") != 0)
						continue;
				} else if (gui.getTransactionType().compareTo(row[2]) != 0)
					// Handle individual cases
					continue;
			}

			// All filters match; log this transaction
			totalTransactions++;

			// Do date-specific timeslot sorting
			sortTransaction(testDate);
		}
		System.out.println("Total transactions: "
				+ Integer.toString(totalTransactions));

		StringBuilder display = new StringBuilder();
		display.append("<p style='background-color: #496e9c; color: white; padding: 10px; margin-top:0;'>");
		display.append("Contact point: <strong>" + gui.getContactPoint()
				+ "</strong><br/>");
		display.append("READ scale: <strong>" + gui.getReadScale()
				+ "</strong><br/>");
		display.append("Transaction type: <strong>" + gui.getTransactionType()
				+ "</strong>");
		display.append("</p>");
		sortedDayKeys = new ArrayList<String>(timeslots.keySet());
		Collections.sort(sortedDayKeys, new StatsDayComparator());
		for (String day : sortedDayKeys) {
			display.append("<br/><b>" + day + "</b>");
			display.append("<table>");
			sortedHourKeys = new ArrayList<String>(timeslots.get(day).keySet());
			Collections.sort(sortedHourKeys, new StatsHourComparator());
			for (String hour : sortedHourKeys) {
				display.append("<tr>");
				display.append("<td>" + hour.toLowerCase() + ": </td>");
				display.append("<td>" + timeslots.get(day).get(hour) + "<td/>");
				display.append("</tr>");
			}
			display.append("</table>");
			display.append("<br/><hr/>");
		}
		gui.setTransactions(totalTransactions);
		gui.setDisplay(display.toString());

	}

	private void sortTransaction(Date testDate) {
		String day = dateConverter.format(testDate);
		String hour = hourConverter.format(testDate);
		if (!timeslots.containsKey(day))
			timeslots.put(day, new HashMap<String, Integer>());
		if (!timeslots.get(day).containsKey(hour))
			timeslots.get(day).put(hour, new Integer(0));
		timeslots.get(day).put(hour, timeslots.get(day).get(hour) + 1);
	}

	/**
	 * Display a file-chooser dialog for selecting a CSV file.
	 */
	public void pickFile() {
		int result = fileChooser.showOpenDialog(gui.getFrame());
		if (result == JFileChooser.APPROVE_OPTION) {
			gui.setFilename(fileChooser.getSelectedFile().getName());
			loadFile(fileChooser.getSelectedFile());
			updateDisplay();
		} else {
			JOptionPane.showMessageDialog(gui.getFrame(),
					"There was an error loading the file.", "Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		updateDisplay();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().compareTo("date") == 0)
			updateDisplay();
	}

	/**
	 * Display a file chooser to get a file to save, then attempt to place
	 * filtered transactions into a CSV file. The resulting file should display
	 * with days on the x-axis and hours on the y-axis, which is more neatly
	 * suited for use in the existing Library Stats spreadsheet.
	 */
	public void exportCsv() {
		int result = fileChooser.showSaveDialog(gui.getFrame());
		if (result == JFileChooser.CANCEL_OPTION)
			return;
		try {
			if (result == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				CSVWriter writer = new CSVWriter(new FileWriter(saveFile));
				writer.writeNext(new String[] { "Library Stats" });
				try {
					writer.writeNext(new String[] {
							"Begin date: ",
							dateConverter.format(gui.getBeginDateCalendar()
									.getTime()) });
				} catch (NullPointerException e) {
					writer.writeNext(new String[] { "No begin date specified" });
				}
				try {
					writer.writeNext(new String[] {
							"End date: ",
							dateConverter.format(gui.getEndDateCalendar()
									.getTime()) });
				} catch (NullPointerException e) {
					writer.writeNext(new String[] { "No end date specified" });
				}
				writer.writeNext(new String[] {
						"Contact point: " + gui.getContactPoint(),
						"READ scale: " + gui.getReadScale(),
						"Transaction type: " + gui.getTransactionType() });
				writer.writeNext(new String[] { "" });
				String[] row = new String[sortedDayKeys.size() + 1];
				row[0] = "";
				System.arraycopy(sortedDayKeys.toArray(new String[0]), 0, row,
						1, sortedDayKeys.size());
				writer.writeNext(row);
				String[] hourKeys = new String[24];
				hourKeys[0] = "12 AM";
				for (int i = 1; i < 12; i++) {
					hourKeys[i] = i + " AM";
				}
				hourKeys[12] = "12 PM";
				for (int i = 1; i < 12; i++) {
					hourKeys[12 + i] = i + " PM";
				}
				System.out.println(Arrays.toString(hourKeys));
				for (String hour : hourKeys) {
					int counter = 0;
					row = new String[sortedDayKeys.size() + 1];
					for (String day : sortedDayKeys) {
						counter++;
						row[0] = hour;
						if (timeslots.get(day).containsKey(hour)) {
							row[counter] = timeslots.get(day).get(hour)
									.toString();
						} else
							row[counter] = "";
					}
					writer.writeNext(row);
				}
				writer.close();
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		JOptionPane.showMessageDialog(gui.getFrame(),
				"An error occurred while saving the file.", "Error",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Uses the date converter defined in this StatsParser to compare two String
	 * dates against each other.
	 * 
	 * @author Zekoff
	 * 
	 */
	public class StatsDayComparator implements Comparator<String> {
		@Override
		public int compare(String date1, String date2) {
			Date d1 = null;
			Date d2 = null;
			try {
				d1 = dateConverter.parse(date1);
				d2 = dateConverter.parse(date2);
			} catch (ParseException e) {
				throw new RuntimeException();
			}
			return d1.compareTo(d2);
		}
	}

	/**
	 * Uses the hour converter defined in this StatsParser to compare two String
	 * hours against each other.
	 * 
	 * @author Zekoff
	 * 
	 */
	public class StatsHourComparator implements Comparator<String> {
		@Override
		public int compare(String hour1, String hour2) {
			Date d1 = null;
			Date d2 = null;
			try {
				d1 = hourConverter.parse(hour1);
				d2 = hourConverter.parse(hour2);
			} catch (ParseException e) {
			}
			return d1.compareTo(d2);
		}
	}

}
