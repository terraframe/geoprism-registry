var webpack = require('webpack');
var webpackMerge = require('webpack-merge');
var commonConfig = require('./webpack.common.js');
var helpers = require('./helpers');

const ENV = process.env.NODE_ENV = process.env.ENV = 'development';

module.exports = webpackMerge(commonConfig, {
  devtool: 'cheap-module-eval-source-map',
  mode:'development',
	optimization : {
		namedChunks : true,
		chunkIds : 'named',
	    splitChunks: {
	        cacheGroups: {
	            default: false,
	            vendors: false,

	            // vendor chunk
	            vendor: {
	            	name:'vendor',
	                // sync + async chunks
	                chunks: 'all',

	                // import file path containing node_modules
	                test: /node_modules/
	            }
	        }
	    }
    },

  output: {
    path: helpers.root('dist'),
    publicPath: 'http://localhost:8080/dist/',
    filename: '[name].js',
    chunkFilename: '[id].chunk.js'
  },

  plugins: [],

  devServer: {
    historyApiFallback: true,
    stats: 'minimal'
  }
});