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
 */
(function(app) {

    app.config(['$stateProvider', function ($stateProvider) {
        $stateProvider.state('events', {
            url: '/events',
            views: {
                "main": {
                    controller: 'EventController',
                    templateUrl: 'events/events.tpl.html'
                }
            },
            data:{ pageTitle: 'Events' },
            accessLevel: accessLevels.public
        });
    }]);

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('EventController', ['$scope', '$modal', 'teamService', 'eventService', 'loginService', 'growlNotifications', function ($scope, $modal, teamService, eventService, loginService, growlNotifications) {
        $scope.teamService = teamService;
        $scope.teamEvents = eventService.teamEvents;
        $scope.selectedEvent = eventService.selectedEvent;
        $scope.dateFormatDate = 'EEE MMM d h:mm a';
        $scope.dateFormatTime = 'h:mm a';
        $scope.dateFormatFull = 'EEEE, MMMM d, yyyy, h:mm a';

        $scope.removeEvent = function(event){
            eventService.selectedEvent = event;
            var removePromise = eventService.deleteTeamEvent(loginService.user.loginId, teamService.selectedTeam.teamId, event.teamEventId);
            removePromise.success(function (data) {
                if(data.response === "deletion successful"){
                    growlNotifications.add("Successfully removed " + event.teamEventName, "success");
                    $scope.refreshTeam();
                }
            });
            removePromise.error(function (data) {
                growlNotifications.add("Failed to remove " +event.teamEventName, "danger");
            });
        };

        $scope.createEventModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'events/createEvent.tpl.html',
                controller: 'CreateEventModalCtrl'
            });
        };

        $scope.modifyEventModal = function (event) {
            eventService.selectedEvent = event;
            var modalInstance = $modal.open({
                templateUrl: 'events/createEvent.tpl.html',
                controller: 'ModifyEventModalCtrl'
            });
        };


        $scope.$watch(function () {
            return eventService.teamEvents;
        },
        function(newVal, oldVal) {
            $scope.teamEvents = eventService.teamEvents;
            $scope.selectedEvent = {};
        }, true);


        $scope.refreshTeam = function(){
            var teamPromise = teamService.getTeam(teamService.selectedTeam.teamId, loginService.user.loginId);
            teamPromise.success(function(data){
                if (data.status === "success") {
                    if(data.response.events != null && data.response.events.length > 0){
                        eventService.setTeamEvents(data.response.events);
                    }else{
                        eventService.setTeamEvents([]);
                    }
                }
            });
        };


    }]);


    app.controller('CreateEventModalCtrl', ['$scope', '$timeout', '$modalInstance', '$http', 'teamService', 'loginService', 'eventService', 'growlNotifications', function ($scope, $timeout, $modalInstance, $http, teamService, loginService, eventService, growlNotifications) {
        $scope.modalTitle = "Add Team Event";
        $scope.event = {teamEventStartTime:new Date(), teamEventEndTime:new Date().addTime(1,0)};
        $scope.eventServiceWorking = false;

        $scope.today = function() {
            $scope.expDate = new Date();
        };
        $scope.today();

        $scope.clear = function () {
            $scope.expDate = null;
        };

        $scope.toggleMin = function() {
            $scope.minDate = $scope.minDate ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function($event,opened) {

            $timeout(function() {
                $event.preventDefault();
                $event.stopPropagation();

                $scope[opened] = true;
            });
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };

        $scope.format = 'yyyy-MM-dd';

        $scope.ok = function () {
            var event = $scope.event;
            $scope.eventServiceWorking = true;
            var startDate = event.teamEventStartTime.getFullYear() + "-" + (event.teamEventStartTime.getMonth()+1) + "-" + event.teamEventStartTime.getDate() + " "+ (event.teamEventStartTime.getHours()<10?'0':'') + event.teamEventStartTime.getHours() + ":" + (event.teamEventStartTime.getMinutes()<10?'0':'')  + event.teamEventStartTime.getMinutes();
            var endDate = event.teamEventEndTime.getFullYear() + "-" + (event.teamEventEndTime.getMonth()+1) + "-" + event.teamEventEndTime.getDate() + " "+ (event.teamEventEndTime.getHours()<10?'0':'')  + event.teamEventEndTime.getHours() + ":" + (event.teamEventEndTime.getMinutes()<10?'0':'') + event.teamEventEndTime.getMinutes();
            var addEventPromise = eventService.createTeamEvent(loginService.user.loginId, teamService.selectedTeam.teamId, event.teamEventName, event.teamEventDescription, event.teamEventLocationString, startDate, endDate);
            addEventPromise.success(function(event, status, headers, config) {
                if(event.status == "success"){
                    $modalInstance.close();
                    $scope.eventServiceWorking = false;
                    growlNotifications.add('Successfully Added an Event!', 'success');
                    $scope.refreshTeam();
                }else{
                    growlNotifications.add('Error Adding an Event!', 'danger');
                    $scope.eventServiceWorking = false;
                }

            });
            addEventPromise.error(function () {
                growlNotifications.add('Error Adding an Event!', 'danger');
                $scope.eventServiceWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };


        $scope.refreshTeam = function(){
            var teamPromise = teamService.getTeam(teamService.selectedTeam.teamId, loginService.user.loginId);
            teamPromise.success(function(data){
                if (data.status === "success") {
                    if(data.response.events != null && data.response.events.length > 0){
                        eventService.setTeamEvents(data.response.events);
                    }else{
                        eventService.setTeamEvents([]);
                    }
                }
            });
        };
    }]);

    app.controller('ModifyEventModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'loginService', 'eventService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, loginService, eventService, growlNotifications) {
        $scope.modalTitle = "Edit Team Event";
        $scope.event = eventService.selectedEvent;
        $scope.eventServiceWorking = false;

        $scope.today = function() {
            $scope.expDate = new Date();
        };
        $scope.today();

        $scope.clear = function () {
            $scope.expDate = null;
        };

        $scope.toggleMin = function() {
            $scope.minDate = $scope.minDate ? null : new Date();
        };
        $scope.toggleMin();

        $scope.open = function($event,opened) {

            $timeout(function() {
                $event.preventDefault();
                $event.stopPropagation();

                $scope[opened] = true;
            });
        };

        $scope.dateOptions = {
            formatYear: 'yy',
            startingDay: 1
        };

        $scope.format = 'yyyy-MM-dd';

        $scope.ok = function () {
            var event = $scope.event;
            $scope.eventServiceWorking = true;
            var startDate = event.teamEventStartTime.getFullYear() + "-" + (event.teamEventStartTime.getMonth()+1) + "-" + event.teamEventStartTime.getDate() + " "+ (event.teamEventStartTime.getHours()<10?'0':'') + event.teamEventStartTime.getHours() + ":" + (event.teamEventStartTime.getMinutes()<10?'0':'')  + event.teamEventStartTime.getMinutes();
            var endDate = event.teamEventEndTime.getFullYear() + "-" + (event.teamEventEndTime.getMonth()+1) + "-" + event.teamEventEndTime.getDate() + " "+ (event.teamEventEndTime.getHours()<10?'0':'')  + event.teamEventEndTime.getHours() + ":" + (event.teamEventEndTime.getMinutes()<10?'0':'') + event.teamEventEndTime.getMinutes();
            var modifyEventPromise = eventService.editTeamEvent(loginService.user.loginId, teamService.selectedTeam.teamId, event.teamEventName, event.teamEventDescription, event.teamEventLocationString, startDate, endDate, event.teamEventId);
            modifyEventPromise.success(function(event, status, headers, config) {
                if(event.status == "success"){
                    $modalInstance.close();
                    $scope.eventServiceWorking = false;
                    growlNotifications.add('Successfully Edited an Event!', 'success');
                    $scope.refreshTeam();
                }else{
                    growlNotifications.add('Error Editing an Event!', 'danger');
                    $scope.eventServiceWorking = false;
                }

            });
            modifyEventPromise.error(function () {
                growlNotifications.add('Error Editing an Event!', 'danger');
                $scope.eventServiceWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };


        $scope.refreshTeam = function(){
            var teamPromise = teamService.getTeam(teamService.selectedTeam.teamId, loginService.user.loginId);
            teamPromise.success(function(data){
                if (data.status === "success") {
                    if(data.response.events != null && data.response.events.length > 0){
                        eventService.setTeamEvents(data.response.events);
                    }else{
                        eventService.setTeamEvents([]);
                    }
                }
            });
        };
    }]);

    Date.prototype.addTime= function(h,m){
        this.setHours(this.getHours()+h);
        this.setMinutes(this.getMinutes()+m);
        return this;
    };

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.events", [
    'AwayTeam.router'
])));