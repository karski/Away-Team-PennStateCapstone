/**
 * Created by Clay Parker on 7/30/2014.
 */

angular.module('managerService', [])
    .provider('managerService', function () {
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

                pendingUsers:[],

                getPendingUsers: function(loginId){
                    var postData = {loginId:loginId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/Manager/PendingUsers",
                        method: "POST",
                        data: postData
                    });

                    promise.success(function (data){
                        if(data.response === "success"){
                            wrappedService.pendingUsers = data.message;
                        }else{
                            wrappedService.pendingUsers = [];
                        }
                    });
                    promise.error(function(){
                        wrappedService.pendingUsers = [];
                        $log.error("Error getting pending users.");
                    });

                    return promise;
                },

                getPendingUsersByTeam: function(loginId, teamId){
                    var postData = {loginId:loginId, teamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/Manager/PendingUsers",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },

                takeAction: function(loginId, teamId, action, subjectLoginId){
                    //valid actions approve, remove, promote, demote

                    var postData = {loginId:loginId, teamId:teamId, action:action, subjectLoginId:subjectLoginId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/Manager/TakeAction",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                }

            };

            return wrappedService;
        };
    });
