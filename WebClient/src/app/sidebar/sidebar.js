/**
 * Created by Clay Parker on 7/20/2014.
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
    app.controller('SidebarController', ['$scope', '$http', '$timeout', '$modal', '$log', 'loginService', 'teamService', function ($scope, $http, $timeout, $modal, $log, loginService, teamService) {

    }]);

// The name of the module, followed by its dependencies (at the bottom to facilitate enclosure)
}(angular.module("AwayTeam.sidebar", [
    'AwayTeam.router'
])));