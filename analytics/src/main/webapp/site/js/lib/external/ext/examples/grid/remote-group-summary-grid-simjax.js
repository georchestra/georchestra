Ext.Loader.setConfig({ enabled: true });
Ext.Loader.setPath('Ext.ux', '../ux');

Ext.require([
    'Ext.ux.ajax.SimManager'
]);

/*
 * Setup our faux Ajax response "simlet".
 */
function initAjaxSim () {
    Ext.ux.ajax.SimManager.register({
        'remote-group-summary-grid.php' : {
            stype: 'json',

            data: [
                {projectId: 100, project: 'Forms: Field Anchoring', taskId: 112, description: 'Integrate 2.0 Forms with 2.0 Layouts', estimate: 6, rate: 150, due:'06/24/2007'},
                {projectId: 100, project: 'Forms: Field Anchoring', taskId: 113, description: 'Implement AnchorLayout', estimate: 4, rate: 150, due:'06/25/2007'},
                {projectId: 100, project: 'Forms: Field Anchoring', taskId: 114, description: 'Add support for multiple types of anchors', estimate: 4, rate: 150, due:'06/27/2007'},
                {projectId: 100, project: 'Forms: Field Anchoring', taskId: 115, description: 'Testing and debugging', estimate: 8, rate: 0, due:'06/29/2007'},
                {projectId: 101, project: 'Grid: Single-level Grouping', taskId: 101, description: 'Add required rendering "hooks" to GridView', estimate: 6, rate: 100, due:'07/01/2007'},
                {projectId: 101, project: 'Grid: Single-level Grouping', taskId: 102, description: 'Extend GridView and override rendering functions', estimate: 6, rate: 100, due:'07/03/2007'},
                {projectId: 101, project: 'Grid: Single-level Grouping', taskId: 103, description: 'Extend Store with grouping functionality', estimate: 4, rate: 100, due:'07/04/2007'},
                {projectId: 101, project: 'Grid: Single-level Grouping', taskId: 121, description: 'Default CSS Styling', estimate: 2, rate: 100, due:'07/05/2007'},
                {projectId: 101, project: 'Grid: Single-level Grouping', taskId: 104, description: 'Testing and debugging', estimate: 6, rate: 100, due:'07/06/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 105, description: 'Ext Grid plugin integration', estimate: 4, rate: 125, due:'07/01/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 106, description: 'Summary creation during rendering phase', estimate: 4, rate: 125, due:'07/02/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 107, description: 'Dynamic summary updates in editor grids', estimate: 6, rate: 125, due:'07/05/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 108, description: 'Remote summary integration', estimate: 4, rate: 125, due:'07/05/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 109, description: 'Summary renderers and calculators', estimate: 4, rate: 125, due:'07/06/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 110, description: 'Integrate summaries with GroupingView', estimate: 10, rate: 125, due:'07/11/2007'},
                {projectId: 102, project: 'Grid: Summary Rows', taskId: 111, description: 'Testing and debugging', estimate: 8, rate: 125, due:'07/15/2007'}
            ],

            getGroupSummary: function (groupField, rows, ctx) {
                var ret = Ext.apply({}, rows[0]);
                ret.cost = 0;
                ret.estimate = 0;
                Ext.each(rows, function (row) {
                    ret.estimate += row.estimate;
                    ret.cost += row.estimate * row.rate;
                });
                ret.estimate *= -1;
                ret.cost *= -1;
                return ret;
            }
        }
    });
}

Ext.onReady(initAjaxSim);
