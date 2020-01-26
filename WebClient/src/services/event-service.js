/**
 * Created by Clay Parker on 7/20/2014.
 */
angular.module('eventService', [])
    .provider('eventService', function () {
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

                teamEvents:[],
                selectedEvent:{},

                createTeamEvent: function(loginId, teamId, eventName, description, locationString, startTime, endTime){
                    var postData = {loginId:loginId, teamEventName:eventName, teamEventDescription:description, teamEventLocationString:locationString, teamEventStartTime:startTime, teamEventEndTime:endTime, teamEventTeamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamevent/createevent",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                editTeamEvent: function(loginId, teamId, eventName, description, locationString, startTime, endTime, eventId){
                    var postData = {loginId:loginId, teamEventId:eventId, teamEventName:eventName, teamEventDescription:description, teamEventLocationString:locationString, teamEventStartTime:startTime, teamEventEndTime:endTime, teamEventTeamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamevent/editevent",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                deleteTeamEvent: function(loginId, teamId, eventId){
                    var postData = {loginId:loginId, teamEventId:eventId, teamEventTeamId:teamId};
                    var promise = $http({
                        url: "https://api.awayteam.redshrt.com/teamevent/deleteevent",
                        method: "POST",
                        data: postData
                    });

                    return promise;
                },
                setTeamEvents: function(data){
                    if(data != null){
                        for(var i = 0; i < data.length; i++){
                            data[i].teamEventStartTime = new Date(data[i].teamEventStartTime);
                            data[i].teamEventEndTime = new Date(data[i].teamEventEndTime);
                        }
                        wrappedService.teamEvents = data;
                    }
                }
            };

            return wrappedService;
        };
    });
