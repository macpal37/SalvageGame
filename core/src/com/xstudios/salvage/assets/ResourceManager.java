/*
 * Pipeline.java
 *
 * This module provides a general resource manager singleton.  Resources are like
 * assets in that we are likely to need them all over the application.  And like 
 * assets, they need to be properly disposed of. But unlike assets, these can be 
 * created programatically. 
 *
 * This code is heavily adapted from AssetManager by mzechner
 *
 * @author Walker M. White
 * @date   4/1/2020
 */
package com.xstudios.salvage.assets;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * This class provides a singleton for managing heavyweight resources
 *
 * This class is heavily based on asset manager.  The primary differences are that 
 * (1) it is a singleton (so that it can be easily accessed anywhere in the game, and
 * (2) the resources are not loaded from a file.  Effectively, this is a singleton 
 * hashtable that allows us to have thread-safe global variables in our application.
 *
 * The primary use of this class is to store parts of our graphics pipeline (shaders,
 * vertex buffers, sprite batches) that will be needed everywhere.  They can be 
 * generated in a loading or splash screen and then used globally.  However, it is
 * also useful for storing other features as well.  If you have only one AssetManager,
 * you can even store that in this class.
 *
 * resources stored in this resource manager are heavyweight in the sense that they 
 * must be actively disposed (we cannot just rely on the garbage collector).  Such 
 * classes must implement {@link Disposable}. When an resource is added to the manager, 
 * the manager becomes the resource owner of that resource, and will be responsible for 
 * disposing of it. All references to the object outside of the reference manager 
 * will be essentially weak references (in the C++ sense).
 */
public class ResourceManager {
	/** The singleton instance of this resource manager */
    private static ResourceManager instance = null;
    /** Resource hashtables, categorized by class */
    final ObjectMap<Class, ObjectMap<String, Disposable>> resources = new ObjectMap<Class, ObjectMap<String, Disposable>>();
    /** The classes supported by this resource manager */
    final ObjectMap<String, Class> resourceTypes = new ObjectMap<String, Class>();

    /**
     * Creates a new resource manager.
     */
    private ResourceManager() { }

    /**
     * Cleans up the resource manager on Garbage collection
     */
    public void finalize() throws Throwable {
        clear();
    }

    /**
     * Returns the singleton resource manager.
     *
     * There is only one resource manager, so the constructor is not public.  Use this
     * method to access it.
     *
     * @return the singleton resource manager.
     */
    public static ResourceManager get() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    /**
     * Disposes the singleton resource manager.
     *
     * All resources stored in this pipeline manager will be disposed and no longer 
     * safe to use. This method should only be called near the end of the application.
     * To prevent a resource from being disposed, it should be removed first.
     */
    public static void dispose() {
        if (instance != null) {
            instance.clear();
            instance = null;
        }
    }

    /**
     * Returns the resource with the given key
     *
     * @param key   The resource key
     *
     * @throws GdxRuntimeException if no resource of type T has that key
     * @return the resource with the given key
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get (String key) {
        Class<T> type = resourceTypes.get(key);
        if (type == null) {
            throw new GdxRuntimeException(String.format("Resource '%s' is not active.", key));
        }
        ObjectMap<String, Disposable> rsrcByType = resources.get(type);
        if (rsrcByType == null) {
            throw new GdxRuntimeException(String.format("Resource '%s' is not active.", key));
        }
        T resource = (T) rsrcByType.get( key );
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Resource '%s' is not active.", key));
        }
        return resource;
    }

    /**
     * Returns the resource with the given key
     *
     * @param key   The resource key
     * @param type  The resource type
     *
     * @throws GdxRuntimeException if no resource of type T has that key
     * @return the resource with the given key
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T get (String key, Class<T> type) {
        ObjectMap<String, Disposable> rsrcByType = resources.get(type);
        if (rsrcByType == null) {
            throw new GdxRuntimeException(String.format("Resource '%s' is not active.", key));
        }
        T resource = (T) rsrcByType.get( key );
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Resource '%s' is not active.", key));
        }
        return resource;
    }

    /**
     * Returns the key for the given resource (null if resource is not present)
     *
     * @param resource	The resource to search for
     *
     * @return the key for the given resource (null if resource is not present)
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> String getKey (T resource) {
        for (Class rsrcType : resources.keys()) {
            ObjectMap<String, Disposable> rsrcByType = resources.get(rsrcType);
            for (String key : rsrcByType.keys()) {
                T other = (T) rsrcByType.get( key );
                if (other == resource || resource.equals(other)) return key;
            }
        }
        return null;
    }

    /**
     * Stores all resources of the given type in the array out
     *
     * This method returns the array passed for method chaining.
     *
     * @param type  The resource type
     * @param out   The storage array
     *
     * @return the array passed for method chaining.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> Array<T> getAll (Class<T> type, Array<T> out) {
        ObjectMap<String, Disposable> rsrcByType = resources.get(type);
        if (rsrcByType != null) {
            for (ObjectMap.Entry<String, Disposable> resource : rsrcByType.entries()) {
                out.add((T) resource.value);
            }
        }
        return out;
    }

    /**
     * Returns true if the specified resource is in this manager.
     *
     * @param key   The resource key
     *
     * @return true if the specified resource is in this manager.
     */
    public synchronized boolean contains(String key) {
        if (key == null) return false;
        return resourceTypes.containsKey(key);
    }

    /**
     * Returns true if the specified resource is in this manager.
     *
     * @param key   The resource key
     * @param type  The resource type
     *
     * @return true if the specified resource is in this manager.
     */
    public synchronized boolean contains(String key, Class type) {
        ObjectMap<String, Disposable> resourcesByType = resources.get(type);
        if (resourcesByType == null) return false;
        return resourcesByType.get(key) != null;
    }

    /**
     * Returns true if the specified resource is in this manager.
     *
     * @param resource   The resource to search for
     *
     * @return true if the specified resource is in this manager.
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> boolean contains(T resource) {
        ObjectMap<String, Disposable> rsrcByType = resources.get(resource.getClass());
        if (rsrcByType == null) return false;
        for (String key : rsrcByType.keys()) {
            T other = (T) rsrcByType.get( key );
            if (other == resource || resource.equals(other)) return true;
        }
        return false;
    }

    /**
     * Disposes of this resource and removes it from the manager
     *
     * Since the manager owns the resource, it is no longer safe to use it once
     * this method is called.
     *
     * @param key   The resource key
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     */
    @SuppressWarnings("unchecked")
    public synchronized void dispose(String key) {
        // get the asset and its type
        Class type = resourceTypes.get(key);
        if (type == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        ObjectMap<String, Disposable> resourcesByType = resources.get(type);
        if (resourcesByType == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        Disposable resource = (Disposable)resourcesByType.get(key);
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        resource.dispose();
        resourcesByType.remove(key);
    }

    /**
     * Disposes of this resource and removes it from the manager
     *
     * Since the manager owns the resource, it is no longer safe to use it once
     * this method is called.
     *
     * @param key   The resource key
     * @param type  The resource type
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     */
    @SuppressWarnings("unchecked")
    public synchronized void dispose(String key, Class type) {
        ObjectMap<String, Disposable> resourcesByType = resources.get(type);
        if (resourcesByType == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        Disposable resource = (Disposable)resourcesByType.get(key);
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        resource.dispose();
        resourcesByType.remove(key);
    }

    /**
     * Disposes of this resource and removes it from the manager
     *
     * Since the manager owns the resource, it is no longer safe to use it once
     * this method is called.
     *
     * @param resource   The resource to search for
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     */
    public synchronized <T> void dispose(T resource) {
        String key = getKey(resource);
        dispose(key);
    }

    /**
     * Empties out this resource manager, disposing all resources
     *
     * Since the manager owns its resources, it is no longer safe to use any of
     * them once this method is called.  This method should only be called near
     * the end of the application.
     */
    public synchronized void clear() {
        for (ObjectMap<String, Disposable>  resourcesByType : resources.values()) {
            for(Disposable resource : resourcesByType.values()) {
                resource.dispose();
            }
        }
        resources.clear();
        resourceTypes.clear();
    }
    
	/**
     * Removes this resource from the manager without disposing it
     *
     * Calling this method results in a transfer of ownership. The caller now
     * owns the resource and is responsible for disposing it.  The resource
     * manager will no longer dispose of it, even when {@link #clear} is called.
     * That is why this method returns the resource.
     *
     * @param key   The resource key
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     * @return the resource with the given key
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T remove(String key) {
        // get the asset and its type
        Class type = resourceTypes.get(key);
        if (type == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        ObjectMap<String, Disposable> resourcesByType = resources.get(type);
        if (resourcesByType == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        Disposable resource = (Disposable)resourcesByType.get(key);
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        resourcesByType.remove(key);
        return (T)resource;
    }

	/**
     * Removes this resource from the manager without disposing it
     *
     * Calling this method results in a transfer of ownership. The caller now
     * owns the resource and is responsible for disposing it.  The resource
     * manager will no longer dispose of it, even when {@link #clear} is called.
     * That is why this method returns the resource.
     *
     * @param key   The resource key
     * @param type  The resource type
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     * @return the resource with the given key
     */
    @SuppressWarnings("unchecked")
    public synchronized <T> T remove(String key, Class type) {
        ObjectMap<String, Disposable> resourcesByType = resources.get(type);
        if (resourcesByType == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        Disposable resource = (Disposable)resourcesByType.get(key);
        if (resource == null) {
            throw new GdxRuntimeException(String.format("Pipeline resource '%s' not active.", key));
        }
        resourcesByType.remove(key);
        return (T)resource;
    }
    
	/**
     * Removes this resource from the manager without disposing it
     *
     * Calling this method results in a transfer of ownership. The caller now
     * owns the resource and is responsible for disposing it.  The resource
     * manager will no longer dispose of it, even when {@link #clear} is called.
     * That is why this method returns the resource.
     *
     * @param resource   The resource to search for
     *
     * @throws GdxRuntimeException if no asset of type T has that key
     * @return the resource with the given key
     */
    public synchronized <T> T remove(T resource) {
        String key = getKey(resource);
        return remove(key);
    }

    /** 
     * Adds a resource to this manager.
     *
     * When assigning a key to a resource, keys should be globally unique, 
     * even across types.
	 *     
     * Calling this method results in a transfer of ownership. The caller no
     * longer owns the resource, having transfered it to the manager. When the
     * manager is deleted, it will dispose of this resource.
     *
     * @param key   	The resource key
     * @param type  	The resource type
     * @param resource	The resource to add
     */
    @SuppressWarnings("unchecked")
    public <T> void add(final String key, Class<T> type, T resource) {
        // add the asset to the filename lookup
        resourceTypes.put(key, type);

        // add the asset to the type lookup
        ObjectMap<String, Disposable> typeToRsrc = resources.get(type);
        if (typeToRsrc == null) {
            typeToRsrc = new ObjectMap<String, Disposable>();
            resources.put(type, typeToRsrc);
        }
        typeToRsrc.put(key, (Disposable)resource);
    }

}