package org.columba.core.htmlviewer;

import java.awt.BorderLayout;

import java.io.StringReader;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.gui.htmlviewer.api.IHTMLViewerPlugin;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.html2.HTMLElement;

public class CobraViewerPlugin extends JPanel implements
		IHTMLViewerPlugin {

	private HtmlPanel htmlPanel = new HtmlPanel();
	private HtmlRendererContext rendererContext;
	private DocumentBuilderImpl builder;

	public CobraViewerPlugin() {
		super();

		setLayout(new BorderLayout());

		rendererContext = new ColumbaHtmlRendererContext(htmlPanel);
		builder = new DocumentBuilderImpl(rendererContext.getUserAgentContext(), rendererContext);
		add(htmlPanel, BorderLayout.CENTER);

		setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
	}

	public void view(String body) {
		if (body == null)
			return;
		try {
			Document document = builder.parse(new InputSourceImpl(new StringReader(body), ""));

			htmlPanel.setDocument(document, rendererContext);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getSelectedText() {
		return htmlPanel.getSelectionText();
	}

	public boolean initialized() {
		return true;
	}

	public JComponent getComponent() {
		return htmlPanel;
	}

	public JComponent getContainer() {
		return this;
	}

	public String getText() {
		return "";
	}
	
	/**
	 * @see org.columba.core.gui.htmlviewer.api.IHTMLViewerPlugin#setCaretPosition(int)
	 */
	public void setCaretPosition(int position) {
	}

	/**
	 * @see org.columba.core.gui.htmlviewer.api.IHTMLViewerPlugin#moveCaretPosition(int)
	 */
	public void moveCaretPosition(int position) {
	}

	private static class ColumbaHtmlRendererContext extends SimpleHtmlRendererContext {
		public ColumbaHtmlRendererContext(HtmlPanel contextComponent) {
			super(contextComponent, new SimpleUserAgentContext());
		}

		public void linkClicked(HTMLElement linkNode, URL url, String target) {
			ColumbaDesktop.getInstance().browse(url);
		}
	}
}
