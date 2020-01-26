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
        $stateProvider.state('members', {
            url: '/members',
            views: {
                "main": {
                    controller: 'MemberController',
                    templateUrl: 'members/members.tpl.html'
                }
            },
            data:{ pageTitle: 'Members' },
            accessLevel: accessLevels.public
        });
    }]);

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('MemberController', ['$scope', '$modal', 'teamService', 'memberService', 'managerService', 'loginService', 'growlNotifications', function ($scope, $modal, teamService, memberService, managerService, loginService, growlNotifications) {
        $scope.teamService = teamService;
        $scope.teamMembers = memberService.teamMembers;
        $scope.currentUserManager = memberService.currentUserManager;
        $scope.managerCount = memberService.managerCount;

        $scope.refreshTeam = function(){
            var teamPromise = teamService.getTeam(teamService.selectedTeam.teamId, loginService.user.loginId);
            teamPromise.success(function(data){
                if (data.status === "success") {
                    if(data.response.members != null && data.response.members.length > 0){
                        memberService.setTeamMembers(data.response.members, loginService.user.loginId);
                    }else{
                        memberService.setTeamMembers([]);
                    }
                }
            });
        };

        $scope.promoteMember = function(member){
            memberService.selectedMember = member;
            if($scope.currentUserManager){
                var promotePromise = managerService.takeAction(loginService.user.loginId, teamService.selectedTeam.teamId, "promote", member.loginId);
                promotePromise.success(function(data){
                    growlNotifications.add("Successfully promoted "+member.firstName, "success");
                    $scope.refreshTeam();
                });
                promotePromise.error(function(){
                    growlNotifications.add("Failed to promote "+member.firstName, "danger");
                });
            }
        };

        $scope.demoteMember = function(member){
            memberService.selectedMember = member;
            if($scope.currentUserManager) {
                if ($scope.managerCount > 1) {
                    var demotePromise = managerService.takeAction(loginService.user.loginId, teamService.selectedTeam.teamId, "demote", member.loginId);
                    demotePromise.success(function () {
                        growlNotifications.add("Successfully demoted " + member.firstName, "success");
                        $scope.refreshTeam();
                    });
                    demotePromise.error(function () {
                        growlNotifications.add("Failed to demote " + member.firstName, "danger");
                    });
                } else {
                    growlNotifications.add("You cannot demote the last remaining manager " + member.firstName, "danger");
                }
            }
        };

        $scope.removeMember = function(member){
            memberService.selectedMember = member;
            if($scope.currentUserManager) {
                if (member.manager && $scope.managerCount == 1) {
                    var removePromise = managerService.takeAction(loginService.user.loginId, teamService.selectedTeam.teamId, "remove", member.loginId);
                    removePromise.success(function (data) {
                        if(data.response === "success"){
                            growlNotifications.add("Successfully removed " + member.firstName, "success");
                            $scope.refreshTeam();
                        }
                    });
                    removePromise.error(function (data) {
                        growlNotifications.add("Failed to remove " + member.firstName, "danger");
                    });
                } else {
                    growlNotifications.add("You cannot remove the last remaining manager " + member.firstName, "danger");
                }
            }
        };

        $scope.$watch(function () {
                return memberService.teamMembers;
            },
            function(newVal, oldVal) {
                $scope.currentUserManager = memberService.currentUserManager;
                $scope.managerCount = memberService.managerCount;
                $scope.teamMembers = memberService.teamMembers;
            }, true);



    }]);

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.members", [
    'AwayTeam.router'
])));