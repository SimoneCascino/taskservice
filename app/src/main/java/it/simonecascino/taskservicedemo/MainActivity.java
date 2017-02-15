package it.simonecascino.taskservicedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;

import it.simonecascino.taskservice.TaskService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*

        TaskService.createMultipleSyncRequests(this, MyService.class, new int[]{MyService.TASK_1,
                MyService.TASK_2,
                MyService.TASK_3}, null);

        TaskService.createMultipleSyncRequests(this, MyService.class, new int[]{MyService.TASK_1,
                MyService.TASK_3}, null);

        TaskService.createAsyncRequest(this, MyService.class, 8, null);
        TaskService.createMultipleSyncRequests(this, MyService.class, createIdsArray(3), createBundlesArray(3));
        TaskService.createAsyncRequest(this, MyService.class, 1, null);
        TaskService.createAsyncRequest(this, MyService.class, 10, null);
        TaskService.createAsyncRequest(this, MyService.class, 11, null);

        TaskService.createSyncRequest(this, MyService.class, 1, null);

        */

        TaskService.createPromiseRequest(this, MyService.class, createIdsArrayList(3, 0), createBundlesArray(3), new int[]{1, 2, 3, 11, 4, 12, 5, 6, 7, 3, 4, 5}, null);
        TaskService.createMultipleSyncRequests(this, MyService.class, createIdsArray(6, 0), createBundlesArray(6));

    }

    private int[] createIdsArray(int dim, int start){

        int[] ids = new int[dim];

        for(int i=0;i<dim;i++)
            ids[i] = i + start;

        return ids;
    }

    private ArrayList<Integer> createIdsArrayList(int dim, int start){

        ArrayList<Integer> ids = new ArrayList<>(dim);

        for(int i=0;i<dim;i++)
            ids.add(i + start);

        return ids;

    }

    private Bundle[] createBundlesArray(int dim){

        Bundle[] args = new Bundle[dim];

        /*

        for(int i=0;i<dim;i++) {
            Bundle bundle = new Bundle();

            bundle.putInt("test_index", i);
            bundle.putInt("test_dim", dim);

            args[i] = bundle;
        }

        */

        return args;

    }

}
