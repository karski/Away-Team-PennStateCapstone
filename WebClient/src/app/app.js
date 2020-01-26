(function(app) {

    app.config(["$stateProvider", '$urlRouterProvider', '$httpProvider', function ($stateProvider, $urlRouterProvider, $httpProvider) {
        //intercepts all httpRequests
        $httpProvider.defaults.transformRequest = function(data){
            if (data === undefined) {
              return data;
            }
            return $.param(data);
        };
        $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded; charset=UTF-8';
        $urlRouterProvider.otherwise('/home');

        $httpProvider.interceptors.push('identityInjector');
    }]);

    app.run(function ($rootScope) {
        /**
       * $rootScope.doingResolve is a flag useful to display a spinner on changing states.
       * Some states may require remote data so it will take awhile to load.
       */
      var resolveDone = function () { $rootScope.doingResolve = false; };
      $rootScope.doingResolve = false;

      $rootScope.$on('$stateChangeStart', function () {
        $rootScope.doingResolve = true;
      });
      $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams){
          if ( angular.isDefined( toState.data.pageTitle ) ) {
              $rootScope.pageTitle = toState.data.pageTitle ;
          }
          resolveDone();
      });
      $rootScope.$on('$stateChangeError', resolveDone);
      $rootScope.$on('$statePermissionError', resolveDone);

      $rootScope.editUserObj = {};
      

    });

    app.controller('AppController', ['$scope', '$state', '$stateParams',
        function ($scope, $state, $stateParams) {
            // Expose $state and $stateParams to the <body> tag
            $scope.$state = $state;
            $scope.$stateParams = $stateParams;
    }]);

    app.factory('identityInjector', ['$q', '$injector', function($q, $injector){
        var identityInjector = {
            request: function(config) {
                var loginService = $injector.get('loginService');
                if (loginService != null && loginService.userIdentifier != null && config.data != null && loginService.user != null && loginService.isLoggedIn) {
                    var unixTimeStamp                   = Math.round((new Date()).getTime() / 1000);
                    var challenge = unixTimeStamp       + loginService.user.loginId.toLowerCase() + loginService.userIdentifier;
                    var encryptChallenge                = CryptoJS.HmacSHA256(challenge, loginService.userSecret).toString(CryptoJS.enc.Hex);
                    config.data.AWT_AUTH                = loginService.userIdentifier;
                    config.data.AWT_AUTH_CHALLENGE      = encryptChallenge;
                }
                return config;
            }
        };
        return identityInjector;
    }]);

}(angular.module("AwayTeam", [
    'angularFileUpload',
    'AwayTeam.header',
    'AwayTeam.home',
    'AwayTeam.editAccount',
    'AwayTeam.error',
    'AwayTeam.expenses',
    'AwayTeam.events',
    'AwayTeam.members',
    'AwayTeam.phoneFilter',
    'AwayTeam.register',
    'AwayTeam.router',
    'AwayTeam.resizable',
    'AwayTeam.sidebar',
    'AwayTeam.tasks',
    'AwayTeam.team',
    'AwayTeam.validators',
    'eventService',
    'expenseService',
    'growlNotifications',
    'loginService',
    'managerService',
    'memberService',
    'ngGrid',
    'ngMap',
    'ngSanitize',
    'taskService',
    'teamService',
    'templates-app',
    'templates-common',
    'ui.bootstrap',
    'ui.router.state',
    'ui.router'
])));
