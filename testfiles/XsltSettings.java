/*
 *  XsltSettings.java - Represents a settings file.
 *
 *  Copyright (C) 2003 Robert McKinnon
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package xslt;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.EditPlugin;
import org.gjt.sp.util.Log;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Map;


/**
 * Represents a settings file containing the configuration for an XSLT pipeline.
 * Is able to be written to a working Ant build.xml file.
 *
 * @author Robert McKinnon - robmckinnon@users.sourceforge.net
 */
public class XsltSettings {

  private XSLTProcessor processor;


  public XsltSettings(XSLTProcessor processor) {
    this.processor = processor;
  }


  public void loadFromFile() throws ParserConfigurationException, IOException, SAXException, TransformerException {
    String[] selections = GUIUtilities.showVFSFileDialog(processor.getView(), getSettingsDirectory(), JFileChooser.OPEN_DIALOG, false);

    if(selections != null) {
      String settingsFile = selections[0];
      Log.log(Log.DEBUG, this, "settings file: " + settingsFile);

      if(settingsFile.indexOf("://") == -1) {
        settingsFile = "file:///" + settingsFile;
      }

      InputSource source = new InputSource(settingsFile);
      Document document = XPathTool.parse(source);

      String sourceFile = XPathTool.evalString(document, "/project/target[xslt][1]/xslt/@in");
      String resultFile = XPathTool.evalString(document, "/project/target/xslt[starts-with(@out, '$')=false()]/@out");
      Log.log(Log.DEBUG, this, "source: " + sourceFile);
      Log.log(Log.DEBUG, this, "result: " + resultFile);

      int stylesheetCount = XPathTool.evalCount(document, "/project/target/xslt");
      int parameterCount = XPathTool.evalCount(document, "/project/target[xslt][1]/xslt/param");
      String[] stylesheets = new String[stylesheetCount];
      String[] parameterNames = new String[parameterCount];
      String[] parameterValues = new String[parameterCount];

      for(int i = 0; i < stylesheetCount; i++) {
        stylesheets[i] = XPathTool.evalString(document, "/project/target[xslt][" + (i + 1) + "]/xslt/@style");
        Log.log(Log.DEBUG, this, "stylsheet " + i + ": " + stylesheets[i]);
      }

      for(int i = 0; i < parameterCount; i++) {
        String parameterXPath = "/project/target[xslt][1]/xslt/param[" + (i + 1) + "]";
        parameterNames[i] = XPathTool.evalString(document, parameterXPath + "/@name");
        parameterValues[i] = XPathTool.evalString(document, parameterXPath + "/@expression");
        Log.log(Log.DEBUG, this, "parameter: " + parameterNames[i] + "=" + parameterValues[i]);
      }
      
      processor.getInputSelectionPanel().setSourceFile(sourceFile);
      
      // special case if "New Untitled Buffer" was checked
      if(resultFile.matches(".*Untitled-\\d+")) {
      	  processor.getResult().setFileSelected(false);
      } else {
      	  processor.getResult().setFileSelected(true);
      	  processor.getResult().setSourceFile(resultFile);
      }

      processor.setStylesheets(stylesheets);
      processor.setStylesheetParameters(parameterNames, parameterValues);
    }
  }


  public void writeToFile() throws IOException {
    if(!processor.getStylesheetPanel().stylesheetsExist()) {
      XSLTPlugin.showMessageDialog("xslt.settings.message.no-stylesheets", processor);
    } else if(!processor.getInputSelectionPanel().isSourceFileDefined()) {
      XSLTPlugin.showMessageDialog("xslt.settings.message.no-source", processor);
    } else if(!processor.getResult().isSourceFileDefined()) {
      XSLTPlugin.showMessageDialog("xslt.settings.message.no-result", processor);
    } else {
      String settingsDirectory = getSettingsDirectory();
      String sourceName = MiscUtilities.getFileNameNoExtension(processor.getInput());
      String resultName = MiscUtilities.getFileNameNoExtension(processor.getResultFile());
      String settingsFile = settingsDirectory + sourceName + "-" + resultName + "-settings.xml";

      String[] selections = GUIUtilities.showVFSFileDialog(processor.getView(), settingsFile, JFileChooser.SAVE_DIALOG, false);

      if(selections != null) {
        String settings = getSettings();
        Log.log(Log.DEBUG, this, settings);

        settingsFile = selections[0];
        Log.log(Log.DEBUG, this, "settings file: " + settingsFile);

        PrintWriter writer = new PrintWriter(new FileWriter(settingsFile));

        writer.print(settings);
        writer.close();
      }
    }
  }


  private String getSettingsDirectory() {
    File settingsDirectory = EditPlugin.getPluginHome(XSLTPlugin.class);

    if(!settingsDirectory.exists()) {
      settingsDirectory.mkdir();
    }

    return settingsDirectory.getPath() + File.separatorChar;
  }


  private String getSettings() {
    StringBuffer buffer = new StringBuffer("<?xml version=\"1.0\"?>\n");
    String resultName = MiscUtilities.getFileNameNoExtension(processor.getResultFile());
    Object[] stylesheets= processor.getStylesheets();
    int stylesheetCount = stylesheets.length;

    buffer.append("<!--" + jEdit.getProperty("xslt.settings.file.declaration") + "-->\n");
    buffer.append("<project name=\"" + resultName + ".pipeline\" default=\"pipe." + (stylesheetCount-1) + "\">\n\n");
    appendProperties(stylesheetCount-1, resultName, buffer);
    appendTargets(buffer, stylesheets);
    buffer.append("</project>\n");

    return buffer.toString();
  }


  private void appendTargets(StringBuffer buffer, Object[] stylesheets) {
    appendInitTarget(buffer);
    
    for(int i = 0; i < stylesheets.length; i++) {
      int last = (i - 1);
      String lastTarget = (i == 0) ? "init" : "pipe." + last;
      buffer.append("  <target name=\"pipe." + i + "\" depends=\"" + lastTarget + "\">\n");

      String in = (i == 0) ?  processor.getInput() : "${temp." + last + "}";
      String out = (i == stylesheets.length-1) ? processor.getResultFile() : "${temp." + i + "}";
      String stylesheet = (String)stylesheets[i];

      buffer.append("    <xslt in=\"" + in + "\" out=\"" + out + "\" style=\"" + stylesheet);

      if(i == 0) {
        buffer.append("\">\n");
        appendStylesheetParameters(buffer);
        buffer.append("    </xslt>\n");
      } else {
        buffer.append("\"/>\n");
      }

      buffer.append("  </target>\n\n");
    }
  }


  private void appendStylesheetParameters(StringBuffer buffer) {
  	  Map<Object,Object> params = processor.getParameterPanel().getMap();
  	  for(Map.Entry<Object,Object> en: params.entrySet()) {
      String name = (String)en.getKey();
      String value = (String)en.getValue();
      buffer.append("      <param name=\"" + name + "\" expression=\"" + value + "\"/>\n");
    }
  }


  private void appendInitTarget(StringBuffer buffer) {
    buffer.append("  <target name=\"init\">\n");
    buffer.append("    <!--<touch file=\"" + processor.getInput() + "\"/>-->\n");
    buffer.append("  </target>\n\n");
  }


  private String getStylesheet(int i) {
    String stylesheet = processor.getStylesheetPanel().getStylesheet(i);
    return stylesheet;
  }


  private void appendProperties(int lastIndex, String resultName, StringBuffer buffer) {
    String resultExtension = MiscUtilities.getFileExtension(processor.getResultFile());
    String resultPath = MiscUtilities.getParentOfPath(processor.getResultFile());

    for(int i = 0; i < lastIndex; i++) {
      String name = "temp." + i;
      String tempResult = resultPath + resultName + "-temp-" + i + resultExtension;
      buffer.append("  <property name=\"" + name + "\" value=\"" + tempResult + "\"/>\n");
    }

    buffer.append("\n");
  }


}