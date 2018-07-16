/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.SyncAccess;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.SwitchDT;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.gui.components.CircuitComponent;

import java.awt.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The double throw switch shape
 */
public class SwitchDTShape implements Shape {

    private final PinDescriptions outputs;
    private final String label;
    private boolean closed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public SwitchDTShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.outputs = outputs;
        closed = attributes.get(Keys.CLOSED);
        label = attributes.getCleanLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE * 2, 0), outputs.get(1)))
    .add(new Pin(new Vector(SIZE * 2, -SIZE), outputs.get(2)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        return new Interactor() {
            @Override
            public boolean clicked(CircuitComponent cc, Point pos, IOState ioState, Element element, SyncAccess modelSync) {
                closed = !closed;
                if (ioState != null) {
                    modelSync.access(() -> ((SwitchDT) element).setClosed(closed));
                }
                return true;
            }
        };
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int yOffs = SIZE / 4;
        graphic.drawLine(new Vector(0, 0), new Vector(0, -SIZE2), Style.NORMAL);
        if (closed) {
            graphic.drawLine(new Vector(0, -SIZE2), new Vector(SIZE * 2, 0), Style.NORMAL);
        } else {
            yOffs = 3 * SIZE / 4;
            graphic.drawLine(new Vector(0, -SIZE2), new Vector(SIZE * 2, -SIZE), Style.NORMAL);
        }
        graphic.drawLine(new Vector(SIZE, -yOffs), new Vector(SIZE, -yOffs - SIZE), Style.THIN);
        graphic.drawLine(new Vector(SIZE2, -yOffs - SIZE), new Vector(SIZE + SIZE2, -yOffs - SIZE), Style.THIN);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE, 4), new Vector(SIZE * 2, 4), label, Orientation.CENTERTOP, Style.SHAPE_PIN);
    }
}
