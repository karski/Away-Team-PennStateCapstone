/**
 * Created by Clay Parker on 7/20/2014.
 */
/**
 * Each section of the site has its own module. It probably also has
 * submodules, though this boilerplate is too simple to demonstrate it. Within
 * 'src/app/home', however, could exist several additional folders representing
 * additional modules that would then be listed as dependencies of this one.
 * For example, a 'note' section could have the submodules 'note.create',
 * 'note.delete', 'note.edit', etc.
 *
 * Regardless, so long as dependencies are managed correctly, the build process
 * will automatically take take of the rest.
 *
 * Basically taken right from TodoMVC
 *
 *      MIT :copyright: Addy Osmani, Sindre Sorhus, Pascal Hartig, Stephen Sawchuk
 */
(function(app) {

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('TaskController', ['$scope', '$http', '$filter', '$timeout', '$modal', '$log', 'loginService', 'teamService', 'taskService', 'growlNotifications', function ($scope, $http, $filter, $timeout, $modal, $log, loginService, teamService, taskService, growlNotifications) {
        $scope.loginService = loginService;
        $scope.teamService  = teamService;
        $scope.taskService  = taskService;

        $scope.teamTasks = [];
        $scope.editTask = {};

        $scope.$watch(function () {
                return teamService.selectedTeam;
            },
            function(newVal, oldVal) {
                if(teamService.selectedTeam == null){
                    $scope.teamTasks = [];
                }else{
                    taskService.getTeamTasks(loginService.user.loginId, teamService.selectedTeam.teamId);
                }
            }, true);

        $scope.$watch(function () {
                return taskService.teamTasks;
            },
            function(newVal, oldVal) {
                if(taskService.teamTasks == null){
                    $scope.teamTasks = [];
                }else {
                    $scope.teamTasks = taskService.teamTasks;
                }
            }, true);

        $scope.markAsComplete = function(task) {
            taskService.selectedTask = task;
            var promise;
            if (task.taskCompleted){
                promise = taskService.updateTeamTask(loginService.user.loginId, teamService.selectedTeam.teamId, task.taskId, false, false);
            }else{
                promise = taskService.updateTeamTask(loginService.user.loginId, teamService.selectedTeam.teamId, task.taskId, true, false);
            }
            promise.success(function(){
                growlNotifications.add("Successfully completed task "+task.taskTitle, 'success');
            });
            promise.error(function(){
                $log.error("Problems deleting task "+task.taskTitle);
            });
        };

        $scope.deleteTask = function(task){
            taskService.selectedTask = task;
            var promise = taskService.updateTeamTask(loginService.user.loginId, teamService.selectedTeam.teamId, task.taskId, false, true);
            promise.success(function(){
                growlNotifications.add("Successfully deleted task "+task.taskTitle, 'success');
            });
            promise.error(function(){
                growlNotifications.add("Problems deleting task "+task.taskTitle, 'danger');
            });
        };

        $scope.createTaskModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'tasks/createTask.tpl.html',
                controller: 'CreateTaskModalCtrl'
            });
        };

        $scope.modifyTaskModal = function (task) {
            taskService.selectedTask = task;
            var modalInstance = $modal.open({
                templateUrl: 'tasks/createTask.tpl.html',
                controller: 'ModifyTaskModalCtrl'
            });
        };
        
    }]);


    app.controller('CreateTaskModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'loginService', 'taskService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, loginService, taskService, growlNotifications) {
        $scope.modalTitle = "Add Team Task";
        $scope.task = {};
        $scope.taskServiceWorking = false;
        $scope.selectedTask = {};

        $scope.ok = function () {
            var task = $scope.task;
            $scope.taskServiceWorking = true;
            var addTaskPromise = taskService.createTeamTask(loginService.user.loginId, teamService.selectedTeam.teamId, task.taskTitle, task.taskDescription);
            addTaskPromise.success(function(task, status, headers, config) {
                if(task.status == "success"){
                    $modalInstance.close();
                    $scope.taskServiceWorking = false;
                    growlNotifications.add('Successfully Added a Task!', 'success');
                }else{
                    growlNotifications.add('Error Adding a Task!', 'danger');
                }

            });
            addTaskPromise.error(function () {
                growlNotifications.add('Error Adding a Task!', 'danger');
                $scope.taskServiceWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

    app.controller('ModifyTaskModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'loginService', 'taskService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, loginService, taskService, growlNotifications) {
        $scope.modalTitle = "Edit Team Task";
        $scope.task = taskService.selectedTask;
        $scope.taskServiceWorking = false;

        $scope.ok = function () {
            var task = $scope.task;
            $scope.taskServiceWorking = true;
            var modifyTaskPromise = taskService.editTeamTask(loginService.user.loginId, task.taskId, teamService.selectedTeam.teamId, task.taskTitle, task.taskDescription);
            modifyTaskPromise.success(function(task, status, headers, config) {
                if(task.status == "success"){
                    $modalInstance.close();
                    $scope.taskServiceWorking = false;
                    growlNotifications.add('Successfully Edited a Task!', 'success');
                }else{
                    growlNotifications.add('Error Editing a Task!', 'danger');
                }

            });
            modifyTaskPromise.error(function () {
                growlNotifications.add('Error Editing a Task!', 'danger');
                $scope.taskServiceWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.tasks", [
    'AwayTeam.router'
])));