/**
 * Created by Clay Parker on 7/20/2014.
 */
angular.module('memberService', [])
    .provider('memberService', function () {
        var errorState = 'error',
            logoutState = 'home';

        this.$get = function ($rootScope, $http, $q, $state, $log) {

            /**
             * Low-level, private functions.
             */
           var managerCheck = function(data, loginId){
                wrappedService.currentUserManager = false;
                wrappedService.managerCount = 0;
                wrappedService.teamMembers = [];
                if(data != null) {
                    for (var i = 0; i < data.length; i++) {
                        if (loginId === data[i].loginId && data[i].manager === "true") {
                            wrappedService.currentUserManager = true;
                        }
                        if (data[i].manager === "true") {
                            wrappedService.managerCount++;
                        }

                        //default location to Penn State lat and long
                        if (data[i].locLatitude == null) {
                            data[i].locLatitude = "40.798412";
                        }
                        if (data[i].locLongitude == null ) {
                            data[i].locLongitude = "-77.859897";
                        }
                    }
                    wrappedService.teamMembers = data;
                }
            };

            /**
             * High level, public methods
             */
            var wrappedService = {
                /**
                 * Public properties
                 */
                teamMembers: [],
                selectedMember: {},
                currentUserManager: false,
                managerCount: 0,

                removeUser: function(loginId, teamId, confirmed){
                    var postData = {teamId:teamId, loginId:loginId, confirmed:confirmed};
                    var promise =$http({
                        url: "https://api.awayteam.redshrt.com/teammember/leaveteam",
                        method: "POST",
                        data: postData
                    });
                    return promise;
                },

                setTeamMembers: function(data, loginId){
                    managerCheck(data, loginId);
                }
            };

            return wrappedService;
        };
    });
