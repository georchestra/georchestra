require('components/area/area.tpl')

class AreaController {

  static $inject = [ '$injector', '$http' ]

  constructor ($injector, $http) {
    this.$injector = $injector

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('area.updated', this.i18n)
    translate('area.error', this.i18n)
  }

  $onInit () {
    const $http = this.$injector.get('$http')
    const CONFIG_URI = this.$injector.get('LDAP_PUBLIC_URI') + 'orgs/areaConfig.json'
    let promises = [ $http.get(CONFIG_URI).then(r => r.data) ]
    if (this.item.$promise) {
      promises.push(this.item.$promise)
    }
    this.$injector.get('$q').all(promises).then(this.initialize.bind(this))
  }

  initialize (resps) {
    const [config] = resps
    this.ids = this.item.cities || []
    this.groups = []

    const buildStyle = (fillColor, strokeColor, width) => new ol.style.Style({
      fill: new ol.style.Fill({ color: fillColor }),
      stroke: new ol.style.Stroke({ color: strokeColor, width: width || 1 })
    })

    const vector = new ol.layer.Vector({
      source: new ol.source.Vector(),
      style: buildStyle([ 255, 255, 255, 0.1 ], [ 0, 0, 0, 0.2 ])
    })

    const map = new ol.Map({
      target: document.querySelector('.map'),
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM({ attributions: null })
        }),
        vector
      ],
      view: new ol.View({
        center: ol.proj.fromLonLat(config.map.center),
        zoom: config.map.zoom
      }),
      logo: false
    })
    this.map = map

    const select = new ol.interaction.Select({
      multi: true,
      style: buildStyle([0, 159, 227, 0.2], [0, 159, 227, 1], 1.5),
      toggleCondition: ol.events.condition.always
    })
    this.collection = select.getFeatures()

    const format = new ol.format.GeoJSON()

    const highlightStyle = buildStyle([255, 0, 0, 0.5], [255, 0, 0, 0.2])
    const highlight = (feature) => {
      feature.setStyle(highlightStyle)
      setTimeout(() => feature.setStyle(), 250)
    }

    this.loading = true
    this.$injector.get('$http').get(config.areas.url).then(
      response => response.data
    ).then(json => {
      const conf = {
        dataProjection: 'EPSG:4326',
        featureProjection: map.getView().getProjection()
      }
      let selected = []
      vector.getSource().addFeatures(format.readFeatures(json, conf))
      vector.getSource().getFeatures().forEach(f => {
        let group = f.get(config.areas.group)
        let key = f.get(config.areas.key).toString()
        if (this.groups.indexOf(group) < 0) {
          this.groups.push(group)
        }
        f.set('_label', f.get(config.areas.value).toString() || '')
        f.set('_group', group)
        f.setId(key)
        if (this.ids.indexOf(f.getId()) >= 0) selected.push(f)
      })
      updateSelection(selected)
      this.loading = false
    })

    map.getInteractions().push(select)
    select.on('select', (e) => {
      updateSelection([], true)
      e.selected.map(highlight)
    })

    const dragBox = new ol.interaction.DragBox({
      condition: ol.events.condition.always
    })
    map.getInteractions().push(dragBox)
    dragBox.setActive(this.draw = false)
    dragBox.on('boxend', () => {
      let selected = []
      vector.getSource().forEachFeatureIntersectingExtent(
        dragBox.getGeometry().getExtent(),
        (feature) => { selected.push(feature) }
      )
      updateSelection(selected, true)
      dragBox.setActive(this.draw = false)
      select.setActive(true)
    })

    const buildRE = (search) => {
      search = search.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&') // eslint-disable-line
      return new RegExp('(' + search.split(' ').join('|') + ')', 'gi')
    }

    new autoComplete({ // eslint-disable-line
      selector: document.querySelector('.search'),
      minChars: 3,
      source: (term, response) => {
        let matches = []
        let re = buildRE(term)
        vector.getSource().getFeatures().forEach(f => {
          if (f.get('_label').match(re)) { matches.push(f) }
        })
        response(matches)
      },
      renderItem: (f, search) =>
        '<div class="autocomplete-suggestion"' +
        'data-val="' + f.get('_label') + '"' +
        'data-id="' + f.getId() + '">' +
        '<span>' +
        ((select.getFeatures().getArray().indexOf(f) >= 0) ? '✓' : '') +
        '</span>' +
        f.get('_label').replace(buildRE(search), '<b>$1</b>') +
        '</div>',
      onSelect: (e, term, item) => {
        let f = vector.getSource().getFeatureById(item.getAttribute('data-id'))
        if (select.getFeatures().getArray().indexOf(f) >= 0) {
          select.getFeatures().remove(f)
          updateSelection([], true)
        } else {
          updateSelection([f], true)
        }
        f.setStyle(buildStyle([255, 0, 0, 0.5], [255, 0, 0, 0.2]))
        setTimeout(() => f.setStyle(), 350)
      }
    })

    const updateSelection = (features, cumulative = false) => {
      this.$injector.get('$timeout')(() => {
        if (!cumulative) this.collection.clear()
        const uniques = this.collection.getArray().concat(features).filter(
          (item, index, self) => index === self.indexOf(item)
        )
        this.collection.clear()
        this.collection.extend(uniques)
        this.ids = uniques.map(f => f.getId())
        features.map(highlight)
        this.collection.getArray().sort(
          (a, b) => a.get('_label').localeCompare(b.get('_label'))
        )
      })
    }

    this.selectBBOX = () => {
      select.setActive(false)
      dragBox.setActive(this.draw = true)
    }

    this.selectBy = () => {
      if (this.group === 'all') {
        updateSelection(vector.getSource().getFeatures())
        return
      }
      if (this.group === 'none') {
        updateSelection([])
        this.group = ''
        return
      }

      let selected = vector.getSource().getFeatures().filter(
        f => f.get('_group') === this.group
      )
      updateSelection(selected)
    }
  }

  save () {
    let flash = this.$injector.get('Flash')
    let $httpDefaultCache = this.$injector.get('$cacheFactory').get('$http')

    this.item.cities = this.ids
    this.item.$update(() => {
      $httpDefaultCache.removeAll()
      flash.create('success', this.i18n.updated)
    }, flash.create.bind(flash, 'danger', this.i18n.error))
  }

}

angular.module('admin_console')
.component('areas', {
  bindings: {
    item: '=',
    callback: '='
  },
  controller: AreaController,
  controllerAs: 'area',
  templateUrl: 'components/area/area.tpl.html'
})
