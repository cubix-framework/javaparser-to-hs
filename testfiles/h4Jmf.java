/*
h4Jmf.java
program is free software GNU General Public License
author Herman Vierendeels,Ternat,Belgium
*/
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.EventQueue;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.*;

import javax.media.*;
import javax.media.protocol.*;

import javax.swing.*;

import org.gjt.sp.jedit.*;
import org.gjt.sp.jedit.buffer.*;
import org.gjt.sp.jedit.msg.*;
import org.gjt.sp.jedit.textarea.*;

import be.dekamer.programs.allerlei.Console;

public class h4Jmf extends JPanel
implements h4JmfActions,EBComponent
{
 private static final Logger logger=Logger.getLogger(h4Jmf.class.getName());
 volatile private static int count=0;

 private View view;

 //private URL url;
 //private MediaLocator mediaLocator;
 //private DataSource ds=null;
 // no more commands if command_busy set!!! nog doen???
 //private Time duration=null;
 //private Calendar clndr=Calendar.getInstance();

 //boolean jmf_ok=false;
 //this is really controlled by h4JmfPlugin having references to JMF-classes. if JMF is not installed , plugin will not load

 public h4Jmf()
 {
  logger.info("no-args constructor");
 }//

 public h4Jmf(View view,String position)
 {
  super();
  count++;
  logger.info("args constructor "+count);

  this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
  //logger.info("view="+view+" position="+position);
  this.view=view;
  //position.equals(DockableWindowManager.FLOATING);
  //cnsl=new Console(100);
  h4JmfPlugin.cnsl.setPreferredSize(new Dimension(500,200));
  JScrollPane jscrllpn=new JScrollPane(h4JmfPlugin.cnsl);
  //jscrllpn.setPreferredSize(new Dimension(500,200));
  //add(BorderLayout.SOUTH,jscrllpn);
  add(jscrllpn);

  if(h4JmfPlugin.playMP3!=null)
  {	  
   java.awt.Component cpc=h4JmfPlugin.playMP3.getControlPanelComponent();
   //logger.info("cpc="+cpc);
   if(cpc!=null)//after panel has been closed, new instance !!
   {
    cpc.setPreferredSize(new Dimension(500,50));
    this.add(cpc);
   }
  }
  this.setPreferredSize(new Dimension(500,300));
  //cnsl.append("begin");
  //cnsl.append("SettingsDirectory="+jEdit.getSettingsDirectory());
  h4JmfPlugin.cnsl.append("args constructor "+count);
  //EditBus.addToBus(this);//moved to notify
  /*************************
  //check a few jmf-classes begin done in h4JmfPlugin
  Class tst=null;
  try
  {
   tst=Class.forName("com.sun.media.codec.audio.mp3.JavaDecoder");
   jmf_ok=true;
   cnsl.append("jmf Java Media Framework seems to be installed.");
  }
  catch(Exception excptn)
  {
   cnsl.append("trying presence of jmf");
   cnsl.append(excptn);
   cnsl.append("jmf Java Media Framework does not seem to be installed!");
   cnsl.append("see README for more info");
  }
  //check a few jmf-classes end
  ****/
  if(! h4JmfPlugin.jmf_ok)
  {
   h4JmfPlugin.cnsl.append("JMF Java Media Framework does not seem to be installed!");
   h4JmfPlugin.cnsl.append("see README for more info");
  }
  logger.info("constructor end");
 }//constructor


 public void chooseFile()
 {
  if(! h4JmfPlugin.jmf_ok) return;
  if(h4JmfPlugin.playMP3!=null)
  {
   h4JmfPlugin.cnsl.append("playMP3!=null");
   return;
  }

  String tmpdir=System.getProperty("java.io.tmpdir");
  String[] paths=GUIUtilities.showVFSFileDialog(view,tmpdir+File.separator,JFileChooser.OPEN_DIALOG,false);
  //if(paths!=null && !paths[0].equals(filename))
  if(paths!=null)
  {
   String filename=paths[0];
   h4JmfPlugin.cnsl.append("filename="+filename);
   try
   {
    URL url=new URL("file://"+filename);
    player_begin(url);
   }
   catch(Exception e)
   {
    logger.severe(e.getMessage());
    h4JmfPlugin.cnsl.append(e);
   }
  }
 }//chooseFile

 public void player_begin(URL url)
 {
  if(h4JmfPlugin.playMP3!=null)
  {
   logger.severe("playMP3!=null");
   h4JmfPlugin.cnsl.append("playMP3!=null");
   return;
  }
  if(url==null)
  {
   h4JmfPlugin.cnsl.append("url==null");
   return;
  }
  MediaLocator mediaLocator=new MediaLocator(url); 
  try
  {
   //final JPanel jpnl_this=this;

   DataSource ds=Manager.createDataSource(mediaLocator);
   //cnsl.append("ds="+ds);
   h4JmfPlugin.playMP3=Manager.createPlayer(ds);
   /************************************************************
   ControllerListener moved to outer class
   addControllerListener done in h4JmfPlugin
   ************************************/
   h4JmfPlugin.player_begin();
  }
  catch(Exception e)
  {
   logger.severe(e.getMessage());
   h4JmfPlugin.cnsl.append(e);
   return;
  }
  //h4JmfPlugin.playMP3.realize();
  //logger.info("after realize()"); 
  //but possible [JMF thread: com.sun.media.PlaybackEngine@1ac13d7[ com.sun.media.PlaybackEngine@1ac13d7 ] ( realizeThread)] [error] PlaybackEngine@1ac13d7 ] ( realizeThread):   Unable to handle format: mpeglayer3, 16000.0 Hz, 16-bit, Mono, LittleEndian, Signed, 2000.0 frame rate, FrameSize=16384 bits
  //running tshvr under hedwig :11:17:08 PM [JMF thread: com.sun.media.content.unknown.Handler@8c7be5 ( prefetchThread)] [error] Handler@8c7be5 ( prefetchThread): Error: Unable to prefetch com.sun.media.PlaybackEngine@6d3b92
 }//player_begin
 public void player_close()
 {
  h4JmfPlugin.cnsl.append("trying to close()");
  if(h4JmfPlugin.playMP3!=null)
  {
   java.awt.Component cpc=h4JmfPlugin.playMP3.getControlPanelComponent();
   //nog doen static h4Jmf.player_close() !!!! playMP3 will be set to null there !!!
   //h4JmfPlugin.playMP3.close();
   h4JmfPlugin.cnsl.append("close()");
   if(cpc!=null)//after panel has been closed, new instance !!
   {
    this.remove(cpc);
   }
   h4JmfPlugin.player_close();
  }
  //cpc=null;
  //h4JmfPlugin.playMP3=null;
 }//
 //methods start,stop,forward,rewind,insert-time,position moved as static to h4JmfPlugin
 //kleine delay inbouwen forward,rewind ???,sometimes player does not respind well ?? why??
 //EBComponent interface
 public void handleMessage(EBMessage message)
 {
  logger.info(message.toString());
  if(message instanceof PropertiesChanged)
  {
   //propertiesChanged();
  }
  //AWT-EventQueue-0: INFO: EditorExiting[source=null]
  else if(message instanceof org.gjt.sp.jedit.msg.PluginUpdate)
  {
   Object what=((PluginUpdate)message).getWhat();
   if(what.equals(PluginUpdate.DEACTIVATED))
   {
    //if(h4JmfPlugin.playMP3!=null) h4JmfPlugin.playMP3.close();
    //h4JmfPlugin.playMP3=null;
    logger.info("close done in h4JmfPlugin");
   }
  }//PluginUpdate
 }//handleMessage
 //JComponent methods
 /****if using these without call to super,view empty! no cnsl, no cpc*/
 public void addNotify()
 {
  logger.info("");
  super.addNotify();
  EditBus.addToBus(this);
 }
 public void removeNotify()
 {
  logger.info("");
  super.removeNotify();
  EditBus.removeFromBus(this);
 }
 //JComponent methods end
}//h4Jmf
/************
DockableWindowUpdate[what=ACTIVATED,dockable=h4Jmf,source=org.gjt.sp.jedit.gui.DockableWindowManagerImpl[,0,0,1024x669,layout=org.gjt.sp.jedit.gui.DockableLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=9,maximumSize=,minimumSize=,preferredSize=]]

EditPaneUpdate[what=DESTROYED,source=org.gjt.sp.jedit.EditPane[active,editpane]]

PluginUpdate[what=UNLOADED,exit=true,source=/downloads/jEdit/4.3pre16/jars/LatestVersion.jar,class=LatestVersionPlugin]


 javax.media.NotRealizedError: Cannot get control panel component on an unrealized player
**************/
