var webpackConfig = require("./webpack.config.js");
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
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['Chrome'],
    singleRun: true,
    concurrency: Infinity    
  });
};