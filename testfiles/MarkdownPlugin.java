package ua.pico.jedit.markdown;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.util.Log;

import org.pegdown.PegDownProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.MessageFormat;
import javax.swing.JOptionPane;

import infoviewer.InfoViewerPlugin;

public class MarkdownPlugin extends EditPlugin {

	public static final String NAME = "markdown";
	public static final String OPTION_PREFIX = "options.markdown.";

	public MarkdownPlugin() {
		super();
	}

	public void renderBuffer(final View view, final Buffer markdownBuffer) {
		renderBuffer(view, markdownBuffer, null);
	}

	public void renderSelection(final View view, final Buffer markdownBuffer, final Selection[] selections) {
		renderSelection(view, markdownBuffer, selections, null);
	}

	public void previewBuffer(final View view, final Buffer markdownBuffer) {
		renderBuffer(view, markdownBuffer, Target.Browser);
	}

	public void previewSelection(final View view, final Buffer markdownBuffer, final Selection[] selections) {
		renderSelection(view, markdownBuffer, selections, Target.Browser);
	}

	public enum Target {
		Buffer,
		Clipboard,
		Browser;
	}
	
	private static final String MODE = "html";

	private void renderBuffer(final View view, final Buffer markdownBuffer, Target target) {
		final MarkdownUtil util = MarkdownUtil.getInstance();
		final PegDownProcessor processor = util.getProcessor();
		String text = markdownBuffer.getText(0, markdownBuffer.getLength());

		if (0 == text.length()) {
			view.getToolkit().beep();
			Log.log(Log.WARNING, MarkdownPlugin.class, "Buffer is empty.");
			JOptionPane.showMessageDialog(null, "Buffer is empty.", "Markdown Plugin", JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (null == target) {
			target = util.getTarget();
		}
		text = processor.markdownToHtml(text);
		switch (target) {
		case Clipboard:
			saveToClipboard(text);
			break;
		case Browser:
			showPreview(view, markdownBuffer, text);
			break;
		case Buffer:
		default:
			saveToBuffer(view, text);
		}
	}

	private void renderSelection(final View view, final Buffer markdownBuffer, final Selection[] selections, Target target) {
		final String newLine = "\n";
		final MarkdownUtil util = MarkdownUtil.getInstance();
		final PegDownProcessor processor = util.getProcessor();
		final StringBuilder selected = new StringBuilder();
		
		String text;

		if (0 == selections.length) {
			view.getToolkit().beep();
			Log.log(Log.WARNING, MarkdownPlugin.class, "Selection is empty.");
			JOptionPane.showMessageDialog(null, "No selected text.", "Markdown Plugin", JOptionPane.WARNING_MESSAGE);

			return;
		}

		if (null == target) {
			target = util.getTarget();
		}
		for (Selection selection : selections) {
			text = markdownBuffer.getText(selection.getStart(), selection.getEnd() - selection.getStart());
			selected.append(text);
			if (!text.endsWith(newLine)) {
				selected.append(newLine);
			}
		}
		text = processor.markdownToHtml(selected.toString());
		switch (target) {
		case Clipboard:
			saveToClipboard(text);
			break;
		case Browser:
			showPreview(view, markdownBuffer, text);
			break;
		case Buffer:
		default:
			saveToBuffer(view, text);
		}
	}

	private void saveToBuffer(final View view, final String text) {
		final Buffer htmlBuffer = jEdit.newFile(view);

		Log.log(Log.DEBUG, MarkdownPlugin.class, "Render to a new buffer.");
		htmlBuffer.insert(0, text);
		htmlBuffer.setMode(MODE);
		view.setBuffer(htmlBuffer);
	}

	private void saveToClipboard(final String text) {
		final Registers.ClipboardRegister clipboard = (Registers.ClipboardRegister) Registers.getRegister('$');

		Log.log(Log.DEBUG, MarkdownPlugin.class, "Render to clipboard.");
		clipboard.setValue(text);
	}

	private void showPreview(final View view, final Buffer buffer, final String text) {
		final String html_prologue = "<DOCTYPE html><html><head><meta charset=\"{0}\"/><title>{1}</title></head><body>";
		final String html_epilogue = "</body></html>";
		final InfoViewerPlugin browser = (InfoViewerPlugin) jEdit.getPlugin("infoviewer.InfoViewerPlugin");
		final String charset = buffer.getStringProperty(buffer.ENCODING);
		String name;
		File html = null;
		Writer writer;
		StringBuilder builder = new StringBuilder();

		if (null == browser) {
			final String message = "InfoViewer plugin not found.";

			view.getToolkit().beep();
			Log.log(Log.ERROR, MarkdownPlugin.class, message);
			JOptionPane.showMessageDialog(null, message, "Markdown Plugin", JOptionPane.ERROR_MESSAGE);

			return;
		}

		if (buffer.isUntitled()) {
			name = "Markdown text";
		} else {
			name = buffer.getName();
		}
		try {
			html = File.createTempFile(name, "." + MODE, new File(buffer.getDirectory()));
			writer = new OutputStreamWriter(new FileOutputStream(html), charset);
			builder.append(MessageFormat.format(html_prologue, charset, name));
			builder.append(text).append(html_epilogue);
			writer.write(builder.toString());
			writer.close();
			Log.log(Log.DEBUG, MarkdownPlugin.class, "Preview in browser.");
			browser.openURL(view, html.toURI().toURL().toString());
		} catch (IOException ioex) {
			final String message = "Cannot create a temporary file: " + ioex.getMessage();

			view.getToolkit().beep();
			Log.log(Log.ERROR, MarkdownPlugin.class, message);
			JOptionPane.showMessageDialog(null, message, "Markdown Plugin", JOptionPane.ERROR_MESSAGE);

			return;
		} finally {
			if (null != html) {
				html.deleteOnExit();
			}
		}
		
	}

}

