/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.core.switching.RelayDT;
import de.neemann.digital.draw.elements.IOState;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE2;

/**
 * The RelayDT shape
 */
public class RelayDTShape implements Shape {

    private final PinDescriptions inputs;
    private final PinDescriptions outputs;
    private final String label;
    private boolean invers;
    private RelayDT relay;
    private boolean relayIsClosed;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     * @param inputs     the inputs
     * @param outputs    the outputs
     */
    public RelayDTShape(ElementAttributes attributes, PinDescriptions inputs, PinDescriptions outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
        invers = attributes.get(Keys.RELAY_NORMALLY_CLOSED);
        relayIsClosed = invers;
        label = attributes.getCleanLabel();
    }

    @Override
    public Pins getPins() {
        return new Pins()
                .add(new Pin(new Vector(0, -SIZE * 2), inputs.get(0)))
                .add(new Pin(new Vector(SIZE * 2, -SIZE * 2), inputs.get(1)))
                .add(new Pin(new Vector(0, 0), outputs.get(0)))
                .add(new Pin(new Vector(SIZE * 2, 0), outputs.get(1)))
                .add(new Pin(new Vector(SIZE * 2, -SIZE), outputs.get(2)));
    }

    @Override
    public InteractorInterface applyStateMonitor(IOState ioState, Observer guiObserver) {
        relay = (RelayDT) ioState.getElement();
        ioState.getInput(0).addObserverToValue(guiObserver);
        ioState.getInput(1).addObserverToValue(guiObserver);
        return null;
    }

    @Override
    public void readObservableValues() {
        if (relay != null)
            relayIsClosed = relay.isClosed();
    }

    @Override
    public void drawTo(Graphic graphic, Style highLight) {
        int yOffs = SIZE / 4;
        graphic.drawLine(new Vector(0, 0), new Vector(0, -SIZE2), Style.NORMAL);
        if (relayIsClosed) {
            graphic.drawLine(new Vector(0, -SIZE2), new Vector(SIZE * 2, 0), Style.NORMAL);
        } else {
            yOffs = 3 * SIZE / 4;
            graphic.drawLine(new Vector(0, -SIZE2), new Vector(SIZE * 2, -SIZE), Style.NORMAL);
        }
        graphic.drawLine(new Vector(SIZE, -yOffs), new Vector(SIZE, 1 - SIZE), Style.THIN);

        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2, -SIZE)
                .add(SIZE2, -SIZE * 3)
                .add(SIZE + SIZE2, -SIZE * 3)
                .add(SIZE + SIZE2, -SIZE), Style.NORMAL);

        graphic.drawLine(new Vector(SIZE2, -SIZE - SIZE2), new Vector(SIZE + SIZE2, -SIZE * 2 - SIZE2), Style.THIN);

        graphic.drawLine(new Vector(SIZE2, -SIZE * 2), new Vector(0, -SIZE * 2), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE + SIZE2, -SIZE * 2), new Vector(SIZE * 2, -SIZE * 2), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE, 4), new Vector(SIZE * 2, 4), label, Orientation.CENTERTOP, Style.SHAPE_PIN);
    }
}
