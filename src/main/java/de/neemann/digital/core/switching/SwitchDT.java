/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.switching;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.wiring.bus.CommonBusValue;
import de.neemann.digital.lang.Lang;

/**
 * A simple double-throw switch basaed on the core switch code.
 */
public class SwitchDT implements Element, NodeInterface {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(SwitchDT.class)
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.CLOSED);

    /**
    * The convention is that pin1 (in1/output1/out1, etc.) is always the common terminal.
    */
    private final ObservableValue output1;
    private final ObservableValue output2;
    private final ObservableValue output3;
    private final int bits;
    private boolean closed;
    private Switch.SwitchModel switchModel;

    /**
     * Creates a new instance of the double-throw switch.
     *
     * @param attr the elements attributes
     */
    public SwitchDT(ElementAttributes attr) {
        this(attr, attr.get(Keys.CLOSED), "out1", "out2", "out3");
        output1.setPinDescription(DESCRIPTION);
        output2.setPinDescription(DESCRIPTION);
        output3.setPinDescription(DESCRIPTION);
    }

    /**
     * Creates a new instance
     *
     * @param attr   the elements attributes
     * @param closed true if switch is closed
     * @param out1   name of output 1
     * @param out2   name of output 2
     * @param out3   name of output 2
     */
    public SwitchDT(ElementAttributes attr, boolean closed, String out1, String out2, String out3) {
        bits = attr.getBits();
        this.closed = closed;
        output1 = new ObservableValue(out1, bits).setBidirectional().setToHighZ();
        output2 = new ObservableValue(out2, bits).setBidirectional().setToHighZ();
        output3 = new ObservableValue(out3, bits).setBidirectional().setToHighZ();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
    /**
     * CASE: This code is based on the switch.java code, which (from what I can make out)
     *       "optimises" the switch if possible by replaceing it with a "SimpleSwitch".
     *       The switch.jave code seems to only make use of the "RealSwiych" model if it
     *       has no other choice, (presumably because the "RealSwitch" is much more expensive
     *       to simluate due to the required net re-caclulations each time it is switched).
     *       This optimisation is more complex for a double-throw switch, but I've tried
     *         to follow the same idea below, I added comments to aid my understanding,
     *         they my not all be accurate! :-)
     */
        ObservableValue input1 = inputs.get(0).addObserverToValue(this).checkBits(bits, null);
        ObservableValue input2 = inputs.get(1).addObserverToValue(this).checkBits(bits, null);
        ObservableValue input3 = inputs.get(2).addObserverToValue(this).checkBits(bits, null);
        if (input1 instanceof CommonBusValue) {
            // The common pin is conneted to a net.
            final CommonBusValue in1 = (CommonBusValue) input1;
            if (input2 instanceof CommonBusValue && input3 instanceof CommonBusValue) {
                // All 3 pins are connected to nets.
                // It might still be possible to optimise if some of those nets resolve to constants.
                final CommonBusValue in2 = (CommonBusValue) input2;
                final CommonBusValue in3 = (CommonBusValue) input3;
                ObservableValue constant1 = in1.searchConstant();
                if (constant1 != null)
                // Pin 1 (the common pin) resolves to a constant, so this switch
                // can be simplified to s Simple Fan Out Switch, (one constant input
                // on the common pin switching to the 2 other outputs).
                    switchModel = new SimpleSwitchFO(constant1, output2, output3);
                else {
                    // Pin 1 is not a constant.
                    ObservableValue constant2 = in2.searchConstant();
                    ObservableValue constant3 = in3.searchConstant();
                    if (constant2 != null && constant3 != null)
                    // Pin 1 is not constant, but the others are, so this switch
                    // can be simplified to a simple Fan In Switch, (two constant
                    // inputs and one output).
                        switchModel = new SimpleSwitchFI(constant2, constant3, output1);
                    else
                    // We have a complex set of net connections, we can't use a simplification.
                        switchModel = new RealSwitchDT(in1, in2, in3);
                }
            } else if (input2 instanceof CommonBusValue || input3 instanceof CommonBusValue) {
                // Pin 1 is connected to a net and one of the other pins are too.
                // I try to simplify this as a "Mixed Switch" (a combination of a simple
                // switch and a real switch). This may not be the best approach!
                final CommonBusValue in;
                final ObservableValue output;
                final boolean invert;
                if (input2 instanceof CommonBusValue) {
                    in = (CommonBusValue) input2;
                    output = output3;
                    invert = false;
                } else {
                    in = (CommonBusValue) input3;
                    output = output2;
                    invert = true;
                }
                switchModel = new MixedSwitchDT(invert, in1, in, output);
            } else {
                // Pin 1 is connected to a net, but none of the other pins are.
                // This can be simplified to a simple Fan Out switch.
                switchModel = new SimpleSwitchFO(input1, output2, output3);
            }
        } else {
            // Pin 1 is not connected to a net.
            if (input2 instanceof CommonBusValue || input3 instanceof CommonBusValue) {
                // At least one of the other pins is connected to a net.
                // I simplify this as a Fan In switch.
                switchModel = new SimpleSwitchFI(input2, input3, output1);
            } else {
                // No pins are connected to nets. Something is wrong here!
                throw new NodeException(Lang.get("err_switchHasNoNet"), output1, output2, output3);
            }
        }
    }

    @Override
    public ObservableValues getOutputs() {
        return new ObservableValues(output1, output2, output3);
    }

    @Override
    public void registerNodes(Model model) {
    }

    @Override
    public void init(Model model) throws NodeException {
        switchModel.setModel(model);
        switchModel.setClosed(closed);
        hasChanged();
    }

    @Override
    public void hasChanged() {
        switchModel.propagate();
    }

    /**
     * Sets the closed state of the switch
     *
     * @param closed true if closed
     */
    public void setClosed(boolean closed) {
        if (this.closed != closed) {
            this.closed = closed;
            switchModel.setClosed(closed);
            hasChanged();
        }
    }

    /**
     * @return true if switch is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return output 1
     */
    ObservableValue getOutput1() {
        return output1;
    }

    /**
     * @return output 2
     */
    ObservableValue getOutput2() {
        return output2;
    }

    /**
     * @return output 3
     */
    ObservableValue getOutput3() {
        return output3;
    }

    private static final class SimpleSwitchFO implements Switch.SwitchModel {
        private final ObservableValue input;
        private final ObservableValue output1;
        private final ObservableValue output2;
        private boolean closed;

        SimpleSwitchFO(ObservableValue input, ObservableValue output1, ObservableValue output2) {
            this.input = input;
            this.output1 = output1;
            this.output2 = output2;
        }

        @Override
        public void propagate() {
            if (closed) {
                output1.set(input.getValue(), input.getHighZ());
                output2.setToHighZ();
            } else {
                output1.setToHighZ();
                output2.set(input.getValue(), input.getHighZ());
            }
        }

        @Override
        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        @Override
        public void setModel(Model model) {
        }
    }

    private static final class SimpleSwitchFI implements Switch.SwitchModel {
        private final ObservableValue input1;
        private final ObservableValue input2;
        private final ObservableValue output;
        private boolean closed;

        SimpleSwitchFI(ObservableValue input1, ObservableValue input2, ObservableValue output) {
            this.input1 = input1;
            this.input2 = input2;
            this.output = output;
        }

        @Override
        public void propagate() {
            if (closed) {
                output.set(input1.getValue(), input1.getHighZ());
            } else {
                output.set(input2.getValue(), input2.getHighZ());
            }
        }

        @Override
        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        @Override
        public void setModel(Model model) {
        }
    }

    /**
     * represents a switch
     */
    public static final class RealSwitchDT implements Switch.SwitchModel {
        private final CommonBusValue input1;
        private final CommonBusValue input2;
        private final CommonBusValue input3;
        private final Switch.RealSwitch sw1;
        private final Switch.RealSwitch sw2;

        /**
         * Creates a new instance.
         *
         * @param input1 The first input.
         * @param input2 The second input.
         * @param input3 The third input.
         */
        public RealSwitchDT(CommonBusValue input1, CommonBusValue input2, CommonBusValue input3) {
            this.input1 = input1;
            this.input2 = input2;
            this.input3 = input3;
            this.sw1 = new Switch.RealSwitch(input1, input2);
            this.sw2 = new Switch.RealSwitch(input1, input3);
        }

        @Override
        public void propagate() {
        }

        @Override
        public void setClosed(boolean closed) {
            sw1.setClosed(closed);
            sw2.setClosed(!closed);
        }

        @Override
        public void setModel(Model model) {
            sw1.setModel(model);
            sw2.setModel(model);
        }

        /**
         * @return the left hand side net
         */
        public CommonBusValue getInput1() {
            return input1;
        }

        /**
         * @return the first/lower right hand side net
         */
        public CommonBusValue getInput2() {
            return input2;
        }

        /**
         * @return the second/higher right hand side net
         */
        public CommonBusValue getInput3() {
            return input3;
        }
    }

    /**
     * represents a switch
     */
    public static final class MixedSwitchDT implements Switch.SwitchModel {
        private final CommonBusValue in1;
        private final ObservableValue input1;
        private final CommonBusValue in2;
        private final ObservableValue output3;
        private final boolean invert;
        private boolean closed;
        private final Switch.RealSwitch rsw;
        private final Switch.UniDirectionalSwitch ssw;

        /**
         * Creates a new instance.
         *
         * @param invert Is the switch operation inverted?
         * @param in1 The first input.
         * @param in2 The second input.
         * @param output3 The third input (output).
         */
        public MixedSwitchDT(boolean invert, CommonBusValue in1, CommonBusValue in2, ObservableValue output3) {
            this.in1 = in1;
            this.input1 = (ObservableValue) in1;
            this.in2 = in2;
            this.output3 = output3;
            this.invert = invert;
            this.closed = false;
            this.rsw = new Switch.RealSwitch(this.in1, this.in2);
            this.ssw = new Switch.UniDirectionalSwitch(this.input1, this.output3);
        }

        @Override
        public void propagate() {
            if (closed ^ invert) {
                output3.set(input1.getValue(), input1.getHighZ());
            }
        }

        @Override
        public void setClosed(boolean closed) {
            if (!invert) {
                rsw.setClosed(closed);
                ssw.setClosed(!closed);
            } else {
                rsw.setClosed(!closed);
                ssw.setClosed(closed);
            }
        }

        @Override
        public void setModel(Model model) {
            rsw.setModel(model);
        }
    }
}
