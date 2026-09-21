<?php
declare(strict_types=1);
require_once __DIR__ . '/../../config.php';
require_once __DIR__ . '/../../lib/crypto.php';
require_once __DIR__ . '/../../lib/meta.php';

$state = trim((string)($_GET['state'] ?? ''));
$code = trim((string)($_GET['code'] ?? ''));
if ($state === '' || $code === '') {
    header('Location: ' . APP_MOBILE_REDIRECT_URI . '?status=error&reason=missing_callback');
    exit;
}

try {
    $stmt = $pdo->prepare('SELECT id,user_id FROM oauth_states WHERE state=? AND expires_at>NOW() LIMIT 1');
    $stmt->execute([$state]);
    $row = $stmt->fetch();
    if (!$row) throw new RuntimeException('Invalid or expired state.');

    $pdo->prepare('DELETE FROM oauth_states WHERE id=?')->execute([(int)$row['id']]);

    $short = exchange_instagram_code($code);
    $long = exchange_long_lived_token((string)$short['access_token']);
    $accessToken = (string)$long['access_token'];
    $profile = instagram_profile($accessToken);

    $instagramId = (string)($profile['user_id'] ?? $profile['id'] ?? '');
    if ($instagramId === '') throw new RuntimeException('Instagram user id missing.');

    $encrypted = encrypt_secret($accessToken);
    $stmt = $pdo->prepare(
        'INSERT INTO instagram_accounts (user_id,instagram_user_id,username,name,profile_picture_url,access_token_encrypted,token_expires_at,is_active,created_at,updated_at)
         VALUES (?,?,?,?,?,?,DATE_ADD(NOW(), INTERVAL 60 DAY),1,NOW(),NOW())
         ON DUPLICATE KEY UPDATE username=VALUES(username),name=VALUES(name),profile_picture_url=VALUES(profile_picture_url),access_token_encrypted=VALUES(access_token_encrypted),token_expires_at=VALUES(token_expires_at),is_active=1,updated_at=NOW()'
    );
    $stmt->execute([
        (int)$row['user_id'],
        $instagramId,
        $profile['username'] ?? null,
        $profile['name'] ?? null,
        $profile['profile_picture_url'] ?? null,
        $encrypted
    ]);

    header('Location: ' . APP_MOBILE_REDIRECT_URI . '?status=success&username=' . rawurlencode((string)($profile['username'] ?? '')));
    exit;
} catch (Throwable $e) {
    header('Location: ' . APP_MOBILE_REDIRECT_URI . '?status=error&reason=' . rawurlencode($e->getMessage()));
    exit;
}
