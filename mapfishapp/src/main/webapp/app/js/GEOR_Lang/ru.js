/*
 * Copyright (C) Camptocamp
 *
 * This file is part of geOrchestra
 *
 * geOrchestra is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with geOrchestra.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * French translation file
 */
OpenLayers.Lang.fr = OpenLayers.Util.extend(OpenLayers.Lang.fr, {
    /* General purpose strings */
"Yes": "Oui", Да
"No": "Non", Нет
"OK": "OK", OK
"or": "ou", или
"Cancel": "Annuler", Отменить ou Аннулировать
"Save": "Sauvegarder", Сохранить
"Loading...": "Chargement...", Идёт загрузка...
"File": "Fichier", Файл
"Layer": "Couche", Слой
"Description": "Description", Описание
"Error": "Erreur", Ошибка
"Server": "Serveur", Сервер
"Close": "Fermer", Закрыть
"labelSeparator": " : ", Разделитель объекта Label
"File submission failed or invalid file": "L'envoi du fichier a
échoué - le fichier est peut-être non valide", Ошибка отправки файла –
возможно, что файл недействителен
/* GEOR.js strings */
"Cities": "Localités", Населённые пункты
"Recentering on GeoNames cities": "Recentrage sur localités<br />de
la base GeoNames", Переориентация на населённые пункты<br /> GeoNames.
ou Повторное центрирование по населённым пунктам <br />GeoNames
"Referentials": "Référentiels", Ссылки
"Recentering on a selection of referential layers": "Recentrage sur
une sélection<br />de couches \"référentiels\"", Повторное центрирование
на выборе<br /> cлоёв-ссылок\"
"Addresses": "Adresses", Адреса
"Recentering on a given address": "Recentrage sur point adresse",
Повторное центрирование на точку адреса
"Available layers": "Couches disponibles", Доступные cлои
"Editing": "Edition", Издание
"resultspanel.emptytext":
"<p>Sélectionnez l'outil d'interrogation " + "<p> Выберите инструмент
запроса" +
"ou construisez une requête sur une couche.<br />" + ''или создайте
запрос на слой. <br />" +
"Les attributs des objets s'afficheront dans ce cadre.</p>", ‘’Атрибуты
объектов появятся в этой рамке. </p> ‘’
/* GEOR_ClassificationPanel.js strings */
"Attribute": "Attribut", Атрибут
"Type": "Type", Тип
"Number of classes": "Nombre de classes", Число классов
"Minimum size": "Taille minimum", Минимальный размер
"Maximum size": "Taille maximum", Максимальный размер
"First color": "Première couleur", Первый цвет
"Last color": "Dernière couleur", Последний цвет
"Palette": "Palette", Палитра
"Auto classification": "Classification automatique", Автоматическая
классификация
"Classify": "Classifier", Классифицировать
"Unique values": "Valeurs uniques", Уникальные значения
"Color range": "Plages de couleurs", Ряд цветов
"Proportional symbols": "Symboles proportionnels", Пропорциональные символы
/* GEOR_FeatureDataModel.js strings */
"objects": "objets", объекты
/* GEOR_address.js strings */
"Go to: ": "Aller à : ", Перейти в...
"searching...": "recherche en cours...", Идёт поиск...
"adressSearchExemple": "ex: 4, Hugo, Brest", "пример поиска адреса : 4,
Hugo, Brest"
/* GEOR_ajaxglobal.js strings strings */
"Server did not respond.": "Le serveur n'a pas répondu.", Сервер не дал
ответа
"Server access denied.": "Le serveur a refusé de répondre.", Cервер
отказался отвечать
"ajax.badresponse":
"Le service a répondu, mais le contenu de la " + Cервис дал ответ, но
содержание
"réponse n'est pas conforme à celle attendue", не соответствует ожидаемому
"Server unavailable.": "Le serveur est temporairement indisponible.
Сервер временно недоступен.
Veuillez réessayer ultérieurement.", Пожалуйста, повторите операцию позднее.
"Too much data.": "Données trop volumineuses.", Данные слишком объёмны.
"Server exception.": "Le serveur a renvoyé une exception.", Исключение
сервера ou Сервер отослал исключение.
"ajax.defaultexception":
"Pour plus d'information, vous pouvez " + "Для более подробной
информации вы можете "+
"chercher le code de retour sur <a href=\"http://" + "отыскать код
возврата <a href=\"http://" +
"en.wikipedia.org/wiki/List_of_HTTP_status_codes\"
target=\"_blank\">" +
"cette page</a>.", "на этой странице</a>.",
"An error occured.<br />": "Une erreur est survenue.<br />", Произошла
ошибка.<br />",
"Warning : browser may freeze": "Attention : risque de blocage du
navigateur", Внимание : возможен риск блокировки навигатора.
"ajaxglobal.data.too.big": "Les données provenant du serveur sont
trop " + Данные сервера слишком’’+
"volumineuses.<br /> "объёмны.<br />
Le serveur a envoyé ${SENT}KO " + Сервер выслал ${SENT}KO " +
"(la limite est à ${LIMIT}KO)<br />Voulez-vous tout de même (лимит на
${LIMIT}KO)<br /> Вы по-прежнему хотите продолжить ?
continuer ?",
/* GEOR_config.js strings */
/* GEOR_cswbrowser.js strings */
"NAME layer": "Couche ${NAME}", Cлой с именем... ou слой${ИМЯ}",
"Metadata without a name": "Métadonnée non nommée", Метаданные без имени
"The getDomain CSW query failed": "La requête CSW getDomain a échoué",
Запрос на получение метаданных Корпоративного Программного Обеспечения
(CSW) не получил одобрения
"Error for the thesaurus": "Erreur sur le thésaurus", Ошибка
относительно тезауруса
"Missing key to access the thesaurus":
"Absence de clé pour accéder à ce thésaurus", Отсутствует ключ к тезаурусу
"Keywords query failed": "La requête des mots clés a échoué", Запрос на
ключевые слова отклонён
"Thesaurus:": "Thésaurus :", Тезаурус
"Thesaurus": "Thésaurus", Тезаурус
"cswbrowser.default.thesaurus.mismatch":
"Administrateur : problème de configuration - " + Администратор :
проблема конфигурации +
"la variable DEFAULT_THESAURUS_KEY ne correspond à aucune" + "переменная
DEFAULT_THESAURUS_KEY не соответствует никакому +
" valeur exportée par GeoNetwork", "из значений, отосланных GeoNetwork",
/* GEOR_cswquerier.js strings */
"Show metadata sheet in a new window":
"Afficher la fiche de métadonnées dans une nouvelle fenêtre", Показать
список метаданных в новом окне
"more": "plus", более, ещё...
"Clic to select or deselect the layer":
"Cliquez pour sélectionner ou désélectionner la couche", Нажать, чтобы
выбрать или отменить выделение слоя.
"Open the URL url in a new window":
"Ouvrir l'url ${URL} dans une nouvelle fenêtre", Открыть URL в новом окне.
"Unreachable server": "Serveur non disponible", Сервер недоступен
"Catalogue": "Catalogue", Каталог
"Find": "Chercher", Найти
"in": "dans", "B",
"Not any layer": "Aucune couche", Слой отсутствует
"1 layer": "1 couche", Один слой
"NB layers": "${NB} couches", ${NB}слоёв
" in NB metadata": " dans ${NB} metadonnées", в ${NB}метаданных
" in 1 metadata": " dans 1 metadonnée", в единице метаданных
": precise your request": " : précisez votre requête", уточните ваш запрос
"Not any metadata correspond to the words specified":
"Aucune métadonnée ne correspond aux termes saisis", Никакая из
метаданных не соответствует внесённым терминам.
/* GEOR_editing.js strings */
/* GEOR_fileupload.js strings */
"Local file": "Fichier", Локальный файл
"The service is inactive": "Le service est inactif", Сервис не работает
"Upload a vector data file.": "Uploadez un fichier de données
vectorielles.", Загрузите файл с векторными данными.
"The allowed formats are the following: ": "Les formats acceptés
sont les suivants : ", Допустимы следующие форматы :
"Use ZIP compression for multifiles formats, such as": "Utilisez la
compression ZIP pour les formats multi-fichiers comme", Используйте ZIP
сжатие для многофайловых форматов, таких как
/* GEOR_geonames.js strings */
/* GEOR_getfeatureinfo.js strings */
"<div>Searching...</div>": "<div>Recherche en cours...</div>", Идёт поиск...
"<div>Search on objects active for NAME layer. Clic on the map.</div>":
"<div>Recherche d\'objets activée sur la couche ${NAME}. " + <div>Поиск
объектов запущен на слое ${ИМЯ}.+
"Cliquez sur la carte.</div>", Щёлкните мышкой на карте.
/* GEOR_layerfinder.js strings */
"Add layers from local files": "Ajouter des couches en uploadant un
fichier depuis votre ordinateur", Добавить слои из локальных файлов.
"Find layers searching in metadata": Найти слои, проводя поиск в базе
метаданных.
"Trouvez des couches en cherchant dans les métadonnées",
"Find layers from keywords": "Trouvez des couches par mots clés",
Отыщите слои посредством ключевых слов.
"Find layers querying WMS servers":
"Trouvez des couches en interrogeant des serveurs WMS", Отыщите слои,
опрашивая серверы WMS (Web Map Service - Сервис Веб-Карт).
"Find layers querying WFS servers":
"Trouvez des couches en interrogeant des serveurs WFS", Отыщите слои,
опрашивая серверы WFS (Web Feature Service - Сервис Веб-Возможностей)
"layerfinder.layer.unavailable":
"La couche ${NAME} n'a pas été trouvée dans le service
WMS.<br/<br/>" + Cлой ${ИМЯ} не был найден на сервисе WMS.<br/<br/>" +
"Peut-être n'avez-vous pas le droit d'y accéder " + Возможно, вы не
обладаете правом доступа к нему +
"ou alors cette couche n'est plus disponible", или же, этот слой
отсутствует,
"Layer projection is not compatible":
"La projection de la couche n'est pas compatible.", Проекция слоя не
совместима. ou Отображение слоя не совместимо.
"The NAME layer does not contain a valid geometry column":
"La couche ${NAME} ne possède pas de colonne géométrique valide.",
Слой${ИМЯ} не обладает действительным геометрическим столбцом.
"Add": "Ajouter", Добавить
"Add layers from a ...": "Ajouter des couches depuis un ...", Добавить
слои из...
"Malformed URL": "URL non conforme.", URL не соответствует требованиям.
"Queryable": "Interrogeable", Cпособный дать ответ на вопрос
"Opaque": "Opaque", Hепрозрачный
"Choose a WMS server: ": "Choisissez un serveur WMS : ", "Выберите
сервер WMS"
"The server is publishing one layer with an incompatible projection":
"Le serveur publie une couche dont la projection n'est pas Сервер
публикует один слой с проекцией, которая не
compatible", совместима",
"The server is publishing NB layers with an incompatible projection":
"Le serveur publie ${NB} couches dont la projection n'est pas
compatible", Сервер публикует число слоёв с несовместимой проекцией.
"WMS server": "Serveur WMS", Сервер WMS
"Choose a WMTS server: ": "Choisissez un serveur WMTS : ", Выберите
сервер WMTS
"WMTS server": "Serveur WMTS", Сервер WMTS
"Choose a WFS server: ": "Choisissez un serveur WFS : ", Выберите сервер
WFS
"... or enter its address: ": "... ou saisissez son adresse : ", ...или
введите его адрес
"Unreachable server or insufficient rights": "Réponse invalide du " +
Ответ сервера недействителен.+
"serveur. Raisons possibles : droits insuffisants, " + Возможные причины
: недостаточные права, +
"serveur injoignable, trop de données, etc.", сервер недостижим, слишком
много данных, и т.д...
"WFS server": "Serveur WFS", Сервер WFS
/* GEOR_managelayers.js strings */
"Confirm NAME layer deletion ?":
"Voulez-vous réellement supprimer la couche ${NAME} ?", Вы действительно
хотите удалить слой${ИМЯ} ?
"1:MAXSCALE to 1:MINSCALE": "1:${MAXSCALE} à 1:${MINSCALE}",
"Visibility range (indicative):<br />from TEXT":
"Plage de visibilité (indicative):<br />de ${TEXT}", Индикаторный
диапазон видимости
"Information on objects of this layer":
"Interroger les objets de cette couche", Справиться у объектов этого слоя
"Default style": "Style par défaut", Стиль по умолчанию
"Recenter on the layer": "Recentrer sur la couche", Повторное
центрирование на слое
"Impossible to get layer extent":
"Impossible d'obtenir l'étendue de la couche.", Невозможно получить
размер слоя.
"Refresh layer": "Recharger la couche", Обновить слой.
"Show metadata": "Afficher les métadonnées", Показать метаданные
"Edit symbology": "Éditer la symbologie", Редактировать символику
"Build a query": "Construire une requête", Сделать запрос
"Cannot proceed: failed to get the equivalent WFS layer.":
"Opération impossible : nous n'avons pu obtenir la couche WFS Операция
невозможна : мы не смогли получить эквивалентный слой WFS.
équivalente.",
"Cannot proceed: the DescribeLayer WMS query failed.":
"Opération impossible : la requête WMS DescribeLayer a échoué.",
Операция невозможна : запрос на WMS DescribeLayer провалился.
"Download data": "Télécharger les données", Скачать данные
"Choose a style": "Choisir un style", Выбрать стиль
"Modify format": "Modifier le format", Изменить формат
"Delete this layer": "Supprimer cette couche", Удалить этот слой
"Push up this layer": "Monter cette couche", Переместить этот слой вверх
"Push down this layer": "descendre cette couche", Опустить этот слой
"Add layers": "Ajouter des couches", Добавить слои
"source: ": "source : ", источник
"unknown": "inconnue", неизвестный
"Actions": "Actions", Действия
/* GEOR_map.js strings */
"Location map": "Carte de situation", Карта локализации
"Warning after loading layer":
"Avertissement suite au chargement de couche", Уведомление, следующее за
загрузкой слоя
"The <b>NAME</b> layer could not appear for that reason: ":
"La couche <b>${NAME}</b> pourrait ne pas apparaître pour " + Возможное
отсутствие слоя<b>${ИМЯ}</b> +
"la raison suivante : ", по следующей причине :
"Min/max visibility scales are invalid":
"Les échelles min/max de visibilité sont invalides.",
Минмальный/Максимальный масштабы видимости являются недействительными.
"Visibility range does not match map scales":
"La plage de visibilité ne correspond pas aux échelles de la Диапазон
видимости не соответствует масштабам карты.
carte.",
"Geografic extent does not match map extent":
"L'étendue géographique ne correspond pas à celle de la carte.",
Географический размер не соответствует размерам карты.
/* GEOR_mapinit.js strings */
"Add layers from WMS services":
"Ajouter des couches depuis des services WMS", Добавить слои из сервисов WMS
"NB layers not imported": "${NB} couches non importées", ${NB}слоёв не
импортированы
"One layer not imported": "Une couche non importée", Не импортирован
один слой.
"mapinit.layers.load.error":
"Les couches nommées ${LIST} n'ont pas pu être chargées. " + Слои с
именем ${ПЕРЕЧЕНЬ} не смогли загрузиться +
"Raisons possibles : droits insuffisants, SRS incompatible ou Возможные
причины : недостаточные права, несоответствие с SRS (Spatial Reference
System - Система Космической Связи) +
couche non existante", или несуществующий слой.
"NB layers imported": "${NB} couches importées", Закачано ${ЧИСЛО ou
NB}слоёв
"One layer imported": "Une couche importée", Один слой закачан
"Not any layer imported": "Aucune couche importée", Не закачано ни
одного слоя.
"The provided context is not valid": "Le contexte fourni n'est pas
Предложенный контекст недействителен.
valide",
"The default context is not defined (and it is a BIG problem!)":
"Le contexte par défaut n'est pas défini " + Контекст по умолчанию не
определён. +
"(et ce n'est pas du tout normal !)", Это представляет собой серьёзную
проблему!
/* GEOR_mappanel.js strings */
"Coordinates in ": "Coordonnées en ", Координаты в...
"scale picker": "échelle", масштаб
/* GEOR_ows.js strings */
"The NAME layer was not found in WMS service.":
"La couche ${NAME} n'a pas été trouvée dans le service WMS.",
Слой${ИМЯ}не был найден в сервисе WMS (Web Map Service : Cервис Веб-Карт).
/* GEOR_print.js strings */
"Sources: ": "Sources : ", Источники
"Source: ": "Source : ", Источник
"Projection: PROJ": "Projection : ${PROJ}", Проекция ou Отображение
"Print error": "Impression impossible", Ошибка печати
"Print server returned an error":
"Le service d'impression a signalé une erreur.", Сервер печати сообщил
об ошибке.
"Contact platform administrator":
"Contactez l'administrateur de la plateforme.", Свяжитесь с
администратором платформы.
"Layer unavailable for printing": "Couche non disponible pour Слой для
печати отсутствует.
impression",
"The NAME layer cannot be printed.": Невозможно распечатать слой${ИМЯ}
"La couche ${NAME} ne peut pas encore être imprimée.", Слой${ИМЯ}пока не
может быть распечатан.
"Unable to print": "Impression non disponible", Печать недоступна.
"The print server is currently unreachable":
"Le service d'impression est actuellement inaccessible.", Сервер печати
временно недоступен.
"print.unknown.layout":
"Erreur de configuration: DEFAULT_PRINT_LAYOUT " + Ошибка конфигурации :
DEFAULT_PRINT_LAYOUT " +
"${LAYOUT} n'est pas dans la liste des formats d'impression", ${LAYOUT}
отсутствует в списке печатных форматов
"print.unknown.resolution":
"Erreur de configuration: DEFAULT_PRINT_RESOLUTION " + Ошибка
конфигурации : DEFAULT_PRINT_RESOLUTION +
"${RESOLUTION} n'est pas dans la liste des résolutions ${RESOLUTION}не
находится в списке резолюций печати.
d'impression",
"Title": "Titre", Заголовок
"Comments": "Commentaires", Комментарии
"Scale: ": "Échelle : ", Масштаб
"Date: ": "Date : ", Дата
"Minimap": "Mini-carte", Миникарта
"North": "Nord", Север
"Scale": "Échelle", Масштаб
"Date": "Date", Дата
"Legend": "Légende", Легенда
"Format": "Format", Формат
"Resolution": "Résolution", Резолюция
"Print the map": "Impression de la carte", Напечатание карты
"Print": "Imprimer", Напечатать
"Printing...": "Impression en cours...", Идёт печать
"Print current map": "Imprimer la carte courante", Напечатать текущую карту
/* GEOR_querier.js strings */
"Fields of filters with a red mark are mandatory": "Vous devez Поля
фильтров, помеченные красным цветом,
remplir " +
"les champs des filtres marqués en rouge.", являются обязательными для
заполнения.
"Request on NAME": "Requêteur sur ${NAME}", Сделать запрос о ${NAME}
"Search": "Rechercher", Искать
"querier.layer.no.geom":
"La couche ne possède pas de colonne géométrique." + Слой не имеет
геометрического столбца. +
"<br />Le requêteur géométrique ne sera pas fonctionnel.",
Геометрический запросчик не будет работать.
"querier.layer.error":
"Impossible d'obtenir les caractéristiques de la couche Невозможно
получить характеристику запрошенного слоя.
demandée." +
"<br />Le requêteur ne sera pas disponible.", Запросчик не будет доступен.
/* GEOR_referentials.js strings */
"Referential": "Référentiel", Ссылка
"There is no geometry column in the selected referential":
"Le référentiel sélectionné ne possède pas de colonne géométrique", В
выделенной ссылке отсутствует геометрический столбец.
"Choose a referential": "Choisissez un référentiel", Выбрать ссылку
/* GEOR_resultspanel.js strings */
"resultspanel.maxfeature.reached":
" <span ext:qtip=\"Utilisez un navigateur plus performant " +
Используйте более совершенный навигатор +
"pour augmenter le nombre d'objets affichables\">" + для того, чтобы
увеличить число фигурирующих на странице объектов.+
"Nombre maximum d'objets atteint (${NB})</span>", Максимальное число
объектов достигнуто.
"NB results": "${NB} résultats", ${NB}результатов
"One result": "1 résultat", Один результат
"Not any result": "Aucun résultat", Никакого результата нет.
"Clean": "Effacer", Стереть
"Clean all results on the map and in the table": "Supprimer les " +
Удалить +
"résultats affichés sur la carte et dans le tableau", все результаты с
карты и таблицы.
"Zoom": "Zoom", "Zoom" ou Зум
"Zoom to results extent": "Cadrer l'étendue de la carte sur celle " +
Применить величину зума карты на +
"des résultats", результаты.
"CSV Export": "Export CSV", Экспорт CSV (Comma-Separated Values -
значения, разделённые запятыми : текстовый формат)
"Export results as CSV": "Exporter l'ensemble des résultats en CSV",
Выслать все результаты в текстовом формате CSV.
"<p>Not any result for that request.</p>": "<p>Aucun objet ne " +
Никакой объект +
"correspond à votre requête.</p>", не соответствует вашему запросу.
/* GEOR_scalecombo.js strings */
/* GEOR_styler.js strings */
"Download style": "Télécharger le style", Скачать стиль
"You can download your SLD style at ": "Votre SLD est disponible à " +
Вы можете скачать ваш стиль в формате SLD +
"l'adresse suivante : ", по следующему адресу :
"Thanks!": "Merci !", Спасибо!
"Saving SLD": "Sauvegarde du SLD", Cохранение файла SLD
"Some classes are invalid, verify that all fields are correct":
"Des " +
"classes ne sont pas valides, vérifier que les champs sont corrects",
Некоторые классы недействительны, необходимо проверить все ли поля
правильны.
"Get SLD": "Récupération du SLD", Получить SLD
"Malformed SLD": "Le SLD n'est pas conforme.", SLD не соответствует
требованию.
"circle": "cercle", круг
"square": "carré", квадрат
"triangle": "triangle", треугольник
"star": "étoile", звезда
"cross": "croix", крест
"x": "x", "x" ou Икс
"customized...": "personnalisé...", Индивидуальный
"Classification ...<br/>(this operation can take some time)":
"Classification ...<br/>(cette opération peut prendre du temps)",
Классифицирование ou Сортировка...<br/>(эта операция может занять
некоторое время)
"Class": "Classe", Класс
"Untitled": "Sans titre", Без заголовка
"styler.guidelines":
"Utiliser le bouton \"+\" pour créer une classe, et le bouton " +
Использовать клавишу\"+\" для создания класса и "\"Analyse\" для того,
чтобы создать совокупность классов, определённых +
"\"Analyse\" pour créer un ensemble de classes définies par une " +
тематическим анализом.
"analyse thématique.</p>",
"Analyze": "Analyse", Анализ
"Add a class": "Ajouter une classe", Добавить класс
"Delete the selected class": "Supprimer la classe sélectionnée", Удалить
выделенный класс
"Styler": "Styleur", Стайлер
"Apply": "Appliquer", Применить
"Impossible to complete the operation:": "Opération impossible :",
Невозможно завершить операцию ou Операция невозможна
"not any WFS service associated to that layer": "aucun service WFS " +
Никакой сервис WFS +
"associé à cette couche.", не связан с этим слоем.
"not any available attribute": "aucun attribut disponible.", нет ни
одного доступного атрибута.
/* GEOR_toolbar.js strings */
"m": "m", "м"
"hectares": "hectares", гектары
"zoom to global extent of the map": "zoom sur l'étendue globale de la
carte", "zoom" на глобальный масштаб карты
"pan": "glisser - déplacer la carte", перетащить карту ou переместить карту
"zoom in": "zoom en avant (pour zoomer sur une emprise: appuyer sur
SHIFT + dessiner l'emprise)", "zoom" вперёд (для того, чтобы зуммировать
в определённых рамках : нажать на SHIFT + нарисовать ограничение
"zoom out": "zoom en arrière", "zoom" назад ou "zoom" обратно
"back to previous zoom": "revenir à la précédente emprise", вернуться к
предыдушему ограничению зума
"go to next zoom": "aller à l'emprise suivante", перейти к следующему
виду зуммирования
"Login": "Connexion", Соединение
"Logout": "Déconnexion", Отключение ou Разъединение
"Help": "Aide", Помощь
"Show help": "Afficher l'aide", Показать помощь
"Show legend": "Afficher la légende", Показать легенду
"Leave this page ? You will lose the current cartographic context.":
"Vous allez quitter cette page et perdre le contexte cartographique
courant", Выйти со страницы? В таком случае, вы потеряете текущий
картографический контекст.
/* GEOR_tools.js strings */
"distance measure": "Mesurer une distance", Измерение расстояния.
"area measure": "Mesurer une surface", Измерение площади
"Measure": "Mesure", Измерение ou Мера
"Tools": "Outils", Инструменты
"tool": "outil", Инструмент
"tool": "outil", Инструмент
"No tool": "aucun outil", Никакого инструмента
"Manage tools": "Gérer les outils", Управлять инструментами
"remember the selection": "se souvenir de la sélection", Запомнить выбор
ou Запомнить выделенное
"Available tools:": "Outils disponibles :", Инструменты в распоряжении
"Clic to select or deselect the tool": "Cliquez pour Щёлкните для того,
чтобы выбрать инструмент или отменить его.
(dé)sélectionner l'outil",
"Could not load addon ADDONNAME": "Impossible de charger l'addon
${ADDONNAME}", Невозможно загрузить аддон ${ИМЯАДДОНА}",
/* GEOR_util.js strings */
"Characters": "Caractères", Письменность (ou Буквы и Цифры в информатике)
"Digital": "Numérique", Цифровой
"Boolean": "Booléen", Логический, булев тип данных (или logical data type)
"Other": "Autre", Другой
"Confirmation": "Confirmation", Подтверждение
"Information": "Information", Информация
/* GEOR_waiter.js strings */
/* GEOR_wmc.js strings */
"The provided file is not a valid OGC context": "Le fichier fourni n'est
pas un contexte OGC valide", Данный файл не является действительным
контекстом OGC (Open Geospatial Consortium — открытый
геопространственный консорциум)
"Warning: trying to restore WMC with a different projection
(PROJCODE1, while map SRS is PROJCODE2). Strange things might occur !":
"Attention: le contexte restauré avait été sauvegardé en ${PROJCODE1}
"Предупреждение : попытка восстановления WMC в одной проекции ${PROJCODE1},
alors que la carte actuelle est en ${PROJCODE2}. Il pourrait y avoir des
тогда как данная карта SRS находится в другой ${PROJCODE2}. Здесь
возможны непредвиденные обстоятельства".
comportements inattendus.",
/* GEOR_wmcbrowser.js strings */
"Could not find WMC file": "Le contexte spécifié n'existe pas",
Невозможно найти WMC файл.
"... or a custom context": "... ou un contexte personnalisé", ...или
индивидуальный контекст,
"Replace current map composition with one of these contexts:":
"Remplacer la composition actuelle de la carte par l'un de ces contextes
:", Заменить составляющую текущей карты одним из предложенных контекстов.
"A unique OSM layer": "Une unique couche OpenStreetMap", Единый слой
OpenStreetMap
"default viewer context": "contexte par défaut", контекст вьюера по
умолчанию
"(default)": "<br/>(contexte par défaut actuel)",
/* GEOR_workspace.js strings */
"Context saving": "Sauvegarde du contexte", Сохранение контекста
"The file is required.": "Un nom de fichier est nécessaire.", Требуется
имя файла
"Context restoring": "Restauration d'un contexte", Восстановление контекста
"<p>Please note that the WMC must be UTF-8 encoded</p>": "<p>Notez
Обратите внимание на то, что файл в формате WMC должен быть закодирован
UTF-8.</p>",
que le fichier de contexte doit être encodé en UTF-8.</p>",
"Load": "Charger", Загрузить
"Workspace": "Espace de travail", Рабочее пространство
"Save the map context": "Sauvegarder la carte", Сохранить карту
"Load a map context": "Charger une carte", Загрузить карту
"Get a permalink": "Obtenir un permalien", Получить постоянную ссылку
"Permalink": "Permalien", Постоянная ссылка
"valid for ": "valide pendant ", Действителен в течение
"months": "mois", месяцы
"month": "mois", месяц
"Share your map with this URL: ": "Partagez la carte avec l'adresse
Доступ к вашей карте с этого URL-адреса :
suivante : ",
"Edit in OSM": "Editer dans OSM", Редактировать в OSM (OpenStreetMap
«открытая карта улиц) - некоммерческий веб-картографический проект по
созданию подробной свободной и бесплатной географической карты мира.
"with JOSM": "avec JOSM", c JOSM (Java OpenStreetMap Editor -
расширяемый редактор для OSM)
"JOSM must be started with the remote control option": "Il vous faut " +
Необходимо +
"auparavant lancer JOSM et activer le contrôle à distance", запустить
JOSM с опцией дистанционного контроля,
"with Potlatch": "avec Potlatch", c редактором Potlatch
"Recommended scale is 1:10.000": "Il est recommandé de travailler à des
échelles proches de 1:10.000", Рекомендуемый масштаб - 1:10.000
"with Potlatch2": "avec Potlatch2", c редактором Potlatch2
"with Walking Papers": "avec Walking Papers", при помощи Walking Papers
/* GEOR_EditingPanel.js */
"Read-only layer": "Couche non éditable", Cлой только для прочтения
"editingpanel.geom.error": "La géométrie de cette couche est de type
${TYPE}. <br/>" + Геометрия этого слоя следующего типа : ${TYPE}.<br/>" +
"Seules les géométries de type point, ligne et polygone" + Только
следующие геометрические типы подлежат редактированию : точка, линия и
многоугольник (а также много-, мульти-)
" (et multi-*) sont éditables.",
"choose a layer": "choisissez une couche", Выберите слой
/* GEOR_LayerEditingPanel.js */
"Modify object": "Modifier un objet", Изменить объект
"Cancel all": "Tout annuler", Отменить всё ou Аннулировать всё
"Enter ": "Saisir ", Ввести
"layereditingpanel.cancel.confirm": "Souhaitez-vous vraiment + Вы
действительно желаете +
annuler toutes les modifications<br />depuis la dernière synchronisation
?", отменить все изменения<br />после последней синхронизации?
"Synchronize": "Synchroniser", Синхронизировать
"layereditingpanel.changes.confirm": "Veuillez confirmer ou annuler " +
Пожалуйста, подтвердите или отмените +
"les modifications en cours.", текущие изменения.
"Delete": "Supprimer", Удалить
"Confirm": "Confirmer", Подтвердить
" (required)": " (requis)", требуемый, необходимый
"Text too long": "Texte trop long", Текст слишком длинный
"Text too short": "Texte trop court", Текст слишком короткий
"Maximum value exceeded": "Valeur maximale dépassée", Максимальное
значение превышено
"Minimum value not reached": "Valeur minimale non atteinte", Минимальное
значение не достигнуто
"Invalid number": "Nombre non valide", Число недействительно
"Synchronization successful.": "Synchronisation réussie.", Синхронизация
прошла успешно.
"Synchronization failed.": "Erreur lors de la synchronisation.",
Произошла ошибка во время процесса синхронизации.
"Recover": "Restaurer", Восстановить
"No feature selected !": "Aucun objet sélectionné !", Ни одна функция не
выбрана
"a point": "un point", точка (ou пункт)
"a line": "une ligne", линия
"a polygon": "un polygone", многоугольник
"Pending changes": "Modifications en cours", Идёт процесс внесения
изменений
"Do you want to confirm changes ?": "Souhaitez-vous confirmer les
modifications ?", Желаете ли вы подтвердить изменения?
/* GeoExt.data.CSW.js */
"no abstract": "pas de résumé" нет резюме
    // no trailing comma
});
