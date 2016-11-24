require('components/area/area.tpl')

class AreaController {

  static $inject = [ '$injector' ]

  constructor ($injector) {
    this.$injector = $injector
    if (this.item.$promise) {
      this.item.$promise.then(this.initialize.bind(this))
    } else {
      this.initialize()
    }

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('area.updated', this.i18n)
    translate('area.error', this.i18n)
  }

  initialize () {
    this.ids = this.item.cities || []
    const PUBLIC_URI = this.$injector.get('LDAP_ROOT_URI') + 'console/public/'

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
          source: new ol.source.OSM({
            attributions: null
          })
        }),
        vector
      ],
      view: new ol.View({
        center: [ 304000, 6440000 ],
        zoom: 8
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
    window.fetch(PUBLIC_URI + 'hdf.json', {
      credentials: 'include'
    }).then(
        response => response.json()
    ).then(json => {
      const conf = {
        dataProjection: 'EPSG:4326',
        featureProjection: map.getView().getProjection()
      }
      let selected = []
      vector.getSource().addFeatures(format.readFeatures(json, conf))
      vector.getSource().getFeatures().forEach(f => {
        let insee = f.get('insee')
        f.setId(insee)
        if (this.ids.indexOf(insee) >= 0) selected.push(f)
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
      search = search.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
      return new RegExp('(' + search.split(' ').join('|') + ')', 'gi')
    }

    new autoComplete({ // eslint-disable-line
      selector: document.querySelector('.search'),
      minChars: 3,
      source: (term, response) => {
        let matches = []
        let re = buildRE(term)
        vector.getSource().getFeatures().forEach(f => {
          if (f.get('nom').match(re)) { matches.push(f) }
        })
        response(matches)
      },
      renderItem: (f, search) =>
        '<div class="autocomplete-suggestion"' +
        'data-val="' + f.get('nom') + '"' +
        'data-id="' + f.getId() + '">' +
        '<span>' +
        ((select.getFeatures().getArray().indexOf(f) >= 0) ? '✓' : '') +
        '</span>' +
        f.get('nom').replace(buildRE(search), '<b>$1</b>') +
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
        const uniques = [ ...new Set(this.collection.getArray().concat(features)) ]
        this.collection.clear()
        this.collection.extend(uniques)
        this.ids = uniques.map(f => f.get('insee'))
        features.map(highlight)
        this.collection.getArray().sort(
          (a, b) => a.get('nom').localeCompare(b.get('nom'))
        )
      })
    }

    this.selectBBOX = () => {
      select.setActive(false)
      dragBox.setActive(this.draw = true)
    }

    this.selectBy = () => {
      if (this.dpt === 'all') {
        updateSelection(vector.getSource().getFeatures())
        return
      }
      if (this.dpt === 'none') {
        updateSelection([])
        this.dpt = ''
        return
      }

      let selected = vector.getSource().getFeatures().filter(
        f => f.get('insee').substr(0, 2) === this.dpt
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
