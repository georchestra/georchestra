    <script type="text/javascript" src="lib/externals/geoext/lib/overrides/override-ext-ajax.js"></script>
    <script type="text/javascript" src="lib/externals/ext/ext-all-debug.js"></script>
    <script type="text/javascript" src="lib/Ext.ux/lib/Ext.ux.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/proj4js.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/lcc.js"></script>
    <script type="text/javascript" src="lib/proj4js/lib/projCode/merc.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers.js"></script>
    <script type="text/javascript" src="lib/externals/openlayers/lib/OpenLayers/Lang/<%= lang %>.js"></script>
    <script type="text/javascript" src="lib/externals/geoext/lib/GeoExt.js"></script>
    <script type="text/javascript" src="lib/externals/sandbox/ux/OpenAddressesSearchCombo/lib/GeoExt.ux/OpenAddressesSearchCombo.js"></script>
    <script type="text/javascript" src="lib/externals/ext/src/locale/ext-lang-<%= lang %>.js"></script>

    <script type="text/javascript" src="lib/addins/loadingPanel/trunk/lib/OpenLayers/Control/LoadingPanel.js"></script>

    <script type="text/javascript" src="lib/styler/lib/Styler/Util.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/SchemaManager.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/SLDManager.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/dispatch.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/StrokeSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FillSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/PointSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RulePanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RuleBuilder.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/MultiSlider.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/BaseFilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/SpatialFilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FilterPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/LineSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/RuleChooser.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/SpatialComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/ComparisonComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/form/FontComboBox.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/ScaleSlider.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/LegendPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FeatureRenderer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/PolygonSymbolizer.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/ScaleLimitPanel.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/FilterBuilder.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/MultiSliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/tips/SliderTip.js"></script>
    <script type="text/javascript" src="lib/styler/lib/Styler/widgets/TextSymbolizer.js"></script>

    <script type="text/javascript" src="app/js/GEOR_config.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Lang/<%= lang %>.js"></script>
            <c:choose>
                <c:when test='${c.edit}'>
    <script type="text/javascript" src="lib/Ext.ux/lib/Ext.ux/widgets/collapsedtitle/PanelCollapsedTitle.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Editing/GEOR_LayerEditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_Editing/GEOR_EditingPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_editing.js"></script>
                </c:when>
                <c:otherwise>
    <script type="text/javascript" src="app/js/GEOR_querier.js"></script>
    <script type="text/javascript" src="app/js/GEOR_styler.js"></script>
    <script type="text/javascript" src="app/js/GEOR_getfeatureinfo.js"></script>
    <script type="text/javascript" src="app/js/GEOR_resultspanel.js"></script>
                </c:otherwise>
            </c:choose>
    <script type="text/javascript" src="app/js/GEOR_util.js"></script>
    <script type="text/javascript" src="app/js/GEOR_FeatureDataModel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ClassificationPanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ows.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wmc.js"></script>
    <script type="text/javascript" src="app/js/GEOR_waiter.js"></script>
    <script type="text/javascript" src="app/js/GEOR_referentials.js"></script>
    <script type="text/javascript" src="app/js/GEOR_geonames.js"></script>
    <script type="text/javascript" src="app/js/GEOR_address.js"></script>
    <script type="text/javascript" src="app/js/GEOR_scalecombo.js"></script>
    <script type="text/javascript" src="app/js/GEOR_toolbar.js"></script>
    <script type="text/javascript" src="app/js/GEOR_workspace.js"></script>
    <script type="text/javascript" src="app/js/GEOR_mappanel.js"></script>
    <script type="text/javascript" src="app/js/GEOR_managelayers.js"></script>
    <script type="text/javascript" src="app/js/GEOR_layerfinder.js"></script>
    <script type="text/javascript" src="app/js/GEOR_cswbrowser.js"></script>
    <script type="text/javascript" src="app/js/GeoExt.data.CSW.js"></script>
    <script type="text/javascript" src="app/js/GEOR_cswquerier.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wmsbrowser.js"></script>
    <script type="text/javascript" src="app/js/GEOR_wfsbrowser.js"></script>
    <script type="text/javascript" src="app/js/GEOR_print.js"></script>
    <script type="text/javascript" src="app/js/GEOR_map.js"></script>
    <script type="text/javascript" src="app/js/GEOR_ajaxglobal.js"></script>
    <script type="text/javascript" src="app/js/GEOR_mapinit.js"></script>
    <script type="text/javascript" src="app/js/GEOR.js"></script>
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite-beta.js"></script>
