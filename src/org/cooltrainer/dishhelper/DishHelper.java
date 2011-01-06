/**
 * @author	Nicole Reid <root@cooltrainer.org>
 * @version	1.0
 */

package org.cooltrainer.dishhelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cookxml.cookswt.CookSwt;
import cookxml.cookswt.ui.XmlDialog;
import cookxml.cookswt.util.SwtUtils;



public class DishHelper
{
	/**
	 * Apache Commons Logging object
	 */
	private Log log;
	
	/**
	 * java.util.prefs object for org.cooltrainer.dishhelper
	 */
	private Preferences prefs;
	
	/**
	 * CookSWT object for building the UI from DishHelper.xml
	 */
	private CookSwt cookSwt;
	
	/**
	 * SWT shell object for the main window.
	 */
	public Shell shell;
	
	/**
	 * Contains all Satellite objects loaded from satellites.xml
	 */
	private ArrayList<Satellite> satellites;
	
	/**
	 * Satellite object chosen by the left satellite Combo in the GUI
	 */
	private Satellite satelliteLeft;
	
	/**
	 * Satellite object chosen by the left satellite Combo in the GUI
	 */
	private Satellite satelliteRight;
	
	/**
	 * Azimuth from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}. Used for drawing the map.
	 */
	private double azimuth;
	
	/**
	 * Magnetic declination from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}. Used for drawing the map.
	 */
	private double declination;

	/**
	 * "Latitude" Spinner widget
	 */
	public Spinner spinnerLatitude;
	
	/**
	 * "Longitude" Spinner widget
	 */
	public Spinner spinnerLongitude;
	
	/**
	 * "North" radio button
	 */
	public Button north;
	
	/**
	 * "South" radio button.
	 */
	public Button south;
	
	/**
	 * "East" radio button
	 */
	public Button east;
	
	/**
	 * "West" radio button
	 */
	public Button west;

	/**
	 * Combo widget to choose left outer Satellite
	 */
	public Combo comboSatelliteLeft;
	
	/**
	 * Combo widget to choose right outer Satellite
	 */
	public Combo comboSatelliteRight;
	
	/**
	 * "True Azimuth" Label to display results from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}
	 */
	public Label labelTrueAz;
	
	/**
	 * "True Azimuth" Label to display results from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}
	 */
	public Label labelMagneticAz;
	
	/**
	 * "True Azimuth" Label to display results from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}
	 */
	public Label labelElevation;
	
	/**
	 * "True Azimuth" Label to display results from {@link #calculate(Double, int, Double, int, Satellite, Satellite)}
	 */
	public Label labelSkew;
	
	/**
	 * Combo widget to select Satellite for detail. Will be filled by Satellites between satelliteLeft and satelliteRight inclusive.
	 */
	public Combo comboDetailSatellites;
	
	/**
	 * Combo to choose the C-band transponders of a detail satellite.
	 */
	public Combo comboDetailTranspondersC;

	/**
	 * Combo to choose the Ku-band transponders of a detail satellite.
	 */
	public Combo comboDetailTranspondersKu;

	/**
	 * Combo to choose the Ka-band transponders of a detail satellite.
	 */
	public Combo comboDetailTranspondersKa;

	/**
	 * "Transponder polarization" label for the detail pane.
	 */
	public Label labelDetailPolarization;
	
	/**
	 * "Transponder symbol rate" label for the detail pane.
	 */
	public Label labelDetailSymbolRate;
	
	/**
	 * Scale widget used for zooming the map
	 */
	public Scale scaleZoom;
	
	/**
	 * Checkbox to toggle map image display
	 */
	public Button checkMap;
	
	/**
	 * Checkbox to toggle compass rose display
	 */
	public Button checkCompass;
	
	/**
	 * Canvas for the map
	 */
	public Canvas canvas;
	
	/**
	 * Map image displayed on the Canvas
	 */
	public Image imageMap;
	
	/**
	 * Compass rose image displayed during offline mode
	 */
	private Image imageCompass;
	
	/**
	 * Width of map image and canvas widget
	 */
	private static final int MAP_WIDTH = 620;
	
	/**
	 * Height of map image and canvas widget
	 */
	private static final int MAP_HEIGHT = 500;
	
	/**
	 * "Status bar" label. SWT doesn't have a StatusBar widget (outside of JFace, anyway) so DishHelper fakes one using a Label.
	 */
	public Label labelStatus;
	
	/**
	 * Automatic Geolocation toggle. If yes, DishHelper tries {@link #geolocate()} when started. Toggle is set false when the user manually adjusts their position and true when Tools -> Geolocate is selected.
	 */
	private boolean geolocate;

	/**
	 * Absolute value of user's latitude
	 */
	private Double latitude = 0.0;
	
	/**
	 * Absolute value of user's longitude
	 */
	private Double longitude = 0.0;
	
	/**
	 * Sign multiple of latitude. 1 for north, -1 for south.
	 */
	private int latitudeSign = 1;
	
	/**
	 * Sign multiple of latitude. 1 for east, -1 for west.
	 */
	private int longitudeSign = 1;
	
	/**
	 * Path to last-known copy of satellites.xml
	 */
	private String xmlPath = "";
	
	/**
	 * URL of our IP-based geolocator
	 */
	public static final String GEOLOCATION_URL = "http://api.ipinfodb.com/v2/ip_query.php?key=9ed2905f51560c24d5ecfaadc65b844e9148edef13b1662428c2b16d88674fec&timezone=false";
	
	/**
	 * URL of Yahoo! map API
	 */
	public static final String YAHOO_URL = "http://local.yahooapis.com/MapsService/V1/mapImage";
	
	/**
	 * Yahoo! map API key for org.cooltrainer.dishhelper
	 */
	public static final String YAHOO_APPID = "erzbaxTV34FlYUZdyUI6.wPrkffJ3wVLNGrWbIGu1JaRZE9CMQQB4SttPz1S2SI-";
	
	/**
	 * Default map zoom level. 1 is street-level. 12 is country-level.
	 */
	private int mapZoom = 6;
	
	/**
	 * Called when the shell window is closed via the window manager.
	 */
	public ShellListener shellListener = new ShellAdapter()
	{
		public void shellClosed(ShellEvent e)
		{
			quit();
		}
	};
	
	/**
	 * Called when the user selects File -> Quit
	 */
	public SelectionListener exitAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			quit();
		}
	};
	
	/**
	 * "About" XmlDialog variable for CookSWT
	 */
	public XmlDialog aboutDialog;
	
	/**
	 * Called when the user selects Help -> About
	 */
	public SelectionListener aboutAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			aboutDialog.open();
		}
	};
	
	/**
	 * Called when the user selects File -> Open. Open a browse dialog to the default location filtered for *.xml files.
	 */
	public SelectionListener openAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			dialog.setFilterExtensions(new String [] {"*.xml"});
			
			
			//Open user.home/Desktop if it exists, else user.home
			String path;
			File desktop = new File(System.getProperty("user.home") + File.separator +"Desktop");
			path = (desktop.exists()) ? System.getProperty("user.home") + File.separator + "Desktop" : System.getProperty("user.home") ;
			
			dialog.setFilterPath(path);
			String result = dialog.open();
			xmlPath = result;
			
			if(result != null) //Can happen if the user cancels the dialog
			{
				Document xml = parseXml(xmlPath);
				satellites = openXml(xml);
				setSatelliteCombos(satellites, longitude);
			}
		}
	};
	
	/**
	 * Called when the user selects Tools -> Geolocate. Attempt to get the user's location by IP and re-enable automatic geolocation.
	 */
	public SelectionListener geolocateAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			double[] location = geolocate();
			latitude = location[0];
			latitudeSign = (int)location[1];
			longitude = location[2];
			longitudeSign = (int)location[3];
			setCoordinates(latitude, latitudeSign, longitude, longitudeSign);
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			geolocate = true;
		}
	};
	
	/**
	 * Called when the user operates the Latitude spinner widget. Disables automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener spinnerLatitudeAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			latitude = Double.parseDouble(spinnerLatitude.getText());
			geolocate = false;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			azimuth = calculations[0];
			declination = calculations[1];
		}
	};
	
	/**
	 * Called when the user operates the Longitude spinner widget. Disables automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener spinnerLongitudeAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			longitude = Double.parseDouble(spinnerLongitude.getText());
			geolocate = false;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			azimuth = calculations[0];
			declination = calculations[1];
		}
	};
	
	/**
	 * Called when the user presses the north latitude button. Sets latitudeSign to 1, disables automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener northAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			latitudeSign = 1;
			geolocate = false;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			if(calculations[0] != null && calculations[1] != null)
			{
				azimuth = calculations[0];
				declination = calculations[1];
			}
		}
	};
	
	/**
	 * Called when the user presses the south latitude button. Sets latitudeSign to -1, disables automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener southAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			latitudeSign = -1;
			geolocate = false;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			if(calculations[0] != null && calculations[1] != null)
			{
				azimuth = calculations[0];
				declination = calculations[1];
			}
		}
	};
	
	/**
	 * Called when the user presses the east longitude button. Sets longitudeSign to 1, disas coming bles automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener eastAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			longitudeSign = 1;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			/*
			 * Just re-populate the Combos when changing from west to east.
			 * The old selection is unlikely to exist in the new Combo,
			 * so save the calculations until the user chooses a new one.
			 */
			setSatelliteCombos(satellites, longitude * longitudeSign);
			satelliteLeft = null;
			satelliteRight = null;
		}
	};
	
	/**
	 * Called when the user presses the west longitude button. Sets longitudeSign to -1, disables automatic geolocation, refreshes the map, and recalculates.
	 */
	public SelectionListener westAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			longitudeSign = -1;
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
			/*
			 * Just re-populate the Combos when changing from west to east.
			 * The old selection is unlikely to exist in the new Combo,
			 * so save the calculations until the user chooses a new one.
			 */
			setSatelliteCombos(satellites, longitude * longitudeSign);
			satelliteLeft = null;
			satelliteRight = null;
		}
	};
	
	/**
	 * Called when the user operates the map zoom slider. Redraws the map.
	 */
	public SelectionListener zoomAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			mapZoom = scaleZoom.getSelection();
			imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
			canvas.redraw();
		}
	};
	
	/**
	 * Called when the user toggles map display.
	 */
	public SelectionListener checkMapAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			canvas.redraw();
			scaleZoom.setEnabled(checkMap.getSelection());
		}
	};
	
	/**
	 * Called when the user toggles compass display.
	 */
	public SelectionListener checkCompassAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			canvas.redraw();
			scaleZoom.setEnabled(checkMap.getSelection());
		}
	};
	
	/**
	 * Called when the user makes a selection from the left satellite Combo. Recalculates and redraws the map.
	 */
	public SelectionListener satLeftAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			satelliteLeft = findSat(comboSatelliteLeft.getText(), satellites);
			setDetailSatellites();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			azimuth = calculations[0];
			declination = calculations[1];
			canvas.redraw();
		}
	};
	
	/**
	 * Called when the user makes a selection from the right satellite Combo. Recalculates and redraws the map.
	 */
	public SelectionListener satRightAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			satelliteRight = findSat(comboSatelliteRight.getText(), satellites);
			setDetailSatellites();
			Double[] calculations = calculate(latitude, latitudeSign, longitude, longitudeSign, satelliteLeft, satelliteRight);
			azimuth = calculations[0];
			declination = calculations[1];
			canvas.redraw();
		}
	};
	
	/**
	 * Called when the user chooses a Satellite in the detail pane.
	 */
	public SelectionListener detailSatelliteAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			setDetailTransponders(comboDetailSatellites.getText());
		}
	};
	
	/**
	 * Called when the user chooses a C-Band transponder in the detail pane.
	 */
	public SelectionListener detailTransponderCAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			setDetailData(comboDetailSatellites.getText(), comboDetailTranspondersC.getText());
			comboDetailTranspondersKu.deselectAll();
			comboDetailTranspondersKa.deselectAll();
		}
	};
	
	/**
	 * Called when the user chooses a Ku-Band transponder in the detail pane.
	 */
	public SelectionListener detailTransponderKuAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			setDetailData(comboDetailSatellites.getText(), comboDetailTranspondersKu.getText());
			comboDetailTranspondersC.deselectAll();
			comboDetailTranspondersKa.deselectAll();
		}
	};
	
	/**
	 * Called when the user chooses a Ka-Band transponder in the detail pane.
	 */
	public SelectionListener detailTransponderKaAction = new SelectionAdapter ()
	{
		public void widgetSelected (SelectionEvent e)
		{
			setDetailData(comboDetailSatellites.getText(), comboDetailTranspondersKa.getText());
			comboDetailTranspondersC.deselectAll();
			comboDetailTranspondersKu.deselectAll();
		}
	};
	
	/**
	 * Used for opening URL link widgets in the default browser
	 */
	public SelectionListener linkListener = new SelectionListener()
	{
		public void widgetDefaultSelected(SelectionEvent arg0)
		{
		}

		public void widgetSelected(SelectionEvent event)
		{
			System.out.println(event.text);
			Program.launch("http://satellites-xml.eu/");
		}
	};

	/**
	 * Called by our other methods when they wish to draw the map. Does the translation between compass readings and polar notation for the Math functions.
	 */
	public PaintListener canvasPaintListener = new PaintListener()
	{
		public void paintControl(PaintEvent e)
		{
			if(imageMap != null && checkMap.getSelection() != false)
			{
				e.gc.drawImage(imageMap, 0, 0);
			}
			if(checkCompass.getSelection() != false || imageMap == null)
			{
				e.gc.drawImage(imageCompass, MAP_WIDTH / 2 - 249, MAP_HEIGHT / 2 - 249);
			}

			int lineRadius = 1000; //Should be large enough to always extend outside the map
			
			/*
			 * Azimuth on the compass begins with 0 north and increases
			 * clockwise. Translate this to match the system that java.Math expects,
			 * 0 at 3-o'clock increasing counterclockwise.
			 */
			double azimuthTranslated = azimuth + declination;
			double azimuth = azimuthTranslated;
			
			//Convert to radians for the Math functions
			azimuthTranslated *= (Math.PI / 180);
			
			//Shift clockwise by 90°
			azimuthTranslated -= (Math.PI / 2);
			
			//Invert
			azimuthTranslated -= (Math.PI * 2);
			
			//Get a positive sign
			azimuthTranslated = Math.abs(azimuthTranslated);
			
			double x = lineRadius * Math.cos(azimuthTranslated);		
			
			double y = lineRadius * Math.sin(azimuthTranslated);
			

			
			/*
			 * Don't draw the line if the user has made no selection.
			 */
			if(satelliteLeft != null || satelliteRight != null)
			{
				e.gc.setLineWidth(3);
				e.gc.setLineStyle(SWT.LINE_DOT);
				e.gc.drawArc(MAP_WIDTH / 2 - 50, MAP_HEIGHT /2 - 50, 100, 100, (int) (azimuthTranslated * (180 / Math.PI)), (int)azimuth);
				
				e.gc.setLineStyle(SWT.LINE_SOLID);
				e.gc.drawLine(MAP_WIDTH / 2, MAP_HEIGHT / 2, MAP_WIDTH / 2 + (int)x, MAP_HEIGHT / 2 - (int)y);
			}
		}
	};

	public DishHelper()
	{
		this.log = LogFactory.getLog(getClass());
		this.satellites = new ArrayList<Satellite>();
		this.satelliteLeft = null;
		this.satelliteRight = null;
		
		this.prefs = Preferences.userNodeForPackage(org.cooltrainer.dishhelper.DishHelper.class); // HKEY_CURRENT_USER
		this.geolocate = this.prefs.getBoolean("geolocate", true);
		this.latitude = this.prefs.getDouble("latitude", 0.0);
		this.longitude = this.prefs.getDouble("longitude", 0.0);
		this.latitudeSign = this.prefs.getInt("latitudeSign", 1);
		this.longitudeSign = this.prefs.getInt("longitudeSign", -1);

		this.cookSwt = new CookSwt(this, this.getClass().getClassLoader());
		Display display = (Display)this.cookSwt.xmlDecode(this.getClass().getResource("ui/DishHelper.xml").toString());
		shell.pack();
		
		/*
		 * Hack to make the map zoom widget not tiny
		 */
		scaleZoom.setBounds(0, 0, scaleZoom.getBounds().width, 200);
		scaleZoom.setSelection(mapZoom);
		
		/*
		 * Hack to set the map canvas to the size of the map image. Useful if
		 * we have no network connection and can't get a map image to make it
		 * automatically resize.
		 */
		canvas.setSize(MAP_WIDTH, MAP_HEIGHT);
		
		/*
		 * Set up app icons
		 * http://www.iconarchive.com/show/transport-icons-by-aha-soft/first-satellite-icon.html
		 */
		Image[] shellIcons = {
			new Image(display, this.getClass().getResourceAsStream("ui/DishHelper-16.png")),
			new Image(display, this.getClass().getResourceAsStream("ui/DishHelper-32.png")),
			new Image(display, this.getClass().getResourceAsStream("ui/DishHelper-64.png")),
			new Image(display, this.getClass().getResourceAsStream("ui/DishHelper-128.png")),
			new Image(display, this.getClass().getResourceAsStream("ui/DishHelper-256.png"))
		};
		this.shell.setImages(shellIcons);
		
		imageCompass =  new Image(display, this.getClass().getResourceAsStream("CompassRose.png"));
		
		String xmlPath = System.getProperty("user.home") + File.separator + ".dishhelper/satellites.xml";
		
		/*
		 * Attempt to load our data file from the config directory, then from the jar if that fails.
		 */
		File xmlFile = new File(xmlPath);
		if(xmlFile.exists() && parseXml(xmlPath) != null)
		{
			satellites = openXml(parseXml(xmlPath));
		}
		else if(!xmlFile.exists())
		{
			if(moveXml(this.getClass().getResourceAsStream("satellites.xml")))
			{
				satellites = openXml(parseXml(xmlPath));
			}
		}
		
		/*
		 * IP-based geolocation for first-run and subsequent starts with geolocate==true.
		 */
		if(this.geolocate != false)
		{
			double[] location = geolocate();
			latitude = location[0];
			latitudeSign = (int)location[1];
			longitude = location[2];
			longitudeSign = (int)location[3];
		}
		setCoordinates(latitude, latitudeSign, longitude, longitudeSign);
		setSatelliteCombos(satellites, longitude * longitudeSign);
		
		imageMap = getMap(latitude, latitudeSign, longitude, longitudeSign, mapZoom);
		canvas.redraw();
		SwtUtils.showDisplay(display);
	}
	
	private void quit()
	{
		this.prefs.putBoolean("geolocate", this.geolocate);
		this.prefs.putDouble("latitude", this.latitude);
		this.prefs.putDouble("longitude", this.longitude);
		this.prefs.putInt("latitudeSign", this.latitudeSign);
		this.prefs.putInt("longitudeSign", this.longitudeSign);

		if(satelliteLeft != null)
		{
			this.prefs.put("satelliteLeft", satelliteLeft.getName());
		}
		
		if(satelliteRight != null)
		{
			this.prefs.put("satelliteRight", satelliteRight.getName());
		}
		
		Display.getCurrent().getShells()[0].dispose();
		System.exit(0);
	}
	
	/*
	 * Alert the user to selected warning messages via the status bar.
	 * Some of the more technical confusing ones should be sent straight
	 * to log.warn() instead.
	 */
	private void warn(String warning, Exception e)
	{
		labelStatus.setText(warning);
		log.warn(warning, e);
	}
	
	/**
	 * Get azimuth, declination, elevation, and skew for the user's geolocation.
	 * 
	 * @param latitude			Absolute value of the user's latitude.
	 * @param latitudeSign		Latitude sign to indicate hemisphere.
	 * @param longitude			Absolute value of the user's longitude.
	 * @param longitudeSign		Longitude sign to indicate hemisphere.
	 * @param satelliteLeft		Outer satellite to aim.
	 * @param satelliteRight	Other outer satellite to aim.
	 * @return					Double array containing azimuth and declination. They are needed elsewhere to draw the map.
	 */
	private Double[] calculate(Double latitude, int latitudeSign, Double longitude, int longitudeSign, Satellite satelliteLeft, Satellite satelliteRight)
	{
		Double[] output = new Double[2];
		boolean havePosition = (latitude != null && longitude != null);
		boolean haveSat = (satelliteLeft != null || satelliteRight != null);
		if(havePosition && haveSat)
		{
			Double orbitalLongitude = 0.0;
			if(satelliteLeft != null && satelliteRight != null)
			{
				orbitalLongitude = ((satelliteLeft.getPosition() + satelliteRight.getPosition()) / 2);
			}
			else if(satelliteLeft == null && satelliteRight != null)
			{
				orbitalLongitude = satelliteRight.getPosition();
			}
			else if(satelliteLeft != null && satelliteRight == null)
			{
				orbitalLongitude = satelliteLeft.getPosition();
			}

			
			double azimuth = azimuth(latitude, latitudeSign, longitude, longitudeSign, orbitalLongitude);
			double declination = declination(latitude, latitudeSign, longitude, longitudeSign);
			double elevation = elevation(latitude, latitudeSign, longitude, longitudeSign, orbitalLongitude);
			double skew = skew(latitude, latitudeSign, longitude, longitudeSign, orbitalLongitude);
			
			DecimalFormat df_output = new DecimalFormat("###.#");

			//Add 180° to azimuth for the southern hemisphere
			azimuth = (latitudeSign > 0) ? azimuth : azimuth + 180;

			//Add 180° to skew for the southern hemisphere
			skew = (latitudeSign > 0) ? skew : skew + 180;
			
			//Azimuth can overflow in the southern hemisphere. Constrain it to 360°
			azimuth = (azimuth > 360) ? azimuth - 360 : azimuth;
			
			//Needed elsewhere to draw the map
			output[0] = azimuth;
			output[1] = declination;
			
			labelTrueAz.setText(df_output.format(azimuth) + "°");
			labelMagneticAz.setText(df_output.format(azimuth + declination) + "°");
			labelElevation.setText(df_output.format(elevation) + "°");
			labelSkew.setText(df_output.format(skew) + "°");
		}
		return output;
	}
	
	/**
	 * Calculate magnetic declination for the user's geolocation using the Magfield class.
	 * 
	 * @param latitude			Absolute value of the user's latitude.
	 * @param latitudeSign		Latitude sign to indicate hemisphere.
	 * @param longitude			Absolute value of the user's longitude.
	 * @param longitudeSign		Longitude sign to indicate hemisphere.
	 * @see						Magfield
	 * @return					Double, magnetic declination.
	 */
	private double declination(double latitude, int latitudeSign, double longitude, int longitudeSign)
	{
		DateFormat yyyyFormat = new SimpleDateFormat("yyyy");
        DateFormat mmFormat = new SimpleDateFormat("mm");
        DateFormat ddFormat = new SimpleDateFormat("dd");
        Date date = new Date();
        int yyyy = Integer.parseInt(yyyyFormat.format(date)) - 2005; //Subtract 2005 since we are using IGRF 2005 coefficients and need the difference
        int mm = Integer.parseInt(mmFormat.format(date));
        int dd = Integer.parseInt(ddFormat.format(date));
		return Magfield.calculate(latitude, longitude, yyyy, mm, dd, latitudeSign, longitudeSign);
	}
	
	/**
	 * Calculate true azimuth based on our location and where we're aiming.
	 * 
	 * @param latitude			Absolute value of the user's latitude.
	 * @param latitudeSign		Latitude sign to indicate hemisphere.
	 * @param longitude			Absolute value of the user's longitude.
	 * @param longitudeSign		Longitude sign to indicate hemisphere.
	 * @param orbitalLongitude	Longitude of orbital location for which we're aiming.
	 * @return					Double, true azimuth.
	 */
	private double azimuth(double latitude, int latitudeSign, double longitude, int longitudeSign, double orbitalLongitude)
	{
		return
		(
			Math.PI -
			(
				Math.atan
				(
					Math.tan
					(
						(
							(orbitalLongitude)
							-
							(longitude * longitudeSign)
						)
						/
						(180 / Math.PI)
					)
					/
					Math.sin
					(
						(latitude * latitudeSign)
						/
						(180 / Math.PI)
					)
				)
			)
		)
		* (180/Math.PI);
	}
	
	/**
	 * Calculate antenna elevation based on our location and where we're aiming.
	 * 
	 * @param latitude			Absolute value of the user's latitude.
	 * @param latitudeSign		Latitude sign to indicate hemisphere.
	 * @param longitude			Absolute value of the user's longitude.
	 * @param longitudeSign		Longitude sign to indicate hemisphere.
	 * @param orbitalLongitude	Longitude of orbital location for which we're aiming.
	 * @return					Double, elevation.
	 */
	private double elevation(double latitude, int latitudeSign, double longitude, int longitudeSign, double orbitalLongitude)
	{
		return
		Math.atan
		(
			(
				Math.cos
				(
					(
						(orbitalLongitude)
						-
						(longitude * longitudeSign)
					)
					/ 
					(180 / Math.PI)
				)
				*
				Math.cos
				(
					(latitude * latitudeSign)
					/ 
					(180 / Math.PI)
				)
				-.1512
			)
			/
			(
				Math.sqrt
				(
					1 -
					(
						Math.pow
						(
							Math.cos
							(
								(
									(orbitalLongitude)
									-
									(longitude * longitudeSign)
								)
								/
								(180 / Math.PI)
							)
							, 2.0
						)
					)
					*
					(
						Math.pow
						(
							Math.cos
							(
								(latitude * latitudeSign)
								/
								(180 / Math.PI)
							)
							, 2.0
						)
					)
				)
			)
		) * (180 / Math.PI);
	}
	/**
	 * Compute LNB skew for our location and where we're aiming.
	 * 
	 * @param latitude			Absolute value of the user's latitude.
	 * @param latitudeSign		Latitude sign to indicate hemisphere.
	 * @param longitude			Absolute value of the user's longitude.
	 * @param longitudeSign		Longitude sign to indicate hemisphere.
	 * @param orbitalLongitude	Longitude of orbital location for which we're aiming.
	 * @return					Double, skew.
	 */
	private double skew(double latitude, int latitudeSign, double longitude, int longitudeSign, double orbitalLongitude)
	{
		return
		(
			Math.atan2
			(
				Math.tan
				(
					(latitude * latitudeSign)
					*
					(Math.PI / 180.0)
				)
				,
				Math.sin
				(
					(
						(
							orbitalLongitude
							-
							(longitude * longitudeSign)
						)
						*
						(Math.PI / 180.0)
					)
				)
			)
			* (180.0 / Math.PI)
		) - 90.0;
	}
	
	/**
	 * Search through the satellites ArrayList to find one of a certain name, since that's the only reference we can get from the Combo widget.
	 * 
	 * @param name			Name of desired satellite.
	 * @param satellites	Satellites ArrayList to search.
	 * @return				Satellite object of requested satellite or null.
	 */
	private Satellite findSat(String name, ArrayList<Satellite> satellites)
	{
		Satellite output = null;
		
		if(satellites.size() > 0)
		{
			for(Satellite sat : satellites)
			{
				if(sat.getName().equals(name))
				{
					output = sat;
				}
			}
		}
		
		return output;
	}
	
	/**
	 * Sets the geolocation GUI widgets.
	 * 
	 * @param latitude		Absolute value of the user's latitude.
	 * @param latitudeSign	Latitude sign to indicate hemisphere.
	 * @param lfrom datongitude		Absolute value of the user's longitude.
	 * @param longitudeSign	Longitude sign to indicate hemisphere.
	 */
	private void setCoordinates(Double latitude, int latitudeSign, Double longitude, int longitudeSign)
	{
		/*
		 * Use DecimalFormat to ensure the proper number of trailing zeros.
		 * Ex: 33.920 instead of 33.92
		 * Because setting the Spinner widgets to "3392" will make it read 3.392.
		 */
		DecimalFormat df_latitude = new DecimalFormat("00.000");
		DecimalFormat df_longitude = new DecimalFormat("000.000");
		
		int int_latitude = Integer.parseInt(df_latitude.format(latitude).replace(".", ""));
		this.spinnerLatitude.setSelection(int_latitude);
		
		if(latitudeSign == 1)
		{
			this.north.setSelection(true);
			this.south.setSelection(false);
		}
		else if(latitudeSign == -1)
		{
			this.north.setSelection(false);
			this.south.setSelection(true);
		}
		
		
		int int_longitude = Integer.parseInt(df_longitude.format(longitude).replace(".", ""));
		this.spinnerLongitude.setSelection(int_longitude);
		
		if(longitudeSign == 1)
		{
			this.east.setSelection(true);
			this.west.setSelection(false);
		}
		else if(longitudeSign == -1)
		{
			this.east.setSelection(false);
			this.west.setSelection(true);
		}
			
	}

	/**
	 * Installs the shipped copy of satellites.xml to the user's home directory.
	 * @param xmlPath URI to the in-JAR data file.
	 * @return True if the file was installed successfully, false otherwise.
	 */
	private boolean moveXml(InputStream xmlStream)
	{
		boolean status = false;
		BufferedReader sourceReader = new BufferedReader(new InputStreamReader(xmlStream));
		//if (sourceFile.exists())
		{
			String configPath = System.getProperty("user.home") + File.separator + ".dishhelper";
			File configDirectory = new File(configPath);
			if (configDirectory.exists() || configDirectory.mkdir())
			{
				File installedFile = new File(configPath + File.separator + "satellites.xml");
				if (installedFile.exists())
				{
					installedFile.delete();
				}
				try
				{
					BufferedWriter outputWriter = new BufferedWriter(new FileWriter(configPath + File.separator + "satellites.xml"));
					
					String line;
					while(sourceReader.ready())
					{
						line = sourceReader.readLine();
						outputWriter.write(line);
					}
					outputWriter.close();
					status = true;
				}
				catch (IOException e)
				{
					log.warn(e);
				}
			}
		}
		return status;
	}
	
	/**
	 * Attempts to parse a satellite data file.
	 * @param xmlPath Path to the XML data file.
	 * @return A Document object of the parsed data file or null if the file is malformed.
	 */
	private Document parseXml(String xmlPath)
	{
		Document document = null;
		
		boolean havePath = xmlPath != null && !xmlPath.equals("");
		if(havePath)
		{
			try
			{
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				document = builder.parse(xmlPath);
			}
			catch(IOException e)
			{
				warn("Unable to open satellites.xml", e);
			}
			catch (ParserConfigurationException e)
			{
				log.warn("Error initializing DocumentBuilder", e);
			}
			catch (SAXException e)
			{
				warn("Improperly-formatted satellites.xml", e);
			}
		}
		
		return document;
	}
	
	/**
	 * Reads a satellites.xml Document object and creates Satellite and Transponder objects.
	 * 
	 * @param doc	Document object from {@link #parseXml(String xmlPath)}
	 * @return	ArrayList of Satellite objects.
	 */
	private ArrayList<Satellite> openXml(Document doc)
	{
		/*
		 * The file we're after is formatted like:
		 * <satellites>
		 * 		<sat name="" position="">
		 * 			<transponder freq="" symbol="" fec="" polar="" mod="" />
		 * 		</sat>
		 * </satellites>
		 */
		ArrayList<Satellite> satellites = new ArrayList<Satellite>();
	
		/*
		 * Attempt to grab the first element of the file. It should be a descriptive comment:
		 * 
		 *	<!--
		 *		file generated on friday, 6th of august 2010, 20:11:41 [GMT] 
		 *		by online satellites.xml generator @ http://satellites-xml.eu 
		 *		please let us know if you find any inconsistencies in this file 
		 *	-->
		 *
		 * We want the first line for the status bar.
		 */
		String[] comment = doc.getFirstChild().getTextContent().split("\n");
		if(comment.length > 1)
		{
			if(comment[1].length() > 50)
			{
				labelStatus.setText("Data " + comment[1].trim());
			}
		}
		
		NodeList sats = doc.getElementsByTagName("sat");
		satellites.clear();
		
		/*
		 * Add blank Satellite to the top of the list
		 */
		Satellite blank = new Satellite();
		blank.setName("None");
		satellites.add(blank);
		
		for (int i = 0; i < sats.getLength(); i++)
		{
			Element sat = (Element)sats.item(i);
			Satellite newSat = new Satellite(
					sat.getAttribute("name"),
					Integer.parseInt(sat.getAttribute("position")));
			NodeList transponders = sat.getElementsByTagName("transponder");
			for (int j = 0; j < transponders.getLength(); j++)
			{
				Element transponder = (Element)transponders.item(j);
				newSat.addTransponder(
						new Transponder(
								Integer.parseInt(transponder.getAttribute("frequency")),
								Integer.parseInt(transponder.getAttribute("symbol_rate")),
								Integer.parseInt(transponder.getAttribute("polarization")),
								Integer.parseInt(transponder.getAttribute("fec_inner")),
								Integer.parseInt(transponder.getAttribute("modulation"))
								)
						);
			}
			satellites.add(newSat);
		}
		
		if(sats.getLength() <= 0)
		{
			this.log.warn("No <sat> tags found in document");
		}
		
		
		return satellites;
	}
	
	/**
	 * Sets the satellite combos to the available satellites based on the
	 * user's location. Excludes everything more than 70 degrees from
	 * the user, since the curvature of the Earth makes them unusable.
	 * 
	 * @param	satellites	List of satellites loaded from XML.
	 * @param	longitude	User's longitude.
	 * @see		org.eclipse.swt.widgets.Combo
	 */
	private void setSatelliteCombos(ArrayList<Satellite> satellites, Double longitude)
	{
		this.comboSatelliteLeft.removeAll();
		this.comboSatelliteRight.removeAll();
		for(Satellite s : satellites)
		{
			if(Math.abs(longitude - s.getPosition()) <= 70)
			{
				this.comboSatelliteLeft.add(s.getName());
				this.comboSatelliteRight.add(s.getName());
			}
		}
	}
	
	/**
	 * Sets the Satellite detail combo to the inclusive range of currently-selected Satellites from the main Combos.
	 * Attempts to save user selection if the previously-selected Satellite is within the new range.
	 */
	private void setDetailSatellites()
	{
		String satelliteSave = comboDetailSatellites.getText();
		comboDetailSatellites.removeAll();
		if(satelliteLeft == null && satelliteRight instanceof Satellite)
		{
			comboDetailSatellites.add(satelliteRight.getName());
		}
		else if(satelliteLeft instanceof Satellite && satelliteRight == null)
		{
			comboDetailSatellites.add(satelliteLeft.getName());
		}
		else
		{
			for(Satellite s : satellites)
			{
				boolean lower = (s.getPosition() >= satelliteLeft.getPosition() && s.getPosition() <= satelliteRight.getPosition());
				boolean higher = (s.getPosition() >= satelliteRight.getPosition() && s.getPosition() <= satelliteLeft.getPosition());
				
				if(lower || higher)
				{
					comboDetailSatellites.add(s.getName());
				}
			}
		}
		comboDetailSatellites.select(comboDetailSatellites.indexOf(satelliteSave));
		if(comboDetailSatellites.getItemCount() == 1)
		{
			comboDetailSatellites.select(0);
		}

		setDetailTransponders(comboDetailSatellites.getText());
	}
	
	/**
	 * Sets the Combos of available transponders. Called when the user selects an in-range satellite.
	 * @param name Name of the selected Satellite.
	 */
	private void setDetailTransponders(String name)
	{
		comboDetailTranspondersC.removeAll();
		comboDetailTranspondersKu.removeAll();
		comboDetailTranspondersKa.removeAll();
		Satellite selected = findSat(name, satellites);
		if(selected != null)
		{
			for(Transponder t : selected.getTransponders())
			{
				int freq = t.getFrequency();
				if(freq > 3400000 && freq < 4200000) //3.4GHz - 4.2
				{
					comboDetailTranspondersC.add(t.getPrettyFrequency());
				}
				else if(freq > 11700000 && freq < 12700000) //11.7GHz - 12.7
				{
					comboDetailTranspondersKu.add(t.getPrettyFrequency());
				}
				else if((freq > 18300000 && freq < 18800000) || (freq > 19700000 && freq < 20200000)) //18.3GHz - 18.8 or 19.7 - 20.2
				{
					comboDetailTranspondersKa.add(t.getPrettyFrequency());
				}
			}
		}
	}
	
	/**
	 * Updates the GUI with information on the currently-selected transponder.
	 * Uses Strings and searches because that's all we can get from our Combos.
	 * 
	 * @param sat Name of selected satellite.
	 * @param tp Name of selected transponder.
	 */
	private void setDetailData(String sat, String tp)
	{
		Satellite selected = findSat(sat, satellites);
		if(selected != null)
		{
			for(Transponder t : selected.getTransponders())
			{
				if(t.getPrettyFrequency().equals(tp))
				{
					labelDetailPolarization.setText(t.getPrettyPolarization());
					labelDetailSymbolRate.setText(t.getPrettySymbolRate().toString());
				}
			}
		}
	}
	
	/**
	 * Attempts to locate the user's machine using IP-based geolocation
	 * from ipinfodb.com.
	 * 
	 * @return Double array containing the latitude, latitude sign, longitude, and longitude sign. These values will be all zeroes if lookup fails.
	 */
	private double[] geolocate()
	{
		double[] values = {0.0, 0.0, 0.0, 0.0};
		HttpClient httpclient = new DefaultHttpClient();
		try
		{
			HttpGet httpget = new HttpGet(DishHelper.GEOLOCATION_URL);
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse(entity.getContent());
				
				NodeList nodes;
				Element ele;
				
				DecimalFormat df_latitude = new DecimalFormat("##.###");
				DecimalFormat df_longitude = new DecimalFormat("###.###");
				
				/*
				 * Grab our geolocation values from XML, convert to Number, round them to three significant digits,
				 * convert to number _again_, determine hemisphere from the sign of the number,
				 * then convert to String (yes another one!), strip negative signs and decimals, 
				 * convert (lol) to Integer for Spinner, and set as selection for the spinner.
				 */
				nodes = doc.getElementsByTagName("Latitude");
				if (nodes.getLength() > 0)
				{
					ele = (Element) nodes.item(0);
					double latitude = ((Number)df_latitude.parse(df_latitude.format(df_latitude.parse(ele.getTextContent())))).doubleValue(); 
					values[1] = (latitude >= 0) ? 1 : -1;
					values[0] = Math.abs(latitude);
				}
				else
				{
					this.log.warn("Unable to find Latitude tag.");
				}
				
				nodes = doc.getElementsByTagName("Longitude");
				if (nodes.getLength() > 0)
				{
					ele = (Element) nodes.item(0);
					double longitude = ((Number)df_longitude.parse(df_longitude.format(df_longitude.parse(ele.getTextContent())))).doubleValue();
					values[3] = (longitude >= 0) ? 1 : -1;
					values[2] = Math.abs(longitude);
				}
				else
				{
					this.log.warn("Unable to find Longitude tag.");
				}
				
				if(latitude == null || longitude == null)
				{
					this.log.warn("Unable to geolocate. Longitude or Latitude came back null.");
				}
			}
		}
		catch (ClientProtocolException e)
		{
			log.warn("Error in the HTTP protocol.", e);
		}
		catch (UnknownHostException e)
		{
			warn("Unknown host. We may not have a network connection.", e);
		}
		catch (IOException e)
		{
			log.warn("Unable to execute HTTPClient or parse its results.", e);
		}
		catch (ParserConfigurationException e)
		{
			log.warn("Error initializing DocumentBuilder.", e);
		}
		catch (SAXException e)
		{
			log.warn("Improperly-formatted XML.", e);
		}
		catch (ParseException e)
		{
			log.warn("Error parsing latitude or longitude.", e);
		}

		httpclient.getConnectionManager().shutdown();
		return values;
	}
	
	/**
	 * Attempts to fetch a map image from Yahoo! Maps Web Services (http://developer.yahoo.com/maps/rest/V1/).
	 * 
	 * @param latitude		Absolute value of the user's latitude.
	 * @param latitudeSign	Latitude sign to indicate hemisphere.
	 * @param longitude		Absolute value of the user's longitude.
	 * @param longitudeSign	Longitude sign to indicate hemisphere.
	 * @param zoom			Map zoom level, 1 (street level) through 12 (country level).
	 * @return				The map image from Yahoo! maps or a null Image object.
	 * @see					org.eclipse.swt.graphics.Image
	 */
	private Image getMap(double latitude, double latitudeSign, double longitude, double longitudeSign, int zoom)
	{
		Image image = null;
		HttpClient httpclient = new DefaultHttpClient();
		try
		{
			HttpGet httpget = new HttpGet(DishHelper.YAHOO_URL + "?appid=" + DishHelper.YAHOO_APPID + "&latitude=" + (latitude * latitudeSign) + "&longitude=" + (longitude * longitudeSign) + "&zoom=" + zoom);
			
			HttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				Document doc = builder.parse(entity.getContent());
				
				NodeList nodes;
				Element ele;
				
				nodes = doc.getElementsByTagName("Result");
				if (nodes.getLength() > 0)
				{
					ele = (Element) nodes.item(0);
					
					/*
					 * The text content of the first Result tag is the URL of our map image.
					 */
					HttpGet imagehttpget = new HttpGet(ele.getTextContent());
					HttpResponse imageresponse = httpclient.execute(imagehttpget);
					HttpEntity imageentity = imageresponse.getEntity();
					if (entity != null)
					{
						final InputStream instream = imageentity.getContent();
						image = new Image(Display.getCurrent(), instream);
						instream.close();
					}
				}
				else
				{
					nodes = doc.getElementsByTagName("Message");
					if (nodes.getLength() > 0)
					{
						ele = (Element) nodes.item(0);
						this.log.warn(ele.getTextContent());
					}
				}
			}
		}
		catch (ClientProtocolException e)
		{
			log.warn("Error in the HTTP protocol.", e);
		}
		catch (UnknownHostException e)
		{
			log.warn("Unknown host. Our map service may be down.");
		}
		catch (IOException e)
		{
			log.warn("Unable to execute HTTPClient or parse its results.", e);
		}
		catch (ParserConfigurationException e)
		{
			log.warn("Error initializing DocumentBuilder.", e);
		}
		catch (SAXException e)
		{
			log.warn("Improperly-formatted XML from Yahoo! Maps.", e);
		}
		return image;
	}
	
	
	public static void main(String[] args)
	{
		new org.cooltrainer.dishhelper.DishHelper();
	}
	
}