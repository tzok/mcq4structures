package pl.poznan.put.visualisation;

import org.w3c.dom.svg.SVGDocument;

public interface Drawable {
  void draw();

  SVGDocument finalizeDrawing();
}
