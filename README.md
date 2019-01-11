# BootstrapUiModule

## Configuring the frontend setup

The javascript is compiled using [webpack](https://github.com/webpack/webpack) and [node-sass](https://github.com/sass/node-sass).
For ease of use, this configuration is split up over various file, so that minimal knowledge is required for basic configuration.

Setting | Description | File 
--- | --- | ---
Entry files | Configured by providing the  | `package.json` 
Output paths | The output path for scss and js can be configured by specifying the `scssOutputPath` and respectively `jsOutputPath` as config variables | `package.json` 
Files to keep | Configured by listing these files in the `keepFiles` property | `settings.js` 
Libraries that are loaded externally | Configured by specifying a dependency to global variable mapping for the dependency in the `externals` object.  | `settings.js` 


## Compilation

Frontend files can be can be compiled in various manners.
Builds can be compiled locally by running `build-local.sh`.
An additional `build-prod.sh` is provided for build agents.

Compiling locally can be done for various end purposes.

Description | command
--- | ---
Compiling javascript | `build-local.sh js`
Compiling style sheets | `build-local.sh scss`
Compiling and watching | `build-local.sh js:watch` (respectively with scss) 
Compiling in production mode | `build-local.sh js:prod` (respectively with scss)