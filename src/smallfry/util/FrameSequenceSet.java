/*
 * FrameSequenceSet.java
 *
 * Created on 12 January 2006, 10:48
 *
 * Copyright (c) 2006 smallfry mobile. All  Rights Reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited unless otherwise indicated in writing
 * by the copyright holder.
 */

package smallfry.util;

/**
 * This class encapsulates the updating of multiple animation sequences.
 *
 * @author Matt
 */
public final class FrameSequenceSet
{
    public int sequence;
    public int frameIdx;
    public int [][] sequences;
    public int [] intervalsMS;
    public int timerMS;
    
    /*
     * Creates a new instance of FrameSequenceSet 
     */
    public FrameSequenceSet()
    {
       
    }

    /*
     * Creates a new instance of FrameSequenceSet 
     */
    public FrameSequenceSet(int [][] sequences, int [] intervalsMS)
    {
       init(sequences, intervalsMS);
    }

    /*
     * Initialize the instance
     *
     * @param sequences Array of frame index sequences
     * @param intervalsMS array of frame intervals 
     */
    public void init(int [][] sequences, int [] intervalsMS)
    {
//#mdebug error
        if(sequences.length != intervalsMS.length)
            throw new IllegalArgumentException("sequence and intervals must be of same length");
//#enddebug
        this.sequences = sequences;
        this.intervalsMS = intervalsMS;
        selectSequence(0);
    }
    
    /*
     * Add a new frame sequence and it's interval
     * @param sequence An array of frame indices
     * @param intervalMS A frame delay
     */
    void addSequence(int [] sequence, int intervalMS)
    {
        if(null == sequences)
        {
            sequences = new int [1][];
            intervalsMS = new int [] {intervalMS};
        }
        else
        {
            int [][] oldSequences = sequences;
            int [] oldIntervals  = intervalsMS;
            
            sequences = new int [oldSequences.length + 1][];
            System.arraycopy(oldSequences, 0, sequences, 0, oldSequences.length);
            sequences[oldSequences.length] = sequence;
            intervalsMS = new int [oldIntervals.length + 1];
            System.arraycopy(oldIntervals, 0, intervalsMS, 0, oldIntervals.length);
            intervalsMS[oldIntervals.length] = intervalMS;            
        }
    }
    
    /*
     * Clear all sequences.
     */
    void clear()
    {
        sequences = null;
        intervalsMS = null;
    }
    
    /*
     * Select the frame sequence to use,
     *  resets to the first frame for that sequence.
     * 
     * @param sequence The sequence to use
     */
    public void selectSequence(int sequence)
    {
       sequence = sequence;
       frameIdx = 0;
       timerMS = intervalsMS[sequence];
    }
    
    /*
     * Update and determine the latest frame
     * @dtms The time Delta
     */
    public int update(int dtms)
    {
        timerMS -= dtms;
        if(timerMS < 0)
        {
            frameIdx++;
            if(frameIdx >= sequences[sequence].length)
            {
                frameIdx = 0;
            }
            timerMS = intervalsMS[sequence];
        }
        
        return sequences[sequence][frameIdx];
    }
    
    /*
     * Return the current frame
     */
    public int getFrame()
    {
        return sequences[sequence][frameIdx];
    }
}
