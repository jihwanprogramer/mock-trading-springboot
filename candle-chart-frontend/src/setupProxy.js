const {createProxyMiddleware} = require('http-proxy-middleware');

module.exports = function (app) {
    app.use(
        '/intra',
        createProxyMiddleware({
            target: 'http://localhost:8082',
            changeOrigin: true,
        })
    );
    app.use(
        '/api/v1/news',
        createProxyMiddleware({
            target: 'http://localhost:8082',
            changeOrigin: true,
        })
    );
};
