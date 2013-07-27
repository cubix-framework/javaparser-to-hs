package recentbuffer;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;

/**
 * The RecentBufferSwitcher dialog box
 *
 * @author Michael Thornhill
 * @version   $Revision: 1.1.1.1 $ $Date: 2005/10/06 13:51:34 $
 */
@SuppressWarnings("serial")
public class RecentBuffer extends JDialog implements KeyListener, MouseListener {
	private View view;
	private JList bufferList;
	private BufferAccessMonitor bufAccessObj = null;

	/**
	 * Default Constructor for the <tt>RecentBuffer</tt> object
	 */
	public RecentBuffer(View view, BufferAccessMonitor debufAccessObj) {
		super(view, null, true);
		this.setUndecorated(true);
		this.bufAccessObj = debufAccessObj;
		this.view = view;		
		this.createLayout();	
	}
	
	/**
	 * create and show the buffer switcher dialog
	 */
	public void createLayout() {
		JPanel panel = new JPanel(new BorderLayout());
		setFocusTraversalKeysEnabled(false);
        this.setContentPane(panel);        
		Buffer currBuff = view.getBuffer();		
		Buffer [] tempBufferList = bufAccessObj.getBufferList(currBuff);
		// render this list
        bufferList = new JList(tempBufferList);
        bufferList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		BufferCellRenderer buffRenderer = new BufferCellRenderer();
        bufferList.setCellRenderer(buffRenderer);
		int maxRowsFromProps = jEdit.getIntegerProperty(RecentBufferSwitcherPlugin.OPTION_PREFIX+ "numberofvisiblerows");
        bufferList.setVisibleRowCount( Math.min( tempBufferList.length, maxRowsFromProps ) );
		JScrollPane scrollPane = new JScrollPane(bufferList);
        this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		bufferList.setFocusTraversalKeysEnabled(false);
		bufferList.addKeyListener(this);
		bufferList.addMouseListener(this);
		this.pack();
		bufferList.setSelectedIndex(0);		
		this.setLocationRelativeTo(this.view);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);		
        this.setVisible(true);		
	}
	
	/**
	 * Switch to selected buffer
	 */
	public void switchToBuffer() {
		Buffer b = (Buffer)bufferList.getSelectedValue();
		if (b == null) {
			view.getToolkit().beep();
		} else {
			view.getEditPane().setBuffer(b);
		}		
	}

	/**
	 * close selected buffer
	 */
	public void closeBuffer() {
		int index = bufferList.getSelectedIndex();
		Buffer b = (Buffer)bufferList.getSelectedValue();
		if (b==null) {
			view.getToolkit().beep();
		} else {
			if (jEdit.closeBuffer(view, b) == true) {
				bufferList.setListData(jEdit.getBuffers());
				// in case last item was removed
				if (index == bufferList.getModel().getSize()) {
					index -=1;
				}
				bufferList.setSelectedIndex(index);
			}
		}
	}
	
	
	public void keyPressed(java.awt.event.KeyEvent evt)	{
		if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            close();
		}
        else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            switchToBuffer();
            close();
		}
        else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            switchToBuffer();		
		}
        else if (evt.getKeyCode() == KeyEvent.VK_TAB || evt.getKeyChar()=='`') {
            int modifiers = evt.getModifiersEx();
            int curIndex = bufferList.getSelectedIndex();
            if (modifiers == 128) { // if modifier is ctrl
                if (curIndex < bufferList.getModel().getSize()-1) {					
					curIndex = curIndex + 1 ;
				} else {
					curIndex = 0;
				}				
			} else { // if modifier is ctrl - shift (or anything else)
				if (curIndex > 0) {
					curIndex = curIndex - 1;
				} else {
					curIndex = bufferList.getModel().getSize()-1;
				}
			}
            bufferList.setSelectedIndex(curIndex);
		}			
        else if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            evt.consume();
            closeBuffer();
		}     
	}
	
	
	/**
	 * close the buffer switcher dialog
	 */	
	public void close() {
        view = null;
        dispose();	
	}
	
	public void keyReleased(java.awt.event.KeyEvent evt) {
		//System.out.println("key released is "+evt);
		if (evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
            close();
		}
        else if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
            switchToBuffer();
            close();
		}
        int selected = bufferList.getSelectedIndex();
        if (selected > -1)
            bufferList.ensureIndexIsVisible(selected);   
	}
	
	public void keyTyped(java.awt.event.KeyEvent action){}	
	public void mouseExited(java.awt.event.MouseEvent action){}
	public void mouseEntered(java.awt.event.MouseEvent action){}	
	public void mouseReleased(java.awt.event.MouseEvent action){}	
	public void mousePressed(java.awt.event.MouseEvent action){}
	
	public void mouseClicked(java.awt.event.MouseEvent action) {
		//if (action.getClickCount() > 1) {
            switchToBuffer();
            close();
		//}     
	}	
}
