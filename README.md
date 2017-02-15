# README #

TaskService is an enhanced IntentService, with the following advantages:

* can run task in parallel;
* can run task in sync (exactly like an Intentservice);
* can run connected task in sync (apparently like an Intentservice, but with a lot of special features, read below for more details);
* can run a task after the execution of other parallel tasks. 


Usage is very similar: 

* Create a class that extends TaskService and implement the abstract method **onAsyncExecution** 
* Declare the class in the manifest as a service.

TaskService should not be started with startService method, instead there are 4 static method able to configure how this service should work in the proper way:

* **createAsyncRequest(context, class, taskId, args);** run a task that can be executed at the same time of others task called with this method. The number of task that can be executed in parallel depends on specific device and android version. Each task started with this method must have different taskId. If a task with taskId = 1 is started and there is another task with id = 1 in execution, 

### What is this repository for? ###

* Quick summary
* Version
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Summary of set up
* Configuration
* Dependencies
* Database configuration
* How to run tests
* Deployment instructions

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact