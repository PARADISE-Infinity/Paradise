/**
* Copyright (C) 2011-2021 German Aerospace Center e. V. (DLR), Braunschweig
*
* This code is part of the PARADISE project and is licensed under the EPL 2.0 license,
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* @author: Axel Berres, Tim Bittner, Söhnke Escher, Dominik Engelhardt, Holger Schumann, Tillman Stehr
*
*/

package de.dlr.premise.view.parameterviewer.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * <p>
 * QueuedMap is a specialised implementation of the Queue which can store Key Value pairs instead of just the Objects. This flexibility
 * comes handy when you want to retrieve a specific Object from the Queue then instead of trying to find the object by iterating the whole
 * Queue you can simply get the Object using the Key.
 * </P>
 * <p>
 * In order for the structure to work properly, it is vital to override the hashCode() and equals(Object obj) methods in your Key class.
 * These are the methods that the underlying Map will use to compare the Keys to retrieve/remove the correct Object from the QueuedStore.
 * </P>
 * <p>
 * The structure will always have a fixed size. If the structure is not initialised with a given value then it will use default value of 64
 * to initialise. Once the Structure is initialised then the size cannot be amended. Once the overall structure reaches the maximum size
 * then any new Key Value pairs added to the structure will result in removing the oldest entry from the structure.
 * </P>
 * <p>
 * There is virtually no size limit to the size of the structure. The structure can be initialised with any arbitrary value, however, at the
 * time of initialisation one should always consider keeping up with the best practices used to initialise * Map data structures as the
 * underlying implementation uses the HashMap to store the Key Value pairs.
 * </P>
 * * @author Baqir Raza Abidi @date 26 Mar 2013 16:03:08
 */
public class QueuedMap<K, V> implements Serializable {

    private static final long serialVersionUID = 1L;
    /** * Final variable indicates the Maximum size of this * structure. */
    private static final int MAX_SIZE = 1024;
    private int size;
    private Map<K, V> values;
    private Queue<K> keys;

    /**
     * Default constructor for the class. Creates a class with the default structure size of {@code 64}. Once the structure is created then
     * the size of the structure will remain the same.
     */
    public QueuedMap() {
        this(64);
    }

    /**
     * *
     * <p>
     * Creates the structure with the given size. The constructor throws Exception if the size given is less then 1. The structure cannot be
     * created with a 0 or -ive size.
     * </p>
     * <p>
     * The maximum size of the structure is also limited to the {@code QueuedStore.MAX_SIZE}
     * </p>
     * 
     * @param size Size of the Structure. @throws IllegalArgumentException If an invalid size is provided.
     */
    public QueuedMap(int size) throws IllegalArgumentException {
        if (size <= 0) {
            throw new IllegalArgumentException("Size can only be a +ive Integer");
        }
        if (size > QueuedMap.MAX_SIZE)
            throw new IllegalArgumentException("Size cannot be more than " + QueuedMap.MAX_SIZE);
        this.size = size;
        this.values = new HashMap<K, V>(this.size);
        this.keys = new LinkedList<K>();
    }

    /**
     * *
     * <p>
     * * Add a new {@code (Key, Value)} pair to the structure. Both the Key and Value can be any {@code Objects}. The method throws a
     * {@code NullPointerException} in case any of the Key and Value are {@code null}.
     * </p>
     * <p>
     * * If both the Key and Value are non null objects then it will try to store the pair to the structure. If the key already exists in
     * the Store then it will simply replace the Value of that Key in the Store with the new Value. If the Key is a new one then it will try
     * to store a new entry in the Structure.
     * </p>
     * <p>
     * * When storing a new entry in the structure, it first checks the size of the Structure and if it is still less than the size with
     * which it was initialised then it will add the Key Value pair to the Structure. In case the size is now reached the limit then the
     * method will first remove the oldest entry from the Structure * and then will add the new Key Value pair to the Store.
     * </p>
     * * * @param key Object represents the Key. @param value Object represents the Value. @throws Exception
     */
    public synchronized void addItem(K key, V value) {
        if (key == null || value == null)
            throw new NullPointerException("Cannot insert a null for either key or value");
        // First see if we already have this key in our queue
        if (this.keys.contains(key)) { // Key found.
            // Simply replace the value in Map
            this.values.put(key, value);
        } else { // Key not found
            // Add value to both Queue and Map
            this.enqueue(key, value);
        }
    }
    public boolean containsKey(K key){
        return keys.contains(key);
    }

    /**
     * * Returns the value to which the specified key is associated, or {@code null} if this Structure contains no association for the key.
     * <p>
     * * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that
     * {@code (key==null ? k==null :   key.equals(k))}, then this method returns {@code v}; otherwise it returns {@code null}. (There can be
     * at most one such mapping.)
     * </p>
     * * @param key the key whose associated value is to be returned * @return the value to which the specified key is mapped, or
     * {@code null} if this map contains no mapping for the key
     */
    public synchronized V getItem(K key) {
        if (key == null)
            return null;
        V val = this.values.get(key);
        return val;
    }

    /**
     * * Removes the mapping for a key from this Structure if it is present (optional operation). More formally, if this Structure contains
     * a mapping from key <tt>k</tt> to value <tt>v</tt> such that <code>(key==null ?  k==null : key.equals(k))</code>, that mapping is
     * removed. @param key key whose mapping is to be removed from the map
     */
    public synchronized void remove(K key) {
        if (key == null)
            throw new NullPointerException("Cannot remove a null key");
        this.keys.remove(key);
        this.values.remove(key);
    }

    /**
     * Returns the number of elements in this collection.
     * 
     * @return size of the structure.
     */
    public int size() {
        return this.keys.size();
    }

    /**
     * * Removes all of the elements from this collection (optional operation). The collection will be empty after this method returns.
     */
    public void clear() {
        this.values.clear();
        this.keys.clear();
    }

    /**
     * Method implementing the actual logic to add the Key Value pair to the structure.
     */
    private void enqueue(K key, V value) {
        if (this.keys.size() < this.size) {
            // We still have space in the queue
            // Add they entry in both queue and the Map
            if (this.keys.add(key)) {
                this.values.put(key, value);
            }
        } else {
            // Queue is full. Need to remove the Head
            // before we can add a new item.
            K old = this.keys.poll();
            if (old != null)
                this.values.remove(old);
            // Now add the new item to both queue and the map
            this.keys.add(key);
            this.values.put(key, value);
        }
    }
}