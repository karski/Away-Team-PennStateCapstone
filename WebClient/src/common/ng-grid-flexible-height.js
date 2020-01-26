ngGridCustomFlexibleHeightPlugin = function (opts) {
    if(opts == null){
        opts = {};
        opts.rowHeight = 30;
        opts.headerRowHeight = 34;
    }
    var self = this;
    self.grid = null;
    self.scope = null;
    self.init = function (scope, grid, services) {
        self.domUtilityService = services.DomUtilityService;
        self.grid = grid;
        self.scope = scope;
        var recalcHeightForData = function () { setTimeout(innerRecalcForData, 1); };
        var innerRecalcForData = function () {
            var gridId = self.grid.gridId;
            var footerPanelSel = '.' + gridId + ' .ngFooterPanel';
            var extraHeight = self.grid.$topPanel.height() + $(footerPanelSel).height();
            var naturalHeight = (grid.data.length - 1) * opts.rowHeight + opts.headerRowHeight;
            self.grid.$viewport.css('height', (naturalHeight + 2) + 'px');
            self.grid.$root.css('height', (naturalHeight + extraHeight + 2) + 'px');
            // self.grid.refreshDomSizes();
            if (!self.scope.$$phase) {
                self.scope.$apply(function () {
                    self.domUtilityService.RebuildGrid(self.scope, self.grid);
                });
            }
            else {
                // $digest or $apply already in progress
                self.domUtilityService.RebuildGrid(self.scope, self.grid);
            }
        };
        scope.$watch(grid.config.data, recalcHeightForData);

    };
};