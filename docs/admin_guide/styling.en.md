# Styling geOrchestra

## Steps to add a style

We try to maintain an easy solution to customize apps within geOrchestra SDI. 

There's some configuration to set in order to have a minimal working style. 

Steps :

- Provide a css file (e.g on /public/georchestra.css)
    - you should have a css which is provided somewhere. It has to be a public accessible from a browser
- Fill the `georchestraStylesheet` in [`default.properties`](https://github.com/georchestra/datadir/blob/75aa3c4d45be9ccd0f21f2f277f41a83d346b63d/default.properties#L43C3-L43C24) in the datadir.
- Fill the stylesheet uri in some apps (described later in specific apps section).
- Restart your apps.

## CSS file content

The css content tries to split each app in order to have a fine control of style within each of them.

```css
@font-face {
  font-family: 'Inter';
  font-style: normal;
  font-weight: 100 900;
  font-display: swap;
  src: url(...) format('woff2');
  unicode-range: U+0460-052F, U+1C80-1C8A, U+20B4, U+2DE0-2DFF, U+A640-A69F, U+FE2E-FE2F;
}
/* This file is used to override the default geOrchestra theme colors */
/* This body part is for all apps. It helps to set global base variables */
body {
  --georchestra-primary: #e20714;
  --georchestra-secondary: rgb(0, 122, 128);
  --georchestra-primary-light: white;
  --georchestra-secondary-light: #eee;
}

/** The header needs georchestra-header-* variables */
header {
  --georchestra-header-primary: #8c8c8c;
  --georchestra-header-primary-light: white;
}

/* Datafeeder can be tricked using those variables*/
body {
  --color-primary: var(--georchestra-primary);
  /* color-mix is a usefull css function to alter a color*/
  --color-primary-lighter: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 30%
  );
  --color-primary-lightest: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 50%
  );
  --color-primary-white: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 95%
  );
  --color-primary-darker: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 30%
  );
  --color-primary-darkest: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 50%
  );
  --color-primary-black: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 90%
  );
  --gradient: linear-gradient(65deg, #fff 0%, var(--color-primary-white) 100%);
}

/* Mapstore needs a spcific configuration : see specific apps */
/* variables starts with georchestra-ms-* */
.geOrchestra[data-ms2-container="ms2"] {
  --georchestra-ms-primary: var(--georchestra-primary);
  --georchestra-ms-main-variant-color: rgb(0, 58, 59);

  --georchestra-ms-button-color: var(--georchestra-primary);
  --georchestra-ms-link-color: color-mix(in srgb, var(--georchestra-primary), #fff 30%);
  --georchestra-ms-link-hover-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 50%
  );
  --georchestra-ms-loader-primary-color: var(--georchestra-primary);
  --georchestra-ms-loader-primary-fade-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 75%
  );
  --georchestra-ms-focus-color: color-mix(in srgb, var(--georchestra-primary), #fff 90%);

  --georchestra-ms-button-bg: #fff;
  --georchestra-ms-button-border-color: #fff;
  --georchestra-ms-button-hover-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 30%
  );
  --georchestra-ms-button-hover-bg: color-mix(in srgb, var(--georchestra-ms-button-bg), #000 15%);
  --georchestra-ms-button-hover-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-bg),
    #000 15%
  );
  --georchestra-ms-button-disabled-bg: color-mix(in srgb, var(--georchestra-ms-button-bg), #fff 30%);
  --georchestra-ms-button-disabled-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-bg),
    #fff 30%
  );
  --georchestra-ms-button-active-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-bg),
    #000 30%
  );
  --georchestra-ms-button-active-border-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 30%
  );
  --georchestra-ms-button-active-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 30%
  );
  --georchestra-ms-button-focus-border-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 30%
  );
  --georchestra-ms-button-active-hover-border-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 30%
  );
  --georchestra-ms-button-active-hover-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #000 30%
  );
  --georchestra-ms-button-focus-color: color-mix(
    in srgb,
    var(--georchestra-primary),
    #fff 30%
  );

  --georchestra-ms-button-primary-bg: var(--georchestra-primary);
  --georchestra-ms-button-primary-border-color: var(--georchestra-primary);
  --georchestra-ms-button-primary-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 15%
  );
  --georchestra-ms-button-primary-hover-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 15%
  );
  --georchestra-ms-button-primary-disabled-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #fff 30%
  );
  --georchestra-ms-button-primary-disabled-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #fff 30%
  );
  --georchestra-ms-button-primary-active-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );
  --georchestra-ms-button-primary-active-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );
  --georchestra-ms-button-primary-active-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );
  --georchestra-ms-button-primary-focus-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );
  --georchestra-ms-button-primary-focus-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );
  --georchestra-ms-button-primary-focus-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-primary-bg),
    #000 30%
  );

  --georchestra-ms-button-success-bg: var(--georchestra-secondary);
  --georchestra-ms-button-success-border-color: var(--georchestra-secondary);
  --georchestra-ms-button-success-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 15%
  );
  --georchestra-ms-button-success-hover-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 15%
  );
  --georchestra-ms-button-success-disabled-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #fff 30%
  );
  --georchestra-ms-button-success-disabled-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #fff 30%
  );
  --georchestra-ms-button-success-active-bg: var(--georchestra-secondary);
  --georchestra-ms-button-success-active-border-color: var(--georchestra-secondary);
  --georchestra-ms-button-success-active-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 30%
  );
  --georchestra-ms-button-success-active-hover-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 30%
  );
  --georchestra-ms-button-success-focus-hover-bg: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 30%
  );
  --georchestra-ms-button-success-focus-border-color: color-mix(
    in srgb,
    var(--georchestra-ms-button-success-bg),
    #000 30%
  );
  --georchestra-ms-button-success-focus-bg: color-mix(
    in srgb,
    var(--georchestra-secondary),
    #000 30%
  );
}

/* Custom code can be used to alter mapstore UI */
.geOrchestra[data-ms2-container="ms2"] #content-tabs > div > div > h2,
.geOrchestra[data-ms2-container="ms2"]
  #content-tabs-container-container
  > div:nth-child(2)
  > div
  > div:nth-child(1)
  > h3 {
  display: none;
}
```

## Specific apps

### Header

The header of geOrchestra is a webcomponent which comes from [georchestra/header](https://github.com/georchestra/header) repo and needs some configuration file.

If you don't want to use a custom config which allows to change entries in the menu, you can fill only the [datadir variables](https://github.com/georchestra/datadir/blob/master/default.properties#L20-L47) which are used for editing the header.

You can refer to he repo to see the full documentation.

Every apps use datadir except Mapstore2 and geonetwork-ui apps (datahub, datafeeder, metadata-editor...).

### Mapstore 

We have developed a specific plugin in mapstore to ease style change. 

This plugin is called `Easytheming` and should be added in `localConfig.json` in three different lines : 

- [In plugins-mobile](https://github.com/georchestra/mapstore2-georchestra/blob/c204edd3de1b55b6f493673dfbb4700d221f7296/configs/localConfig.json#L263)
- [In desktop](https://github.com/georchestra/mapstore2-georchestra/blob/c204edd3de1b55b6f493673dfbb4700d221f7296/configs/localConfig.json#L390)
- [In context-creator](https://github.com/georchestra/mapstore2-georchestra/blob/c204edd3de1b55b6f493673dfbb4700d221f7296/configs/localConfig.json#L1101)

You need either to fill the [`stylesheetUri` here](https://github.com/georchestra/mapstore2-georchestra/blob/c204edd3de1b55b6f493673dfbb4700d221f7296/configs/localConfig.json#L21)

And the for the header, if you want to style it too. [Here](https://github.com/georchestra/mapstore2-georchestra/blob/c204edd3de1b55b6f493673dfbb4700d221f7296/configs/localConfig.json#L29).

**Behavior with css variables**
All the georchestra variables are available in [variables.less](https://github.com/georchestra/mapstore2-georchestra/blob/master/themes/default/variables.less#L97) file. Those are all who begins with `--georchestra-ms-*`.

First, `--ms-*` variables are used (those are set in contexts in Mapstore) then it fallbacks on `--georchestra-ms-*` ones (set in the css file). If not present, it fallback on georchestra's colors.

### Geonetwork-UI apps

Geonetwork-ui apps (Datahub, Metadata-editor, datafeeder) aren't inside georchestra repo's and in order to add the header, it should have some scripts to inject header inside HTML.

You can check `georchestra/docker` or `georchestra/ansible` repos to have a glimpse of how we can do it.

Datahub comes with his own configuration and can be found in the datadir : [default.toml]https://github.com/georchestra/datadir/blob/master/datahub/conf/default.toml

### Geoserver 

Geoserver may have CSP enabled and so we can't use @import statement in css file. 
For details, please see PR: https://github.com/georchestra/geoserver/pull/43
