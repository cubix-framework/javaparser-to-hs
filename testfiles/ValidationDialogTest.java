/*
* $Revision: 20171 $
* $Date: 2011-10-30 05:05:25 -0400 (Sun, 30 Oct 2011) $
* $Author: kerik-sf $
*
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

package cswilly.spell;


//{{{ Imports

import java.io.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;

//{{{ 	jEdit
import org.gjt.sp.jedit.*;

//}}}

//{{{	junit
import org.junit.*;
import static org.junit.Assert.*;
//}}}

//{{{	FEST...
import org.fest.swing.fixture.*;
import org.fest.swing.core.*;
import org.fest.swing.finder.WindowFinder;
//}}}

///}}}

import org.gjt.sp.jedit.testframework.TestUtils;
import static org.gjt.sp.jedit.testframework.TestUtils.*;


/**
 * Test the functions of ValidationDialog
 *	- displays a result correctly
 *	- list of suggestions
 *	- result of button-clicking
 */
public class ValidationDialogTest{
	@BeforeClass
	public static void setUpFrame(){
		TestUtils.setUpjEdit();
		TestUtils.robot().settings().delayBetweenEvents(200);
	}

	@AfterClass
	public static void tearDownjEdit(){
		TestUtils.tearDownjEdit();
	}

	
	@Test
	public void testDisplay(){
		final ValidationDialog valid = new ValidationDialog(TestUtils.view());

		final Result resS  = new Result(5,Result.SUGGESTION,Arrays.asList(new String[]{"wick","quick","Vick"}),"qwick");
		
		Thread spellThread = new Thread(){
			public void run(){
				try{
				valid.showAndGo(resS,new MockCallback());
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		DialogFixture spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(2000).using(TestUtils.robot());
		spellDialog.textBox("originalWord").requireText("qwick");
		spellDialog.textBox("changeTo").requireText("wick");

		List<String> sugg = Arrays.asList(spellDialog.list().contents());
		List<String> origSugg = resS.getSuggestions();

		assertEquals(origSugg.size(),sugg.size());

		for(int i=0;i<origSugg.size();i++)
			assertTrue(sugg.contains(origSugg.get(i)));
		
		spellDialog.button("Change").click();
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());

		final Result resNONE  = new Result(5,Result.NONE,null,"qwack");

		spellThread = new Thread(){
			public void run(){
				try{
				valid.showAndGo(resNONE,new MockCallback());
				}catch(SpellException spe){}
			}
		};
		spellThread.start();

		spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		spellDialog.textBox("originalWord").requireText("qwack");
		spellDialog.textBox("changeTo").requireText("qwack");

		sugg = Arrays.asList(spellDialog.list().contents());

		assertEquals(1,sugg.size());
		
		spellDialog.button("Change").click();
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
	}

	@Test
	public void testList(){
		final ValidationDialog valid = new ValidationDialog(TestUtils.view());

		final Result resS  = new Result(5,Result.SUGGESTION,Arrays.asList(new String[]{"wick","quick","Vick"}),"qwick");
		
		final AtomicReference<String>change = new AtomicReference<String>(null);
		Thread spellThread = new Thread(){
			public void run(){
				try{
				valid.showAndGo(resS,
					new MockCallback(){
						public Result change(String newWord){
							change.set(newWord);
							return null;
						}
					});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		DialogFixture spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());

		try{Thread.sleep(2000);}catch(InterruptedException ie){}
		spellDialog.textBox("originalWord").requireText("qwick");
		spellDialog.textBox("changeTo").requireText("wick");
		
		spellDialog.list().selectItem("quick");
		spellDialog.textBox("changeTo").requireText("quick");
		spellDialog.list().selectItem("wick");
		spellDialog.textBox("changeTo").requireText("wick");

		spellDialog.button("Change").click();
		try{Thread.sleep(2000);}catch(InterruptedException ie){}
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertEquals("wick",change.get());
		assertTrue("validation didn't return", !spellThread.isAlive());
	}
	
	/** this one is very weak...
	 */
	@Test
	public void testReturn(){
		final ValidationDialog valid = new ValidationDialog(TestUtils.view());

		final Result resS  = new Result(5,Result.SUGGESTION,Arrays.asList(new String[]{"wick","quick","Vick"}),"qwick");
		
		final AtomicReference<Boolean> action = new AtomicReference<Boolean>(null);
		
		Thread spellThread = new Thread(){
			public void run(){
				try{
				valid.showAndGo(resS,
					new MockCallback(){
						public Result change(String s){action.set(Boolean.TRUE);return null;}
					});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		DialogFixture spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		
		spellDialog.button("Change").click();
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
				
		assertEquals(Boolean.TRUE,action.get());

		action.set(null);

		spellThread = new Thread(){
			public void run(){
				try{
					valid.showAndGo(resS,
						new MockCallback(){
							public Result add(){action.set(Boolean.TRUE);return null;}
						});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		
		spellDialog.button("Add").click();
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
				
		assertEquals(Boolean.TRUE,action.get());

		// Test dialog closed
		action.set(null);
		
		spellThread = new Thread(){
			public void run(){
				try{
					valid.showAndGo(resS,
						new MockCallback(){
							public boolean cancel(){
								action.set(Boolean.TRUE);
								return true;
							}
						});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		
		spellDialog.close();
		// try{
		// 	Thread.sleep(5000);
		// }catch(InterruptedException ie){}
		
		
		// spellDialog.close();
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
				
		assertEquals(Boolean.TRUE,action.get());
		System.err.println("test escape");
		//test Escape
		action.set(null);
		spellThread = new Thread(){
			public void run(){
				try{
					valid.showAndGo(resS,
						new MockCallback(){
							private boolean firstTime=true;
							public boolean cancel(){
								if(firstTime){
									firstTime = false;
									return false;
								}
								action.set(Boolean.TRUE);
								return true;
							}
						});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		
		TestUtils.robot().pressAndReleaseKey(KeyEvent.VK_ESCAPE,0);
		 try{
			Thread.sleep(1000);
		}catch(InterruptedException ie){}
		
		TestUtils.robot().pressAndReleaseKey(KeyEvent.VK_ESCAPE,0);
		
		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
				
		assertEquals(Boolean.TRUE,action.get());
	}


	@Test
	public void testPrevious(){
		final ValidationDialog valid = new ValidationDialog(TestUtils.view());

		final Result resS  = new Result(5,Result.SUGGESTION,Arrays.asList(new String[]{"wick","quick","Vick"}),"qwick");
		final Result resS2  = new Result(10,Result.SUGGESTION,Arrays.asList(new String[]{"wack"}),"qwack");
		
		final AtomicReference<Boolean> action = new AtomicReference<Boolean>(null);
		
		Thread spellThread = new Thread(){
			public void run(){
				try{
					valid.showAndGo(resS,
						new MockCallback(){
							private int i=0;
							@Override
							public Result change(String newWord){
								if(i++==0){
									action.set(Boolean.TRUE);
									return resS2;
								}
								else{
									action.set(Boolean.FALSE);
									return null;
								}
							}
							
							public Result previous(){
								if(i--==1){
									action.set(Boolean.TRUE);
									return resS;
								}else{
									action.set(Boolean.FALSE);
									return null;
								}
							}
							
							public boolean hasPrevious(){
								return i>0;
							}
						});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		DialogFixture spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		spellDialog.button("Previous").requireDisabled();
		
		spellDialog.button("Change").click();
		
		try{spellThread.sleep(2000);}catch(InterruptedException ie){}
		assertEquals(Boolean.TRUE,action.get());
		spellDialog.textBox("originalWord").requireText("qwack");

		action.set(null);
		
		spellDialog.button("Previous").requireEnabled();
		spellDialog.button("Previous").click();

		try{spellThread.sleep(1000);}catch(InterruptedException ie){}
		assertEquals(Boolean.TRUE,action.get());
		spellDialog.textBox("originalWord").requireText("qwick");

		spellDialog.button("Cancel").click();

		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
		
		
	}

	@Test
	public void testSuggest(){
		final ValidationDialog valid = new ValidationDialog(TestUtils.view());

		final Result resS  = new Result(5,Result.SUGGESTION,Arrays.asList(new String[]{"wick","quick","Vick"}),"qwick");
		final Result resSuggestList  = new Result(0,Result.SUGGESTION,Arrays.asList(new String[]{"wack"}),"qwack");
		final Result resSuggestNone  = new Result(0,Result.NONE,null,"qwock");
		final Result resSuggestOK  = new Result(0,Result.OK,null,"quick");
		
		
		Thread spellThread = new Thread(){
			public void run(){
				try{
					valid.showAndGo(resS,
						new MockCallback(){
							private int i=0;
							@Override
							public Result suggest(String newWord){
								if("qwack".equals(newWord))return resSuggestList;
								else if("qwock".equals(newWord))return resSuggestNone;
								else if("quick".equals(newWord))return resSuggestOK;
								else return null;
							}
							
						});
				}catch(SpellException spe){}
			}
		};
		spellThread.start();
		
		DialogFixture spellDialog = WindowFinder.findDialog(ValidationDialog.class).withTimeout(10000).using(TestUtils.robot());
		
		TestUtils.replaceText(spellDialog.textBox("changeTo"),"qwack");
		spellDialog.button("Suggest").click();
		
		try{spellThread.sleep(2000);}catch(InterruptedException ie){}
		spellDialog.textBox("changeTo").requireText("qwack");
		assertEquals(1,spellDialog.list().contents().length);
		assertEquals("wack",spellDialog.list().item(0).value());

		
		TestUtils.replaceText(spellDialog.textBox("changeTo"),"qwock");
		spellDialog.button("Suggest").click();

		try{spellThread.sleep(2000);}catch(InterruptedException ie){}
		spellDialog.textBox("changeTo").requireText("qwock");
		List<String> sugg = Arrays.asList(spellDialog.list().contents());
		assertEquals(1,sugg.size());

		TestUtils.replaceText(spellDialog.textBox("changeTo"),"quick");
		spellDialog.button("Suggest").click();

		try{spellThread.sleep(2000);}catch(InterruptedException ie){}
		spellDialog.textBox("changeTo").requireText("quick");
		sugg = Arrays.asList(spellDialog.list().contents());
		assertEquals(1,sugg.size());

		spellDialog.button("Cancel").click();

		try{
			spellThread.join(5000);
		}catch(InterruptedException ie){}
		
		assertTrue("validation didn't return", !spellThread.isAlive());
		
	}
	
	class MockCallback implements ValidationDialog.Callback{
		
		public Result add()throws SpellException
		{
			return null;
		}
		
		public Result change(String newWord)throws SpellException
		{
			return null;
		}
		
		public Result changeAll(String newWord)throws SpellException
		{
			return null;
		}
		
		public Result ignore()throws SpellException
		{
			return null;
		}

		public Result ignoreAll()throws SpellException
		{
			return null;
		}
		
		
		public Result suggest(String newWord)throws SpellException
		{
			return null;
		}
		
	
		public Result previous()throws SpellException
		{
			return null;
		}
		
		public boolean cancel()
		{
			return true;
		}

		public void done()
		{
		}

		
		public boolean hasPrevious()
		{
			return false;
		}

		public boolean hasIgnored()
		{
			return false;
		}

	}
}
