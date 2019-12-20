var webpack = require('webpack');
var webpackMerge = require('webpack-merge');
var commonConfig = require('./webpack.common.js');
var helpers = require('./helpers');
var path = require('path');
var BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const ENV = process.env.NODE_ENV = process.env.ENV = 'production';

module.exports = webpackMerge(commonConfig, {
	devtool : 'source-map',
	mode : 'production',
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
	output : {
		path : path.resolve('../webapp/dist'),
		filename : '[name].js',
		chunkFilename : '[id].chunk.js',
		publicPath : '../dist/', // The public path is relative to the hosted
									// page (/georegistry/cgr/manage) ->
									// /georegistry/dist
	},

	plugins : [ 
//		new BundleAnalyzerPlugin()
    ]
});