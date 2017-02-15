package it.simonecascino.taskservicedemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

import it.simonecascino.taskservice.SharedArgs;
import it.simonecascino.taskservice.TaskService;

/**
 * Created by Simone on 20/09/2016.
 */
public class MyService extends TaskService{


    @Override
    public boolean onBackgroundExecution(int id, @Nullable Bundle args, int type, @Nullable SharedArgs sharedArgs) {
        String requestType = null;

        switch(type){

            case TYPE_PARALLEL:
                requestType = ", parallel";
                break;

            case TYPE_SYNC:
                requestType = ", sync";
                break;

            case TYPE_PROMISE:
                requestType = ", promise";
                break;
        }

        if(sharedArgs !=null && !sharedArgs.hasSkipIds() && id==1){

            ArrayList<Integer> ids = new ArrayList<>();
            ids.add(3);
            ids.add(4);
            ids.add(5);

            sharedArgs.setSkipIds(ids);

        }


        Log.d("test_request", "Start: " + String.valueOf(id) + requestType + ", " + System.currentTimeMillis());


        if(sharedArgs !=null)
            Log.d("test_request", "next --> " + Arrays.toString(sharedArgs.getNextIds()) + ", skip --> " + sharedArgs.getSkipIds());



        //Log.d("test_request", args==null ? "bundle is null" : "bundle is not null");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Log.d("test_request", "End: " + String.valueOf(id) + requestType);

        return true;
    }
}
