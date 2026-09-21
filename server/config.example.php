<?php
declare(strict_types=1);

define('DB_HOST', 'localhost');
define('DB_PORT', '3306');
define('DB_NAME', 'YOUR_DATABASE');
define('DB_USER', 'YOUR_DATABASE_USER');
define('DB_PASS', 'YOUR_DATABASE_PASSWORD');

define('APP_ENV', 'production');
define('APP_CORS_ORIGIN', '*');
define('APP_MOBILE_REDIRECT_URI', 'edith://instagram/callback');

/*
 * Generate with:
 * php -r "echo bin2hex(random_bytes(32)), PHP_EOL;"
 */
define('APP_ENCRYPTION_KEY', 'YOUR_64_HEX_CHARACTER_KEY');

define('DEFAULT_RESPONSE_MODEL', 'gemini-3.1-flash-lite');
define('DEFAULT_ANALYSIS_MODEL', 'gemini-3.8-flash');

/*
 * Meta / Instagram Login
 * Set these to the values from your Meta developer app.
 */
define('META_APP_ID', 'YOUR_META_APP_ID');
define('META_APP_SECRET', 'YOUR_META_APP_SECRET');
define('META_OAUTH_REDIRECT_URI', 'https://YOUR-DOMAIN/instagram/api/instagram/callback.php');
define('META_OAUTH_AUTHORIZE_URL', 'https://www.instagram.com/oauth/authorize');
define('META_OAUTH_TOKEN_URL', 'https://api.instagram.com/oauth/access_token');
define('META_LONG_LIVED_TOKEN_URL', 'https://graph.instagram.com/access_token');

/*
 * Keep the Graph API base/version configurable because Meta versions change.
 * Replace v23.0 with the version currently supported by your Meta app.
 */
define('META_GRAPH_BASE_URL', 'https://graph.instagram.com/v23.0');

define(
    'META_SCOPES',
    'instagram_business_basic,instagram_business_manage_messages,instagram_business_manage_comments'
);

define('META_WEBHOOK_VERIFY_TOKEN', 'CHANGE_THIS_TO_A_LONG_RANDOM_VERIFY_TOKEN');

try {
    $dsn = 'mysql:host='.DB_HOST.';port='.DB_PORT.';dbname='.DB_NAME.';charset=utf8mb4';
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES => false,
    ]);
} catch (Throwable $e) {
    http_response_code(500);
    exit('Database unavailable');
}
