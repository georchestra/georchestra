/**
 * Spanish/Latin American Translation by genius551v 04-08-2007
 * Revised by efege, 2007-04-15.
 * Revised by Rafaga2k 10-01-2007 (mm/dd/yyyy)
 * Revised by FeDe 12-13-2007 (mm/dd/yyyy)
 * Synchronized with 2.2 version of ext-lang-en.js (provided by Condor 8 aug 2008)
 *     by halkon_polako 14-aug-2008
 */
Ext.onReady(function() {
    var cm = Ext.ClassManager,
        exists = Ext.Function.bind(cm.get, cm);

    if (Ext.Updater) {
        Ext.Updater.defaults.indicatorText = '<div class="loading-indicator">Cargando...</div>';
    }

    Ext.define("Ext.locale.es.view.View", {
        override: "Ext.view.View",
        emptyText: ""
    });

    Ext.define("Ext.locale.es.grid.Panel", {
        override: "Ext.grid.Panel",
        ddText: "{0} fila(s) seleccionada(s)"
    });

    // changing the msg text below will affect the LoadMask
    Ext.define("Ext.locale.es.view.AbstractView", {
        override: "Ext.view.AbstractView",
        msg: "Cargando..."
    });

    if (Ext.Date) {
        Ext.Date.monthNames = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];

        Ext.Date.getShortMonthName = function(month) {
            return Ext.Date.monthNames[month].substring(0, 3);
        };

        Ext.Date.monthNumbers = {
            Ene: 0,
            Feb: 1,
            Mar: 2,
            Abr: 3,
            May: 4,
            Jun: 5,
            Jul: 6,
            Ago: 7,
            Sep: 8,
            Oct: 9,
            Nov: 10,
            Dic: 11
        };

        Ext.Date.getMonthNumber = function(name) {
            return Ext.Date.monthNumbers[name.substring(0, 1).toUpperCase() + name.substring(1, 3).toLowerCase()];
        };

        Ext.Date.dayNames = ["Domingo", "Lunes", "Martes", "Mi&#233;rcoles", "Jueves", "Viernes", "S&#225;bado"];

        Ext.Date.getShortDayName = function(day) {
            if (day == 3) return "Mié";
            if (day == 6) return "Sáb";
            return Ext.Date.dayNames[day].substring(0, 3);
        };

        Ext.Date.parseCodes.S.s = "(?:st|nd|rd|th)";
    }

    if (Ext.MessageBox) {
        Ext.MessageBox.buttonText = {
            ok: "Aceptar",
            cancel: "Cancelar",
            yes: "S&#237;",
            no: "No"
        };
    }

    if (exists('Ext.util.Format')) {
        Ext.apply(Ext.util.Format, {
            thousandSeparator: '.',
            decimalSeparator: ',',
            currencySign: '\u20ac',
            // Spanish Euro
            dateFormat: 'd/m/Y'
        });
    }

    Ext.define("Ext.locale.es.picker.Date", {
        override: "Ext.picker.Date",
        todayText: "Hoy",
        minText: "Esta fecha es anterior a la fecha m&#237;nima",
        maxText: "Esta fecha es posterior a la fecha m&#225;xima",
        disabledDaysText: "",
        disabledDatesText: "",
        monthNames: Ext.Date.monthNames,
        dayNames: Ext.Date.dayNames,
        nextText: 'Mes Siguiente (Control+Right)',
        prevText: 'Mes Anterior (Control+Left)',
        monthYearText: 'Seleccione un mes (Control+Up/Down para desplazar el a&#241;o)',
        todayTip: "{0} (Barra espaciadora)",
        format: "d/m/Y",
        startDay: 1
    });

    Ext.define("Ext.locale.es.picker.Month", {
        override: "Ext.picker.Month",
        okText: "&#160;Aceptar&#160;",
        cancelText: "Cancelar"
    });

    Ext.define("Ext.locale.es.toolbar.Paging", {
        override: "Ext.PagingToolbar",
        beforePageText: "P&#225;gina",
        afterPageText: "de {0}",
        firstText: "Primera p&#225;gina",
        prevText: "P&#225;gina anterior",
        nextText: "P&#225;gina siguiente",
        lastText: "Última p&#225;gina",
        refreshText: "Actualizar",
        displayMsg: "Mostrando {0} - {1} de {2}",
        emptyMsg: 'Sin datos para mostrar'
    });

    Ext.define("Ext.locale.es.form.field.Base", {
        override: "Ext.form.field.Base",
        invalidText: "El valor en este campo es inv&#225;lido"
    });

    Ext.define("Ext.locale.es.form.field.Text", {
        override: "Ext.form.field.Text",
        minLengthText: "El tama&#241;o m&#237;nimo para este campo es de {0}",
        maxLengthText: "El tama&#241;o m&#225;ximo para este campo es de {0}",
        blankText: "Este campo es obligatorio",
        regexText: "",
        emptyText: null
    });

    Ext.define("Ext.locale.es.form.field.Number", {
        override: "Ext.form.field.Number",
        decimalSeparator: ",",
        decimalPrecision: 2,
        minText: "El valor m&#237;nimo para este campo es de {0}",
        maxText: "El valor m&#225;ximo para este campo es de {0}",
        nanText: "{0} no es un n&#250;mero v&#225;lido"
    });

    Ext.define("Ext.locale.es.form.field.Date", {
        override: "Ext.form.field.Date",
        disabledDaysText: "Deshabilitado",
        disabledDatesText: "Deshabilitado",
        minText: "La fecha para este campo debe ser posterior a {0}",
        maxText: "La fecha para este campo debe ser anterior a {0}",
        invalidText: "{0} no es una fecha v&#225;lida - debe tener el formato {1}",
        format: "d/m/Y",
        altFormats: "d/m/Y|d-m-y|d-m-Y|d/m|d-m|dm|dmy|dmY|d|Y-m-d"
    });

    Ext.define("Ext.locale.es.form.field.ComboBox", {
        override: "Ext.form.field.ComboBox",
        valueNotFoundText: undefined
    }, function() {
        Ext.apply(Ext.form.field.ComboBox.prototype.defaultListConfig, {
            loadingText: "Cargando..."
        });
    });

    if (exists('Ext.form.field.VTypes')) {
        Ext.apply(Ext.form.field.VTypes, {
            emailText: 'Este campo debe ser una direcci&#243;n de correo electr&#243;nico con el formato "usuario@dominio.com"',
            urlText: 'Este campo debe ser una URL con el formato "http:/' + '/www.dominio.com"',
            alphaText: 'Este campo s&#243;lo debe contener letras y _',
            alphanumText: 'Este campo s&#243;lo debe contener letras, n&#250;meros y _'
        });
    }

    Ext.define("Ext.locale.es.form.field.HtmlEditor", {
        override: "Ext.form.field.HtmlEditor",
        createLinkText: "Por favor proporcione la URL para el enlace:"
    }, function() {
        Ext.apply(Ext.form.field.HtmlEditor.prototype, {
            buttonTips: {
                bold: {
                    title: 'Negritas (Ctrl+B)',
                    text: 'Transforma el texto seleccionado en Negritas.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                italic: {
                    title: 'It&#225;lica (Ctrl+I)',
                    text: 'Transforma el texto seleccionado en It&#225;licas.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                underline: {
                    title: 'Subrayado (Ctrl+U)',
                    text: 'Subraya el texto seleccionado.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                increasefontsize: {
                    title: 'Aumentar la fuente',
                    text: 'Aumenta el tama&#241;o de la fuente',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                decreasefontsize: {
                    title: 'Reducir la fuente',
                    text: 'Reduce el tama&#241;o de la fuente.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                backcolor: {
                    title: 'Color de fondo',
                    text: 'Modifica el color de fondo del texto seleccionado.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                forecolor: {
                    title: 'Color de la fuente',
                    text: 'Modifica el color del texto seleccionado.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                justifyleft: {
                    title: 'Alinear a la izquierda',
                    text: 'Alinea el texto a la izquierda.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                justifycenter: {
                    title: 'Centrar',
                    text: 'Centrar el texto.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                justifyright: {
                    title: 'Alinear a la derecha',
                    text: 'Alinea el texto a la derecha.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                insertunorderedlist: {
                    title: 'Lista de vi&#241;etas',
                    text: 'Inicia una lista con vi&#241;etas.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                insertorderedlist: {
                    title: 'Lista numerada',
                    text: 'Inicia una lista numerada.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                createlink: {
                    title: 'Enlace',
                    text: 'Inserta un enlace de hipertexto.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                },
                sourceedit: {
                    title: 'C&#243;digo Fuente',
                    text: 'Pasar al modo de edici&#243;n de c&#243;digo fuente.',
                    cls: Ext.baseCSSPrefix + 'html-editor-tip'
                }
            }
        });
    });

    Ext.define("Ext.locale.es.grid.header.Container", {
        override: "Ext.grid.header.Container",
        sortAscText: "Ordenar en forma ascendente",
        sortDescText: "Ordenar en forma descendente",
        columnsText: "Columnas"
    });

    Ext.define("Ext.locale.es.grid.GroupingFeature", {
        override: "Ext.grid.GroupingFeature",
        emptyGroupText: '(Ninguno)',
        groupByText: 'Agrupar por este campo',
        showGroupsText: 'Mostrar en grupos'
    });

    Ext.define("Ext.locale.es.grid.PropertyColumnModel", {
        override: "Ext.grid.PropertyColumnModel",
        nameText: "Nombre",
        valueText: "Valor",
        dateFormat: "j/m/Y"
    });

    Ext.define("Ext.locale.es.form.field.Time", {
        override: "Ext.form.field.Time",
        minText: "La hora en este campo debe ser igual o posterior a {0}",
        maxText: "La hora en este campo debe ser igual o anterior a {0}",
        invalidText: "{0} no es una hora v&#225;lida",
        format: "g:i A",
        altFormats: "g:ia|g:iA|g:i a|g:i A|h:i|g:i|H:i|ga|ha|gA|h a|g a|g A|gi|hi|gia|hia|g|H"
    });

    Ext.define("Ext.locale.es.form.CheckboxGroup", {
        override: "Ext.form.CheckboxGroup",
        blankText: "Debe seleccionar al menos un &#233;tem de este grupo"
    });

    Ext.define("Ext.locale.es.form.RadioGroup", {
        override: "Ext.form.RadioGroup",
        blankText: "Debe seleccionar un &#233;tem de este grupo"
    });
});
