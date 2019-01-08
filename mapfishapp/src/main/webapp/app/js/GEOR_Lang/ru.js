/*
 * Copyright (C) 2009-2016 by the geOrchestra PSC
 *
 * This file is part of geOrchestra.
 *
 * geOrchestra is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * geOrchestra is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * @requires GeoExt/Lang.js
 */

/*
 * Russian translation file courtesy of Liudmila Tesson
 */
OpenLayers.Lang.ru = OpenLayers.Util.extend(OpenLayers.Lang.ru, {
    /* General purpose strings */
    "Yes": "Да",
    "No": "Нет",
    "OK": "OK",
    "or": "или",
    "Cancel": "Отменить ou Аннулировать",
    "Save": "Сохранить",
    "Loading...": "Идёт загрузка...",
    "File": "Файл",
    "Layer": "Слой",
    //"layers": "Слой",
    "Description": "Описание",
    "Error": "EОшибка",
    "Server": "Сервер",
    "Close": "Закрыть",
    "labelSeparator": " : ",
    "File submission failed or invalid file": "Ошибка отправки файла – возможно, что файл недействителен",
    "Type": "Тип",
    "Title": "Заголовок",
    "Actions": "Действия",
    //"Reload": "Recharger",
    // TODO "Incorrect server response.": "Incorrect server response.",
    // TODO "No features found.": "No features found.",
    /* GEOR.js strings */
    "Cities": "Населённые пункты",
    "Recentering on GeoNames cities": "Переориентация на населённые пункты<br /> GeoNames.",
    "Referentials": "Ссылки",
    "Recentering on a selection of referential layers": "Повторное центрирование на выборе<br /> cлоёв-ссылок",
    "Addresses": "Адреса",
    "Recentering on a given address": "Повторное центрирование на точку адреса",
    "Available layers": "Доступные cлои",
    "Editing": "Издание",
    "resultspanel.emptytext": "<p> Выберите инструмент запроса или создайте запрос на слой. <br />Атрибуты объектов появятся в этой рамке. </p>",
    /* GEOR_ClassificationPanel.js strings */
    "Attribute": "Атрибут",
    "Number of classes": "Число классов",
    "Minimum size": "Минимальный размер",
    "Maximum size": "Максимальный размер",
    "First color": "Первый цвет",
    "Last color": "Последний цвет",
    "Palette": "Палитра",
    "Auto classification": "Автоматическая классификация",
    "Classify": "Классифицировать",
    "Unique values": "Уникальные значения",
    "Color range": "Ряд цветов",
    "Proportional symbols": "Пропорциональные символы",
    /* GEOR_FeatureDataModel.js strings */
    "objects": "объекты",
    /* GEOR_address.js strings */
    "Go to: ": "Перейти в...",
    "searching...": "Идёт поиск...",
    "adressSearchExemple": "пример поиска адреса : 4, Hugo, Brest",
    /* GEOR_ajaxglobal.js strings strings */
    "Server did not respond.": "Сервер не дал ответа",
    "Server access denied.": "Cервер отказался отвечать",
    "ajax.badresponse": "Cервис дал ответ, но содержание не соответствует ожидаемому",
    "Server unavailable.": "Сервер временно недоступен. Пожалуйста, повторите операцию позднее.",
    "Too much data.": "Данные слишком объёмны.",
    "Server exception.": "Исключение сервера ou Сервер отослал исключение.",
    "ajax.defaultexception": "Для более подробной информации вы можете отыскать код возврата <a href=\"http://en.wikipedia.org/wiki/List_of_HTTP_status_codes\" target=\"_blank\">на этой странице</a>.",
    "An error occured.<br />": "Произошла ошибка.<br />",
    "Warning : browser may freeze": "Внимание : возможен риск блокировки навигатора.",
    "ajaxglobal.data.too.big": "Данные сервера слишком объёмны.<br />Сервер выслал ${SENT}KO (лимит на ${LIMIT}KO)<br /> Вы по-прежнему хотите продолжить ?",
    /* GEOR_config.js strings */
    /* GEOR_cswbrowser.js strings */
    "NAME layer": "Cлой с именем... ou слой${NAME}",
    "Metadata without a name": "Метаданные без имени",
    "The getDomain CSW query failed": "Запрос на получение метаданных Корпоративного Программного Обеспечения (CSW) не получил одобрения",
    "Error for the thesaurus": "Ошибка относительно тезауруса",
    "Missing key to access the thesaurus": "Отсутствует ключ к тезаурусу",
    "Keywords query failed": "Запрос на ключевые слова отклонён",
    "Thesaurus:": "Тезаурус",
    "cswbrowser.default.thesaurus.mismatch": "Администратор : проблема конфигурации переменная DEFAULT_THESAURUS_KEY не соответствует никакому из значений, отосланных GeoNetwork",
    /* GEOR_cswquerier.js strings */
    "cswquerier.help.title": "Hints for advanced query",
    "cswquerier.help.message": '<ul><li><b>@word</b> looks for "word" in the organization name.</li><li><b>#word</b> looks for "word" in the metadata keywords.</li><li><b>?word</b> broadens the search by looking for "word" in any metadata field.</li></ul>',
    //"Show metadata essentials in a window":
        //"Afficher les métadonnées basiques",
    //"Show metadata sheet in a new browser tab": 
        //"Afficher la métadonnée complète dans un nouvel onglet",
    "more": "более, ещё...",
    "Click to select or deselect the layer": "Нажать, чтобы выбрать или отменить выделение слоя.",
    "Open the URL url in a new window": "Открыть URL в новом окне.",
    "Unreachable server": "Сервер недоступен",
    "Catalogue": "Каталог",
    "Find": "Найти",
    "in": "B",
    /* To be translated ...
    "No linked layer.": "Aucune couche.",
    "One layer found.": "Une couche trouvée.",
    "NB layers found.": "${NB} couches trouvées.",
    "NB metadata match the query.": "${NB} métadonnées correspondent à la requête.",
    "A single metadata matches the query.": "Une unique métadonnée correspond à la requête.",
    "Precise your request.": "Précisez votre requête.",
    "No metadata matches the query.":
        "Aucune métadonnée ne correspond à la requête.",
    "Search limited to current map extent.": "Recherche limitée à l'étendue de la carte.",
    */    
    /* GEOR_fileupload.js strings */
    "2D only": "2D",
    "Local file": "Локальный файл",
    "The service is inactive": "Сервис не работает",
    "Upload a vector data file.": "Загрузите файл с векторными данными.",
    "The allowed formats are the following: ": "Допустимы следующие форматы :",
    "Use ZIP compression for multifiles formats, such as": "Используйте ZIP сжатие для многофайловых форматов, таких как",
    /* TODO "fileupload_error_incompleteMIF": "Incomplete MIF/MID file.",
    "fileupload_error_incompleteSHP": "Incomplete shapefile.",
    "fileupload_error_incompleteTAB": "Incomplete TAB file.",
    "fileupload_error_ioError": "Server-side I/O exception. Contact platform administrator for more details.",
    "fileupload_error_multipleFiles": "Multiple data files encountered in ZIP archive. It should only contain one.",
    "fileupload_error_outOfMemory": "Server is out of memory. Contact platform administrator for more details.",
    "fileupload_error_sizeError": "This file is too large to be uploaded.",
    "fileupload_error_unsupportedFormat": "This format is not supported.",*/
    // TODO "server upload error: ERROR": "Upload failed. ${ERROR}",
    /* GEOR_geonames.js strings */
    /* GEOR_getfeatureinfo.js strings */
    "<div>Searching...</div>": "<div>Идёт поиск...</div>",
    "<div>Search on objects active for NAME layer. Click on the map.</div>": "<div>Поиск объектов запущен на слое ${NAME}. Щёлкните мышкой на карте.</div>",
    /* GEOR_layerfinder.js strings */
    //"metadata": "métadonnée",
    "Add layers from local files": "Добавить слои из локальных файлов.",
    "Find layers searching in metadata": "Найти слои, проводя поиск в базе метаданных.",
    "Find layers from keywords": "Отыщите слои посредством ключевых слов.",
    "Find layers querying OGC services": "Отыщите слои, опрашивая серверы OGC",
    "layerfinder.layer.unavailable": "Cлой ${NAME} не был найден на сервисе WMS.<br/<br/>Возможно, вы не обладаете правом доступа к нему или же, этот слой отсутствует",
    "Layer projection is not compatible": "Проекция слоя не совместима. ou Отображение слоя не совместимо",
    "The NAME layer does not contain a valid geometry column": "Слой$ ${NAME} не обладает действительным геометрическим столбцом",
    "Add": "Добавить",
    "Add layers from a ...": "Добавить слои из...",
    "Malformed URL": "URL не соответствует требованиям.",
    "Queryable": "Cпособный дать ответ на вопрос",
    "Opaque": "Hепрозрачный",
    "OGC server": "OGC сервер",
    //"I'm looking for ...": "Je recherche ...",
    //"Service type": "Type de service",
    "Choose a server": "Выберите сервер",
    "... or enter its address": "...или введите его адрес",
    "The server is publishing one layer with an incompatible projection": "Сервер публикует один слой с проекцией, которая не совместима",
    "The server is publishing NB layers with an incompatible projection": "Сервер публикует ${NB} число слоёв с несовместимой проекцией.",
    //"This server does not support HTTP POST": "Ce serveur ne supporte pas HTTP POST",
    "Unreachable server or insufficient rights": "Ответ сервера недействителен. Возможные причины : недостаточные права, сервер недостижим, слишком много данных, и т.д...",
    /* GEOR_managelayers.js strings */
    //"layergroup": "couche composite",
    //"Service": "Service",
    //"Protocol": "Protocole",
    //"About this layer": "A propos de cette couche",
    //"Set as overlay": "Passer en calque",
    //"Set as baselayer": "Passer en couche de fond",
    "Confirm NAME layer deletion ?": "Вы действительно хотите удалить слой${NAME} ?",
    "1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} à 1:${MINSCALE}",
    "Visibility range (indicative):<br />from TEXT": "Индикаторный диапазон видимости ${TEXT}",
    "Information on objects of this layer": "Справиться у объектов этого слоя",
    "default style": "Стиль по умолчанию",
    //"no styling": "Стиль по умолчанию", // TODO: translate
    "Recenter on the layer": "Повторное центрирование на слое",
    "Impossible to get layer extent": "Невозможно получить размер слоя.",
    "Refresh layer": "Обновить слой.",
    "Show metadata": "Показать метаданные",
    "Edit symbology": "Редактировать символику",
    "Build a query": "Сделать запрос",
    "Download data": "Скачать данные",
    "Choose a style": "Выбрать стиль",
    "Modify format": "Изменить формат",
    "Tiled mode" : "Режим черепицы",
    "Delete this layer": "Удалить этот слой",
    "Push up this layer": "Переместить этот слой вверх",
    "Push down this layer": "Опустить этот слой",
    "Add layers": "Добавить слои",
    //"Remove all layers": "Supprimer toutes les couches", // TODO: translate
    //"Are you sure you want to remove all layers ?": "Voulez vous réellement supprimer toutes les couches ?", // TODO: translate
    "source: ": "источник : ",
    "unknown": "неизвестный",
    //"Draw new point": "Dessiner un nouveau point",
    //"Draw new line": "Dessiner une nouvelle ligne",
    //"Draw new polygon": "Dessiner un nouveau polygone",
    //"Edition": "Edition",
    "Editing": "Издание",
    /*
    "Edition": "Edition",
    "Switch on/off edit mode for this layer": "Basculer cette couche en mode édition",
    "No geometry column.": "Colonne géométrique non détectée.",
    "Geometry column type (TYPE) is unsupported.": "Геометрия этого слоя следующего типа : ${TYPE}.",
    "Switching to attributes-only edition.": "Seuls les attributs des objets existants seront éditables.",
    */
    /* GEOR_map.js strings */
    "Location map": "арта локализации",
    "Warning after loading layer": "Уведомление, следующее за загрузкой слоя",
    "The <b>NAME</b> layer could not appear for this reason: ": "Возможное отсутствие слоя<b>${NAME}</b>по следующей причине: ",
    "Min/max visibility scales are invalid": "Минмальный/Максимальный масштабы видимости являются недействительными.",
    "Visibility range does not match map scales": "видимости не соответствует масштабам карты.",
    "Geografic extent does not match map extent": "Географический размер не соответствует размерам карты.",
    /* GEOR_mapinit.js strings */
    "Add layers from WMS services": "Добавить слои из сервисов WMS",
    "Add layers from WFS services": "Добавить слои из сервисов WFS",
    "NB layers not imported": "${NB} слоёв не импортированы",
    "One layer not imported": "Не импортирован один слой.",
    "mapinit.layers.load.error":  "Слои с именем ${LIST} не смогли загрузиться. Возможные причины : недостаточные права, несоответствие с SRS (Spatial Reference System - Система Космической Связи) или несуществующий слой.",
    "NB layers imported": "Закачано ${NB} слоёв",
    "One layer imported": "Один слой закачан",
    "No layer imported": "Не закачано ни одного слоя.",
    "The provided context is not valid": "Предложенный контекст недействителен",
    "The default context is not defined (and it is a BIG problem!)": "Контекст по умолчанию не определён. Это представляет собой серьёзную проблему!",
    //"Error while loading file": "Erreur au chargement du fichier",
    /* GEOR_mappanel.js strings */
    "Coordinates in ": "Координаты в ",
    "scale picker": "масштаб",
    /* GEOR_ows.js strings */
    "The NAME layer was not found in WMS service.": "Слой ${NAME} не был найден в сервисе WMS (Web Map Service : Cервис Веб-Карт).",
    //"Problem restoring a context saved with buggy Chrome 36 or 37":
    //    "Nous ne pouvons restaurer un contexte enregistré avec Chrome 36 ou 37", // TODO: translate
    /* GEOR_print.js strings */
    "Sources: ": "Источники",
    "Source: ": "Источник",
    "Projection: PROJ": "Проекция : ${PROJ}",
    "Print error": "Ошибка печати",
    "Print server returned an error": "Сервер печати сообщил об ошибке.",
    "Contact platform administrator": "Свяжитесь с администратором платформы.",
    "Layer unavailable for printing": "Слой для печати отсутствует.",
    "The NAME layer cannot be printed.": "Слой ${NAME} пока не может быть распечатан.",
    "Unable to print": "Печать недоступна.",
    "The print server is currently unreachable": "Сервер печати временно недоступен.",
    "print.unknown.layout": "Ошибка конфигурации : DEFAULT_PRINT_LAYOUT ${LAYOUT} отсутствует в списке печатных форматов",
    "print.unknown.resolution": "Ошибка конфигурации : DEFAULT_PRINT_RESOLUTION ${RESOLUTION} не находится в списке резолюций печати.",
    /*
    "print.unknown.format":
        "Erreur de configuration: le format " +
        "${FORMAT} n'est pas supporté par le serveur d'impression",
    "Pick an output format": "Choisissez un format de sortie",
    */
    "Comments": "Комментарии",
    "Scale: ": "Масштаб",
    "Date: ": "Дата : ",
    "Minimap": "Миникарта",
    "North": "Север",
    "Scale": "Масштаб",
    "Date": "Дата",
    "Legend": "Легенда",
    "Format": "Формат",
    "Resolution": "Резолюция",
    "Print the map": "Напечатание карты",
    "Print": "Напечатать",
    "Printing...": "Идёт печать...",
    "Print current map": "Напечатать текущую карту",
    /* GEOR_Querier.js strings */
    "Fields of filters with a red mark are mandatory": "Поля фильтров, помеченные красным цветом, являются обязательными для заполнения.",
    "Request on NAME": "Сделать запрос о ${NAME}",
    "Search": "Искать",
    "querier.layer.no.geom": "Слой не имеет геометрического столбца.<br />Геометрический запросчик не будет работать.",
    "querier.layer.error": "получить характеристику запрошенного слоя.<br />Запросчик не будет доступен.",
    /* GEOR_referentials.js strings */
    "Referential": "Ссылка",
    "There is no geometry column in the selected referential": "В выделенной ссылке отсутствует геометрический столбец.",
    "Choose a referential": "Выбрать ссылку",
    /* GEOR_resultspanel.js strings */
    //"Symbology": "Symbologie",
    //"Edit this panel's features symbology": "Editer la symbologie de la sélection",
    //"Reset": "Réinitialiser",
    //"Export is not possible: features have no geometry": "Export impossible : absence de géométries",
    "resultspanel.maxfeature.reached": " <span ext:qtip=\"Используйте более совершенный навигатор для того, чтобы увеличить число фигурирующих на странице объектов.\">Максимальное число объектов достигнуто ${NB}</span>", 
    "NB results": "${NB} результатов",
    "One result": "Один результат",
    "No result": "Никакого результата нет",
    "Clean": "Стереть",
    //"All": "Tous", // TODO: translate
    //"None": "Aucun",
    //"Invert selection": "Inverser la sélection",
    //"Actions on the selection or on all results if no row is selected":
    //    "Actions sur la sélection ou sur tous les résultats si aucun n'est sélectionné",
    //"Store the geometry": 
    //    "Enregistrer la géométrie",
    //"Aggregates the geometries of the selected features and stores it in your browser for later use in the querier": 
    //    "La géométrie des objets sélectionnés est enregistrée pour un usage ultérieur dans le requêteur",
    //"Geometry successfully stored in this browser": 
    //    "Géométrie enregistrée avec succès sur ce navigateur",
    "Clean all results on the map and in the table": "Удалить все результаты с карты и таблицы.",
    "Zoom": "Zoom",
    "Zoom to results extent": "Применить величину зума карты на результаты.",
    "Export": "Экспорт",
    "Export results as": "Выслать все результаты в текстовом формате",
    "<p>No result for this request.</p>": "<p>Никакой объект не соответствует вашему запросу.</p>",
    /* GEOR_scalecombo.js strings */
    /* GEOR_styler.js strings */
    "Download style": "Скачать стиль",
    "You can download your SLD style at ": "Вы можете скачать ваш стиль в формате SLD по следующему адресу ",
    "Thanks!": "Спасибо!",
    "Saving SLD": "Cохранение файла SLD",
    "Some classes are invalid, verify that all fields are correct": "Некоторые классы недействительны, необходимо проверить все ли поля правильны.",
    "Get SLD": "Получить SLD",
    "Malformed SLD": "SLD не соответствует требованию.",
    "circle": "круг",
    "square": "квадрат",
    "triangle": "треугольник",
    "star": "звезда",
    "cross": "крест",
    "x": "x",
    "customized...": "Индивидуальный",
    "Classification ...<br/>(this operation can take some time)": "Классифицирование ou Сортировка...<br/>(эта операция может занять некоторое время)",
    "Class": "Класс",
    "Untitled": "Без заголовка",
    "styler.guidelines": "Использовать клавишу\"+\" для создания класса и \"Analyse\" для того, чтобы создать совокупность классов, определённых тематическим анализом.</p>",
    "Analyze": "Анализ",
    "Add a class": "Добавить класс",
    "Delete the selected class": "Удалить выделенный класс",
    "Styler": "Стайлер",
    "Apply": "Применить",
    "Impossible to complete the operation:": "Невозможно завершить операцию ou Операция невозможна",
    "no available attribute": "нет ни одного доступного атрибута.",
    /* GEOR_toolbar.js strings */
    "m": "м",
    "hectares": "гектары",
    "zoom to global extent of the map": "\"zoom\" на глобальный масштаб карты",
    "pan": "перетащить карту ou переместить карту",
    "zoom in": "\"zoom\" вперёд (для того, чтобы зуммировать в определённых рамках : нажать на SHIFT + нарисовать ограничение",
    "zoom out": "\"zoom\" назад",
    "back to previous zoom": "вернуться к предыдушему ограничению зума",
    "go to next zoom": "перейти к следующему виду зуммирования",
    "Login": "Соединение",
    "Logout": "Отключение",
    "Help": "Помощь",
    "Show legend": "Показать легенду",
    "Leave this page ? You will lose the current cartographic context.": "Выйти со страницы? В таком случае, вы потеряете текущий картографический контекст.",
    /*
    "Online help": "Aide en ligne",
    "Display the user guide": "Afficher le guide de l'utilisateur",
    "Contextual help": "Aide contextuelle",
    "Activate or deactivate contextual help bubbles": "Activer ou désactiver les bulles d'aide contextuelle",
    */
    /* GEOR_tools.js strings */
    "Tools": "Инструменты",
    "tool": "Инструмент",
    "No tool": "Никакого инструмента",
    "Manage tools": "Управлять инструментами",
    "remember the selection": "Запомнить выбор ou Запомнить выделенное",
    "Available tools:": "Инструменты в распоряжении",
    "Click to select or deselect the tool": "Щёлкните для того, чтобы выбрать инструмент или отменить его",
    "Could not load addon ADDONNAME": "Невозможно загрузить аддон ${ADDONNAME}",
    //"Your new tools are now available in the tools menu.": 'Vos nouveaux outils sont disponibles dans le menu "outils"',
    /* GEOR_util.js strings */
    "Characters": "Письменность",
    "Digital": "Цифровой",
    "Boolean": "Логический",
    "Other": "Другой",
    "Confirmation": "Подтверждение",
    "Information": "Информация",
    //"Could not parse metadata.": "Impossible d'analyser la métadonnée",
    //"Could not get metadata.": "Impossible d'obtenir la métadonnée",
    //"pointOfContact": "contact",
    //"custodian": "producteur",
    //"distributor": "distributeur",
    //"originator": "instigateur",
    //"More": "Plus",
    /* GEOR_waiter.js strings */
    /* GEOR_wmc.js strings */
    "The provided file is not a valid OGC context": "Данный файл не является действительным контекстом OGC",
    "Warning: trying to restore WMC with a different projection (PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !": "Предупреждение : попытка восстановления WMC в одной проекции ${PROJCODE1}, тогда как данная карта SRS находится в другой ${PROJCODE2}. Здесь возможны непредвиденные обстоятельства",
/* GEOR_wmcbrowser.js strings */
    //"all contexts": "tous les contextes",
    "Could not find WMC file": "Невозможно найти WMC файл",
    //"... or a local context": "...или индивидуальный контекст", // TODO: update translation
    //"Load or add the layers from one of these map contexts:" : "Заменить составляющую текущей карты одним из предложенных контекстов:",
    "A unique OSM layer": "Единый слой OpenStreetMap",
    "default viewer context": "контекст вьюера по умолчанию",
    /* GEOR_workspace.js strings */
    //"Created:": "Date de création : ",
    //"Last accessed:": "Date de dernier accès : ",
    //"Access count:": "Nombre d'accès : ",
    //"Permalink:": "Permalien : ",
    //"My contexts": "Mes contextes",
    //"Created": "Création",
    //"Accessed": "Accédé",
    //"Count": "Accès",
    //"View": "Visualiser",
    //"View the selected context": "Visualiser le contexte sélectionné (attention: remplacera le contexte courant)",
    //"Download": "Télécharger",
    //"Download the selected context": "Télécharger le contexte sélectionné",
    //"Delete": "Supprimer",
    //"Delete the selected context": "Supprimer le contexte sélectionné",
    //"Failed to delete context": "Impossible de supprimer le contexte",
    //"Manage my contexts": "Gérer mes contextes",
    //"Keywords": "Mots clés",
    //"comma separated keywords": "mots clés séparés par une virgule",
    //"Save to metadata": "Créer une métadonnée",
    //"in group": "dans le groupe",
    //"The context title is mandatory": "Le titre du contexte est obligatoire",
    //"There was an error creating the metadata.": "La création de la métadonnée a échoué.",
    //"Share this map": "Partager cette carte",
    //"Mobile viewer": "Visualiseur mobile",
    //"Mobile compatible viewer on sdi.georchestra.org": "Visualiseur mobile sur sdi.georchestra.org",
    //"Desktop viewer": "Visualiseur web",
    //"Desktop viewer on sdi.georchestra.org": "Visualiseur web sur sdi.georchestra.org",
    //"Abstract": "Résumé",
    "Context saving": "Сохранение контекста",
    "The file is required.": "Требуется имя файла",
    "Context restoring": "Восстановление контекста",
    "<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>Обратите внимание на то, что файл в формате WMC должен быть закодирован UTF-8.</p>",
    "Load": "Загрузить",
    "Workspace": "Рабочее пространство",
    "Save the map context": "Сохранить карту",
    "Load a map context": "Загрузить карту",
    "Get a permalink": "Получить постоянную ссылку",
    "Permalink": "Постоянная ссылка",
    "Share your map with this URL: ": "Доступ к вашей карте с этого URL-адреса : ",
    "Edit in OSM": "Редактировать в OSM (OpenStreetMap)",
    "with JOSM": "c JOSM",
    "JOSM must be started with the remote control option": "Необходимо запустить JOSM с опцией дистанционного контроля",
    "with iD": "c редактором iD",
    "Recommended scale is 1:10.000": "Рекомендуемый масштаб - 1:10.000",
    "with Potlatch2": "c редактором Potlatch2",
    "with Walking Papers": "при помощи Walking Papers",
    /* GEOR_edit.js */
    //"Req.": "Req.", // requis
    //"Required": "Requis",
    //"Not required": "Non requis",
    //"Edit activated": "Edition activée", 
    //"Hover the feature you wish to edit, or choose \"new feature\" in the edit menu": "Survolez les objets de la couche que vous souhaitez modifier, ou choisissez \"nouvel objet\" dans le menu d'édition de la couche",
    "Synchronization failed.": "Произошла ошибка во время процесса синхронизации.",
    /* GeoExt.data.CSW.js */
    "no abstract": "нет резюме"
    // no trailing comma
});

GeoExt.Lang.add("ru", {
    "GeoExt.ux.FeatureEditorGrid.prototype": {
        deleteMsgTitle: "Delete Feature?",
        deleteMsg: "Are you sure you want to delete this feature?",
        deleteButtonText: "Delete",
        deleteButtonTooltip: "Delete this feature",
        cancelMsgTitle: "Cancel Editing?",
        cancelMsg: "There are unsaved changes. Are you sure you want to cancel?",
        cancelButtonText: "Cancel",
        cancelButtonTooltip: "Stop editing, discard changes",
        saveButtonText: "Save",
        saveButtonTooltip: "Save changes",
        nameHeader: "Name",
        valueHeader: "Value"
    }
});
