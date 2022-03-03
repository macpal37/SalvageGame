/*
 * ControllerManager.java
 *
 * This class is a wrapper for the LibGDX Controllers class.  It allows you
 * to safely turn controller support on and off in your code.  This is
 * necessary because we have seen the Controllers class cause segfaults
 * on some systems.
 *
 * Author: Walker M. White
 * LibGDX version, 1/2/2021
 */
package com.xstudios.salvage.util;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.utils.Array;

/**
 * This class is a safe wrapper for the LibGDX Controllers class
 *
 * The existing Controllers class has been known to segfault on some systems due to the
 * native libraries that it uses.  We need a way to "turn it off" in that case. This
 * wrapper does just that, as it controls access via the GDXAppSettings value.
 */
public class Controllers {
    /** The singleton for this class */
    static private Controllers singleton;

    /** Whether controller support is active */
    private boolean active;

    /**
     * Creates a new Controllers wrapper with active status.
     */
    private Controllers() {
        this(true);
    }

    /**
     * Creates a new Controllers wrapper with the given status.
     *
     * @param active    Whether to activate the controllers
     */
    private Controllers(boolean active) {
        this.active = active;
    }

    /**
     * Toggles the active status of this wrapper
     *
     * @param active    The active status of the wrapper
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the active status of this wrapper
     *
     * @return the active status of this wrapper
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Returns the wrapper singleton
     *
     * This method constructs a new wrapper if one did not previously exist.
     *
     * @return the wrapper singleton
     */
    public static Controllers get() {
        if (singleton == null) {
            singleton = new Controllers();
        }
        return singleton;
    }

    /**
     * Returns an array of connected {@link Controller} instances.
     *
     * If the wrapper is not active, the array will be empty. This method should only
     * be called on the rendering thread.
     *
     * @return an array of connected {@link Controller} instances.
     */
    public Array<Controller> getControllers () {
        if (active) {
            try {
                return com.badlogic.gdx.controllers.Controllers.getControllers();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Array<Controller>();
    }

    /**
     * Returns an array of connected {@link XBoxController} instances.
     *
     * If the wrapper is not active, the array will be empty. This method should only
     * be called on the rendering thread.
     *
     * @return an array of connected {@link XBoxController} instances.
     */
    public Array<XBoxController> getXBoxControllers () {
        Array<XBoxController> xBoxControllers = new Array<XBoxController>();
        if (active) {
            try {
                for(Controller controller: com.badlogic.gdx.controllers.Controllers.getControllers()) {
                    String name = controller.getName().toLowerCase();
                    if (name.contains( "xbox" ) || name.contains( "pc" )) {
                        xBoxControllers.add(new XBoxController(controller));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return xBoxControllers;
    }

    /**
     * Adds a global {@link ControllerListener}.
     *
     * This listener can react to events from all {@link Controller} instances. However, no
     * listener will be added The listener will be added if the wrapper is not active.
     * This method should only be invoked on the rendering thread.
     *
     * @param listener  The global listener to add
     */
    public void addListener (ControllerListener listener) {
        if (active) {
            try {
                com.badlogic.gdx.controllers.Controllers.addListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes a global {@link ControllerListener}.
     *
     * No listener will be added The listener will be removed if the wrapper is not active.
     * This method should only be invoked on the rendering thread.
     *
     * @param listener  The global listener to remove
     */
    public void removeListener (ControllerListener listener) {
        if (active) {
            try {
                com.badlogic.gdx.controllers.Controllers.removeListener(listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes every global {@link ControllerListener} previously added.
     *
     * No listener will be added The listener will be removed if the wrapper is not active.
     * This method should only be invoked on the rendering thread.
     */
    public void clearListeners () {
        if (active) {
            try {
                com.badlogic.gdx.controllers.Controllers.clearListeners();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns all listeners currently registered.
     *
     * If the wrapper is not active, the array will be empty. This method should only
     * be called on the rendering thread. Modifying this array will result in undefined
     * behaviour.
     *
     * @return all listeners currently registered.
     */
    public Array<ControllerListener> getListeners() {
        if (active) {
            try {
                return com.badlogic.gdx.controllers.Controllers.getListeners();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Array<ControllerListener>();
    }
}
