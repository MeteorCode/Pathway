package com.meteorcode.pathway.samples.sparks;

import com.meteorcode.pathway.model.Context;
import com.meteorcode.pathway.model.Event;
import com.meteorcode.pathway.script.ScriptException;

/**
 * AsciiViewEvent is an Event which updates an AsciiBallView
 * when the event is evaluated.
 */
public class AsciiViewEvent extends Event {
    private AsciiBallView view;

    public AsciiViewEvent(String name, Context origin, int x, int y) {
        super(name, origin);
        this.view = new AsciiBallView(x, y,origin);
    }

    @Override
    public void evalEvent() throws ScriptException {
        view.render();

        //Wait some, causing the animation to be more
        //visible in a terminal.
        wait(100);
    }

    private void wait(int millis) {
        long then = System.currentTimeMillis();
        while(then + millis > System.currentTimeMillis());
    }
}
