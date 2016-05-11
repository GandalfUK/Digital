package de.neemann.digital.draw.shapes.ieee;

import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * IEEE Standard 91-1984 XOr Shape
 *
 * @author hneemann
 */
public class IEEEXOrShape extends IEEEGenericShape {

    private static final int STEPS = 11;
    private static final int STEPS2 = 7;
    private static final Polygon POLYGON = createPoly();
    private static final Polygon POLYGON2 = createPoly2();

    private static Polygon createPoly() {
        Polygon p = new Polygon(true);

        p.add(SIZE2, SIZE * 2 + SIZE2);
        p.add(new Vector(SIZE, SIZE * 2),
                new Vector(SIZE, 0),
                new Vector(SIZE2, -SIZE2));
        p.add(new Vector(SIZE, -SIZE2),
                new Vector(SIZE*2, 0),
                new Vector(SIZE*3, SIZE));
        p.add(new Vector(SIZE*2, SIZE*2),
                new Vector(SIZE, SIZE*2+SIZE2),
                new Vector(SIZE2, SIZE*2+SIZE2));

        return p;
    }

    private static Polygon createPoly2() {
        Polygon p = new Polygon(false);
        p.add(0, SIZE * 2 + SIZE2);
        p.add(new Vector(SIZE2, SIZE * 2),
                new Vector(SIZE2, 0),
                new Vector(0, -SIZE2));
        return p;
    }

    /**
     * Creates a new instance
     *
     * @param inputs  inputs
     * @param outputs outputs
     * @param invert  true if XNOr
     */
    public IEEEXOrShape(PinDescription[] inputs, PinDescription[] outputs, boolean invert) {
        super(inputs, outputs, invert);
    }

    @Override
    protected void drawIEEE(Graphic graphic) {
        graphic.drawPolygon(POLYGON, Style.NORMAL);
        graphic.drawPolygon(POLYGON2, Style.NORMAL);
    }
}