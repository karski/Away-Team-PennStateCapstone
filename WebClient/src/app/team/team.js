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
        $stateProvider.state('team', {
            url: '/team',
            views: {
                "main": {
                    controller: 'TeamController',
                    templateUrl: 'team/team.tpl.html'
                }
            },
            data:{ pageTitle: 'Team' },
            accessLevel: accessLevels.public
        });
    }]);

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('TeamController', ['$scope', '$state', '$modal', 'teamService', 'loginService', 'memberService', 'eventService', 'growlNotifications', function ($scope, $state, $modal, teamService, loginService, memberService, eventService, growlNotifications) {
        $scope.teamService = teamService;
        $scope.loginService = loginService;

        $scope.selectTeam = function(teamId, pendingApproval){
            if(!pendingApproval){
                var teamPromise = teamService.getTeam(teamId, loginService.user.loginId);
                teamPromise.success(function(data){
                    if (data.status === "success") {
                        if(data.response.members != null && data.response.members.length > 0){
                            memberService.setTeamMembers(data.response.members, loginService.user.loginId);
                        }else{
                            memberService.setTeamMembers([]);
                        }

                        if(data.response.events != null && data.response.events.length > 0){
                            eventService.setTeamEvents(data.response.events);
                        }else{
                            eventService.setTeamEvents([]);
                        }
                    }
                });
                if($state.current.name != "events" && $state.current.name != "expenses" && $state.current.name != "team" && $state.current.name != "members" ){
                    $state.go('team');
                }
            }
        };

        $scope.teamInit = function(){
            if(!loginService.isLoggedIn){
                $state.go('home');
                return;
            }
        };

        $scope.teamInit();

        $scope.leaveTeam = function(){
            var leavePromise = teamService.leaveTeam(loginService.user.loginId, teamService.selectedTeam.teamId, true);
            leavePromise.success(function(data){
                if(data.status === "success"){
                    growlNotifications.add("Successfully left team", "success");
                    $state.go('home');
                    return;
                }
            });
            leavePromise.error(function(data){
                growlNotifications.add("Failed to leave team", "danger");
            });
        };

        $scope.createTeamModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'team/createTeam.tpl.html',
                controller: 'CreateTeamModalCtrl'
            });
        };

        $scope.modifyTeamModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'team/createTeam.tpl.html',
                controller: 'ModifyTeamModalCtrl'
            });
        };


        $scope.joinTeamModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'team/joinTeam.tpl.html',
                controller: 'JoinTeamModalCtrl'
            });
        };

    }]);

    app.controller('CreateTeamModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'loginService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, loginService, growlNotifications) {
        $scope.modalTitle = "Create Team";
        $scope.createTeam = {
            teamManaged: false
        };
        $scope.createTeamWorking = false;
        $scope.ok = function () {
            var team = $scope.createTeam;
            $scope.createTeamWorking = true;
            var createTeamPromise = teamService.createTeam(loginService.user.loginId, team.teamName, team.teamDescription, team.teamLocationName, team.teamManaged);
            createTeamPromise.success(function(user, status, headers, config) {
                $modalInstance.close();
                $scope.createTeamWorking = false;
                growlNotifications.add('Successfully Created Team!', 'success');
                teamService.loadAllTeams();
                teamService.loadUserTeams(loginService.user.loginId);
            });
            createTeamPromise.error(function () {
                $scope.createTeamWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

    app.controller('ModifyTeamModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'memberService', 'loginService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, memberService, loginService, growlNotifications) {
        $scope.modalTitle = "Modify Team";
        $scope.createTeam = teamService.selectedTeam;
        $scope.createTeamWorking = false;
        $scope.disableEdit = ($scope.createTeam.teamManaged && memberService.currentUserManager) || !$scope.createTeam.teamManaged;

        $scope.ok = function () {
            var team = $scope.createTeam;
            $scope.createTeamWorking = true;
            var modifyTeamPromise = teamService.modifyTeam(loginService.user.loginId, team.teamId, team.teamName, team.teamLocationName, team.teamDescription,  team.teamManaged);
            modifyTeamPromise.success(function(team, status, headers, config) {
                if(team.status == "success"){
                    $modalInstance.close();
                    $scope.createTeamWorking = false;
                    growlNotifications.add('Successfully Modified Team!', 'success');
                    teamService.loadAllTeams();
                    var loadTeamsPromise = teamService.loadUserTeams(loginService.user.loginId);
                    loadTeamsPromise.success(function(team){
                        if(team.status === "success"){
                            if(teamService.selectedTeam.teamId != null){ //reload selected team
                                for(var i = 0; i < teamService.userTeams.length; i++){
                                    if(teamService.selectedTeam.teamId == teamService.userTeams[i].teamId){
                                        teamService.getTeam(loginService.user.loginId, teamService.userTeams[i]);
                                    }
                                }
                            }
                        }else{
                            growlNotifications.add('Error loading teams!', 'danger');
                        }
                    });
                    loadTeamsPromise.error(function(){
                        growlNotifications.add('Error loading teams!', 'danger');
                    });
                }else{
                    growlNotifications.add('Error Modifying Team!', 'danger');
                }

            });
            modifyTeamPromise.error(function () {
                growlNotifications.add('Error Modifying Team!', 'danger');
                $scope.createTeamWorking = false;
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

    app.controller('JoinTeamModalCtrl', ['$scope', '$modalInstance', '$http', 'teamService', 'loginService', 'growlNotifications', function ($scope, $modalInstance, $http, teamService, loginService, growlNotifications) {
        $scope.allTeams = {};
        $scope.selectedTeam = [];
        $scope.joinTeamWorking = false;
        $scope.loadingTeams = true;

        $scope.filterName = '';
        $scope.filterLocation = '';
        $scope.filterOptions = {
            filterText: '',
            useExternalFilter: false
        };

        $scope.joinTeamGridOptions = {
            data: 'allTeams',
            columnDefs: [
                { field: 'teamName', displayName: 'Name' },
                { field: 'teamDescription',   displayName: 'Description'   },
                { field: 'teamLocationName',  displayName: 'Location'  }
            ],
            multiSelect: false,
            afterSelectionChange: function(data) {
                $scope.teamNotSelected();
            },
            selectedItems: $scope.selectedTeam,
            filterOptions: $scope.filterOptions,
            sortInfo: {
                fields: ['teamName'],
                directions: ['asc']
            }
        };

        $scope.$watch(function () {
                return teamService.allTeams;
            },
            function(newVal, oldVal) {
                $scope.allTeams = teamService.allTeams;
            }, true);

        $scope.loadTeams = function() {
            $scope.loadingTeams = true;
            var promise = teamService.loadAllTeams();
            promise.success(function(){
                $scope.loadingTeams = false;
            });
            promise.error(function(){
                $scope.loadingTeams = false;
            });
        };

        $scope.teamNotSelected = function() {
            if ($scope.selectedTeam[0] != null) {
                return false;
            }
            else {
                return true;
            }
        };

        $scope.setFilterName = function(){
            $scope.filterName = this.filterName;
            $scope.setFilterText();
        };

        $scope.setFilterLocation = function(){
            $scope.filterLocation = this.filterLocation;
            $scope.setFilterText();
        };

        $scope.setFilterText = function()
        {
            $scope.filterOptions.filterText = 'Name:' + $scope.filterName + ';Location:' + $scope.filterLocation;
            $scope.test = 'Name: ' + $scope.filterName + ';Location:' + $scope.filterLocation;
        };

        $scope.ok = function () {
            var team = $scope.selectedTeam[0];
            $scope.joinTeamWorking = true;
            var joinTeamPromise = teamService.joinTeam(loginService.user.loginId, $scope.selectedTeam[0].teamId);
            joinTeamPromise.success(function(data, status, headers, config) {
                if(data.status === "success"){
                    $modalInstance.close();
                    $scope.joinTeamWorking = false;
                    growlNotifications.add('Request sent to join ' + $scope.selectedTeam[0].teamName, 'success');
                    teamService.loadUserTeams(loginService.user.loginId);
                }
                else{
                    $scope.joinTeamWorking = false; //TODO present error message to the user
                }
            });
            joinTeamPromise.error(function () {
                $scope.joinTeamWorking = false; //TODO present error message to the user
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.team", [
    'AwayTeam.router'
])));