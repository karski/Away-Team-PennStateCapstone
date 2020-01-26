(function(app) {

    app.config(['$stateProvider', function ($stateProvider) {
        $stateProvider.state('error', {
            url: '/error',
            views: {
                "main": {
                    templateUrl: 'error/error.tpl.html'
                }
            },
            data:{ pageTitle: 'Error' },
            accessLevel: accessLevels.public
        });
    }]);

}(angular.module("AwayTeam.error", [
    'AwayTeam.router'
])));