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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.MissingResourceException;

public abstract class TaskService extends Service{

	private HashSet<Integer> parallelIds;
	private ArrayDeque<Intent> syncIntents;
	private ArrayDeque<Intent> promiseIntents;
	
	public static final String TAG = "TaskService";
	
	private static final String EXTRA_REQUEST_TYPE = "request_type";

	private static final String EXTRA_REQUEST_ID = "request_id";
	private static final String EXTRA_REQUEST_BUNDLE = "request_bundle";

	private static final String EXTRA_REQUEST_IDS = "request_ids";
	private static final String EXTRA_REQUEST_BUNDLES = "request_bundles";

	private static final String EXTRA_REQUEST_NEXT_IDS = "request_next_ids";
	private static final String EXTRA_REQUEST_NEXT_BUNDLES = "request_next_bundles";

	public static final String EXTRA_SHARED_NEXT_ID = "shared_next_id";
	public static final String EXTRA_SHARED_SKIP_ID = "shared_skip_id";

    /**
     * Constant for adding a task to the parallel pool
     */
	public static final int TYPE_PARALLEL = 1;

    /**
     * Constant for adding a task to the sync pool
     */
    public static final int TYPE_SYNC = 2;

    /**
     * Constant for adding a task to the promise pool
     */
	public static final int TYPE_PROMISE = 3;
	
	@Override
	public IBinder onBind(Intent p1) {
		return null;
	}

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "destroyed");
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(intent!=null){
			
			if(!intent.hasExtra(EXTRA_REQUEST_TYPE))
                throw new MissingResourceException("An Integer must be supplied as extra with the EXTRA_REQUEST_TYPE key.", "Integer", "EXTRA_REQUEST_TYPE");
			
			handleStartIntent(intent);
			
		}

		else checkStopConditions();
		
		return START_NOT_STICKY;
	}

	private void handleStartIntent(Intent intent){

		switch(intent.getIntExtra(EXTRA_REQUEST_TYPE, 0)){

			case TYPE_PARALLEL:

				if(!intent.hasExtra(EXTRA_REQUEST_ID))
					throw new MissingResourceException("An Integer must be supplied as extra with the EXTRA_REQUEST_ID key.", "Integer", "EXTRA_REQUEST_ID");

				if(parallelIds==null)
					parallelIds = new HashSet<>();

				buildAsyncRequest(intent);

				break;

			case TYPE_SYNC:

				if(!intent.hasExtra(EXTRA_REQUEST_IDS))
					throw new MissingResourceException("An Integer array must be supplied as extra with the EXTRA_REQUEST_IDS key.", "Integer", "EXTRA_REQUEST_IDS");

				if(syncIntents==null)
					syncIntents = new ArrayDeque<>();

				syncIntents.add(intent);

				if(syncIntents.size()==1)
					buildSyncRequest(intent);

				break;

			case TYPE_PROMISE:

				if(!intent.hasExtra(EXTRA_REQUEST_NEXT_IDS))
					throw new MissingResourceException("An Integer must be supplied as extra with the EXTRA_REQUEST_NEXT_IDS key.", "Integer", "EXTRA_REQUEST_NEXT_IDS");

				if(!intent.hasExtra(EXTRA_REQUEST_IDS))
					throw new MissingResourceException("An Integer array must be supplied as extra with the EXTRA_REQUEST_IDS key.", "Integer", "EXTRA_REQUEST_IDS");

				if(promiseIntents ==null)
					promiseIntents = new ArrayDeque<>();

				promiseIntents.add(intent);

				if(promiseIntents.size()==1)
					buildPromiseRequest(intent);

				break;

			default: throw new UnsupportedOperationException("Invalid value as REQUEST_TYPE, allowed types are defined as public constants in the TaskService class.");

		}

	}

	private void checkStopConditions(){
		
		boolean idsIsEmpty = true;
		boolean syncIntentsIsEmpty = true;
        boolean promiseIntentsIsEmpty = true;
		
		if(parallelIds!=null)
			idsIsEmpty = parallelIds.isEmpty();
			
		if(syncIntents!=null)
			syncIntentsIsEmpty = syncIntents.isEmpty();

        if(promiseIntents !=null)
            promiseIntentsIsEmpty = promiseIntents.isEmpty();

		if(idsIsEmpty && syncIntentsIsEmpty && promiseIntentsIsEmpty)
			stopSelf();
	}

	private void buildAsyncRequest(Intent intent){

		final int sendedId = intent.getIntExtra(EXTRA_REQUEST_ID, 0);
		final Bundle args = intent.getBundleExtra(EXTRA_REQUEST_BUNDLE);

		launchAsync(sendedId, args);
	}

    private void buildSyncRequest(Intent intent){

		int[] ids = intent.getIntArrayExtra(EXTRA_REQUEST_IDS);

		Bundle[] argsArray = obtainArgsArray(intent.getParcelableArrayExtra(EXTRA_REQUEST_BUNDLES), ids.length);

		new RequestExecutor(ids, argsArray, TYPE_SYNC).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

	}

	private void buildPromiseRequest(Intent intent){
		
		ArrayList<Integer>ids = intent.getIntegerArrayListExtra(EXTRA_REQUEST_IDS);

		Bundle[] argsArray = obtainArgsArray(intent.getParcelableArrayExtra(EXTRA_REQUEST_BUNDLES), ids.size());

		final int length = ids.size();

		for(int i=0;i<length;i++)
			launchAsync(ids.get(i), argsArray[i]);
		
	}

	private void launchAsync(int sendedId, Bundle args){

		if(parallelIds==null)
			parallelIds = new HashSet<>();

		if(parallelIds.add(sendedId))
			new RequestExecutor(new int[]{sendedId}, new Bundle[]{args}, TYPE_PARALLEL).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}



	private Bundle[] obtainArgsArray(Parcelable[] parcelables, int size){

		Bundle[] argsArray;

		if(parcelables!=null){
			argsArray = new Bundle[parcelables.length];
			System.arraycopy(parcelables, 0, argsArray, 0, parcelables.length);
		}

		else argsArray = new Bundle[size];

		return argsArray;

	}



	
	public class RequestExecutor extends AsyncTask<Void, Void, Void> {

		private Bundle[] args;
		private int[] ids;
		private SharedArgs sharedArgs;
		private int type;

		public RequestExecutor(int[] ids, Bundle[] args, int type){
			this.ids=ids;
			this.args=args;
			this.type=type;

            if(type!=TYPE_PARALLEL)
                sharedArgs = new SharedArgs(new Bundle());

		}
		
		
		@Override
		protected Void doInBackground(Void[] p1) {

			final int length = ids.length;

			for(int i=0;i<length;i++){

                if(sharedArgs !=null) {

					sharedArgs.setNextIds(ids, i+1);

                    final int nextId = ids[i];

					if(sharedArgs.hasSkipIds()){

						ArrayList<Integer> skipIds = sharedArgs.getSkipIds();

                        int skipId = skipIds.get(0);

						if(skipId == nextId) {
							skipIds.remove(0);
							continue;
						}

					}
                }
				
				if(!onBackgroundExecution(ids[i], args[i], type, sharedArgs))
					break;
				
			}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			switch(type){
				
				case TYPE_SYNC:

                    syncIntents.poll();

                    if(syncIntents.isEmpty())
                        checkStopConditions();

                    else buildSyncRequest(syncIntents.peek());
					
					break;
					
				case TYPE_PARALLEL:

                    parallelIds.remove(ids[0]);

                    if(promiseIntents!=null && !promiseIntents.isEmpty()){

                        Intent promiseIntent = promiseIntents.peek();

                        ArrayList<Integer> promiseIds = promiseIntent.getIntegerArrayListExtra(EXTRA_REQUEST_IDS);

                        int index = promiseIds.indexOf(ids[0]);

                        if(index>=0) {

							promiseIds.remove(index);

                            if (promiseIds.isEmpty()) {

                                final int[] ids = promiseIntent.getIntArrayExtra(EXTRA_REQUEST_NEXT_IDS);
                                final Bundle[] argsArray = obtainArgsArray(promiseIntent.getParcelableArrayExtra(EXTRA_REQUEST_NEXT_BUNDLES), ids.length);

                                new RequestExecutor(ids, argsArray, TYPE_PROMISE).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    }

					else checkStopConditions();

					break;

                case TYPE_PROMISE:

                    promiseIntents.poll();

                    if(promiseIntents.isEmpty())
                        checkStopConditions();

                    else buildPromiseRequest(promiseIntents.peek());

                    break;

			}

		}
		
	}






    /**
     * Method that will be implemented in a class that extends TaskService. Is executed in a worker thread.
     *
     * @param id            The id of the task.
     * @param args          The bundle passed when the request is built.
     * @param type          Type of the request, one of TYPE_SYNC, TYPE_PARALLEL and TYPE_PROMISE.
     * @param sharedArgs  	Used only for multiple sync request, null otherwise. This object is a wrapper for a bundle, shared between multiple task. For example, calling createMultipleSyncRequests with ids = 1 - 2 - 3, this object is available when executing 1,
     *                      so is possible to put values. When 2 is executing, the same object is available, so is possible to get data putting in the previous task. Also this bundle could have 2 private fields, EXTRA_SHARED_NEXT_ID, that contains
     *                      the id of the next task, and EXTRA_SHARED_SKIP_ID, a customizable fields useful for skip tasks (putting an integer id with this key the task that match this id will be skipped).
     * @return a boolean value, useful only with a chain of sync requests(using createMultipleSyncRequests). Return true if the execution should be continue. A return of false break the entire chain.
     */
    public abstract boolean onBackgroundExecution(int id, @Nullable Bundle args, int type, @Nullable SharedArgs sharedArgs);


    /**
     * Create a single request. This request will be executed asynchronously to other requests
     *
     * @param context   A Context of the application package implementing this class.
     * @param cls       The component class that is to be used (must extends TaskService).
     * @param id        Id parameter for the request. Is required. Operations with duplicated id cannot be executed in parallel, so if you call 2 times this method with the same id, the second call is ignored.
     * @param args      An optional bundle.
     */
	public static void createAsyncRequest(Context context, Class<?> cls, int id, Bundle args){

		Intent intent = new Intent(context, cls);
		intent.putExtra(EXTRA_REQUEST_ID, id);
		intent.putExtra(EXTRA_REQUEST_BUNDLE, args);
		intent.putExtra(EXTRA_REQUEST_TYPE, TYPE_PARALLEL);

		context.startService(intent);

	}

    //// TODO: 24/10/2016 force users to pass null or a same-size array for bundle

    /**
     * Create a single request. This request will be executed synchronously to other sync requests, asynchronously to other asyc requests.
     *
     * @param context   A Context of the application package implementing this class.
     * @param cls       The component class that is to be used (must extends TaskService).
     * @param id        Id parameter for the request. Is required.
     * @param args      An optional bundle.
     */
	public static void createSyncRequest(Context context, Class<?> cls, int id, Bundle args){

		int[] ids = new int[]{id};
		Bundle[] argsArray = new Bundle[]{args};
		
		Intent intent = new Intent(context, cls);
		intent.putExtra(EXTRA_REQUEST_IDS, ids);
		intent.putExtra(EXTRA_REQUEST_BUNDLES, argsArray);
		intent.putExtra(EXTRA_REQUEST_TYPE, TYPE_SYNC);

		context.startService(intent);

	}

    /**
     * Create multiple requests. These requests are synchronized.
     *
     * @param context       A Context of the application package implementing this class.
     * @param cls           The component class that is to be used (must extends TaskService).
     * @param ids           An array of int that identify each request.
     * @param argsArray     An array of bundle, one for each request.
     */
	public static void createMultipleSyncRequests(Context context, Class<?> cls, int[] ids, Bundle[] argsArray){

		Intent intent = new Intent(context, cls);
		intent.putExtra(EXTRA_REQUEST_IDS, ids);
		intent.putExtra(EXTRA_REQUEST_BUNDLES, argsArray);
		intent.putExtra(EXTRA_REQUEST_TYPE, TYPE_SYNC);

		context.startService(intent);
		
	}

	/**
	 * Create synchronized requests that will be executed after a specific number of parallel tasks. These parallel tasks are added to the async pool, so parallel requests with the same id will be ignored.
	 * If you called twice this method, the second calls will be executed at the end of the final task of the first call.
	 *
	 * @param context           A Context of the application package implementing this class.
	 * @param cls               The component class that is to be used (must extends TaskService).
	 * @param ids               An ArrayList of int that identify each parallel request.
	 * @param argsArray         An array of bundle, one for each parallel request.
	 * @param syncIds            Id of the requests that will be executed when all parallel tasks defined by ids array ends.
	 * @param syncBundles        An optional bundle array for the synchronized requests.
	 */
	public static void createPromiseRequest(Context context, Class<?> cls, ArrayList<Integer>ids, Bundle[] argsArray, int []syncIds, Bundle [] syncBundles){

        Intent intent = new Intent(context, cls);
        intent.putExtra(EXTRA_REQUEST_IDS, ids);
        intent.putExtra(EXTRA_REQUEST_BUNDLES, argsArray);
        intent.putExtra(EXTRA_REQUEST_NEXT_IDS, syncIds);
        intent.putExtra(EXTRA_REQUEST_NEXT_BUNDLES, syncBundles);
        intent.putExtra(EXTRA_REQUEST_TYPE, TYPE_PROMISE);

        context.startService(intent);

    }
	
}
