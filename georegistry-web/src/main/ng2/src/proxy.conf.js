const PROXY_CONFIG = [
    {
        context: [
            "/api", "/net/geoprism/images/", "/glyphs", "/session"
        ],
        target: "https://localhost:8443/georegistry/",
        "changeOrigin": true,       // solves CORS Error in F12
        "logLevel": "debug",         //"info": prints out in console
        "rejectUnauthorzied": true, // must be false if not specify here
        "secure": false,            // PROD must be "true", but DEV false else "UNABLE_TO_VERIFY_LEAF_SIGNATURE"
        "strictSSL": true,          // must be false if not specify here
        "withCredentials": true,     // required for Angular to send in cookie
        cookiePathRewrite: {
            "/georegistry": "/",
          }
    }
]
module.exports = PROXY_CONFIG;