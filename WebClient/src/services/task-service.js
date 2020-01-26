/**
 * Created by Clay Parker on 7/30/2014.
 */

angular.module('taskService', [])
    .provider('taskService', function () {
        var errorState = 'error',
            logoutState = 'home';

        this.$get = function ($rootScope, $http, $q, $state, $log) {

            /**
             * Low-level, private functions.
             */


            /**
             * High level, public methods
             */
            var wrappedService = {
                /**
                 * Public properties
                 */

                teamTasks: [],
                selectedTask: {},

                createTeamTask: function(loginId, teamId, title, description){
                    var postData = {loginId:loginId, taskTeamId:teamId, taskTitle:title, taskDescription:description};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamtasks/createtask",
                        method: "POST",
                        data: postData
                    });

                    promise.success(function(data){
                        wrappedService.getTeamTasks(loginId, teamId);
                    });

                    promise.error(function(){
                        wrappedService.selectedTask = {};
                        $log.error("Problem creating team task for teamId = "+teamId);
                    });

                    return promise;
                },

                editTeamTask: function(loginId, taskId, teamId, title, description){
                    var postData = {loginId:loginId, taskId:taskId, taskTeamId:teamId, taskTitle:title, taskDescription:description};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamtasks/edittask",
                        method: "POST",
                        data: postData
                    });

                    promise.success(function(data){
                        wrappedService.getTeamTasks(loginId, teamId);
                    });

                    promise.error(function(){
                        wrappedService.selectedTask = {};
                        $log.error("Problem editing team task for teamId = "+teamId);
                    });

                    return promise;
                },

                updateTeamTask: function(loginId, teamId, taskId, taskCompleted, taskDeletion){
                    var postData = {taskId:taskId, taskCompleted:taskCompleted, taskDeletion:taskDeletion, taskTeamId:teamId, loginId:loginId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamtasks/updatetask",
                        method: "POST",
                        data: postData
                    });

                    promise.success(function(data){
                        wrappedService.getTeamTasks(loginId, teamId);
                    });

                    promise.error(function(){
                        wrappedService.selectedTask = {};
                        $log.error("Problem editing team task for teamId = "+teamId);
                    });

                    return promise;
                },

                getTeamTasks: function(loginId, teamId){
                    var postData = {loginId:loginId, taskTeamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamtasks/gettasks",
                        method: "POST",
                        data: postData
                    });

                    promise.success(function(data){
                        wrappedService.selectedTask = {};
                        if(data.response === "success"){
                            wrappedService.teamTasks = data.message;
                        }else{
                            wrappedService.teamTasks = [];
                        }
                    });

                    promise.error(function(){
                        wrappedService.selectedTask = {};
                        $log.error("Problem retrieving team tasks for teamId = "+teamId);
                    });

                    return promise;
                }

            };

            return wrappedService;
        };
    });
