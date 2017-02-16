/*
 * Copyright 2017 Simone Cascino
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.simonecascino.taskservice;

import android.os.Bundle;

import java.util.ArrayList;

/**
 * This class is a wrapper for a bundle object, that can be shared between multiple sync and promise tasks.
 * This class contains also nextIds array, that contains the ids of the next tasks, and the skipIds ArrayList, optional,
 * that if setted, contains the ids of future tasks that will be skipped.
 */
public final class SharedArgs {

    private Bundle bundle;
    private ArrayList<Integer> skipIds;
    private int[] nextIds;

    SharedArgs(Bundle bundle){
        this.bundle = bundle;
    }

    /**
     * Obtain a bundle shared between synchronized tasks.
     *
     * @return a bundle.
     */
    public Bundle getBundle(){
        return bundle;
    }

    /**
     * Helper method for getting knows if there are task ids in the skip pool.
     *
     * @return true if there are task ids in the skip pool.
     */
    public boolean hasSkipIds(){
        return skipIds != null && skipIds.size()>0;
    }

    /**
     * Set the ids of scheduled task you want to skip. If you schedule task with ids 12334, if set has skip ids only the value 3, will be skipped only the first task
     * with id = 3. If you want skip both tasks with id 3, you have to set 2 entries with value = 3.
     *
     * @param skipIds   An Integer ArrayList containing the ids of scheduled tasks you want to skip.
     */
    public void setSkipIds(ArrayList<Integer> skipIds){
        this.skipIds = skipIds;
    }

    /**
     * get an integer ArrayList that contains the ids of the future task that should be skipped. Is optional and null if not set.
     *
     * @return an integer ArrayList with the ids of future tasks that should be skipped.
     */
    public ArrayList<Integer> getSkipIds(){
        return skipIds;
    }

    void setNextIds(int[] ids, int point){

        final int length = ids.length;

        int[] nextIds = new int[length - point];

        System.arraycopy(ids, point, nextIds, 0, nextIds.length);

        this.nextIds = nextIds;

    }

    /**
     *
     * @return an array containing the ids of the next tasks.
     */
    public int[] getNextIds(){
        return nextIds;
    }

}
