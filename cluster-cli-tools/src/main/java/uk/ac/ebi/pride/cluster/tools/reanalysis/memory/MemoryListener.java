/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.ebi.pride.cluster.tools.reanalysis.memory;

/**
 *
 * @author Kenneth Verheggen
 */
public interface MemoryListener {

    /**
     * Checks whether the memory usage potentially causes an issue
     * @param usedMemory the current used memory
     * @param maxMemory the maximum allocated memory
     */
    public void checkMemoryUsage(long usedMemory, long maxMemory);
}
