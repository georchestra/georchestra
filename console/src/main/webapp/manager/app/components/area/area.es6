require('components/area/area.tpl')

const buildStyle = (fillColor, strokeColor, width) => new ol.style.Style({
  fill: new ol.style.Fill({ color: fillColor }),
  stroke: new ol.style.Stroke({ color: strokeColor, width: width || 1 })
})
const highlightStyle = buildStyle([255, 0, 0, 0.5], [255, 0, 0, 0.2])
const highlight = feature => {
  feature.setStyle(highlightStyle)
  setTimeout(() => feature.setStyle(), 250)
  return feature
}

class AreaController {
  static $inject = [ '$injector', '$http', '$scope' ]

  constructor ($injector, $http, $scope) {
    this.$injector = $injector
    this.$scope = $scope

    let translate = $injector.get('translate')
    this.i18n = {}
    translate('area.updated', this.i18n)
    translate('area.error', this.i18n)
    this.canExport = window.Blob && window.FileReader
  }

  $onInit () {
    this.maponly = this.readonly === 'true'
    const $http = this.$injector.get('$http')
    const CONFIG_URI = this.$injector.get('CONSOLE_PUBLIC_PATH') + 'orgs/areaConfig.json'
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

    const vector = new ol.layer.Vector({
      source: new ol.source.Vector(),
      style: buildStyle([ 255, 255, 255, 0.1 ], [ 0, 0, 0, 0.2 ])
    })
    this.source = vector.getSource()

    const map = new ol.Map({
      target: document.querySelector('.map'),
      layers: [
        new ol.layer.Tile({
          source: new ol.source.OSM({ attributions: null })
        }),
        vector
      ],
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

    this.loading = true
    this.$injector.get('$http').get(config.areas.url).then(
      response => response.data
    ).then(json => {
      const conf = {
        dataProjection: 'EPSG:4326',
        featureProjection: map.getView().getProjection()
      }
      let selected = []
      json.features.forEach(f => {
        f.id = f.properties[config.areas.key].toString()
      })
      vector.getSource().addFeatures(format.readFeatures(json, conf))
      vector.getSource().getFeatures().forEach(f => {
        let group = f.get(config.areas.group)
        if (this.groups.indexOf(group) < 0) {
          this.groups.push(group)
        }
        f.set('_label', f.get(config.areas.value).toString() || '')
        f.set('_group', group)
        if (this.ids.indexOf(f.getId()) >= 0) selected.push(f)
      })

      this.map.set('config', config.map)
      if (!config.map) {
        this.map.getView().fit(vector.getSource().getExtent(), map.getSize())
      } else {
        this.map.getView().setCenter(ol.proj.fromLonLat(config.map.center))
        this.map.getView().setZoom(config.map.zoom)
      }

      if (selected.length > 0) {
        let extent = ol.extent.createEmpty()
        selected.forEach(f => ol.extent.extend(extent, f.getGeometry().getExtent()))
        this.map.getView().fit(extent, map.getSize())
      }

      this.updateSelection(selected)
      this.loading = false
    })

    map.getInteractions().push(select)
    if (this.maponly) {
      select.setActive(false)
    }

    select.on('select', (e) => {
      this.updateSelection([], true)
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
      this.updateSelection(selected, true)
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
          this.updateSelection([], true)
        } else {
          this.updateSelection([f], true)
        }
        f.setStyle(buildStyle([255, 0, 0, 0.5], [255, 0, 0, 0.2]))
        setTimeout(() => f.setStyle(), 350)
      }
    })

    this.selectBBOX = () => {
      select.setActive(false)
      dragBox.setActive(this.draw = true)
    }

    this.selectBy = () => {
      if (this.group === 'all') {
        this.updateSelection(vector.getSource().getFeatures())
        return
      }
      if (this.group === 'none') {
        this.updateSelection([])
        this.group = ''
        return
      }

      let selected = vector.getSource().getFeatures().filter(
        f => f.get('_group') === this.group
      )
      this.updateSelection(selected)
    }
  }

  updateSelection (features, cumulative = false) {
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

  removeFromSelection (feature) {
    highlight(feature)
    this.collection.remove(feature)
    this.ids = this.collection.getArray().map(f => f.getId())
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

  export () {
    const a = document.createElement('a')
    a.href = window.URL.createObjectURL(new Blob(
      [ this.ids.join('\n') ],
      { type: 'text/csv' }
    ))
    a.download = 'export.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  }

  import () {
    const fileInput = document.createElement('input')
    const reader = new window.FileReader()
    fileInput.type = 'file'
    fileInput.accept = 'text/csv'
    this.ids = []
    this.collection.clear()
    reader.onload = () => this.$scope.$apply(() => {
      reader.result.split('\n').forEach(line => {
        const [id] = line.split(',')
        let f = this.source.getFeatureById(id)
        if (!f) return
        this.collection.push(highlight(f))
        this.ids.push(id)
      })
    })
    fileInput.addEventListener(
      'change',
      () => reader.readAsBinaryString(fileInput.files[0]))
    fileInput.click()
  }
}

angular.module('manager').component('areas', {
  bindings: {
    readonly: '=',
    item: '=',
    callback: '='
  },
  controller: AreaController,
  controllerAs: 'area',
  templateUrl: 'components/area/area.tpl.html'
})
