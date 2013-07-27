/******************************************************************************
*   Copyright 2002 BITart Gerd Knops. All rights reserved.
*
*   Project   : CodeBrowser
*   File   : CBListCellRenderer.java
*   Author   : Gerd Knops gerti@BITart.com
*
*******************************************************************************
*                                    :mode=java:folding=indent:collapseFolds=1:
*   History:
*   020510 Creation of file
*
*******************************************************************************
*
*   Description:
*   CBListCellRenderer is a ListCellRenderer displaying a buffers name as
*   follows:
*
*      - The file name is displayed using the color the VFS assigns to it
*      - As much as possible from the END of the buffers path is displayed
*        in dark gray if the buffer is saved, and dark red if the buffer
*        has unsaved changes
*      - A bold font is used if the buffer is selected 
*
*   $Id: CBListCellRenderer.java 7140 2006-09-27 19:36:10Z bambara $
*
*******************************************************************************
*
* DISCLAIMER
*
* BITart and Gerd Knops make no warranties, representations or commitments
* with regard to the contents of this software. BITart and Gerd Knops
* specifically disclaim any and all warranties, wether express, implied or
* statutory, including, but not limited to, any warranty of merchantability
* or fitness for a particular purpose, and non-infringement. Under no
* circumstances will BITart or Gerd Knops be liable for loss of data,
* special, incidental or consequential damages out of the use of this
* software, even if those damages were forseeable, or BITart or Gerd Knops
* was informed of their potential.
*
******************************************************************************/
package com.bitart.codebrowser;
/******************************************************************************
* Imports
******************************************************************************/

   import java.awt.*;
   import java.awt.font.*;
   import java.awt.geom.*;
   import javax.swing.*;
   import org.gjt.sp.jedit.*;
   import org.gjt.sp.jedit.io.*;
   
/*****************************************************************************/
public class CBListCellRenderer extends JPanel implements ListCellRenderer
{
/**
    * 
    */
   private static final long serialVersionUID = 1L;
/******************************************************************************
* Vars
******************************************************************************/

   JList   list;
   Buffer   buf;
   boolean   selected;
   
   int      fontHeight=20;
      
/******************************************************************************
* Implementation
******************************************************************************/
public Dimension getMinimumSize()
   {
      return new Dimension(10,fontHeight);
   }
   
public Dimension getPreferredSize()
   {
      return new Dimension(10,fontHeight);
   }
   
public void paintComponent(Graphics g)
   {
      int w=getWidth();
      //Font f=list.getFont();
      Font f=UIManager.getFont("Tree.font");
      if(selected) f=f.deriveFont(Font.BOLD);
      Font fi=f.deriveFont((selected)?Font.BOLD|Font.ITALIC:Font.ITALIC,f.getSize()-1);
      FontRenderContext frc=new FontRenderContext(null,true,false);
      
      LineMetrics lm=f.getLineMetrics("AZfj",frc);
      int baseline=2+(int)Math.ceil(lm.getAscent());
      
      Rectangle2D r2d=f.getStringBounds("   ",frc);
      int spacesWidth=(int)Math.ceil(r2d.getWidth());
      r2d=fi.getStringBounds("...",frc);
      int elipsesWidth=(int)Math.ceil(r2d.getWidth());
            
      String sep=java.io.File.separator;
      String name=buf.getName();
      String path=buf.getPath();
      
      int idx=path.lastIndexOf(sep);
      if(idx>0) path=path.substring(0,idx);
      
      r2d=f.getStringBounds(name,frc);
      int nl=(int)Math.ceil(r2d.getWidth());
      r2d=fi.getStringBounds(path,frc);
      int pl=(int)Math.ceil(r2d.getWidth());
      if(nl+pl+spacesWidth+elipsesWidth>w-10)
      {
         while(nl+pl+spacesWidth+elipsesWidth>w-10)
         {
            idx=path.indexOf(sep,1);
            if(idx<=0) break;
            path=path.substring(idx);
            r2d=fi.getStringBounds(path,frc);
            pl=(int)Math.ceil(r2d.getWidth());
         }
         path="..."+path;
         pl+=elipsesWidth;
      }
      if(nl+pl+spacesWidth>w-10)
      {
         path="...";
         pl=elipsesWidth;
         if(nl+pl+spacesWidth>w-10)
         {
            path="";
            pl=0;
         }
      }
      
      //g.setColor(Color.black);
      g.setColor(VFS.getDefaultColorFor(name));
      
      g.setFont(f);
      g.drawString(name,5,baseline);
      if(pl>0)
      {
         if(buf.isDirty())
         {
            g.setColor(new Color(0x800000));
         }
         else
         {
            g.setColor(Color.darkGray);
         }
         g.setFont(fi);
         g.drawString(path,w-pl-5,baseline);
      }
   }
   
/******************************************************************************
* ListCellRenderer interface
******************************************************************************/
public Component getListCellRendererComponent(
      JList list,
      Object value,
      int index,
      boolean selected,
      boolean hasFocus
   )
   {
      this.list=list;
      buf=(Buffer)value;
      this.selected=selected;
      
      Rectangle2D r=list.getFont().getMaxCharBounds(new FontRenderContext(null,true,false));
      fontHeight=4+(int)Math.ceil(r.getHeight());
      
      return this;
   }
}
/*************************************************************************EOF*/

