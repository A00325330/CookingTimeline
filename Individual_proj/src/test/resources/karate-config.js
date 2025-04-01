function fn() {
    let env = karate.env || 'local';

    let config = {
        baseUrl: 'http://localhost:8081', // default fallback
        logFilePath: 'logs/skipped_rows.log',
        adminUser: { email: 'admin@example.com', password: 'Admin123!' },
        testUser: { email: 'user@example.com', password: 'userpass' }
    };

    if (env === 'dev') {
        config.baseUrl = 'http://dev.server.com';
    } else if (env === 'staging') {
        config.baseUrl = 'http://staging.server.com';
    } else if (env === 'prod') {
        config.baseUrl = 'http://prod.server.com';
    }

    karate.log('Running tests in environment:', env);
    return config;
}
