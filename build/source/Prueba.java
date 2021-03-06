import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import frames.core.Frame; 
import frames.core.Graph; 
import frames.primitives.Rectangle; 
import frames.primitives.Vector; 
import frames.processing.Scene; 
import frames.processing.Shape; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Prueba extends PApplet {








Scene scene;
// ArrayList<Shape> shapes;
// Shape trackedShape;

Ejes ejes;
Nodos nodos;

Punto _trackedPunto;

Vector _screenCoordinates;
Vector _worldCoordinatesMouse = new Vector();

String _coordinates;

int indexNivelZ = 0;

// Selector
int colorStroke;
int colorFill;
int colorAlpha;

boolean zoomOnRegion;
boolean drawSelector;
boolean drawNivelesZ;

boolean showCoordinates = true;
boolean showNivelZ = true;

boolean addNodo;

public void settings() {
  size(800, 600, P3D);
}

public void setup() {
  scene = new Scene(this);
  scene.setRightHanded();
  scene.setType(Graph.Type.ORTHOGRAPHIC);
  scene.setRadius(50);
  scene.setFieldOfView(PI / 3);
  scene.fitBall();

  ejes = new Ejes(scene);
  nodos = new Nodos(scene);

  colorStroke = 127;
  colorFill   = 127;
  colorAlpha  = 63;

  // Ejes
  int dx = 5;
  int dy = 5;
  int length = dx * dy;
  String[] xBubbleText = {"A", "B", "C", "D", "E"};
  for (int i = 0; i < 5; i++) {
    ejes.addEje(new Vector(-dx, dy * i), new Vector(length + dy, dy * i), xBubbleText[i]);
  }
  String[] yBubbleText = {"1", "2", "3", "4", "5"};
  for (int i = 0; i < 5; i++) {
    ejes.addEje(new Vector(dx * i, length), new Vector(dx * i, -dy), yBubbleText[i]);
  }
  ejes.addNivelZ(5f);
  ejes.addNivelZ(10f);

  // shapes = new Shape[25];
  // for (int i = 0; i < shapes.length; i++) {
  //   shapes[i] = new Shape(scene, shape());
  //   scene.randomize(shapes[i]);
  //   shapes[i].setPrecisionThreshold(25);
  //   shapes[i].setPrecision(Frame.Precision.ADAPTIVE);
  // }
}

public void draw() {
  background(127);
  scene.drawAxes();
  // for (int i = 0; i < shapes.length; i++) {
  //   scene.draw(shapes[i]);
  //   pushStyle();
  //   stroke(255);
  //   scene.drawShooterTarget(shapes[i]);
  //   popStyle();
  // }

  scene.cast();
  // scene.castOnMouseClick();
  if (drawSelector) drawSelector();
  if (showCoordinates) showCoordinates();
  if (drawNivelesZ) drawNivelesZ();
  if (showNivelZ) showNivelZ();
}


public String coordinates() {
  return _coordinates;
}

public void setCoordinates(String coordinates) {
  _coordinates = coordinates;
}

public Punto trackedPunto() {
  return _trackedPunto;
}

public void setTrackedPunto() {
  _trackedPunto = null;

  for (int i = 0; i < ejes.puntos().size(); i++) {
    if (scene.track(mouseX, mouseY, ejes.puntos().get(i))) {
      _trackedPunto = ejes.puntos().get(i);
      return;
    }
  }
}

public Vector worldCoordinatesMouse() {
  return _worldCoordinatesMouse;
}

public void setWorldCoordinatesMouse() {
  if (trackedPunto() == null) {
    _worldCoordinatesMouse = scene.location(new Vector(mouseX, mouseY));
  } else {
    _worldCoordinatesMouse = _trackedPunto.position();
  }
}

public Vector screenCoordinates() {
  return _screenCoordinates;
}

public void setScreenCoordinates(Vector i) {
  _screenCoordinates = i;
}

public void drawSelector() {
  pushStyle();

  rectMode(CORNERS);

  if (mouseX >= screenCoordinates().x()) {
    stroke(0, colorStroke, 0);
    fill(0, colorFill, 0, colorAlpha);
  } else if (mouseX < screenCoordinates().x()){
    stroke(colorStroke, 0, 0);
    fill(colorFill, 0, 0, colorAlpha);
  }

  scene.beginScreenDrawing();
  rect(screenCoordinates().x(), screenCoordinates().y(), mouseX, mouseY);
  scene.endScreenDrawing();

  popStyle();

  println("Hay que implementar un rectSelector !");
}


public void showCoordinates() {
  String coordinates;

  pushStyle();
  scene.beginScreenDrawing();

  textAlign(RIGHT, BOTTOM);
  textSize(14);

  fill(0);
  text(")", width, height);
  coordinates = ")";

  fill(0, 0, 255);
  text(nf(worldCoordinatesMouse().z(), 0, 3), width - textWidth(coordinates), height);
  coordinates = nf(worldCoordinatesMouse().z(), 0, 3) + coordinates;

  fill(0);
  text(" ,", width - textWidth(coordinates), height);
  coordinates = " ," + coordinates;

  fill(0, 255, 0);
  text(nf(worldCoordinatesMouse().y(), 0, 3), width - textWidth(coordinates), height);
  coordinates = nf(worldCoordinatesMouse().y(), 0, 3) + coordinates;

  fill(0);
  text(" ,", width - textWidth(coordinates), height);
  coordinates = " ," + coordinates;

  fill(255, 0, 0);
  text(nf(worldCoordinatesMouse().x(), 0, 3), width - textWidth(coordinates), height);
  coordinates = nf(worldCoordinatesMouse().x(), 0, 3) + coordinates;

  fill(0);
  text("(", width - textWidth(coordinates), height);
  coordinates = "(" + coordinates;

  setCoordinates(coordinates);

  scene.endScreenDrawing();
  popStyle();
}

public void showNivelZ() {
  pushStyle();
  scene.beginScreenDrawing();

  textAlign(RIGHT, BOTTOM);
  textSize(14);

  fill(0);

  int widthAfter = 0;
  if (showCoordinates) widthAfter = 10;

  text("Nivel Z: " + nf(ejes.nivelesZ().get(indexNivelZ), 0, 3),
    width - textWidth(coordinates()) - widthAfter, height);

  scene.endScreenDrawing();
  popStyle();
}

public void zoomOnRegion() {
  if (screenCoordinates().x() < mouseX) {
    Rectangle screenRectangle = new Rectangle((int) screenCoordinates().x(),
      (int) screenCoordinates().y(), mouseX - (int) screenCoordinates().x(),
      mouseY - (int) screenCoordinates().y());
    scene.fitScreenRegionInterpolation(screenRectangle); // version antigua
    // scene.zoomOnRegion(screenRectangle); // version nueva
  } else {
    println("Hay que implementar un zoom out !");
  }
}

public void drawNivelesZ() {
  for (int i = 0; i < ejes.nivelesZ().size(); i++) {
    pushStyle();
    if (i == ejes.actualIndexNivelZ()) {
      stroke(144, 238, 144);
      fill(144, 238, 144, 15);
    } else {
      stroke(31, 117, 254);
      fill(31, 117, 254, 15);
    }
    pushMatrix();
    translate(0, 0, ejes.nivelesZ().get(i));
    ellipse(scene.center().x(), scene.center().y(),
      2 * scene.radius(), 2 * scene.radius());
    popMatrix();
    popStyle();
  }
}

public void addNodo() {
  nodos.add(worldCoordinatesMouse());
  println(nodos.nodos().size());
}

public void mouseClicked(MouseEvent event) {
  if (mouseButton == CENTER  && event.getCount() == 2) scene.fitBallInterpolation();
  if (addNodo && mouseButton == LEFT && event.getCount() == 1) {
    addNodo();
    addNodo = false;
    println("done");
  }
}

public void mouseDragged(MouseEvent event) {
  if (mouseButton == LEFT) {
    drawSelector = true;
    if (event.isShiftDown()) {
      zoomOnRegion = true;
    }
  } else if (mouseButton == CENTER) {
    scene.mouseTranslate(scene.eye());
  }
  else if (mouseButton == RIGHT) {
    scene.mouseCAD(new Vector (0, 0, 1));
  }
//   else if (mouseButton == CENTER)
//     scene.mouseLookAround(); // scene.scale(mouseX - pmouseX, defaultShape());
}

public void mouseMoved() {
  setTrackedPunto();
  setWorldCoordinatesMouse();
  // scene.setCenter(new Vector(worldCoordinatesMouse().x(),
  //   worldCoordinatesMouse().y(), 0));
  // println(scene.center());


  // scene.setAnchor(new Vector(worldCoordinates.x(),
  //   worldCoordinates.y(), 0));
}

public void mouseWheel(MouseEvent event) {
  scene.translate(new Vector(0, 0, event.getCount() * 50), -1, scene.eye());
  // scene.translate(new Vector(0, 0, event.getCount() * 50), -1, scene.eye()); // no funciona en la ultima version
  setWorldCoordinatesMouse();
}

public void mousePressed() {
  if (mouseButton == LEFT) setScreenCoordinates(new Vector(mouseX, mouseY));
}

public void mouseReleased() {
  if (zoomOnRegion) zoomOnRegion();

  setScreenCoordinates(null);
  drawSelector = false;
  zoomOnRegion = false;
}

public void keyPressed() {
  switch (key) {
    case 'c':
      showCoordinates = !showCoordinates;
      if (!showCoordinates) setCoordinates("");
      break;
    case 'e':
      nodos.setDrawEtiqueta(!nodos.drawEtiqueta());
      break;
    case 'n':
      addNodo = !addNodo;
      if (addNodo) {
        println("add nodo");
      } else {
        println("cancel");
      }
      break;
    case '+':
      indexNivelZ = indexNivelZ < ejes.nivelesZ().size() - 1 ? indexNivelZ + 1 : 0;
      ejes.setActualIndexNivelZ(indexNivelZ);
      break;
    case '-':
      indexNivelZ = 0 < indexNivelZ ? indexNivelZ - 1 : ejes.nivelesZ().size() - 1;
      ejes.setActualIndexNivelZ(indexNivelZ);
      break;
    case 'z':
      drawNivelesZ = !drawNivelesZ;
      break;
  }
}

  //   if (mouseButton == LEFT) {
  //     trackedShape = null;
  //     for (int i = 0; i < shapes.length; i++) {
  //       if (scene.track(mouseX, mouseY, shapes[i])) {
  //         trackedShape = shapes[i];
  //         trackedShape.
  //       }
  //     }

  //   // trackedShape = null;
  //   // for (int i = 0; i < shapes.length; i++)
  //   //   if (scene.track(mouseX, mouseY, shapes[i])) {
  //   //     trackedShape = shapes[i];
  //   //     break;
  //   //   }
  // }

  // Frame defaultShape() {
  //   return trackedShape == null ? scene.eye() : trackedShape;
  // }
  //
  // PShape shape() {
  //   PShape fig = scene.is3D() ? createShape(BOX, 15) : createShape(RECT, 0, 0, 15, 15);
  //   fig.setStroke(255);
  //   fig.setFill(color(random(0, 255), random(0, 255), random(0, 255)));
  //   return fig;
  // }
class Eje extends Shape {
  Scene _scene;

  Vector _i;
  Vector _j;

  String _bubbleText;
  int _bubbleSize;
  int _bubbleTextColor;

  int _ejeColor;
  int _ejeStroke;

  Eje(Scene scene, Vector i, Vector j, String bubbleText) {
    this(scene, i, j, bubbleText, 40, color(239, 127, 26), 3, color(0));
  }

  Eje(Scene scene, Vector i, Vector j, String bubbleText,
    int bubbleSize, int ejeColor, int ejeStroke, int bubbleTextColor) {
    super(scene);

    _scene = scene;

    setI(i);
    setJ(j);

    setBubbleText(bubbleText);
    setBubbleSize(bubbleSize);
    setBubbleTextColor(bubbleTextColor);

    setEjeColor(ejeColor);
    setEjeStroke(ejeStroke);
  }

  public Scene scene() {
    return _scene;
  }

  public Vector i() {
    return _i;
  }

  public void setI(Vector i) {
    _i = i;
  }

  public Vector j() {
    return _j;
  }

  public void setJ(Vector j) {
    _j = j;
  }

  public String bubbleText() {
    return _bubbleText;
  }

  public void setBubbleText(String bubbleText) {
    _bubbleText = bubbleText;
  }

  public int bubbleSize() {
    return _bubbleSize;
  }

  public void setBubbleSize(int bubbleSize) {
    _bubbleSize = bubbleSize;
  }

  public int bubbleTextColor() {
    return _bubbleTextColor;
  }

  public void setBubbleTextColor(int bubbleTextColor) {
    _bubbleTextColor = bubbleTextColor;
  }

  public int ejeColor() {
    return _ejeColor;
  }

  public void setEjeColor(int ejeColor) {
    _ejeColor = ejeColor;
  }

  public int ejeStroke() {
    return _ejeStroke;
  }

  public void setEjeStroke(int ejeStroke) {
    _ejeStroke = ejeStroke;
  }

  public float nivelZ() {
    return position().z();
  }

  public void setNivelZ(float nivelZ) {
    setI(new Vector(i().x(), i().y(), nivelZ));
    setJ(new Vector(j().x(), j().y(), nivelZ));
    setPosition(new Vector(position().x(), position().y(), nivelZ));
  }

  public @Override
  void setGraphics(PGraphics pGraphics) {
    pGraphics.pushStyle();
    pGraphics.stroke(ejeColor());
    pGraphics.strokeWeight(ejeStroke());
    pGraphics.line(i().x(), i().y(), j().x(), j().y());
    pGraphics.popStyle();

    Vector iScreen = scene().screenLocation(i());
    Vector jScreen = scene().screenLocation(j());
    Vector parallelDirection = Vector.subtract(jScreen, iScreen);
    parallelDirection.normalize();

    Vector center = Vector.add(iScreen,
      Vector.multiply(parallelDirection, -bubbleSize() / 2));

    scene().beginScreenDrawing();
    pushStyle();
    stroke(ejeColor());
    strokeWeight(ejeStroke());
    fill(red(ejeColor()), green(ejeColor()), blue(ejeColor()), 63);
    ellipse(center.x(), center.y(), bubbleSize(), bubbleSize());
    popStyle();

    pushStyle();
    textAlign(CENTER, CENTER);
    textSize(0.5f * bubbleSize());  // problemas
    fill(bubbleTextColor());
    text(bubbleText(), center.x(), center.y());  // problemas
    popStyle();
    scene().endScreenDrawing();
  }
}
/**
 * stressUNAL
 *
 * @author
 *
 *
 */

 /**
  * A class to store many eje.
  */

class Ejes {
  Scene _scene;
  ArrayList<Eje> _ejes;
  ArrayList<Punto> _puntos;

  int _actualIndexNivelZ;
  ArrayList<Float> _nivelesZ;

  /**
   * Creates a ejes.
   */
  public Ejes(Scene scene) {
    _scene = scene;

    _ejes = new ArrayList();
    _puntos = new ArrayList();

    _actualIndexNivelZ = 0;

    _nivelesZ = new ArrayList();
    _nivelesZ.add(0f);
  }

  protected Scene scene() {
    return _scene;
  }

  public ArrayList<Eje> ejes() {
    return _ejes;
  }

  public ArrayList<Punto> puntos() {
    return _puntos;
  }

  public int actualIndexNivelZ() {
    return _actualIndexNivelZ;
  }

  public void setActualIndexNivelZ(int index) {
    if ((0 <= index) && (index < nivelesZ().size())) {
      _actualIndexNivelZ = index;
      for (Punto punto : puntos()) {
        punto.setNivelZ(nivelesZ().get(actualIndexNivelZ()));
      }
      for (Eje eje : ejes()) {
        eje.setNivelZ(nivelesZ().get(actualIndexNivelZ()));
      }
    }
  }

  public ArrayList<Float> nivelesZ() {
    return _nivelesZ;
  }

  public void addNivelZ(float nivelZ) {
    nivelesZ().add(nivelZ);
  }

  public void addEje(Vector i, Vector j, String bubbleTexto) {
    addEje(new Eje(scene(), i, j, bubbleTexto));
  }

  public void addEje(Eje eje) {
    _ejes.add(eje);
    addPuntos();
    moveCenterScene();
    // scene().setAnchor(new Vector(25, 25, 0));
  }

  /**
   * Add puntos located at intersection between each eje.
   */
  public void addPuntos() {
    int ejesSize = ejes().size();

    if (ejesSize > 1) {
      Eje lastEje = ejes().get(ejesSize - 1);
      Vector intersection;

      for (int i = 0; i < ejesSize - 1; i++) {
        intersection = intersectionBetweenEjes(ejes.ejes().get(i), lastEje);
        if (intersection != null) {
          _puntos.add(new Punto(scene(), intersection));
        }
      }
    }
  }

  public void moveCenterScene() {
    Vector center = new Vector();

    for (int i = 0; i < puntos().size(); i++) {
      center.add(puntos().get(i).position());
    }
    center.divide(puntos().size());

    scene().setCenter(center);
  }

  /**
   * Add a point
   */
  public void addPunto(Vector i) {
    puntos().add(new Punto(scene(), i));
  }

  /**
   * Get vector intersection between two ejes
   */
  public Vector intersectionBetweenEjes(Eje eje1, Eje eje2) {
    Vector intersection;

    float dx1 = eje1.j().x() - eje1.i().x();
    float dx2 = eje2.j().x() - eje2.i().x();

    float dy1 = eje1.j().y() - eje1.i().y();
    float dy2 = eje2.j().y() - eje2.i().y();

    float dx12 = eje2.i().x() - eje1.i().x();
    float dy12 = eje2.i().y() - eje1.i().y();

    float alpha = (dx2 * dy1 - dx1 * dy2);

    if (1e-5f < abs(alpha)) {
      alpha = 1 / alpha;

      float t1 = alpha * (dx2 * dy12 - dy2 * dx12);
      float t2 = alpha * (dx1 * dy12 - dy1 * dx12);

      if ((0 <= t1) && (t1 <= 1) && (0 <= t2) && (t2 <= 1)) {
        Vector i = new Vector(eje1.i().x(), eje1.i().y(), eje1.i().z());
        Vector j = new Vector(eje1.j().x(), eje1.j().y(), eje1.j().z());
        i.multiply(1 - t1);
        j.multiply(t1);
        intersection = Vector.add(i, j);
      } else {
        intersection = null;  // the lines don't cross between them
      }
    } else {
      intersection = null;  // the line are parallels
    }

    return intersection;
  }
}
class Nodo extends Frame{
  Scene _scene;

  String _etiqueta;

  float _sphereSize;
  int _sphereColor;

  int _etiquetaColor;
  int _etiquetaSize;

  boolean _drawEtiqueta;

  Nodo(Scene scene, Vector i, String etiqueta) {
    this(scene, i, etiqueta, 0.25f, color(128, 0, 255), color(0), 14, true);
  }

  Nodo(Scene scene, Vector i, String etiqueta, float sphereSize,
    int sphereColor, int etiquetaColor, int etiquetaSize,
    boolean drawEtiqueta) {
    super(scene);

    setScene(scene);
    setPosition(i);

    setEtiqueta(etiqueta);

    setSphereSize(sphereSize);
    setSphereColor(sphereColor);

    setEtiquetaColor(etiquetaColor);
    setEtiquetaSize(etiquetaSize);

    setDrawEtiqueta(drawEtiqueta);
  }

  public Scene scene() {
    return _scene;
  }

  public void setScene(Scene scene) {
    _scene = scene;
  }

  public float sphereSize() {
    return _sphereSize;
  }

  public void setSphereSize(float sphereSize) {
    _sphereSize = sphereSize;
  }

  public int sphereColor() {
    return _sphereColor;
  }

  public void setSphereColor(int sphereColor) {
    _sphereColor = sphereColor;
  }

  public String etiqueta() {
    return _etiqueta;
  }

  public void setEtiqueta(String etiqueta) {
    _etiqueta = etiqueta;
  }

  public int etiquetaColor() {
    return _etiquetaColor;
  }

  public void setEtiquetaColor(int etiquetaColor) {
    _etiquetaColor = etiquetaColor;
  }

  public float etiquetaSize() {
    return _etiquetaSize;
  }

  public void setEtiquetaSize(int etiquetaSize) {
    _etiquetaSize = etiquetaSize;
  }

  public boolean drawEtiqueta() {
    return _drawEtiqueta;
  }

  public void setDrawEtiqueta(boolean drawEtiqueta) {
    _drawEtiqueta = drawEtiqueta;
  }

  public @Override
  void visit() {
    pushStyle();
    noStroke();
    fill(sphereColor());
    sphere(sphereSize());
    popStyle();

    Vector iScreen = scene().screenLocation(position());

    if (drawEtiqueta()) {
      scene().beginScreenDrawing();
      pushStyle();
      textAlign(CENTER, CENTER);
      textSize(etiquetaSize());  // problemas
      fill(etiquetaColor());
      text(etiqueta(), iScreen.x() + etiquetaSize(), iScreen.y() - etiquetaSize());  // problemas
      popStyle();
      scene().endScreenDrawing();
    }
  }
}





/* ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR ERROR
*/



// class Nodo extends Frame{
//   Scene _scene;
//
//   String _etiqueta;
//
//   float _sphereSize;
//   int _sphereColor;
//
//   int _etiquetaColor;
//   int _etiquetaSize;
//
//   // boolean _drawEtiqueta;
//
//   Nodo(Scene scene, Vector i, String etiqueta) {
//     // this(scene, i, etiqueta, 0.25, color(128, 0, 255), color(0), 12, true);
//     this(scene, i, etiqueta, 0.25, color(128, 0, 255), color(0), 12);
//   }
//
//   // Nodo(Scene scene, Vector i, String etiqueta, float sphereSize,
//   //   int sphereColor, int etiquetaColor, int etiquetaSize,
//   //   boolean drawEtiqueta) {
//   Nodo(Scene scene, Vector i, String etiqueta, float sphereSize,
//     int sphereColor, int etiquetaColor, int etiquetaSize) {
//     super(scene);
//
//     setScene(scene);
//     setPosition(i);
//
//     setEtiqueta(etiqueta);
//
//     setSphereSize(sphereSize);
//     setSphereColor(sphereColor);
//
//     setEtiquetaColor(etiquetaColor);
//     setEtiquetaSize(etiquetaSize);
//
//     // setDrawEtiqueta(drawEtiqueta);
//   }
//
//   Scene scene() {
//     return _scene;
//   }
//
//   void setScene(Scene scene) {
//     _scene = scene;
//   }
//
//   float sphereSize() {
//     return _sphereSize;
//   }
//
//   void setSphereSize(float sphereSize) {
//     _sphereSize = sphereSize;
//   }
//
//   int sphereColor() {
//     return _sphereColor;
//   }
//
//   void setSphereColor(int sphereColor) {
//     _sphereColor = sphereColor;
//   }
//
//   String etiqueta() {
//     return _etiqueta;
//   }
//
//   void setEtiqueta(String etiqueta) {
//     _etiqueta = etiqueta;
//   }
//
//   int etiquetaColor() {
//     return _etiquetaColor;
//   }
//
//   void setEtiquetaColor(int etiquetaColor) {
//     _etiquetaColor = etiquetaColor;
//   }
//
//   float etiquetaSize() {
//     return _etiquetaSize;
//   }
//
//   void setEtiquetaSize(int etiquetaSize) {
//     _etiquetaSize = etiquetaSize;
//   }
//
//   // boolean drawEtiqueta() {
//   //   return _drawEtiqueta;
//   // }
//
//   // void setDrawEtiqueta(boolean drawEtiqueta) {
//   //   _drawEtiqueta = drawEtiqueta;
//   // }
//
//   @Override
//   void visit() {
//     pushStyle();
//     noStroke();
//     fill(sphereColor());
//     sphere(sphereSize());
//     popStyle();
//
//     // Vector iScreen = scene().screenLocation(position());
//
//     // if (drawEtiqueta()) {
//     //   scene().beginScreenDrawing();
//     //   pushStyle();
//     //   textAlign(CENTER, CENTER);
//     //   textSize(etiquetaSize());  // problemas
//     //   fill(etiquetaColor());
//     //   text(etiqueta(), iScreen.x(), iScreen.y());  // problemas
//     //   popStyle();
//     //   scene().endScreenDrawing();
//     // }
//   }
// }
class Nodos {
  Scene _scene;

  ArrayList<Nodo> _nodos;

  boolean _drawEtiqueta;

  public Nodos(Scene scene) {
    setScene(scene);

    _nodos = new ArrayList();

    setDrawEtiqueta(true);
  }

  public Scene scene() {
    return _scene;
  }

  public void setScene(Scene scene) {
    _scene = scene;
  }

  public ArrayList<Nodo> nodos() {
    return _nodos;
  }

  public boolean drawEtiqueta() {
    return _drawEtiqueta;
  }

  public void setDrawEtiqueta(boolean drawEtiqueta) {
    for (Nodo nodo : nodos()) {
      nodo.setDrawEtiqueta(drawEtiqueta);
    }
    _drawEtiqueta = drawEtiqueta;
  }

  public void add(Vector i) {
    add(new Nodo(scene(), i, Integer.toString(nodos().size() + 1)));
  }

  public void add(Nodo nodo) {
    nodos().add(nodo);
  }
}
class Punto extends Frame {
  Scene _scene;

  int _crossSize;
  int _crossColor;
  float _crossWeightStroke;

  Punto(Scene scene) {
    this(scene, new Vector());
  }

  Punto(Scene scene, Vector i) {
    super(scene);

    _crossSize = 20;
    _crossColor = color(53, 56, 57);
    _crossWeightStroke = 1.5f;

    setPosition(i);
    _scene = scene;
  }

  public Scene scene() {
    return _scene;
  }

  public int crossSize() {
    return _crossSize;
  }

  public int crossColor() {
    return _crossColor;
  }

  public float crossWeightStroke() {
    return _crossWeightStroke;
  }

  public float nivelZ() {
    return position().z();
  }

  public void setNivelZ(float nivelZ) {
    setPosition(new Vector(position().x(), position().y(), nivelZ));
  }

public @Override
void visit() {
  Vector center = scene.screenLocation(this.position());
  this.graph().beginScreenDrawing();
  pushStyle();
  stroke(crossColor());
  strokeWeight(crossWeightStroke());
  noFill();
  ellipse(center.x(), center.y(), 3 * crossSize() / 8, 3 * crossSize() / 8);
  line(center.x() - crossSize() / 2, center.y(),
    center.x() + crossSize() / 2, center.y());
  line(center.x(), center.y() - crossSize() / 2,
    center.x(), center.y() + crossSize() / 2);
  popStyle();
  this.graph().endScreenDrawing();
}
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Prueba" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
