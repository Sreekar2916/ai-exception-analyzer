const path = require("path");
const HtmlWebpackPlugin = require("html-webpack-plugin");

module.exports = (env, argv) => {
  const isProduction = argv.mode === "production";

  return {
    entry: "./src/index.js",
    output: {
      path: path.resolve(__dirname, "dist"),
      filename: isProduction ? "bundle.[contenthash].js" : "bundle.js",
      publicPath: "/",
      clean: true,
    },
    devtool: isProduction ? false : "eval-source-map",
    module: {
      rules: [
        {
          test: /\.(js|jsx)$/,
          exclude: /node_modules/,
          use: "babel-loader",
        },
        {
          test: /\.css$/,
          use: ["style-loader", "css-loader"],
        },
      ],
    },
    resolve: {
      extensions: [".js", ".jsx"],
    },
    plugins: [
      new HtmlWebpackPlugin({
        template: "./public/index.html",
      }),
    ],
    devServer: {
      port: 3000,
      open: false,
      historyApiFallback: true,
      // Forward /api requests to the Spring Boot backend so the browser
      // sees a same-origin request and CORS is never involved.
      proxy: [
        {
          context: ["/api"],
          target: "http://localhost:8080",
          changeOrigin: true,
        },
      ],
    },
  };
};
