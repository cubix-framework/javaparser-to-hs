import java.awt.Color;
import java.awt.GridLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.DefaultMultipleCDockable;
import bibliothek.gui.dock.common.MultipleCDockableFactory;
import bibliothek.gui.dock.common.MultipleCDockableLayout;
import bibliothek.gui.dock.common.menu.CLayoutChoiceMenuPiece;
import bibliothek.gui.dock.facile.menu.RootMenuPiece;
import bibliothek.util.xml.XElement;

public class Dock {
       public static void main( String[] args ){
               JFrame frame = new JFrame( "Demo" );
               CControl control = new CControl( frame );

               // Initialize factory
               ColorFactory factory = new ColorFactory();
               control.addMultipleDockableFactory( "color", factory );

               // setup basic frame
               frame.add( control.getContentArea() );

               // the main area, no Dockable can be dragged into this area unless
               // explicitly allowed
               CWorkingArea mainArea = control.createWorkingArea( "main-area" );

               // setup basic layout with help of a CGrid
               CGrid grid = new CGrid( control );

               grid.add( 0, 0, 1, 2, new ColorDockable( factory, "Red 1", Color.RED ) );
               grid.add( 0, 0, 1, 2, new ColorDockable( factory, "Red 2", Color.RED ) );

               grid.add( 1, 0, 2, 2, mainArea );

               grid.add( 2, 0, 1, 3, new ColorDockable( factory, "Green 1", Color.GREEN ) );
               grid.add( 2, 0, 1, 3, new ColorDockable( factory, "Green 2", Color.GREEN ) );
               grid.add( 2, 0, 1, 3, new ColorDockable( factory, "Green 3", Color.GREEN ) );

               grid.add( 0, 2, 2, 1, new ColorDockable( factory, "Blue 1", Color.BLUE ) );
               grid.add( 0, 2, 2, 1, new ColorDockable( factory, "Blue 2", Color.BLUE ) );
               grid.add( 0, 2, 2, 1, new ColorDockable( factory, "Blue 3", Color.BLUE ) );

               control.getContentArea().deploy( grid );

               // Add additional dockables directly
               ColorDockable yellow1 = new ColorDockable( factory, "Yellow 1", Color.YELLOW );
               yellow1.setLocation( CLocation.base().normal().rectangle( 0, 0, 0.25, 0.25 ) );
               control.add( yellow1 );
               yellow1.setVisible( true );

               // Add elements to the main area
               ColorDockable main1 = new ColorDockable( factory, "Main 1", Color.BLACK );
               ColorDockable main2 = new ColorDockable( factory, "Main 2", Color.DARK_GRAY );

               control.add( main1 );
               control.add( main2 );

               main1.setLocation( mainArea.getStationLocation() );
               main2.setLocation( mainArea.getStationLocation() );

               main1.setVisible( true );
               main2.setVisible( true );

               // a menu to play around with the layout (no persistant storage)
               JMenuBar menuBar = new JMenuBar();
               RootMenuPiece menu = new RootMenuPiece( "Layout", false, new CLayoutChoiceMenuPiece( control, true ) );
               menuBar.add( menu.getMenu() );
               frame.setJMenuBar( menuBar );

               frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
               frame.setBounds( 20, 20, 600, 600 );

               frame.setVisible( true );
       }
}

// our Dockable
class ColorDockable extends DefaultMultipleCDockable{
       private Color color;

       public ColorDockable( ColorFactory factory, String title, Color color ){
               super( factory );
               setTitleText( title );
               this.color = color;

               JPanel panel = new JPanel();
               panel.setOpaque( true );
               panel.setBackground( color );
               setLayout( new GridLayout( 1, 1 ) );
               add( panel );
       }

       public Color getColor(){
               return color;
       }
}

// Factory to create ColorDockables
class ColorFactory implements MultipleCDockableFactory<ColorDockable, ColorLayout>{
       public ColorLayout create(){
               return new ColorLayout( null, null );
       }

       public ColorDockable read( ColorLayout layout ){
               return new ColorDockable( this, layout.getTitle(), layout.getColor() );
       }

       public ColorLayout write( ColorDockable dockable ){
               return new ColorLayout( dockable.getTitleText(), dockable.getColor() );
       }

	public boolean match(ColorDockable dockable, ColorLayout layout)
	{
		return false;
	}
}

//Layout information about a ColorDockable
class ColorLayout implements MultipleCDockableLayout{
       private Color color;
       private String title;

       public ColorLayout( String title, Color color ){
               this.title = title;
               this.color = color;
       }

       public String getTitle(){
               return title;
       }

       public Color getColor(){
               return color;
       }

       public void readStream( DataInputStream in ) throws IOException{
               title = in.readUTF();
               color = new Color( in.readInt() );
       }

       public void readXML( XElement element ){
               title = element.getElement( "title" ).getString();
               color = new Color( element.getElement( "rgb" ).getInt() );
       }

       public void writeStream( DataOutputStream out ) throws IOException{
               out.writeUTF( title );
               out.writeInt( color.getRGB() );
       }

       public void writeXML( XElement element ){
               element.addElement( "title" ).setString( title );
               element.addElement( "rgb" ).setInt( color.getRGB() );
       }
}