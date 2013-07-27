/*
 * $Revision: 15300 $
 * $Date: 2009-05-17 10:50:24 -0400 (Sun, 17 May 2009) $
 * $Author: kerik-sf $
 *
 * Copyright (C) 2001 C. Scott Willy
 * Copyright (C) 2008 Eric Le Lay
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package cswilly.jeditPlugins.spell;

import cswilly.spell.EngineManager;
import cswilly.spell.Engine;
import cswilly.spell.Validator;
import cswilly.spell.AspellEngine;
import cswilly.spell.SpellException;
import cswilly.spell.FutureListDicts;
import cswilly.spell.Dictionary;

import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.msg.PropertiesChanged;

import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.OperatingSystem;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.util.Log;

import java.io.*;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import java.util.concurrent.Future;

/**
 * keep track of running Aspell instances
 * @see cswilly.spell.AspellEngine
 * @see cswilly.spell.FutureListDicts
 */
public class AspellEngineManager implements EngineManager
{
  /** property holding the executable to invoke as Aspell */
  public static final String ASPELL_EXE_PROP                    = "spell-check-aspell-exe";
  /** property holding the way aspell has to handle modes (none/automatic/manual) */
  public static final String ASPELL_MARKUP_MODE_PROP            = "spell-check-aspell-markup-mode";
  /** property holding additional parameters */
  public static final String ASPELL_OTHER_PARAMS_PROP			= "spell-check-aspell-other-params";
  /** value for automatic mode */
  public static final String FILTER_AUTO						= "AUTO";
  /** root for properties holding the configured filter for each mode */
  public static final String FILTERS_PROP						= "spell-check-filter";					
  /** property holding the encoding used to communicate with aspell */
  public static final String ASPELL_ENCODING_PROP            = "spell-check-aspell-encoding";
  /** property holding wether to check positions for each word */
  public static final String ASPELL_CHECK_POS_PROP            = "spell-check-aspell-check-pos";

  /**
   * available modes for Aspell
   */
  public static enum AspellMarkupMode{
	  NO_MARKUP_MODE("aspellNoMarkupMode"),
	  MANUAL_MARKUP_MODE("aspellManualMarkupMode"),
	  AUTO_MARKUP_MODE("aspellAutoMarkupMode");
	  
	  AspellMarkupMode(String v){ this.value = v; }
	  
	  private final String value;
	  public String toString(){ return value; }
	  public static AspellMarkupMode fromString(String s){
		  for(AspellMarkupMode mode : AspellMarkupMode.values()){
			  if(mode.value.equals(s))return mode;
		  }
		  throw new IllegalArgumentException("Invalid mode name :"+s);
	  }
  }


  	private Map<List<String>,AspellEngine> engines;
	
	private String oldAspellExeFilename;
	private List<String> oldAspellCommandLine;
	
	public AspellEngineManager(){
		engines = new HashMap<List<String>,AspellEngine>();
	}
	
	public Engine getEngine(String mode,String language,boolean terse) throws SpellException
  {
	  String encoding = jEdit.getProperty(ASPELL_ENCODING_PROP,"UTF-8");
	  boolean check = Boolean.parseBoolean(jEdit.getProperty(ASPELL_CHECK_POS_PROP,"false"));
	  List<String> aspellCommandLine = initCommandLine(mode,language,terse,encoding);
	  String aspellExeFilename = getAspellExeFilename();
	  if(aspellExeFilename == null)throw new SpellException("Aspell executable is not defined");
	  aspellCommandLine.add(0,aspellExeFilename);
	  AspellEngine engine = engines.get(aspellCommandLine);
	if(engine == null || engine.isStopped()){

		String logStr = "command line is:"+aspellExeFilename;
		for(int i=0;i<aspellCommandLine.size();i++)logStr+=" "+aspellCommandLine.get(i);
		Log.log(Log.DEBUG,this,logStr);
	
		engine = new AspellEngine(aspellCommandLine,encoding,check);
		
		engines.put(aspellCommandLine,engine);
	}
	return engine;
  }
    
  public Validator getValidator(String mode,String language){
	  return null;
  }
  
  private List<String> initCommandLine(String mode, String language,boolean terse, String encoding){
	 String dict = language;
	 if(dict == null) dict = SpellCheckPlugin.getMainLanguage();

    // Construct aspell command line arguments
    List<String> aspellCommandLine = new ArrayList<String>(4);
	// use this option switch to prevent any encoding issue
	// available at least since aspell 0.5.3
	aspellCommandLine.add("--encoding="+encoding.toLowerCase());
	
	
	AspellMarkupMode markup = getAspellMarkupMode();
	if( AspellMarkupMode.NO_MARKUP_MODE == markup)
			aspellCommandLine.add("--mode=none");
	else if( AspellMarkupMode.MANUAL_MARKUP_MODE == markup ){
		String m = jEdit.getProperty(FILTERS_PROP+"."+mode);
		if(m!=null)
			aspellCommandLine.add("--mode="+m);
	}
	//else : AUTO : do nothing

	
    if ( !dict.equals("") ){
      aspellCommandLine.add("--lang=" + dict);
	  //can't find this option:
	  //aspellCommandLine.add("--language-tag=" + aspellMainLanguage);
	}

	if( terse){
		aspellCommandLine.add("--dont-suggest");
	}
	
	String aspellOtherParams = getAspellOtherParams();
	//TODO : fix params with spaces protected with quotes
	for(StringTokenizer st=new StringTokenizer(aspellOtherParams);st.hasMoreTokens();){
		aspellCommandLine.add(st.nextToken());
	}

    aspellCommandLine.add("pipe");
	return aspellCommandLine;
  }
  

  static
  String getAspellExeFilename()
  {
    String aspellExeFilename = jEdit.getProperty( ASPELL_EXE_PROP );

    if( aspellExeFilename == null || aspellExeFilename.equals("") )
    {
      if ( OperatingSystem.isUnix() )
        aspellExeFilename = "aspell";
      else
      {
        GUIUtilities.message(null, "spell-check-noAspellExe", null);
        String[] paths = GUIUtilities.showVFSFileDialog( null, null, JFileChooser.OPEN_DIALOG, false );

        if (paths != null)
          aspellExeFilename = paths[0];
        else
        {
          return null;
        }
      }
      jEdit.setProperty( ASPELL_EXE_PROP, aspellExeFilename );
    }

    return aspellExeFilename;
  }

  
  private static
  String getAspellOtherParams()
  {
    return jEdit.getProperty( ASPELL_OTHER_PARAMS_PROP, "");
  }

  private static
  AspellMarkupMode getAspellMarkupMode()
  {
	  try{
		  return AspellMarkupMode.fromString(jEdit.getProperty( ASPELL_MARKUP_MODE_PROP));
	  }catch(IllegalArgumentException iae){
		  return AspellMarkupMode.AUTO_MARKUP_MODE;
	  }
  }


  public Future<Vector<Dictionary>> getAlternateLangDictionaries()
  {
	  return new FutureListDicts(getAspellExeFilename());
  }
  
  public void stop(){
	  for(Engine engine:engines.values()){
		  Log.log(Log.DEBUG,this,"stopping the Aspell engine");
		  engine.stop();
	  }
	  engines.clear();
  }
   
  
  public void handleMessage(EBMessage message){
	  if(message instanceof PropertiesChanged){
		  //TODO : maybe initCommandLine() from here (but I won't have the language nor the node)
		  
		  // flush engines to take into account a change of encoding / check offset props
		  for(AspellEngine e:engines.values()){
			  e.stop();
		  }
		  engines.clear();
	  }
  }
  
  public String getDescription(){
	  return jEdit.getProperty("spell-check-aspell-engine.description");
  }
}