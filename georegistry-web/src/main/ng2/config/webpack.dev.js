const { merge } = require('webpack-merge');
var commonConfig = require('./webpack.common.js');
var helpers = require('./helpers');

const ENV = process.env.NODE_ENV = process.env.ENV = 'development';

module.exports = merge(commonConfig, {
  devtool: 'cheap-module-eval-source-map',
  mode:'development',
	optimization : {
		namedChunks : true,
		chunkIds : 'named',
	    splitChunks: {
	        cacheGroups: {
	            // vendor chunk
	            vendor: {
	            	name:'vendor',
	                // sync + async chunks
	                chunks: 'all',

	                // import file path containing node_modules
	                test: /[\\/]node_modules[\\/]/,
	                priority: -10
	            },
	            default: {
	                minChunks: 2,
	                priority: -20,
	                reuseExistingChunk: true
	            }
	        }
	    }
    },

  output: {
    path: helpers.root('dist'),
    publicPath: 'https://localhost:8080/dist/',
    filename: '[name].js',
    chunkFilename: '[id].chunk.js'
  },

  plugins: [],

  devServer: {
    historyApiFallback: true,
    stats: 'minimal'
  }
});