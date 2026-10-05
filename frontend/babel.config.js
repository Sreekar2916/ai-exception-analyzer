module.exports = {
  presets: [
    "@babel/preset-env",
    // "automatic" runtime means components don't need `import React`
    ["@babel/preset-react", { runtime: "automatic" }],
  ],
};
