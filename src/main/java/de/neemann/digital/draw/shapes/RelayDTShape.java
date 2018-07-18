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
    private final int poles;
    private final boolean invers;
    private RelayDT relay;
    private boolean relayIsClosed;
    private Pins pins;

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
        label = attributes.getCleanLabel();
        poles = attributes.get(Keys.POLES);
        invers = attributes.get(Keys.RELAY_NORMALLY_CLOSED);
        relayIsClosed = invers;
    }

    @Override
    public Pins getPins() {
        if (pins == null) {
            pins = new Pins()
                    .add(new Pin(new Vector(0, -SIZE * 3), inputs.get(0)))
                    .add(new Pin(new Vector(SIZE * 2, -SIZE * 3), inputs.get(1)));

            final int relayStepY = 2 * SIZE;
            int relayBaseY = 0;
            for (int p = 0; p < poles; p++){
                pins
                    .add(new Pin(new Vector(0, relayBaseY - SIZE), outputs.get(p * 4)))
                    .add(new Pin(new Vector(0, relayBaseY - SIZE), outputs.get(p * 4 + 1)))
                    .add(new Pin(new Vector(SIZE * 2, relayBaseY), outputs.get(p * 4 + 2)))
                    .add(new Pin(new Vector(SIZE * 2, relayBaseY - SIZE), outputs.get(p * 4 + 3)));
                relayBaseY+=relayStepY;
            }
        }
        return pins;
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
        final int relayTipY;
        if (relayIsClosed) {
            relayTipY = SIZE;
        } else {
            relayTipY = 0;
        }

        final int relayStepY = 2 * SIZE;
        int relayBaseY = 0;
        for (int p = 0; p < poles; p++) {
            graphic.drawPolygon(new Polygon(false)
                                .add(0, relayBaseY - SIZE)
                                .add(0, relayBaseY - SIZE2)
                                .add(SIZE * 2, relayBaseY - relayTipY), Style.NORMAL);
            relayBaseY+=relayStepY;
        }

        final int yOffs = (SIZE / 4) + (relayTipY / 2);
        graphic.drawLine(new Vector(SIZE, (poles - 1) * SIZE * 2 - yOffs), new Vector(SIZE, 1 - SIZE * 2), Style.DASH);

        // the coil
        graphic.drawPolygon(new Polygon(true)
                .add(SIZE2, -SIZE * 2)
                .add(SIZE2, -SIZE * 4)
                .add(SIZE + SIZE2, -SIZE * 4)
                .add(SIZE + SIZE2, -SIZE * 2), Style.NORMAL);

        graphic.drawLine(new Vector(SIZE2, -SIZE * 2 - SIZE2), new Vector(SIZE + SIZE2, -SIZE * 3 - SIZE2), Style.THIN);

        graphic.drawLine(new Vector(SIZE2, -SIZE * 3), new Vector(0, -SIZE * 3), Style.NORMAL);
        graphic.drawLine(new Vector(SIZE + SIZE2, -SIZE * 3), new Vector(SIZE * 2, -SIZE * 3), Style.NORMAL);

        if (label != null && label.length() > 0)
            graphic.drawText(new Vector(SIZE, 4), new Vector(SIZE * 2, 4), label, Orientation.CENTERTOP, Style.SHAPE_PIN);
    }
}
