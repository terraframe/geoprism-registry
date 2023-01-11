var webpackConfig = require("./config/webpack.test.js");
webpackConfig.entry = null;

module.exports = (config) => {
  config.set({
	  basePath:'',
	  frameworks: ['jasmine'],
	  client:{
		clearContext:false  
	  },
	  coverageIstanbulReporter:{
		  reports:['html', 'lcovonly'],
		  fixWebpackSourcePaths: true
	  },
	  files: [
		  'test/global.js',
		  'test/test.ts'
		],
		 
		preprocessors: {
			'test/test.ts': [ 'webpack', 'sourcemap' ]
		},

	    webpackMiddleware: {
	        // webpack-dev-middleware configuration
	        // i. e.
	        stats: 'errors-only',
	      },

    webpack: webpackConfig,
 
    reporters: ['progress'],
    port: 9876,
    colors: true,
    logLevel: config.LOG_DEBUG,
    autoWatch: true,
    browsers: ['Chrome'],
    singleRun: false,
    concurrency: Infinity    
  });
};