/**
 * Created by Clay Parker on 7/14/2014.
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
 */
(function(app) {

    // As you add controllers to a module and they grow in size, feel free to place them in their own files.
    //  Let each module grow organically, adding appropriate organization and sub-folders as needed.
    app.controller('HeaderController', ['$scope', '$http', '$timeout', '$modal', '$log', 'loginService', 'teamService', 'managerService', 'growlNotifications', function ($scope, $http, $timeout, $modal, $log, loginService, teamService, managerService, growlNotifications) {
        /*********************** AUTHENTICATION FUNCTIONS *****************/
            // loginService exposed and a new Object containing login user/pwd
        $scope.loginService = loginService;
        $scope.teamService = teamService;
        $scope.managerService = managerService;
        $scope.userTeams = [];
        $scope.pendingUsers = [];
        $scope.navbarCollapsed = true;


        $scope.login = {
            working: false,
            wrong: false
        };
        $scope.attemptLogin = function () {
            // setup promise, and 'working' flag
            var userLogin = {loginId:$scope.login.loginId, password:$scope.login.password};
            var rememberCreds = $scope.login.rememberMe;
            var loginPromise = $http({
                url: "https://api.awayteam.redshrt.com/user/AuthenticatePassword",
                method: "POST",
                data: userLogin
            });

            $scope.login.working = true;
            $scope.login.wrong = false;

            loginService.loginUser(loginPromise, userLogin, rememberCreds, getUsersTeams);
            loginPromise.error(function () {
                $scope.login.wrong = true;
                $timeout(function () { $scope.login.wrong = false; }, 8000);
                growlNotifications.add('Error with Credentials!', 'danger');
            });
            loginPromise.finally(function () {
                $scope.login.working = false;
            });
        };
        $scope.logout = function () {
            loginService.logoutUser();
        };

        $scope.setLoginForm = function(){
            if(localStorage.getItem("awayteamLoginId") != null){
                $scope.login.loginId = localStorage.getItem("awayteamLoginId");
                $scope.login.password = localStorage.getItem("awayteamPassword");
                $scope.login.rememberMe = "true";
                $scope.attemptLogin();
            }
        };

        $scope.changePWOpen = function () {
            var modalInstance = $modal.open({
                templateUrl: 'changePassword/changePassword.tpl.html',
                controller: 'ChangePasswordModalCtrl'
            });
        };

        $scope.resetPWOpen = function () {
            var modalInstance = $modal.open({
                templateUrl: 'changePassword/resetPassword.tpl.html',
                controller: 'ResetPasswordModalCtrl'
            });
        };

        $scope.$watch(function () {
                return teamService.userTeams;
            },
            function(newVal, oldVal) {
               $scope.userTeams = teamService.userTeams;
            }, true);

        $scope.$watch(function () {
                return managerService.pendingUsers;
            },
            function(newVal, oldVal) {
                $scope.pendingUsers = managerService.pendingUsers;
            }, true);


        var getUsersTeams = function(){
            if($scope.loginService.isLoggedIn){
                var userTeamPromise = $scope.teamService.loadUserTeams($scope.login.loginId);
                $scope.teamService.loadAllTeams();
                $scope.loadPendingUsers();
            }
        };

        $scope.loadPendingUsers = function(){
            if($scope.login.loginId != null){
                var pendingUserPromise = $scope.managerService.getPendingUsers($scope.login.loginId);
            }
            else{
                $scope.managerService.pendingUsers = [];
            }
        };

        $scope.managerAction = function(teamId, action, subjectId){
            if($scope.login.loginId != null) {
                var takeActionPromise = $scope.managerService.takeAction($scope.login.loginId, teamId, action, subjectId);
                takeActionPromise.success(function(){
                    if(action === "approve"){
                        growlNotifications.add('Successfully approved user!', 'success');
                        $scope.loadPendingUsers();
                    }else{
                        growlNotifications.add('Successfully denied user!', 'success');
                        $scope.loadPendingUsers();
                    }

                });
                takeActionPromise.error(function(){
                    growlNotifications.add('Failed to take action on user!', 'danger');
                });
            }
        };

        // Function to replicate setInterval using $timeout service.
        $scope.intervalFunction = function(){
            //load pending users every 5 minutes
            $timeout(function() {
                $scope.loadPendingUsers();
            }, 300000);
        };

        //start loading pending users periodically

        $scope.intervalFunction();


    }]);


    app.controller('ChangePasswordModalCtrl', ['$scope', '$modalInstance', '$http', '$timeout',  'loginService', 'growlNotifications',  function ($scope, $modalInstance, $http, $timeout, loginService, growlNotifications) {
        $scope.changePassword = {};
        $scope.changePasswordWorking = false;

        $scope.ok = function () {
            var postData = $scope.changePassword;
            postData.loginId = loginService.user.loginId;
            $scope.changePasswordWorking = true;
            var changePasswordPromise = $http({
                url: "https://api.awayteam.redshrt.com/user/changepassword",
                method: "POST",
                data: postData
            });
            changePasswordPromise.success(function(password, status, headers, config) {
                if (password.response === "success") {
                    $scope.changePasswordWorking = false;
                    growlNotifications.add('Successfully Changed Password!', 'success');
                    $modalInstance.close();
                    loginService.logoutUser();
                }
            });
            changePasswordPromise.error(function () {
                growlNotifications.add('Failed to Change Password!', 'danger');
                $scope.changePasswordWorking = false; //TODO present error message to the user
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

    app.controller('ResetPasswordModalCtrl', ['$scope', '$modalInstance', '$http', '$timeout',  'loginService', 'growlNotifications',  function ($scope, $modalInstance, $http, $timeout, loginService, growlNotifications) {
        $scope.resetPassword = {};
        $scope.resetPasswordWorking = false;

        $scope.ok = function () {
            var postData = $scope.resetPassword;
            $scope.resetPasswordWorking = true;
            var resetPasswordPromise = $http({
                url: "https://api.awayteam.redshrt.com/user/ResetPassword",
                method: "POST",
                data: postData
            });
            resetPasswordPromise.success(function(password, status, headers, config) {
                if (password.response === "success") {
                    $scope.resetPasswordWorking = false;
                    growlNotifications.add('Successfully Reset Password! Check your provided email for new password.', 'success');
                    $modalInstance.close();
                }
            });
            resetPasswordPromise.error(function () {
                growlNotifications.add('Failed to Reset Password!', 'danger');
                $scope.resetPasswordWorking = false; //TODO present error message to the user
            });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    }]);

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.header", [
    'AwayTeam.router'
])));