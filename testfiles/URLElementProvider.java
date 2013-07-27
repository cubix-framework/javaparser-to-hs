package elementmatcher.elements.internet;

import elementmatcher.AbstractRegexElementProvider;
import org.apache.commons.collections15.iterators.ObjectArrayIterator;
import org.gjt.sp.util.Log;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;

// TODO: remove = from email addresses
public class URLElementProvider extends AbstractRegexElementProvider<URI> {

    private final Pattern regex = Pattern.compile(
            // generic URL
            "(\\b(https?|ftp|gopher|telnet|file|mailto):(///?)[\\w\\d:#@%/;\\$\\(\\)~_\\?\\+\\-=\\\\\\.&]+\\b/?)" +
            "|" +
            // email address
            "(\\b[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+(?:[a-zA-Z]{2}|com|org|net|gov|mil|biz|info|mobi|name|aero|jobs|museum)\\b)");
    private final OpenURIAction openURLAction = new OpenURIAction();
    private final Action[] actions = new Action[] { openURLAction };
    private final ObjectArrayIterator<Action> actionIterator = new ObjectArrayIterator<Action>(actions);

    public URLElementProvider() {
        super("URLElementProvider");
    }

    public Pattern getRegex() {
        return regex;
    }

    protected URI getElement(MatchResult match) {
        try {
            String s = match.group();
            if (s.indexOf(':') == -1) {
                s = "mailto:" + s;
            }
            return new URI(s);
        } catch (URISyntaxException e) {
            Log.log(Log.WARNING, this, e);
        }
        return null;
    }

    public Iterator<Action> getActions(URI data) {
        openURLAction.reset(data);
        actionIterator.reset();
        return actionIterator;
    }

    public String getToolTip(URI uri) {
        return "URL: " + uri.toString();
    }

    private class OpenURIAction extends AbstractAction {

        private URI uri;

        private void reset(URI uri) {
            this.uri = uri;
            putValue(NAME, "Open " + uri);
        }

        public void actionPerformed(ActionEvent evt) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (Exception e) {
                JOptionPane.showMessageDialog((Component)evt.getSource(), "Cannot open URL: " + uri, "ElementMatcher", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

}