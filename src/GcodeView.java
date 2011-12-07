import java.applet.Applet;
import java.awt.Color;
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
import com.sun.j3d.utils.behaviors.mouse.*;
import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.loaders.Scene;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;
public class GcodeView extends Applet implements MouseWheelListener, KeyListener{

	protected Canvas3D c1 = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	// private Canvas3D c2 = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	private static MainFrame mf;
	
	private final static Vector3f CAMERA_TRANSLATION_DEFAULT = new Vector3f(+0.0f,-0.15f,-3.6f);
	private Vector3f cameraTranslation = new Vector3f(CAMERA_TRANSLATION_DEFAULT);
	
	private Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
	private Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
	private Color3f red = new Color3f(0.7f, .0f, .15f);
	private Color3f green = new Color3f(0f, .7f, .15f);
	private Color3f orange = new Color3f(Color.ORANGE);
	private static boolean debug = false;
	protected SimpleUniverse u = null;
	protected BranchGroup scene = null;
	protected float eyeOffset =0.09F;
	protected static int size=700;
	public void init() {
		setLayout(new FlowLayout());
		GraphicsConfiguration config =
			SimpleUniverse.getPreferredConfiguration();

		c1.setSize(size, size);
		add(c1);
		
		
		scene = createSceneGraph(0);
		u = new SimpleUniverse(c1);
		// This will move the ViewPlatform back a bit so the
		// objects in the scene can be viewed.
		u.getViewingPlatform().setNominalViewingTransform();
		u.addBranchGraph(scene);
	

	}
	private void updateZoom() {
		System.out.println("zoom updating");
		TransformGroup viewTG = u.getViewingPlatform().getViewPlatformTransform();
		Transform3D trans = new Transform3D();
		trans.setTranslation(cameraTranslation);
		viewTG.setTransform(trans);

		
	}
	public Shape3D createLines() {

		ArrayList<LineSegment> objCommands = new ArrayList<LineSegment>();
		
		System.out.println("trying to obj");
		GcodeViewParse gcvp = new GcodeViewParse();
		ArrayList<String> gcodeText = readFiletoArrayList(new File("/home/noah/Downloads/filament_holder.gcode"));
		objCommands = (gcvp.toObj(gcodeText));
		
		Shape3D plShape = new Shape3D(); 
		   
		plShape.removeGeometry(0);  
		 
		for(LineSegment ls : objCommands)
		{
		Point3f[] plaPts = ls.getPointArray();
		LineArray pla = new LineArray(2, LineArray.COORDINATES);
		pla.setCoordinates(0, plaPts);
		plShape.addGeometry(pla);
		}
		System.out.println("plShape geos" + plShape.numGeometries());
		return plShape;
		

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
			Transform3D t = new Transform3D();
			TransformGroup tg = new TransformGroup(t);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
			tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			objTrans.addChild(tg);

			Transform3D myTrans = new Transform3D();
			myTrans.setTranslation(new Vector3f(eyeOffset, -eyeOffset, 0F));
			TransformGroup mytg = new TransformGroup(myTrans);
			
		
			LineAttributes laA = new LineAttributes();
			laA.setLineWidth(1.0f);
			laA.setLinePattern(LineAttributes.PATTERN_SOLID);
			

			Appearance ap = new Appearance();
			ap.setColoringAttributes(new ColoringAttributes(red, ColoringAttributes.SHADE_FLAT)); 
			ap.setLineAttributes(laA);

			ap.setMaterial(new Material(green,black, green, black, 1.0f));

			float transparencyValue = 0.5f;
			TransparencyAttributes t_attr =
				new TransparencyAttributes(
						TransparencyAttributes.BLENDED,
						transparencyValue,
						TransparencyAttributes.BLEND_SRC_ALPHA,
						TransparencyAttributes.BLEND_ONE);
			ap.setTransparencyAttributes( t_attr );
			ap.setRenderingAttributes( new RenderingAttributes() );
			// bg.addChild(ap);
			
			Shape3D lines3D = createLines();
			lines3D.setAppearance(ap);
			mytg.addChild(lines3D);
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

			KeyNavigatorBehavior keyNavBeh = new KeyNavigatorBehavior(tg);
			keyNavBeh.setSchedulingBounds(new BoundingSphere(
					new Point3d(),1000.0));
			objTrans.addChild(keyNavBeh);

			behavior.setSchedulingBounds(bounds);
			objRoot.addChild(objTrans);
		} catch(Throwable t){System.out.println("Error: "+t);}
		return objRoot;
	}
	public Shape3D createPyramid()
	{
		Point3f e = new Point3f(1.0f, 0.0f, 0.0f); // east
		Point3f s = new Point3f(0.0f, 0.0f, 1.0f); // south
		Point3f w = new Point3f(-1.0f, 0.0f, 0.0f); // west
		Point3f n = new Point3f(0.0f, 0.0f, -1.0f); // north
		Point3f t = new Point3f(0.0f, 0.721f, 0.0f); // top

		TriangleArray pyramidGeometry = new TriangleArray(18,
				TriangleArray.COORDINATES);
		pyramidGeometry.setCoordinate(0, e);
		pyramidGeometry.setCoordinate(1, t);
		pyramidGeometry.setCoordinate(2, s);

		pyramidGeometry.setCoordinate(3, s);
		pyramidGeometry.setCoordinate(4, t);
		pyramidGeometry.setCoordinate(5, w);

		pyramidGeometry.setCoordinate(6, w);
		pyramidGeometry.setCoordinate(7, t);
		pyramidGeometry.setCoordinate(8, n);

		pyramidGeometry.setCoordinate(9, n);
		pyramidGeometry.setCoordinate(10, t);
		pyramidGeometry.setCoordinate(11, e);

		pyramidGeometry.setCoordinate(12, e);
		pyramidGeometry.setCoordinate(13, s);
		pyramidGeometry.setCoordinate(14, w);

		pyramidGeometry.setCoordinate(15, w);
		pyramidGeometry.setCoordinate(16, n);
		pyramidGeometry.setCoordinate(17, e);
		GeometryInfo geometryInfo = new GeometryInfo(pyramidGeometry);
		NormalGenerator ng = new NormalGenerator();
		ng.generateNormals(geometryInfo);

		GeometryArray result = geometryInfo.getGeometryArray();
		
		// yellow appearance
		Appearance appearance = new Appearance();
		Color3f color = new Color3f(Color.yellow);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f white = new Color3f(1.0f, 1.0f, 1.0f);
		Texture texture = new Texture2D();
		TextureAttributes texAttr = new TextureAttributes();
		texAttr.setTextureMode(TextureAttributes.MODULATE);
		texture.setBoundaryModeS(Texture.WRAP);
		texture.setBoundaryModeT(Texture.WRAP);
		texture.setBoundaryColor(new Color4f(0.0f, 1.0f, 0.0f, 0.0f));
		Material mat = new Material(color, black, color, white, 70f);
		appearance.setTextureAttributes(texAttr);
		appearance.setMaterial(mat);
		appearance.setTexture(texture);
		Shape3D shape = new Shape3D(result, appearance);
		return shape;
	}
	public GcodeView() {
	}
	public void destroy() {
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
		mf = new MainFrame(s, size, size);
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent arg0) {
		System.out.println("wheel rolling");
		int notches = arg0.getWheelRotation();
		cameraTranslation.z = (10f * notches);
		updateZoom();
		
	}
	@Override
	public void keyPressed(KeyEvent e) {
		System.out.println("camTrans = " + cameraTranslation.z);
		if(e.getKeyChar() == '-')
		{
			cameraTranslation.z -= 10f;
			
		}
		else if(e.getKeyChar() == '=')
		{
			cameraTranslation.z += 10f;
			
		}
		else if(e.getKeyChar() == 'w')
		{
			cameraTranslation.y += 10f;
			
		}
		else if(e.getKeyChar() == 's')
		{
			cameraTranslation.y += 10f;
			
		}
		else if(e.getKeyChar() == 'a')
		{
			cameraTranslation.y += 10f;
			
		}
		else if(e.getKeyChar() == 'd')
		{
			cameraTranslation.y += 10f;
			
		}
		updateZoom();
	}
	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}