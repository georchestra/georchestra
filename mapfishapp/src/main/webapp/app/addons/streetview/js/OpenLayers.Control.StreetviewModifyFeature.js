OpenLayers.Control.StreetviewModifyFeature = OpenLayers.Class(OpenLayers.Control.ModifyFeature, {
    /**
     * Method: collectRadiusHandle
     * Collect the radius handle for the selected geometry.
     * Overloaded here to let the radiusHandler stay at the same angle.
     */
    collectRadiusHandle: function() {
        var geometry = this.feature.geometry;
        if (!geometry.angle) {
            // 0 means North
            // 90 means East, etc...
            geometry.angle = 0;
        }
        var bounds = geometry.getBounds();
        var center = bounds.getCenterLonLat();
        var originGeometry = new OpenLayers.Geometry.Point(
            center.lon, center.lat
        );
        // create this radiusGeometry point geometry at a given distance from the center of bbox, 
        // which is a bit bigger than the original bbox and at the correct angle.
        var radiusGeometry = new OpenLayers.Geometry.Point(
            center.lon + Math.sin((geometry.angle)/180*Math.PI) * 12 * bounds.getWidth(),
            center.lat + Math.cos((geometry.angle)/180*Math.PI) * 12 * bounds.getWidth()
        );
        var radius = new OpenLayers.Feature.Vector(radiusGeometry);

        radiusGeometry.move = function(x, y) {
            OpenLayers.Geometry.Point.prototype.move.call(this, x, y);
            var dx1 = this.x - originGeometry.x;
            var dy1 = this.y - originGeometry.y;
            var dx0 = dx1 - x;
            var dy0 = dy1 - y;
            var a0 = Math.atan2(dy0, dx0);
            var a1 = Math.atan2(dy1, dx1);
            var angle = a1 - a0;
            angle *= 180 / Math.PI;
            geometry.rotate(angle, originGeometry);
            geometry.angle -= angle;
        };
        radius._sketch = true;
        this.radiusHandle = radius;
        this.radiusHandle.renderIntent = this.vertexRenderIntent;
        this.layer.addFeatures([this.radiusHandle], {silent: true});
    },
    CLASS_NAME: "OpenLayers.Control.StreetviewModifyFeature"
});