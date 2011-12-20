import java.applet.Applet;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;

import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.loaders.Scene;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.List;
public class GcodeView extends Applet implements MouseWheelListener, ChangeListener{

	// Remember to comment thiss Noah!!!
	protected Canvas3D c1 = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	// private Canvas3D c2 = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	private static MainFrame mf;

	private final Vector3f CAMERA_TRANSLATION_DEFAULT = new Vector3f(+0.0f,-0.15f,-3.6f);
	private Vector3f cameraTranslation = new Vector3f(CAMERA_TRANSLATION_DEFAULT);


	private final int LOW_SPEED = 700;
	private final int MEDIUM_SPEED = 1400;
	private final int HIGH_SPEED = 1900;
	private Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
	private Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
	private Color3f red = new Color3f(0.7f, .0f, .15f);
	private Color3f green = new Color3f(0f, .7f, .15f);
	private Color3f orange = new Color3f(Color.ORANGE);
	private Color3f blue = new Color3f(Color.BLUE);
	private int maxLayer = 0;

	JCheckBox dualExtrusionColoring;
	private File fr = null;
	private ArrayList<LineSegment> objCommands = new ArrayList<LineSegment>();

	///////ADMIN BOX//////////////////////////////////////////////////////////////
	//
	private static boolean debug = false;										//
	private static final boolean ADMIN_MODE = true;								//
	//////////////////////////////////////////////////////////////////////////////

	private final int SLIDER_MIN = 0;
	private final int SLIDER_MAX = 100;
	private final int SLIDER_INIT = SLIDER_MAX;    //initial frames per second

	protected SimpleUniverse u = null;
	protected BranchGroup scene = null;
	protected float eyeOffset =0.09F;
	protected static int xsize=850, ysize = 500;


	public void init() 
	{
		this.setLayout(new MigLayout());
		this.setSize(xsize*4/5,ysize);
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// TODO Auto-generated catch block

		}
		JButton butt = new JButton("Open");
		butt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				JFileChooser gcodeChooser = new JFileChooser();
				FileFilter filter = new FileNameExtensionFilter("Gcode file", "gcode", "ngc");			
				JFrame chooseFramer = new JFrame("Select Gcode...");
				gcodeChooser.setFileFilter(filter);
				gcodeChooser.showOpenDialog(chooseFramer);
				fr = gcodeChooser.getSelectedFile();
				System.out.println(fr.getPath());
				readGcode();

			}
		});
		this.add(butt, "split");
		if(ADMIN_MODE)
		{
			JButton reDraw = new JButton("ReDraw");
			reDraw.addActionListener(new ActionListener()
			{

				@Override
				public void actionPerformed(ActionEvent arg0) {
					draw();

				}

			});
			this.add(reDraw, "split");
		}
		JLabel dualQuestion = new JLabel("Enable Dual Extrusion Coloring?");
		this.add(dualQuestion, "split");
		dualExtrusionColoring = new JCheckBox();
		this.add(dualExtrusionColoring, "wrap");
		/*
		GraphicsConfiguration config =
			SimpleUniverse.getPreferredConfiguration();
		 */
		c1.setSize(xsize, ysize);
		this.add(c1, "split");
		JSlider jaSlide = new JSlider(JSlider.VERTICAL,
				SLIDER_MIN, SLIDER_MAX, SLIDER_INIT);
		jaSlide.addChangeListener(this);
		add(jaSlide, "split");
		Container zoomBox = new Container();
		zoomBox.setLayout(new MigLayout());
		JButton zoomIn = new JButton("Zoom In");
		zoomIn.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//cameraTranslation.z += 10f;
				updateZoom(10f);

			}

		});
		zoomBox.add(zoomIn, "wrap");
		JButton zoomOut = new JButton("Zoom Out");
		zoomOut.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				//cameraTranslation.z -= 10f;
				updateZoom(-10f);

			}

		});
		zoomBox.add(zoomOut, "wrap");
		add(zoomBox, "wrap");
		add(panButtons(), "center");
		u = new SimpleUniverse(c1);
		//this.validate();

	}
	private Container panButtons()
	{
		Container c = new Container();
		c.setLayout(new MigLayout());
		JButton left = new JButton("Pan Left");
		left.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updatePan(-5f, 0f);

			}

		});
		JButton right = new JButton("Pan Right");
		right.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updatePan(5f, 0f);

			}

		});
		JButton up = new JButton("Pan Up");
		up.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updatePan(0f, 5f);

			}

		});
		JButton down = new JButton("Pan Down");
		down.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0) {
				updatePan(0f, -5f);

			}

		});
		c.add(up, "wrap");
		c.add(left, "split");
		c.add(right, "gapleft 20, wrap");
		c.add(down, "wrap");
		return c;
	}
	private void readGcode()
	{
		System.out.println("trying to obj");
		GcodeViewParse gcvp = new GcodeViewParse();
		ArrayList<String> gcodeText = readFiletoArrayList(fr);
		objCommands = (gcvp.toObj(gcodeText));
		maxLayer = objCommands.get(objCommands.size() - 1).getLayer();
		draw();
	}
	private void draw()
	{
		if(scene != null)
		{
			u.getLocale().removeBranchGraph(scene);
		}
		//u = new SimpleUniverse(c1);
		System.out.println("num bgs" + u.getLocale().numBranchGraphs());
		/*
		if(u.getLocale().numBranchGraphs() > 1)
		{
			List<BranchGroup> bgrl = (List) u.getLocale().getAllBranchGraphs();

			u.getLocale().removeBranchGraph(bgrl.get(0));
		}
		 */
		System.out.println(u.numLocales());
		scene = createSceneGraph(0);
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		u.getViewingPlatform().setNominalViewingTransform();
		u.addBranchGraph(scene);

	}
	private void updateZoom(float f) {

		System.out.println("zoom updating");
		TransformGroup viewTG = u.getViewingPlatform().getViewPlatformTransform();
		Transform3D trans = new Transform3D();
		//Transform3D t3d = new Transform3D();
		//t3d.mul(cameraTranslation);fa
		cameraTranslation.z += f;
		trans.setTranslation(cameraTranslation);

		viewTG.setTransform(trans);

	}
	private void updatePan(float deltaX, float deltaY)
	{
		TransformGroup viewTG = u.getViewingPlatform().getViewPlatformTransform();
		Transform3D trans = new Transform3D();

		cameraTranslation.x += deltaX;
		cameraTranslation.y += deltaY;

		trans.setTranslation(cameraTranslation);

		viewTG.setTransform(trans);
	}
	private LineAttributes getLA()
	{
		LineAttributes laA = new LineAttributes();
		laA.setLineWidth(0.6f);
		laA.setLinePattern(LineAttributes.PATTERN_SOLID);
		return laA;
	}
	private Appearance getColoredAppearance(Color3f col)
	{
		float transparencyValue = 0.3f;
		return getColoredAppearance(col, transparencyValue);
	}
	private Appearance getColoredAppearance(Color3f col, float transparency)
	{
		Appearance ap = new Appearance();
		ap.setColoringAttributes(new ColoringAttributes(col, ColoringAttributes.FASTEST)); 
		ap.setLineAttributes(getLA());


		TransparencyAttributes t_attr =
				new TransparencyAttributes(
						TransparencyAttributes.BLENDED,
						transparency,
						TransparencyAttributes.BLEND_SRC_ALPHA,
						TransparencyAttributes.BLEND_ONE);
		ap.setTransparencyAttributes( t_attr );
		ap.setRenderingAttributes( new RenderingAttributes() );
		return ap;

	}
	public Group[] createLines() {


		System.out.println("creatingLines");
		Group[] gp = new Group[maxLayer + 1];
		int curLayer = -1;





		int colIterate = 0;

		for(LineSegment ls : objCommands)
		{


			Shape3D plShape = new Shape3D(); 
			Point3f[] plaPts = ls.getPointArray();
			LineArray pla = new LineArray(2, LineArray.COORDINATES);
			pla.setCoordinates(0, plaPts);
			plShape.setGeometry(pla);

			/*
			switch (colIterate)
			{
			case 0: plShape.setAppearance(getColoredAppearance(red)); break;
			case 1:  plShape.setAppearance(getColoredAppearance(green)); break;
            case 2:  plShape.setAppearance(getColoredAppearance(blue)); break;
			}
            colIterate++;

            if(colIterate > 2) {colIterate = 0;}
			 */
			if(!ls.getExtruding())
			{
				plShape.setAppearance(getColoredAppearance(white));
			}
			if(!dualExtrusionColoring.isSelected())
			{
				if(ls.getExtruding())
				{
					if(ls.getSpeed() > LOW_SPEED && ls.getSpeed() < MEDIUM_SPEED)
					{
						plShape.setAppearance(getColoredAppearance(green));
					}
					if(ls.getSpeed() > MEDIUM_SPEED && ls.getSpeed() < HIGH_SPEED)
					{
						plShape.setAppearance(getColoredAppearance(blue));
					}
					else if(ls.getSpeed() >= HIGH_SPEED)
					{
						plShape.setAppearance(getColoredAppearance(orange));
					}
					else //Very low speed....
					{
						plShape.setAppearance(getColoredAppearance(red));
					}
				}
			}
			if(dualExtrusionColoring.isSelected())
			{
				if(ls.getExtruding())
				{
					if(ls.getToolhead() == 0)
					{
						plShape.setAppearance(getColoredAppearance(blue));
					}
					if(ls.getToolhead() == 1)
					{
						plShape.setAppearance(getColoredAppearance(orange));
					}
				}
			}
			if(ls.getLayer() != curLayer)
			{

				curLayer = ls.getLayer();
				gp[curLayer] = new Group();


			}
			gp[curLayer].addChild(plShape);
		}
		return gp;


	}
	public BranchGroup createSceneGraph(int i) {

		// Create the root of the branch graph
		BranchGroup objRoot = new BranchGroup();
		try{
			Transform3D myTransform3D = new Transform3D();
			//myTransform3D.setTranslation(new Vector3f(+0.0f,-0.15f,-3.6f));
			myTransform3D.setTranslation(cameraTranslation);
			TransformGroup objTrans = new TransformGroup(myTransform3D);
			objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			objTrans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			Transform3D t = new Transform3D();
			TransformGroup tg = new TransformGroup(t);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			objTrans.addChild(tg);

			Transform3D myTrans = new Transform3D();
			myTrans.setTranslation(new Vector3f(eyeOffset, -eyeOffset, 0F));
			TransformGroup mytg = new TransformGroup(myTrans);



			// bg.addChild(ap);
			tg.removeAllChildren();
			Group[] lines3D = createLines();
			for(Group curGp : lines3D)
			{
				mytg.addChild(curGp);
			}
			tg.addChild(mytg);

			//objTrans.addChild(createPyramid());
			BoundingSphere bounds =
					new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
			Color3f light1Color = new Color3f(.9f, 0.9f, 0.9f);
			Vector3f light1Direction  = new Vector3f(4.0f, -7.0f, -12.0f);
			DirectionalLight light1
			= new DirectionalLight(light1Color, light1Direction);
			light1.setInfluencingBounds(bounds);
			objTrans.addChild(light1);
			// Set up the ambient light
			Color3f ambientColor = new Color3f(1.0f, .4f, 0.3f);
			AmbientLight ambientLightNode = new AmbientLight(ambientColor);
			ambientLightNode.setInfluencingBounds(bounds);
			objTrans.addChild(ambientLightNode);


			MouseRotate behavior = new MouseRotate();
			behavior.setTransformGroup(tg);
			objTrans.addChild(behavior);
			// Create the translate behavior node
			MouseTranslate behavior3 = new MouseTranslate();
			behavior3.setTransformGroup(tg);
			objTrans.addChild(behavior3);
			behavior3.setSchedulingBounds(bounds);

			BoundingSphere zoomSphere = new BoundingSphere(new Point3d(),1000.0d);
			MouseWheelZoom mwz = new MouseWheelZoom(tg);
			mwz.setSchedulingBounds(zoomSphere);
			objTrans.addChild(mwz);

			KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(tg);
			keyNavBeh.setSchedulingBounds(zoomSphere);
			objTrans.addChild(keyNavBeh);

			behavior.setSchedulingBounds(bounds);
			objRoot.addChild(objTrans);
		} catch(Throwable t){System.out.println("Error: "+t);}
		return objRoot;
	}

	public GcodeView() {
	}
	public void destroy() 
	{
		u.removeAllLocales();
	}
	public static ArrayList<String> readFiletoArrayList(File f) {
		ArrayList<String> vect = new ArrayList<String>();
		String curline;
		try {
			BufferedReader bir = new BufferedReader(new FileReader(f));
			curline = bir.readLine();
			while (curline != null) {
				vect.add(curline);
				curline = bir.readLine();
			}

			//System.out.println("ArrayList production was successful");
			bir.close();
		} catch (IOException e) {
			System.err.println("couldnt read file " + f.getAbsolutePath());
		}
		return vect;
	}
	public static void main(String[] args) {

		GcodeView s = new GcodeView();
		//mf = new MainFrame(s, xsize, ysize);
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		System.out.println("wheel rolling");
		int notches = arg0.getWheelRotation();
		float zChange = (10f * notches);
		updateZoom(zChange);

	}

	@Override
	public void stateChanged(ChangeEvent ce) {
		JSlider source = (JSlider)ce.getSource();
		if (!source.getValueIsAdjusting()) {
			int sliderVal = (int)source.getValue(); 
		}
	}

}

