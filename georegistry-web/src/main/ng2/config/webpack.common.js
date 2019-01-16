var webpack = require('webpack');
var HtmlWebpackPlugin = require('html-webpack-plugin');
var ExtractTextPlugin = require('extract-text-webpack-plugin');
var helpers = require('./helpers');

module.exports = {
	entry : {
		'cgr-polyfills' : './src/polyfills.ts',
		'cgr-vendor' : './src/vendor.ts',
		'cgr-app' : './src/main.ts'
	},
	resolve : {
		extensions : [ '.ts', '.js', '.scss' ]
	},

	module : {
		rules : [ {
			test : /\.ts$/,
			loader : [ {
				loader : 'awesome-typescript-loader',
				options : {
					configFileName : helpers.root('tsconfig.json')
				}
			}, 'angular2-template-loader' ]
		}, {
			test : /\.html$/,
			use : 'html-loader'
		}, {
			test : /\.(png|jpe?g|gif|svg|woff|woff2|ttf|eot|ico)$/,
			use : 'file-loader?name=assets/[name].[hash].[ext]'
		}, {
			test : /\.css$/,
			exclude : helpers.root('src', 'app'),
			use : ExtractTextPlugin.extract({
				fallback : 'style-loader',
				use : 'css-loader?sourceMap'
			})
		}, {
			test : /\.css$/,
			include : helpers.root('src', 'app'),
			use : 'raw-loader'
		}, {
			test : /\.scss$/,
			// include: helpers.root('styles'),
			use : [ "style-loader", // creates style nodes from JS strings
			"css-loader", // translates CSS into CommonJS
			"sass-loader" // compiles Sass to CSS, using Node Sass by default
			]
		} ]
	},

	plugins : [
	// Workaround for angular/angular#11580
	new webpack.ContextReplacementPlugin(
	// The (\\|\/) piece accounts for path separators in *nix and Windows
	/\@angular(\\|\/)core(\\|\/)esm5/, helpers.root('./src'), // location of
	// your src
	{} // a map of your routes
	), ]
};