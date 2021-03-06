import frames.core.Frame;
import frames.core.Graph;
import frames.primitives.Rectangle;
import frames.primitives.Vector;
import frames.processing.Scene;
import frames.processing.Shape;

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

void settings() {
  size(800, 600, P3D);
}

void setup() {
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

void draw() {
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


String coordinates() {
  return _coordinates;
}

void setCoordinates(String coordinates) {
  _coordinates = coordinates;
}

Punto trackedPunto() {
  return _trackedPunto;
}

void setTrackedPunto() {
  _trackedPunto = null;

  for (int i = 0; i < ejes.puntos().size(); i++) {
    if (scene.track(mouseX, mouseY, ejes.puntos().get(i))) {
      _trackedPunto = ejes.puntos().get(i);
      return;
    }
  }
}

Vector worldCoordinatesMouse() {
  return _worldCoordinatesMouse;
}

void setWorldCoordinatesMouse() {
  if (trackedPunto() == null) {
    _worldCoordinatesMouse = scene.location(new Vector(mouseX, mouseY));
  } else {
    _worldCoordinatesMouse = _trackedPunto.position();
  }
}

Vector screenCoordinates() {
  return _screenCoordinates;
}

void setScreenCoordinates(Vector i) {
  _screenCoordinates = i;
}

void drawSelector() {
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


void showCoordinates() {
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

void showNivelZ() {
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

void zoomOnRegion() {
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

void drawNivelesZ() {
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

void addNodo() {
  nodos.add(worldCoordinatesMouse());
  println(nodos.nodos().size());
}

void mouseClicked(MouseEvent event) {
  if (mouseButton == CENTER  && event.getCount() == 2) scene.fitBallInterpolation();
  if (addNodo && mouseButton == LEFT && event.getCount() == 1) {
    addNodo();
    addNodo = false;
    println("done");
  }
}

void mouseDragged(MouseEvent event) {
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

void mouseMoved() {
  setTrackedPunto();
  setWorldCoordinatesMouse();
  // scene.setCenter(new Vector(worldCoordinatesMouse().x(),
  //   worldCoordinatesMouse().y(), 0));
  // println(scene.center());


  // scene.setAnchor(new Vector(worldCoordinates.x(),
  //   worldCoordinates.y(), 0));
}

void mouseWheel(MouseEvent event) {
  scene.translate(new Vector(0, 0, event.getCount() * 50), -1, scene.eye());
  // scene.translate(new Vector(0, 0, event.getCount() * 50), -1, scene.eye()); // no funciona en la ultima version
  setWorldCoordinatesMouse();
}

void mousePressed() {
  if (mouseButton == LEFT) setScreenCoordinates(new Vector(mouseX, mouseY));
}

void mouseReleased() {
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
