var webpack = require('webpack');
var helpers = require('./helpers');
var ngToolsWebpack = require('@ngtools/webpack');

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
//        include: helpers.root('src', 'app'),
//        exclude:
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

  plugins: [
    new ngToolsWebpack.AngularCompilerPlugin({
      tsConfigPath: './tsconfig.json',
      entryModule: './src/app/cgr-app.module#CgrAppModule',
      sourceMap: true
    })    
  ]
};