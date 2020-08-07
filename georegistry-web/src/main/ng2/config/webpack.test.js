var ngToolsWebpack = require('@ngtools/webpack');

var helpers = require('./helpers');

module.exports = {
  entry : {
    'cgr-polyfills' : './src/polyfills.ts',
    'cgr-vendor' : './src/vendor.ts',
    'cgr-app' : './src/main.ts'
  },
  resolve: {
    extensions: ['.ts', '.js', '.scss'],
  },
  node: {
    fs: "empty"
  },
  module: {
    rules: [
      {
          test: /(?:\.ngfactory\.js|\.ngstyle\.js|\.ts)$/,
          loader: '@ngtools/webpack'
      },
      {
          test: /\.html$/,
          use: [ {
              loader: 'html-loader',
              options: {
                minimize: false
              }
            }]    	
      },
      {
        test: /\.(png|jpe?g|gif|svg|woff|woff2|ttf|eot|ico)$/,
        loader: 'file-loader?name=assets/[name].[hash].[ext]'
      },
      {
        test: /\.css$/,
// include: helpers.root('src', 'app'),
// exclude:
        loader: 'raw-loader'
      },
      {
        test: /\.scss$/,
        use: [
            "style-loader", // creates style nodes from JS strings
            "css-loader", // translates CSS into CommonJS
            "sass-loader" // compiles Sass to CSS, using Node Sass by default
        ]
      }      
    ]
  },
  
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
  plugins: [
    new ngToolsWebpack.AngularCompilerPlugin({
      tsConfigPath: './tsconfig.spec.json',
      entryModule: './src/app/cgr-app.module#CgrAppModule',
      sourceMap: true
    })    
  ]
};