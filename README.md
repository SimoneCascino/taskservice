# README #

TaskService is an enhanced IntentService, with the following advantages:

* can run task in parallel;
* can run task in sync (exactly like an IntentService);
* can run connected task in sync (apparently like an IntentService, but with a lot of special features, read below for more details);
* can run tasks in sync after the execution of other parallel tasks.




Usage is very similar:

* Create a class that extends TaskService and implement the abstract method **onBackgroundExecution**
* Declare the class in the manifest as a Service.

TaskService **should not be started with startService method**, instead there are 4 static method able to configure how this service should work in the proper way:

1. **createAsyncRequest(context, class, taskId, args)**; run a task that can be executed at the same time of others task called with this method. The number of task that can be executed in parallel depends on specific device and android version. Each task started with this method must have different taskId. If a task with taskId = 1 is started and there is another task with id = 1 in execution, only the first will be executed.
2. **createSyncRequest(Context context, Class<?> cls, int id, Bundle args)**; run a task that will be added to the sync pool. This task will be executed after other sync tasks started before (or immediatly if the sync pool is empty). This behavior is the same of the IntentService.
3. **createMultipleSyncRequests(Context context, Class<?> cls, int[] ids, Bundle[] argsArray)**; like createSyncRequest, but this method take as arguments an array of id and bundle. This means that is possible start a number of tasks in one time. Also these tasks are "connected". Connection between tasks allow some fatures:
  * the method "onBackgroundExecution returns a boolean. If the return is false, the chain of tasks is broken, so next tasks not be executed.
  * the "onBackgroundExecution" method has as argument a non null *SharedArgs* object. SharedArgs is a wrapper for a Bundle that is shared between tasks, in order to put inside it variables. Also, SharedArgs has a method that returns an integer ArrayList with the ids of the next tasks, and some helper method for set an integer ArrayList with the ids of future tasks that shold be skipped. Note that if you have a sequence like 1,2,3,2 and you set in skip ids ArrayList the number 2, only the first will be skipped.


### Set up ###

TaskService is available on jCenter repository. Just add to your dependencies:

**compile 'com.android.support:support-annotations:25.1.1'**





##Simone Cascino
##simone.cascino1984@gmail.com
