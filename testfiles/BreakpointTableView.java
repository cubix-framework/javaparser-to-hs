package perl.breakpoints;

import perl.breakpoints.BreakpointList.BreakpointListListener;
import perl.core.CommandManager;
import perl.core.Debugger;
import perl.core.GdbState;
import perl.core.GdbView;
import perl.core.Parser.GdbResult;
import perl.core.Parser.ResultHandler;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class BreakpointTableView extends GdbView {

	private static final String ENABLED_COLUMN_NAME = "Enabled";
	private static final String LINE_COLUMN_NAME = "Line";
	private static final String FILE_COLUMN_NAME = "File";

	@Override
	public void running() {
		createModel();
	}

	@Override
	public void sessionEnded() {
		// TODO Auto-generated method stub
		super.sessionEnded();
	}

	@Override
	public void update() {
		model.update();
	}

	public static class BreakpointTableModel extends AbstractTableModel {

		Vector<String> columns = new Vector<String>();
		Vector<Vector<String>> rows = new Vector<Vector<String>>();
		boolean structureChanged;
		
		public BreakpointTableModel() {
			columns.add(ENABLED_COLUMN_NAME);
			columns.add(FILE_COLUMN_NAME);
			columns.add(LINE_COLUMN_NAME);
			update();
		}

		private void update() {
			rows.clear();
			CommandManager cmdMgr = Debugger.getInstance().getCommandManager();
			if (cmdMgr == null)
				return;
			cmdMgr.add("-break-list", new ResultHandler() {
				@SuppressWarnings("unchecked")
				public void handle(String msg, GdbResult res) {
					if ((! msg.equals("done")) || res == null)
						return;
					structureChanged = false;
					// Reading the headers only for the order
					Object hdr = res.getValue("BreakpointTable/hdr");
					if (hdr != null && hdr instanceof Vector) {
						Vector<Object> cols = (Vector<Object>)hdr;
						for (int i = 0; i < cols.size(); i++) {
							Object col = cols.get(i);
							if (col instanceof Hashtable) {
								Hashtable<String, Object> colHash =
									(Hashtable<String, Object>)col;
								String colname = capitalize(
										(String)colHash.get("col_name"));
								if (! columns.contains(colname)) {
									columns.add(colname);
									structureChanged = true;
								}
							}
						}
					}
					Object body = res.getValue("BreakpointTable/body");
					if (body != null && body instanceof Vector) {
						Vector<Object> breakpoints = (Vector<Object>)body;
						for (int i = 0; i < breakpoints.size(); i++) {
							Object brk = breakpoints.get(i);
							if (brk instanceof Hashtable) {
								Hashtable<String, Object> brkHash =
									(Hashtable<String, Object>)brk;
								Object bkpt = brkHash.get("bkpt");
								if (bkpt instanceof Hashtable) {
									Hashtable<String, Object> bkptHash =
										(Hashtable<String, Object>)(bkpt);
									Vector<String> row = new Vector<String>();
									for (int j = 0; j < columns.size(); j++)
										row.add("");
									rows.add(row);
									Enumeration<String> k = bkptHash.keys();
									while (k.hasMoreElements()) {
										String key = k.nextElement();
										String capKey = capitalize(key);
										String val = (String) bkptHash.get(key);
										int colIndex = columns.indexOf(capKey);
										if (colIndex < 0) {
											columns.add(capKey);
											structureChanged = true;
											row.add("");
											colIndex = columns.size() - 1;
										}
										row.set(colIndex, val);
									}
								}
							}
						}
					}
					synchronized(columns) {
						columns.notify();
					}
					if (structureChanged)
						BreakpointTableModel.this.fireTableStructureChanged();
					else
						BreakpointTableModel.this.fireTableDataChanged();
					/*
^done,BreakpointTable={nr_rows="2",nr_cols="6",
hdr=[{width="3",alignment="-1",col_name="number",colhdr="Num"},
{width="14",alignment="-1",col_name="type",colhdr="Type"},
{width="4",alignment="-1",col_name="disp",colhdr="Disp"},
{width="3",alignment="-1",col_name="enabled",colhdr="Enb"},
{width="10",alignment="-1",col_name="addr",colhdr="Address"},
{width="40",alignment="2",col_name="what",colhdr="What"}],
body=[bkpt={number="1",type="breakpoint",disp="keep",enabled="y",
addr="0x000100d0",func="main",file="hello.c",line="5",times="0"},
bkpt={number="2",type="breakpoint",disp="keep",enabled="y",
addr="0x00010114",func="foo",file="hello.c",fullname="/home/foo/hello.c",
line="13",times="0"}]}
					 */
				}
			});
		}

		public Class<?> getColumnClass(int arg0) {
			if (columns.get(arg0).equals(ENABLED_COLUMN_NAME)) {
				return Boolean.class;
			}
			return String.class;
		}

		public int getColumnCount() {
			synchronized(columns) {
				if (columns.size() == 0) {
					try {
						columns.wait(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return columns.size();
		}

		private String capitalize(String s) {
			if (s == null)
				return s;
			return s.substring(0, 1).toUpperCase() + s.substring(1);
		}
		public String getColumnName(int i) {
			return columns.get(i);
		}

		public int getRowCount() {
			if (getColumnCount() == 0)
				return 0;
			return rows.size();
		}

		public Object getValueAt(int row, int col) {
			if (row >= rows.size())
				return null;
			Vector<String> r = rows.get(row);
			if (col >= r.size())
				return null;
			if (columns.get(col).equals(ENABLED_COLUMN_NAME)) {
				return new Boolean(r.get(col).equals("y"));
			}
			return r.get(col);
		}

		public boolean isCellEditable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		public void setValueAt(Object arg0, int arg1, int arg2) {
			// TODO Auto-generated method stub

		}

	}

	private BreakpointTableModel model = null;
	private JTable table;
	
	public BreakpointTableView() {
		setLayout(new BorderLayout());
		table = new JTable();
		//table.setAutoCreateRowSorter(true);
		add(new JScrollPane(table), BorderLayout.CENTER);
		if (GdbState.isRunning())
			createModel();
		BreakpointList.getInstance().addListListener(new BreakpointListListener() {
			private void update() {
				if (model != null)
					model.update();
			}
			public void breakpointAdded(Breakpoint bp) {
				update();
			}
			public void breakpointChanged(Breakpoint bp) {
				update();
			}
			public void breakpointRemoved(Breakpoint bp) {
				update();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					jumpToSelected();
				} else {
					super.mouseClicked(e);
				}
			}
		});
	}

	private void jumpToSelected() {
		int index = table.getSelectedRow();
		int fileCol = model.findColumn(FILE_COLUMN_NAME);
		int lineCol = model.findColumn(LINE_COLUMN_NAME);
		Debugger.getInstance().getFrontEnd().setCurrentLocation(
				(String)model.getValueAt(index, fileCol),
				Integer.parseInt((String)model.getValueAt(index, lineCol)));
	}
	private void createModel() {
		if (model != null)
			return;
		model = new BreakpointTableModel();
		table.setModel(model);
	}
}
